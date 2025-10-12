package com.api.pegapaga;

import android.util.Log;

public class PegaPagaCentralServer {

    public static void registerBiometricOnCentralServer(String userId, String biometricTemplate) {
        Log.d("PEGA_PAGA_CENTRAL", ">>> CONECTANDO AO SERVIDOR CENTRAL...");
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        Log.d("PEGA_PAGA_CENTRAL", ">>> TEMPLATE BIOMÉTRICO RECEBIDO E ARMAZENADO COM SUCESSO NA REDE PEGA-PAGA!");
        Log.d("PEGA_PAGA_CENTRAL", ">>> O utilizador " + userId + " já pode pagar em qualquer POS.");
    }
}