package com.api.pegapaga;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreatAccountActivity extends AppCompatActivity {

    // Campos de input
    private TextInputEditText fullNameField, phoneField, pinField, confirmPinField;
    private CheckBox termsCheckbox;
    private Button submitButton;

    // Lista simulando um pequeno banco local
    public static ArrayList<Map<String, String>> userDatabase = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.create_account);

        // Liga os componentes do layout
        fullNameField = findViewById(R.id.fullNameField);
        phoneField = findViewById(R.id.phoneField);
        pinField = findViewById(R.id.pinField);
        confirmPinField = findViewById(R.id.confirmPinField);
        termsCheckbox = findViewById(R.id.termsCheckbox);
        submitButton = findViewById(R.id.submitButton);

        // Clique no botão de cadastro
        submitButton.setOnClickListener(v -> {
            String nome = fullNameField.getText() != null ? fullNameField.getText().toString().trim() : "";
            String telefone = phoneField.getText() != null ? phoneField.getText().toString().trim() : "";
            String pin = pinField.getText() != null ? pinField.getText().toString().trim() : "";
            String confirmarPin = confirmPinField.getText() != null ? confirmPinField.getText().toString().trim() : "";

            // Validações simples
            if (nome.isEmpty() || telefone.isEmpty() || pin.isEmpty() || confirmarPin.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pin.length() != 4) {
                Toast.makeText(this, "O PIN deve ter 4 dígitos.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pin.equals(confirmarPin)) {
                Toast.makeText(this, "Os PINs não coincidem!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!termsCheckbox.isChecked()) {
                Toast.makeText(this, "É necessário aceitar os Termos e Condições.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Salva os dados localmente (simulação)
            Map<String, String> novoUsuario = new HashMap<>();
            novoUsuario.put("nome", nome);
            novoUsuario.put("telefone", telefone);
            novoUsuario.put("pin", pin);
            userDatabase.add(novoUsuario);

            Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();

            // Salva usuário e saldo inicial em SharedPreferences
            getSharedPreferences("PegaPagaPrefs", MODE_PRIVATE).edit()
                    .putString("usuario_nome", nome)
                    .putString("usuario_telefone", telefone)
                    .putFloat("saldo", 0f)
                    .apply();

            // Abre MainActivity diretamente e limpa histórico
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish(); // finaliza esta activity
        });
    }
}
