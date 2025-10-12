package com.api.pegapaga.pos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.api.pegapaga.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executor;

public class RequestMoneyPosActivity extends AppCompatActivity {

    private TextInputLayout inputAmountLayout;
    private TextInputEditText amountInput;
    private TextView amountDisplayText;
    private Button nextButton;
    private LinearLayout layoutFingerprintConfirmation;
    private TextView paymentConfirmed;

    private float valorSolicitado = 0f;
    private int etapa = 0; // 0=digitar valor, 1=confirmar biometria, 2=pagamento concluído

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.request_money_pos);

        // Liga elementos
        inputAmountLayout = findViewById(R.id.input_amount_layout);
        amountInput = findViewById(R.id.amount_input);
        amountDisplayText = findViewById(R.id.amount_display_text);
        nextButton = findViewById(R.id.next_button);
        layoutFingerprintConfirmation = findViewById(R.id.layout_fingerprint_confirmation);
        paymentConfirmed = findViewById(R.id.payment_confirmed);

        // Inicialmente oculta elementos
        layoutFingerprintConfirmation.setVisibility(View.GONE);
        amountDisplayText.setVisibility(View.GONE);

        // Configura executor e callback da biometria
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                runOnUiThread(() -> {
                    paymentConfirmed.setVisibility(android.view.View.VISIBLE);
                    nextButton.setText("Concluir");
                    etapa = 2;
                });
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                runOnUiThread(() -> Toast.makeText(RequestMoneyPosActivity.this,
                        "Erro na autenticação: " + errString, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                runOnUiThread(() -> Toast.makeText(RequestMoneyPosActivity.this,
                        "Biometria não reconhecida", Toast.LENGTH_SHORT).show());
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Confirme sua biometria")
                .setSubtitle("Use sua impressão digital para confirmar o pagamento")
                .setNegativeButtonText("Cancelar")
                .build();

        nextButton.setOnClickListener(v -> {
            switch (etapa) {
                case 0:
                    // Etapa 0: digitar montante
                    String texto = amountInput.getText() != null ? amountInput.getText().toString().trim() : "";
                    try {
                        valorSolicitado = Float.parseFloat(texto);
                        if (valorSolicitado <= 0f) {
                            Toast.makeText(this, "Digite um valor válido!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Digite um valor válido!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Atualiza UI para mostrar valor e solicitar biometria
                    amountDisplayText.setText(String.format("Valor: MZN %.2f", valorSolicitado));
                    amountDisplayText.setVisibility(android.view.View.VISIBLE);
                    layoutFingerprintConfirmation.setVisibility(android.view.View.VISIBLE);

                    // Esconde input
                    inputAmountLayout.setVisibility(android.view.View.GONE);
                    nextButton.setText("Confirmar Biometria");
                    etapa = 1;
                    break;

                case 1:
                    // Etapa 1: biometria real
                    if (BiometricManager.from(this).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                        biometricPrompt.authenticate(promptInfo);
                    } else {
                        Toast.makeText(this, "Dispositivo não suporta biometria", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case 2:
                    // Etapa 2: retorna resultado para MainActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("valor_recebido", valorSolicitado);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                    break;
            }
        });
    }
}
