package br.com.sosviale.util;

import br.com.sosviale.model.TipoDocumento;

public class DocumentoValidator {

    public static boolean isValido(String documento, TipoDocumento tipo) {
        if (documento == null || documento.trim().isEmpty()) return false;

        // Limpa caracteres especiais e padroniza para maiúsculo (importante para RG e Passaporte)
        String docLimpo = documento.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

        return switch (tipo) {
            case CPF -> validarCPF(docLimpo);
            case RG -> validarRG(docLimpo);
            case CNH -> validarCNH(docLimpo);
            case PASSAPORTE -> validarPassaporte(docLimpo);
            default -> false;
        };
    }

    private static boolean validarCPF(String cpf) {
        // Bloqueia se não tiver 11 dígitos ou se for sequência repetida
        if (!cpf.matches("\\d{11}") || cpf.matches("(\\d)\\1{10}")) return false;

        try {
            int d1 = 0, d2 = 0, p1 = 10, p2 = 11;

            for (int i = 0; i < 9; i++) d1 += (Character.getNumericValue(cpf.charAt(i)) * p1--);
            d1 = 11 - (d1 % 11);
            if (d1 > 9) d1 = 0;

            for (int i = 0; i < 10; i++) d2 += (Character.getNumericValue(cpf.charAt(i)) * p2--);
            d2 = 11 - (d2 % 11);
            if (d2 > 9) d2 = 0;

            return (d1 == Character.getNumericValue(cpf.charAt(9))) &&
                    (d2 == Character.getNumericValue(cpf.charAt(10)));
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean validarRG(String rg) {
        // Aceita de 7 a 9 caracteres, bloqueia repetidos e permite 'X' no final
        if (rg.length() < 7 || rg.length() > 9) return false;
        if (rg.matches("(\\w)\\1{6,8}")) return false;
        return rg.matches("\\d+[0-9X]?");
    }

    private static boolean validarCNH(String cnh) {
        // CNH nacional sempre possui 11 dígitos numéricos
        return cnh.length() == 11 && cnh.matches("\\d+") && !cnh.matches("(\\d)\\1{10}");
    }

    private static boolean validarPassaporte(String passaporte) {
        if (passaporte == null || passaporte.length() < 3) return false;

        //Tenta validar primeiro contra o padrão brasileiro (Regra mais restrita)
        if (passaporte.matches("^[A-Z]{2}\\d{6,7}$")) {
            return true;
        }

        //Se não é brasileiro, aplica a regra geral da ICAO (International Civil Aviation Organization)
        return passaporte.matches("^[A-Z0-9]{6,12}$");
    }
}