package com.api.pegapaga;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TransferActivity extends AppCompatActivity {

    private EditText inputSearch;
    private Button buttonSend, buttonReceive;
    private LinearLayout itemUser;
    private TextView userName, userId;
    private SharedPreferences prefs;

    // Navegação inferior
    private ImageView navHome, navCard, navTransfer, navUser;

    private String selectedUserId = null;
    private String selectedUserName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.transfer);

        // ------------------- Ajuste Edge-to-Edge -------------------
        View root = findViewById(R.id.transfer_root1);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            View bottomNav = findViewById(R.id.bottom_nav);
            if (bottomNav != null) {
                bottomNav.setPadding(0, 0, 0, systemBars.bottom);
            }

            return insets;
        });

        prefs = getSharedPreferences("PegaPagaPrefs", MODE_PRIVATE);

        // ------------------- Inicializa elementos -------------------
        inputSearch = findViewById(R.id.input_search);
        buttonSend = findViewById(R.id.buttonsend);
        buttonReceive = findViewById(R.id.buttonreceive);
        itemUser = findViewById(R.id.item_user);
        userName = itemUser.findViewById(R.id.tv_user_name);
        userId = itemUser.findViewById(R.id.tv_user_id);

        // ------------------- Botões -------------------
        buttonSend.setOnClickListener(v -> tryFindUser());
        buttonReceive.setOnClickListener(v -> showReceiveDialog());
        itemUser.setOnClickListener(v -> {
            if (selectedUserId != null) showSendMoneyDialog();
        });

        // ------------------- Navegação inferior -------------------
        navHome = findViewById(R.id.nav_home);
        navCard = findViewById(R.id.nav_card);
        navTransfer = findViewById(R.id.nav_transfer);
        navUser = findViewById(R.id.nav_user);

        atualizarNavBar();

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        navCard.setOnClickListener(v -> {
            startActivity(new Intent(this, CardActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        navTransfer.setOnClickListener(v -> {
            // Já está nesta tela
        });

        navUser.setOnClickListener(v -> {
            startActivity(new Intent(this, UserActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    /**
     * Atualiza a barra inferior, acendendo apenas o ícone da Activity atual
     */
    private void atualizarNavBar() {
        int corAtiva = getResources().getColor(R.color.primary_blue, null);
        int corInativa = getResources().getColor(R.color.gray, null);

        navHome.setColorFilter(corInativa);
        navCard.setColorFilter(corInativa);
        navTransfer.setColorFilter(corInativa);
        navUser.setColorFilter(corInativa);

        // Ativo: Transfer
        navTransfer.setColorFilter(corAtiva);
    }

    /** Busca local simulada de usuário */
    private void tryFindUser() {
        String query = inputSearch.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Digite um nome ou ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simula resultado válido
        if (query.startsWith("@") || query.matches("[0-9]{6,}")) {
            selectedUserId = query;
            selectedUserName = "Usuário " + query.replace("@", "");
            showUserResult(selectedUserName, selectedUserId);
        } else {
            Toast.makeText(this, "Usuário não encontrado.", Toast.LENGTH_SHORT).show();
        }
    }

    /** Mostra o usuário encontrado */
    private void showUserResult(String name, String id) {
        itemUser.setVisibility(View.VISIBLE);
        userName.setText(name);
        userId.setText("ID: " + id);
    }

    /** Dialog para enviar dinheiro */
    private void showSendMoneyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enviar para " + selectedUserName);

        final EditText input = new EditText(this);
        input.setHint("Valor a enviar (MT)");
        builder.setView(input);

        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String valorStr = input.getText().toString().trim();
            if (valorStr.isEmpty()) {
                Toast.makeText(this, "Digite o valor.", Toast.LENGTH_SHORT).show();
                return;
            }

            double valor = Double.parseDouble(valorStr);
            double saldo = prefs.getFloat("saldo", 0);

            if (saldo >= valor) {
                saldo -= valor;
                prefs.edit().putFloat("saldo", (float) saldo).apply();
                Toast.makeText(this, "Transferência enviada com sucesso!", Toast.LENGTH_LONG).show();
                addTransactionToHistory(selectedUserName, valor);
            } else {
                Toast.makeText(this, "Saldo insuficiente!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /** Simula recebimento */
    private void showReceiveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Solicitar pagamento");
        builder.setMessage("Seu código de recebimento: PP-" + (int) (Math.random() * 900000 + 100000));
        builder.setPositiveButton("Fechar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /** Adiciona transação local */
    private void addTransactionToHistory(String name, double valor) {
        Toast.makeText(this, "Histórico atualizado: -" + valor + " MT para " + name, Toast.LENGTH_SHORT).show();
    }
}
