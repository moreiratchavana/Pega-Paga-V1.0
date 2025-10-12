package com.api.pegapaga;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class BiometricUtils {

    private static final String KEY_NAME = "PEGAPAGA_BIOMETRIC_KEY";
    private static final String KEYSTORE_ALIAS = "AndroidKeyStore";

    // Tenta criar uma chave se ela ainda não existir
    public static void createKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_ALIAS);
            keyStore.load(null); // Carrega o Keystore

            // Se a chave já existe, não fazemos nada
            if (keyStore.containsAlias(KEY_NAME)) {
                Log.d("PEGA_PAGA_BIOMETRIC", "Chave biométrica já existe.");
                return;
            }

            // Gerador de chaves
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_ALIAS);

            // Especificações da nossa chave
            KeyGenParameterSpec keyGenParameterSpec = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                        KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setUserAuthenticationRequired(true) // **A MÁGICA: Requer autenticação do utilizador**
                        .setUserAuthenticationValidityDurationSeconds(-1) // Requer autenticação sempre
                        .build();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenerator.init(keyGenParameterSpec);
            }
            keyGenerator.generateKey();

            Log.d("PEGA_PAGA_BIOMETRIC", "Nova chave biométrica criada com sucesso!");

        } catch (NoSuchAlgorithmException | NoSuchProviderException |
                 InvalidAlgorithmParameterException | KeyStoreException |
                 IOException | CertificateException e) {
            Log.e("PEGA_PAGA_BIOMETRIC", "Erro ao criar a chave biométrica", e);
            throw new RuntimeException("Falha ao criar chave", e);
        }
    }

    // Prepara o Cipher para ser usado no prompt biométrico
    public static Cipher getCipher() {
        try {
            Cipher cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/" +
                            KeyProperties.BLOCK_MODE_CBC + "/" +
                            KeyProperties.ENCRYPTION_PADDING_PKCS7);

            SecretKey key = getSecretKey();
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return cipher;
        } catch (Exception e) {
            Log.e("PEGA_PAGA_BIOMETRIC", "Erro ao inicializar o Cipher", e);
            throw new RuntimeException("Falha ao preparar Cipher", e);
        }
    }

    // Obtém a chave secreta do Keystore
    private static SecretKey getSecretKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_ALIAS);
            keyStore.load(null);
            return (SecretKey) keyStore.getKey(KEY_NAME, null);
        } catch (Exception e) {
            Log.e("PEGA_PAGA_BIOMETRIC", "Erro ao obter a chave secreta", e);
            throw new RuntimeException("Falha ao obter chave", e);
        }
    }
}