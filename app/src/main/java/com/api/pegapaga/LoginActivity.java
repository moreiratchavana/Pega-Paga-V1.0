package com.api.pegapaga;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricManager;
import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);

        CardView biometricCard = findViewById(R.id.biometricCard);
        Button biometricLoginButton = findViewById(R.id.biometricLoginButton);
        Button createAccountButton = findViewById(R.id.createAccountButton);

        setupBiometric();

        biometricCard.setOnClickListener(v -> {
            if (isBiometricAvailable()) {
                biometricPrompt.authenticate(promptInfo);
            } else {
                Toast.makeText(this, "Biometria não disponível.", Toast.LENGTH_SHORT).show();
            }
        });

        biometricLoginButton.setOnClickListener(v -> {
            if (isBiometricAvailable()) {
                biometricPrompt.authenticate(promptInfo);
            }
        });
        createAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreatAccountActivity.class);
            startActivity(intent);
        });

    }

    private void setupBiometric() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                navigateToHome();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED)
                    Toast.makeText(getApplicationContext(), "Erro: " + errString, Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login Biométrico")
                .setSubtitle("Use sua impressão digital para entrar")
                .setNegativeButtonText("Usar PIN")
                .build();
    }

    private boolean isBiometricAvailable() {
        BiometricManager manager = BiometricManager.from(this);
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
