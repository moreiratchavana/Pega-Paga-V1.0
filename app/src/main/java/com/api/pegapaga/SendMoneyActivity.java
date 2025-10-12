package com.api.pegapaga;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SendMoneyActivity extends AppCompatActivity {

    private TextInputEditText userIdInput;
    private TextInputLayout inputUserLayout;
    private Button nextButton;

    private boolean etapaUsuario = true; // true = inserir usuário, false = inserir montante
    private String destinatario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.send_money);

        inputUserLayout = findViewById(R.id.input_user_id_layout);
        userIdInput = findViewById(R.id.user_id_input);
        nextButton = findViewById(R.id.next_button);

        nextButton.setOnClickListener(v -> {
            String texto = userIdInput.getText() != null ? userIdInput.getText().toString().trim() : "";

            if (etapaUsuario) {
                // Etapa 1: inserir usuário
                if (texto.isEmpty()) {
                    Toast.makeText(this, "Digite o ID do destinatário!", Toast.LENGTH_SHORT).show();
                    return;
                }
                destinatario = texto;

                // Prepara etapa 2: inserir montante
                userIdInput.setText("");
                inputUserLayout.setHint("Digite o montante (MZN)");
                userIdInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                etapaUsuario = false;
                nextButton.setText("Enviar");
            } else {
                // Etapa 2: inserir montante
                float valor;
                try {
                    valor = Float.parseFloat(texto);
                    if (valor <= 0f) {
                        Toast.makeText(this, "Digite um valor válido!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Digite um valor válido!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Envia resultado para MainActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("destinatario", destinatario);
                resultIntent.putExtra("valor_enviado", valor);
                setResult(RESULT_OK, resultIntent);

                Toast.makeText(this, "Enviado " + valor + " MZN para " + destinatario, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
