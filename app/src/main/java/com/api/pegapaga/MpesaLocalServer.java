package com.api.pegapaga;

import android.content.Context;

import org.json.JSONObject;

import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;

public class MpesaLocalServer extends NanoHTTPD {

    private final Context context;
    private final MpesaTopUpManager topUpManager;

    public MpesaLocalServer(Context context, int port) {
        super(port);
        this.context = context;
        this.topUpManager = new MpesaTopUpManager(context);
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (Method.POST.equals(session.getMethod())) {
            try {
                // Lê corpo JSON
                InputStream inputStream = session.getInputStream();
                int contentLength = Integer.parseInt(session.getHeaders().getOrDefault("content-length", "0"));
                byte[] buffer = new byte[contentLength];
                int read = 0;
                while (read < contentLength) {
                    int r = inputStream.read(buffer, read, contentLength - read);
                    if (r == -1) break;
                    read += r;
                }
                String body = new String(buffer);
                JSONObject json = new JSONObject(body);

                String msisdn = json.getString("msisdn");
                double amount = json.getDouble("amount");
                String reference = json.getString("reference");

                // Cria um objeto de resposta síncrono
                final StringBuilder sdkResult = new StringBuilder();
                final boolean[] success = {false};

                // Chama o SDK do M-Pesa
                topUpManager.topUp(msisdn, amount, reference, new MpesaTopUpManager.MpesaCallback() {
                    @Override
                    public void onSuccess(org.paymentsds.mpesa.Response response) {
                        sdkResult.append(response.toString());
                        success[0] = true;
                    }

                    @Override
                    public void onError(Exception e) {
                        sdkResult.append("Erro SDK: ").append(e.getMessage());
                        success[0] = false;
                    }
                });

                // **Aguardar o SDK processar** (Thread.sleep ou melhor: CountDownLatch)
                int waited = 0;
                while (sdkResult.length() == 0 && waited < 15000) { // espera max 15s
                    Thread.sleep(200);
                    waited += 200;
                }

                if (success[0]) {
                    return newFixedLengthResponse(Response.Status.OK, "application/json",
                            "{\"status\":\"sucesso\",\"detalhes\":\"" + sdkResult + "\"}");
                } else {
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                            "{\"status\":\"falha\",\"detalhes\":\"" + sdkResult + "\"}");
                }

            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain",
                        "Erro: " + e.getMessage());
            }
        }
        return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain",
                "Apenas POST permitido");
    }
}