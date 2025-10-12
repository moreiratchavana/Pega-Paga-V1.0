package com.api.pegapaga.pos;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.api.pegapaga.R;

import java.util.HashMap;

public class FingerprintRegisterPos extends AppCompatActivity {

    private TextView statusText;
    private Button registerButton;
    private SharedPreferences prefs;
    private TemplateStore templateStore;

    private static final long TIMEOUT_MS = 10000; // 10 segundos para colocar o dedo
    private boolean sensorConectado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint_register);

        registerButton = findViewById(R.id.registerButton);
        registerButton.setEnabled(false);

        prefs = getSharedPreferences("PegaPagaPrefs", MODE_PRIVATE);
        templateStore = new TemplateStore(this);

        // 1️⃣ Verifica se o sensor USB está conectado
        sensorConectado = verificarSensorUsb();

        if (!sensorConectado) {
            statusText.setText("Nenhum sensor POS detectado.");
            Toast.makeText(this, "Modo demonstração: sensor não conectado.", Toast.LENGTH_LONG).show();
            return; // sai sem inicializar o SDK
        }

        statusText.setText("Inicializando sensor POS...");

        // 2️⃣ Inicializa o SDK somente se o sensor estiver conectado
        PegaPagaSensorSDK.initializeWithCallback(getApplicationContext(), new PegaPagaSensorSDK.InitializationCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    statusText.setText("Sensor POS detectado! Pronto para leitura da digital.");
                    Toast.makeText(FingerprintRegisterPos.this, "Sensor POS detectado e inicializado!", Toast.LENGTH_SHORT).show();
                    registerButton.setEnabled(true);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    statusText.setText("Falha ao inicializar sensor POS!");
                    Toast.makeText(FingerprintRegisterPos.this, "Erro: " + errorMessage, Toast.LENGTH_LONG).show();
                    registerButton.setEnabled(false);
                });
            }
        });

        registerButton.setOnClickListener(v -> startEnrollmentFlow());
    }

    /**
     * Verifica dispositivos USB conectados e tenta identificar o sensor POS.
     */
    private boolean verificarSensorUsb() {
        try {
            UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            if (usbManager == null) {
                Toast.makeText(this, "Erro: USB Manager não disponível.", Toast.LENGTH_SHORT).show();
                return false;
            }

            HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
            if (deviceList.isEmpty()) {
                Log.w("PEGA_PAGA", "Nenhum dispositivo USB encontrado.");
                return false;
            }

            for (UsbDevice device : deviceList.values()) {
                Log.d("PEGA_PAGA", "USB detectado: " + device.getDeviceName() +
                        " | VendorID=" + device.getVendorId() + " | ProductID=" + device.getProductId());

                // Aqui você pode filtrar pelo seu sensor real (substitua pelos IDs corretos)
                if (device.getVendorId() == 1234 && device.getProductId() == 5678) {
                    Log.i("PEGA_PAGA", "Sensor POS compatível encontrado!");
                    return true;
                }
            }

            Log.w("PEGA_PAGA", "Nenhum sensor compatível encontrado.");
            return false;
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao verificar USB: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Inicia o processo de cadastro biométrico (se sensor ativo).
     */
    private void startEnrollmentFlow() {
        if (!sensorConectado) {
            Toast.makeText(this, "Sensor não conectado — modo demonstração.", Toast.LENGTH_LONG).show();
            return;
        }

        if (prefs.getBoolean("biometricEnrolled", false)) {
            Toast.makeText(this, "Template já cadastrado!", Toast.LENGTH_SHORT).show();
            return;
        }

        registerButton.setEnabled(false);
        statusText.setText("Aguardando digital no POS...");
        Toast.makeText(this, "Coloque o dedo no sensor POS", Toast.LENGTH_SHORT).show();

        PegaPagaSensorSDK.captureFingerprint(getApplicationContext(), TIMEOUT_MS,
                new PegaPagaSensorSDK.FingerprintCaptureCallback() {
                    @Override
                    public void onSuccess(float[] descriptor) {
                        try {
                            String templateId = "user_" + System.currentTimeMillis();
                            templateStore.saveTemplate(templateId, descriptor);

                            prefs.edit()
                                    .putBoolean("biometricEnrolled", true)
                                    .putString("templateId", templateId)
                                    .apply();

                            statusText.setText("Template cadastrado com sucesso!");
                            Toast.makeText(FingerprintRegisterPos.this, "Template capturado e salvo!", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            statusText.setText("Erro ao salvar template");
                            Toast.makeText(FingerprintRegisterPos.this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } finally {
                            registerButton.setEnabled(true);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        statusText.setText("Erro na captura da digital");
                        Toast.makeText(FingerprintRegisterPos.this, "Erro: " + errorMessage, Toast.LENGTH_LONG).show();
                        registerButton.setEnabled(true);
                    }
                });
    }
}
