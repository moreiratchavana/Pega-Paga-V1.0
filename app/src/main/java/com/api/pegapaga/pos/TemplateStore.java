// TemplateStore.java
package com.api.pegapaga.pos;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * TemplateStore
 * Responsável por armazenar, carregar e gerenciar templates biométricos de forma segura.
 * Todos os templates são salvos criptografados no armazenamento interno do aplicativo.
 */
public class TemplateStore {

    private static final String TAG = "TemplateStore";
    private final Context ctx;
    private final File storageDir;

    public TemplateStore(Context ctx) {
        this.ctx = ctx;
        this.storageDir = new File(ctx.getFilesDir(), "templates");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
            Log.i(TAG, "Diretório de templates criado: " + storageDir.getAbsolutePath());
        }
    }

    /**
     * Salva um template biométrico de um usuário.
     * @param userId ID único do usuário
     * @param descriptor float[] gerado pelo sensor (PegaPagaSensorSDK)
     * @throws Exception se ocorrer erro de criptografia ou escrita
     */
    public void saveTemplate(String userId, float[] descriptor) throws Exception {
        CryptoManager.ensureKeyExists();
        byte[] bytes = floatsToBytes(descriptor);
        String encrypted = CryptoManager.encrypt(bytes);

        File outFile = new File(storageDir, sanitizeFilename(userId) + ".tpl");
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(encrypted.getBytes("UTF-8"));
            fos.flush();
        }
        Log.i(TAG, "Template salvo para usuário " + userId);
    }

    /**
     * Remove o template de um usuário.
     * @param userId ID do usuário
     * @return true se removido com sucesso, false caso contrário
     */
    public boolean removeTemplate(String userId) {
        File f = new File(storageDir, sanitizeFilename(userId) + ".tpl");
        return f.exists() && f.delete();
    }

    /**
     * Lista todos os IDs de usuários com template salvo.
     */
    public List<String> listUserIds() {
        List<String> ids = new ArrayList<>();
        File[] files = storageDir.listFiles((dir, name) -> name.endsWith(".tpl"));
        if (files == null) return ids;
        for (File f : files) {
            String name = f.getName();
            ids.add(name.substring(0, name.length() - 4)); // remove .tpl
        }
        return ids;
    }

    /**
     * Carrega o template de um usuário e retorna float[] para comparação.
     * @param userId ID do usuário
     * @return float[] do template ou null se não encontrado
     * @throws Exception se erro na leitura ou criptografia
     */
    public float[] loadDescriptor(String userId) throws Exception {
        File f = new File(storageDir, sanitizeFilename(userId) + ".tpl");
        if (!f.exists()) return null;

        byte[] encryptedBytes;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            encryptedBytes = Files.readAllBytes(f.toPath());
        } else {
            // fallback para versões antigas
            encryptedBytes = readFileLegacy(f);
        }

        String base64 = new String(encryptedBytes, "UTF-8");
        byte[] decrypted = CryptoManager.decrypt(base64);
        return bytesToFloats(decrypted);
    }

    // --- Utilitários ---
    private static byte[] floatsToBytes(float[] arr) {
        ByteBuffer bb = ByteBuffer.allocate(arr.length * 4);
        for (float v : arr) bb.putFloat(v);
        return bb.array();
    }

    private static float[] bytesToFloats(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        int n = bytes.length / 4;
        float[] out = new float[n];
        for (int i = 0; i < n; i++) out[i] = bb.getFloat();
        return out;
    }

    private static String sanitizeFilename(String s) {
        return s.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");
    }

    // fallback leitura para pre-Oreo
    private static byte[] readFileLegacy(File f) throws IOException {
        FileOutputStream fos = null;
        byte[] buffer = new byte[(int) f.length()];
        java.io.FileInputStream fis = new java.io.FileInputStream(f);
        fis.read(buffer);
        fis.close();
        return buffer;
    }
}
