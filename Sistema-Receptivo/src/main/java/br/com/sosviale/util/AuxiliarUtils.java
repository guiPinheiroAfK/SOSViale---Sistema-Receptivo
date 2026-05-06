package br.com.sosviale.util;

import org.jline.reader.LineReader;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AuxiliarUtils {

    // retorna true se o nome contém apenas letras e espaços com no mínimo 3 caracteres
    public static boolean isNomeValido(String nome) {
        return nome != null && nome.trim().matches("^[a-zA-ZÀ-ÿ\\s]{3,}$");
    }

    // retorna true se a string contém apenas dígitos numéricos
    public static boolean isApenasNumeros(String dados) {
        return dados != null && !dados.isEmpty() && dados.matches("\\d+");
    }

    // valida placa no padrão Mercosul (ex: ABC1D23)
    public static boolean isPlacaValida(String placa) {
        return placa != null && placa.matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}");
    }

    // ----- MÉTODOS AUXILIARES DE LEITURA -----

    // lê uma string não vazia; repete o prompt até receber entrada válida
    public static String lerStringObrigatoria(LineReader reader, String mensagem) {
        while (true) {
            String valor = reader.readLine(mensagem).trim();
            if (!valor.isEmpty()) return valor;
            System.out.println("\u001B[31mEste campo é obrigatório!\u001B[0m");
        }
    }

    // lê um nome válido (apenas letras, mín. 3 chars); repete até receber entrada válida
    public static String lerNomeValido(LineReader reader, String mensagem) {
        while (true) {
            String nome = reader.readLine(mensagem).trim();
            if (isNomeValido(nome)) return nome;
            System.out.println("\u001B[31mNome inválido! Use apenas letras (mín. 3).\u001B[0m");
        }
    }

    // lê um ID numérico positivo; repete até receber entrada válida
    public static Long lerIdValido(LineReader reader, String mensagem) {
        while (true) {
            String idStr = reader.readLine(mensagem).trim();
            if (isApenasNumeros(idStr) && Long.parseLong(idStr) > 0) return Long.parseLong(idStr);
            System.out.println("\u001B[31mID inválido! Digite apenas números positivos.\u001B[0m");
        }
    }

    // lê um BigDecimal maior que zero; repete até receber entrada válida
    public static BigDecimal lerBigDecimalValido(LineReader reader, String mensagem) {
        while (true) {
            String valorStr = reader.readLine(mensagem).replace(",", ".").trim();
            try {
                BigDecimal valor = new BigDecimal(valorStr);
                if (valor.compareTo(BigDecimal.ZERO) > 0) return valor;
                System.out.println("\u001B[31mO valor deve ser maior que zero.\u001B[0m");
            } catch (NumberFormatException e) {
                System.out.println("\u001B[31mValor inválido! Ex: 150.00\u001B[0m");
            }
        }
    }

    // lê uma data/hora no formato dd/MM/yyyy HH:mm; repete até receber entrada válida
    public static LocalDateTime lerDataHoraValida(LineReader reader, String mensagem) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        while (true) {
            try {
                String dataStr = reader.readLine(mensagem).trim();
                return LocalDateTime.parse(dataStr, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("\u001B[31mFormato inválido! Use: dd/MM/yyyy HH:mm (Ex: 15/04/2026 14:30)\u001B[0m");
            }
        }
    }

    // ** fim funçoes auxiliares **//

}