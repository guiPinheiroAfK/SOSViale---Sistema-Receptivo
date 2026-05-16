package br.com.sosviale.service;

public enum Moeda {
    BRL("R$"),
    USD("US$"),
    PYG("₲");

    private final String simbolo;
    Moeda(String simbolo) { this.simbolo = simbolo; }
    public String getSimbolo() { return simbolo; }
}