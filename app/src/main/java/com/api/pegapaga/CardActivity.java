package com.api.pegapaga;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class CardActivity extends AppCompatActivity {

    private EditText cardNumberEdit, cardNameEdit, cardExpiryEdit, cardCvvEdit;
    private Button registerCardButton;
    private final List<String> cardList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.card);

        // ------------------- Ajuste Edge-to-Edge -------------------
        View root = findViewById(R.id.scroll_content); // ID existente na raiz ou container principal
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // 🔹 Liga os elementos do layout
        cardNumberEdit = findViewById(R.id.card_number_edit_text);
        cardNameEdit = findViewById(R.id.card_name_edit_text);
        cardExpiryEdit = findViewById(R.id.card_expiry_edit_text);
        cardCvvEdit = findViewById(R.id.card_cvv_edit_text);
        registerCardButton = findViewById(R.id.button_register_card);

        // 🔹 Ícones da barra inferior
        ImageView navHome = findViewById(R.id.nav_home);
        ImageView navTransfer = findViewById(R.id.nav_transfer);
        ImageView navUser = findViewById(R.id.nav_user);

        // 🟦 Botão "Cadastrar Cartão"
        registerCardButton.setOnClickListener(v -> {
            String number = cardNumberEdit.getText().toString().trim();
            String name = cardNameEdit.getText().toString().trim();
            String expiry = cardExpiryEdit.getText().toString().trim();
            String cvv = cardCvvEdit.getText().toString().trim();

            if (number.isEmpty() || name.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            String cardInfo = "**** **** **** " + number.substring(Math.max(0, number.length() - 4))
                    + " - " + name.toUpperCase()
                    + " (" + expiry + ")";
            cardList.add(cardInfo);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("novo_cartao", cardInfo);
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "Cartão cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        });

        // 🟩 Navegação inferior
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        navTransfer.setOnClickListener(v -> {
            startActivity(new Intent(this, TransferActivity.class));
            overridePendingTransition(0, 0);
        });

        navUser.setOnClickListener(v -> {
            startActivity(new Intent(this, UserActivity.class));
            overridePendingTransition(0, 0);
        });
    }
}
