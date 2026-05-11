package br.com.sosviale.util;

import br.com.sosviale.model.TipoDocumento;

public class DocumentoValidator {

    public static boolean isValido(String documento, TipoDocumento tipo) {
        if (documento == null || documento.trim().isEmpty()) return false;

        // Remove tudo que não for letra ou número para facilitar a conta
        String docLimpo = documento.replaceAll("[^a-zA-Z0-9]", "");

        return switch (tipo) {
            case CPF -> docLimpo.length() == 11 && docLimpo.matches("\\d+");
            case CNH -> docLimpo.length() == 11 && docLimpo.matches("\\d+");
            case RG -> docLimpo.length() >= 7 && docLimpo.length() <= 9;
            case PASSAPORTE -> docLimpo.matches("[A-Z]{2}\\d{6,7}"); // Ex: AB123456
            default -> false;
        };
    }
}