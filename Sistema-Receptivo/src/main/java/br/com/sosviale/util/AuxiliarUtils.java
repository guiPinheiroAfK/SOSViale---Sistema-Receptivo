package br.com.sosviale.util;

import org.jline.reader.LineReader;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Deprecated

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

}