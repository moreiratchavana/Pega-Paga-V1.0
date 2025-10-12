package com.api.pegapaga.pos;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * PegaPagaSensorSDK - adaptador universal para sensores biométricos.
 * Suporta Native, USB Serial e Bluetooth via "bridges".
 */
public class PegaPagaSensorSDK {

    private static final String TAG = "PegaPagaSensorSDK";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private static volatile boolean initialized = false;

    /** Retorna true se o SDK foi inicializado com sucesso */
    public static boolean isReady() {
        return initialized;
    }

    public interface FingerprintCaptureCallback {
        void onSuccess(float[] descriptor);
        void onError(String errorMessage);
    }

    public interface InitializationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    private interface SensorBridge {
        boolean isAvailable();
        void initialize(Context ctx) throws Exception;
        void capture(Context ctx, long timeoutMillis, CaptureCallback cb);
        void close();
    }

    private interface CaptureCallback {
        void onCaptured(byte[] rawTemplate);
        void onFail(String reason);
    }

    private static final List<SensorBridge> BRIDGES = new ArrayList<>();

    /** Inicializa todas as bridges com callback para sucesso/falha */
    public static synchronized void initializeWithCallback(Context ctx, InitializationCallback cb) {
        if (initialized) {
            MAIN.post(cb::onSuccess);
            return;
        }

        new Thread(() -> {
            BRIDGES.clear();
            BRIDGES.add(new NativeLibBridge());
            BRIDGES.add(new UsbSerialBridge());
            BRIDGES.add(new BluetoothBridge());

            boolean anyInitialized = false;
            for (SensorBridge b : BRIDGES) {
                try {
                    b.initialize(ctx);
                    Log.i(TAG, "Bridge inicializada: " + b.getClass().getSimpleName());
                    anyInitialized = true;
                } catch (Exception e) {
                    Log.w(TAG, "Bridge não inicializada: " + b.getClass().getSimpleName() + " -> " + e.getMessage());
                }
            }
            initialized = anyInitialized;

            if (initialized) MAIN.post(cb::onSuccess);
            else MAIN.post(() -> cb.onError("Nenhuma bridge disponível / POS não inicializado"));
        }).start();
    }

    /** Captura template de digital usando as bridges disponíveis */
    public static void captureFingerprint(Context ctx, long timeoutMillis, FingerprintCaptureCallback cb) {
        if (!initialized) {
            cb.onError("POS não inicializado");
            return;
        }
        attemptBridgeSequential(ctx, 0, timeoutMillis, cb);
    }

    private static void attemptBridgeSequential(Context ctx, int index, long timeoutMillis, FingerprintCaptureCallback userCb) {
        if (index >= BRIDGES.size()) {
            MAIN.post(() -> userCb.onError("Nenhum sensor disponível / nenhum bridge suportado."));
            return;
        }

        SensorBridge bridge = BRIDGES.get(index);
        if (!bridge.isAvailable()) {
            attemptBridgeSequential(ctx, index + 1, timeoutMillis, userCb);
            return;
        }

        final AtomicBoolean responded = new AtomicBoolean(false);
        bridge.capture(ctx, timeoutMillis, new CaptureCallback() {
            @Override
            public void onCaptured(byte[] rawTemplate) {
                if (responded.getAndSet(true)) return;
                try {
                    float[] descriptor = convertRawToFloatDescriptor(rawTemplate);
                    MAIN.post(() -> userCb.onSuccess(descriptor));
                } catch (Exception e) {
                    Log.e(TAG, "Erro convertendo template: " + e.getMessage(), e);
                    attemptBridgeSequential(ctx, index + 1, timeoutMillis, userCb);
                }
            }

            @Override
            public void onFail(String reason) {
                if (responded.getAndSet(true)) return;
                Log.w(TAG, "Bridge falhou: " + bridge.getClass().getSimpleName() + " -> " + reason);
                attemptBridgeSequential(ctx, index + 1, timeoutMillis, userCb);
            }
        });
    }

    /** Converte raw template para float[] genérico */
    private static float[] convertRawToFloatDescriptor(byte[] raw) throws Exception {
        if (raw == null || raw.length == 0) throw new Exception("Raw template vazio");
        if (raw.length % 4 == 0) {
            ByteBuffer bb = ByteBuffer.wrap(raw);
            int n = raw.length / 4;
            float[] out = new float[n];
            for (int i = 0; i < n; i++) out[i] = bb.getFloat();
            return out;
        }
        if (raw.length % 2 == 0) {
            ByteBuffer bb = ByteBuffer.wrap(raw);
            int n = raw.length / 2;
            float[] out = new float[n];
            for (int i = 0; i < n; i++) out[i] = (float) bb.getShort() / Short.MAX_VALUE;
            return out;
        }
        float[] out = new float[raw.length];
        for (int i = 0; i < raw.length; i++) out[i] = (raw[i] & 0xFF) / 255.0f;
        return out;
    }

    // ------------------------------
    // Bridges (esqueleto, implementar vendor)
    // ------------------------------

    private static class NativeLibBridge implements SensorBridge {
        private boolean available = false;
        static { try { System.loadLibrary("vendor_sensor"); } catch (Throwable ignored) {} }

        private static native byte[] nativeCaptureTemplate(int timeoutMs) throws Exception;
        private static native boolean nativeInitialize() throws Exception;

        @Override public boolean isAvailable() { return available; }
        @Override public void initialize(Context ctx) throws Exception { available = nativeInitialize(); }
        @Override public void capture(Context ctx, long timeoutMillis, CaptureCallback cb) {
            new Thread(() -> {
                try {
                    byte[] raw = nativeCaptureTemplate((int) timeoutMillis);
                    if (raw == null || raw.length == 0) cb.onFail("Template vazio do native");
                    else cb.onCaptured(raw);
                } catch (Throwable t) { cb.onFail("Erro native capture: " + t.getMessage()); }
            }).start();
        }
        @Override public void close() {}
    }

    private static class UsbSerialBridge implements SensorBridge {
        private boolean available = false;
        @Override public boolean isAvailable() { return available; }
        @Override public void initialize(Context ctx) throws Exception { available = true; /* TODO: checar USB real */ }
        @Override public void capture(Context ctx, long timeoutMillis, CaptureCallback cb) { cb.onFail("Implementar captura USB vendor"); }
        @Override public void close() {}
    }

    private static class BluetoothBridge implements SensorBridge {
        private boolean available = false;
        @Override public boolean isAvailable() { return available; }
        @Override public void initialize(Context ctx) throws Exception { available = true; /* TODO: checar BT real */ }
        @Override public void capture(Context ctx, long timeoutMillis, CaptureCallback cb) { cb.onFail("Implementar captura Bluetooth vendor"); }
        @Override public void close() {}
    }
}
