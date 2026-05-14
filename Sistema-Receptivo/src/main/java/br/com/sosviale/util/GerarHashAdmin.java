// Arquivo: GerarHashAdmin.java
package br.com.sosviale.util;

public class GerarHashAdmin {
    public static void main(String[] args) {
        String senha = "admin123";
        String hash = PasswordUtil.hashSenha(senha);

        System.out.println("=== RODE ESTE SQL NO SEU BANCO DE DADOS ===");
        System.out.println("UPDATE usuarios SET senha = '" + hash + "' WHERE usuario = 'admin';");
        System.out.println("===========================================");
    }
}