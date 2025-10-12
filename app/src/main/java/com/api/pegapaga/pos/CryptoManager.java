package com.api.pegapaga.pos;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.ByteBuffer;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class CryptoManager {
    private static final String KEY_ALIAS = "pega_paga_pos_aes_key";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    public static void ensureKeyExists() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setKeySize(256)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setUserAuthenticationRequired(false)
                        .build();
                keyGenerator.init(spec);
            }
            keyGenerator.generateKey();
        }
    }

    private static SecretKey getKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        return ((SecretKey) keyStore.getKey(KEY_ALIAS, null));
    }

    public static String encrypt(byte[] plaintext) throws Exception {
        SecretKey key = getKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = cipher.getIV();
        byte[] cipherBytes = cipher.doFinal(plaintext);
        ByteBuffer bb = ByteBuffer.allocate(iv.length + cipherBytes.length);
        bb.put(iv);
        bb.put(cipherBytes);
        return Base64.encodeToString(bb.array(), Base64.NO_WRAP);
    }

    public static byte[] decrypt(String base64) throws Exception {
        SecretKey key = getKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] allBytes = Base64.decode(base64, Base64.NO_WRAP);
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(allBytes, 0, iv, 0, GCM_IV_LENGTH);
        byte[] cipherBytes = new byte[allBytes.length - GCM_IV_LENGTH];
        System.arraycopy(allBytes, GCM_IV_LENGTH, cipherBytes, 0, cipherBytes.length);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        return cipher.doFinal(cipherBytes);
    }
}
