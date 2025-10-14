package com.api.pegapaga;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MpesaHttpManager {

    private final Context context;
    private final OkHttpClient client;
    private final Handler mainHandler;
    private final String serverUrl; // URL do servidor local

    public MpesaHttpManager(Context context, int localPort) {
        this.context = context;
        this.client = new OkHttpClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
        // Define URL para servidor local
        this.serverUrl = "http://127.0.0.1:" + localPort + "/topup";
    }

    public void topUp(String msisdn, double amount, String reference, MpesaCallback callback) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("msisdn", msisdn);
                json.put("amount", amount);
                json.put("reference", reference);

                RequestBody body = RequestBody.create(
                        MediaType.get("application/json; charset=utf-8"),
                        json.toString()
                );

                Request request = new Request.Builder()
                        .url(serverUrl)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String responseText = response.body() != null ? response.body().string() : "";

                mainHandler.post(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Pagamento concluído!", Toast.LENGTH_LONG).show();
                        Log.d("MPESA_HTTP", responseText);
                        callback.onSuccess(responseText);
                    } else {
                        Toast.makeText(context, "Falha: " + response.code(), Toast.LENGTH_LONG).show();
                        Log.e("MPESA_HTTP", responseText);
                        callback.onError(new Exception("Erro HTTP: " + response.code()));
                    }
                });

            } catch (IOException | org.json.JSONException e) {
                mainHandler.post(() -> {
                    Toast.makeText(context, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("MPESA_HTTP", e.getMessage());
                    callback.onError(e);
                });
            }
        }).start();
    }

    public interface MpesaCallback {
        void onSuccess(String response);
        void onError(Exception e);
    }
}