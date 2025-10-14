package com.api.pegapaga;

public class Usuario {
    private static Usuario instance;
    private String telefone = "25884XXXXXXX"; // Exemplo
    private double saldo = 0;

    private Usuario() {}

    public static Usuario getInstance() {
        if (instance == null) instance = new Usuario();
        return instance;
    }

    public String getTelefone() {
        return telefone;
    }

    public double getSaldo() {
        return saldo;
    }

    public void adicionarSaldo(double valor) {
        saldo += valor;
    }
}