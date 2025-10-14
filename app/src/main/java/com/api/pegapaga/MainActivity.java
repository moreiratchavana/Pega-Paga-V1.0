package com.api.pegapaga;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;



import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.api.pegapaga.pos.RequestMoneyPosActivity;

public class MainActivity extends AppCompatActivity {








    private Button btnRegisterFingerprint, btnEnviar, btnSolicitar, btnHistorico;
    private TextView tvSaldo;
    private SharedPreferences prefs;


    private LinearLayout transacoesListContainer;

    private ImageView navHome, navCard, navTransfer, navUser, imgTopupmoney;

    // Launcher para criar novo usuário
    private final ActivityResultLauncher<Intent> createAccountLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String nome = result.getData().getStringExtra("nome");
                    String telefone = result.getData().getStringExtra("telefone");
                    boolean novoUsuario = result.getData().getBooleanExtra("novo_usuario", false);

                    if (nome != null && telefone != null) {
                        prefs.edit()
                                .putString("usuario_nome", nome)
                                .putString("usuario_telefone", telefone)
                                .putFloat("saldo", 0f)
                                .apply();


                        if (novoUsuario) {
                            transacoesListContainer.removeAllViews(); // limpa histórico
                        }

                        Toast.makeText(this, "Conta criada com sucesso: " + nome, Toast.LENGTH_SHORT).show();
                        atualizarSaldo();
                    }
                }
            });

    // Launcher para enviar dinheiro
    private final ActivityResultLauncher<Intent> sendMoneyLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    float valorEnviado = result.getData().getFloatExtra("valor_enviado", 0f);
                    if (valorEnviado > 0f) {
                        float saldoAtual = prefs.getFloat("saldo", 0f);
                        float novoSaldo = saldoAtual - valorEnviado;
                        prefs.edit().putFloat("saldo", novoSaldo).apply();

                        atualizarSaldo();
                        adicionarTransacao("Enviado", valorEnviado, false);
                    }
                }
            });

    // Launcher para receber via POS
    private final ActivityResultLauncher<Intent> requestMoneyLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    float valorRecebido = result.getData().getFloatExtra("valor_recebido", 0f);
                    if (valorRecebido > 0f) {
                        float saldoAtual = prefs.getFloat("saldo", 0f);
                        float novoSaldo = saldoAtual + valorRecebido;
                        prefs.edit().putFloat("saldo", novoSaldo).apply();

                        atualizarSaldo();
                        adicionarTransacao("Recebido via POS", valorRecebido, true);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);

        // Inicializações
        prefs = getSharedPreferences("PegaPagaPrefs", MODE_PRIVATE);
        tvSaldo = findViewById(R.id.tv_saldo_atual);
        transacoesListContainer = findViewById(R.id.transacoes_container);
        transacoesListContainer.removeAllViews();

        btnRegisterFingerprint = findViewById(R.id.btn_rgfingerprint);
        btnEnviar = findViewById(R.id.btn_enviar);
        btnSolicitar = findViewById(R.id.btn_solicitar);
        btnHistorico = findViewById(R.id.btn_historico);
        imgTopupmoney = findViewById(R.id.topUpmoney);


        navHome = findViewById(R.id.nav_home);
        navCard = findViewById(R.id.nav_card);
        navTransfer = findViewById(R.id.nav_transfer);
        navUser = findViewById(R.id.nav_user);

        atualizarSaldo();

        // ----------------- BOTÕES -----------------
        btnRegisterFingerprint.setOnClickListener(v ->
                startActivity(new Intent(this, FingerprintRegister.class))
        );

        btnEnviar.setOnClickListener(v -> sendMoneyLauncher.launch(new Intent(this, SendMoneyActivity.class)));

        btnSolicitar.setOnClickListener(v -> requestMoneyLauncher.launch(new Intent(this, RequestMoneyPosActivity.class)));

        btnHistorico.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class))
        );
        imgTopupmoney.setOnClickListener(v ->
                startActivity(new Intent(this, TopUpActivity.class))
        );

        // ----------------- BARRA INFERIOR -----------------
        navHome.setOnClickListener(v -> {});
        navCard.setOnClickListener(v -> { startActivity(new Intent(this, CardActivity.class)); finish(); });
        navTransfer.setOnClickListener(v -> { startActivity(new Intent(this, TransferActivity.class)); finish(); });
        navUser.setOnClickListener(v -> { startActivity(new Intent(this, UserActivity.class)); finish(); });

        // ----------------- VERIFICA USUÁRIO EXISTENTE -----------------
        String usuarioNome = prefs.getString("usuario_nome", null);
        if (usuarioNome == null) {
            createAccountLauncher.launch(new Intent(this, CreatAccountActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarSaldo();
        atualizarNavBar();
    }

    // ----------------- FUNÇÕES AUXILIARES -----------------
    private void atualizarSaldo() {
        float saldoAtual = prefs.getFloat("saldo", 0f);
        tvSaldo.setText(String.format("MZN %.2f", saldoAtual));
    }

    public void adicionarTransacao(String descricao, float valor, boolean recebido) {
        if (transacoesListContainer == null) return;

        View item = LayoutInflater.from(this)
                .inflate(R.layout.item_transaction, transacoesListContainer, false);

        TextView tipo = item.findViewById(R.id.tv_tipo);
        TextView data = item.findViewById(R.id.tv_data);
        TextView valorText = item.findViewById(R.id.tv_valor);

        tipo.setText(descricao);
        data.setText("Hoje");
        valorText.setText(String.format("%s MZN %.2f", recebido ? "+" : "-", valor));
        valorText.setTextColor(getResources().getColor(recebido ? R.color.green : R.color.red, null));

        transacoesListContainer.addView(item, 0);
    }

    private void atualizarNavBar() {
        int corAtiva = getResources().getColor(R.color.primary_blue, null);
        int corInativa = getResources().getColor(R.color.gray, null);

        navHome.setColorFilter(corInativa);
        navCard.setColorFilter(corInativa);
        navTransfer.setColorFilter(corInativa);
        navUser.setColorFilter(corInativa);

        switch (this.getClass().getSimpleName()) {
            case "MainActivity": navHome.setColorFilter(corAtiva); break;
            case "CardActivity": navCard.setColorFilter(corAtiva); break;
            case "TransferActivity": navTransfer.setColorFilter(corAtiva); break;
            case "UserActivity": navUser.setColorFilter(corAtiva); break;
        }
    }
}
