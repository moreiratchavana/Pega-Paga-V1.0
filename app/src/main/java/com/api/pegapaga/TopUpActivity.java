package com.api.pegapaga;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class TopUpActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topup_account);

        webView = findViewById(R.id.webView);

        // Configura WebView
        webView.setWebViewClient(new WebViewClient()); // garante que links abram dentro da WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // permite JavaScript
        webSettings.setDomStorageEnabled(true); // permite armazenamento local

        // URL da página que queres abrir (XAMPP ou externa)
        webView.loadUrl("https://api-mpesa.infinityfree.me/?i=1/"); // substitui pelo IP e pasta do XAMPP
    }


}
