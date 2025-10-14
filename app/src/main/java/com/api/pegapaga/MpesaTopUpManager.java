package com.api.pegapaga;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.paymentsds.mpesa.Client;
import org.paymentsds.mpesa.Request;
import org.paymentsds.mpesa.Response;
import org.paymentsds.mpesa.Environment;

public class MpesaTopUpManager {

    private final Context context;
    private final Client client;
    private final Handler mainHandler;

    public MpesaTopUpManager(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());

        // Configura o client real do M-Pesa
        client = new Client.Builder()
                .apiKey("7k6548wb7p7zjxokwl342rcpfdahqhqt")              // Substitua pela sua API Key
                .publicKey("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAmptSWqV7cGUUJJhUBxsMLonux24u+FoTlrb+4Kgc6092JIszmI1QUoMohaDDXSVueXx6IXwYGsjjWY32HGXj1iQhkALXfObJ4DqXn5h6E8y5/xQYNAyd5bpN5Z8r892B6toGzZQVB7qtebH4apDjmvTi5FGZVjVYxalyyQkj4uQbbRQjgCkubSi45Xl4CGtLqZztsKssWz3mcKncgTnq3DHGYYEYiKq0xIj100LGbnvNz20Sgqmw/cH+Bua4GJsWYLEqf/h/yiMgiBbxFxsnwZl0im5vXDlwKPw+QnO2fscDhxZFAwV06bgG0oEoWm9FnjMsfvwm0rUNYFlZ+TOtCEhmhtFp+Tsx9jPCuOd5h2emGdSKD8A6jtwhNa7oQ8RtLEEqwAn44orENa1ibOkxMiiiFpmmJkwgZPOG/zMCjXIrrhDWTDUOZaPx/lEQoInJoE2i43VN/HTGCCw8dKQAwg0jsEXau5ixD0GUothqvuX3B9taoeoFAIvUPEq35YulprMM7ThdKodSHvhnwKG82dCsodRwY428kg2xM/UjiTENog4B6zzZfPhMxFlOSFX4MnrqkAS+8Jamhy1GgoHkEMrsT5+/ofjCx0HjKbT5NuA2V/lmzgJLl3jIERadLzuTYnKGWxVJcGLkWXlEPYLbiaKzbJb2sYxt+Kt5OxQqC1MCAwEAAQ==")        // Substitua pela Public Key
                .serviceProviderCode("171717")
                .initiatorIdentifier("TestInitiator")
                .environment(Environment.PRODUCTION) // use PRODUCTION em live
                .build();
    }

    public void topUp(String msisdn, double amount, String reference, MpesaCallback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .amount(amount)
                        .from(msisdn)
                        .reference(reference)
                        .transaction("TX" + System.currentTimeMillis())
                        .build();

                Response response = client.receive(request);

                mainHandler.post(() -> {
                    Toast.makeText(context, "Pagamento realizado com sucesso!", Toast.LENGTH_LONG).show();
                    Log.d("MPESA", "Resposta: " + response.toString());
                    callback.onSuccess(response);
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    Toast.makeText(context, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("MPESA", "Falha: " + e.getMessage());
                    callback.onError(e);
                });
            }
        }).start();
    }

    public interface MpesaCallback {
        void onSuccess(Response response);
        void onError(Exception e);
    }
}