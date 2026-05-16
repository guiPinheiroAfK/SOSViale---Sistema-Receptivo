package br.com.sosviale.service;

import br.com.sosviale.model.Veiculo;
import br.com.sosviale.repository.VeiculoRepository;

import java.util.List;

public class VeiculoService {

    private final VeiculoRepository repository = new VeiculoRepository();

    public void salvar(String label, String placa, Integer capacidade) {
        salvar(label, placa, capacidade, null, null);
    }

    public void salvar(String label, String placa, Integer capacidade, String marca, String tipo) {
        validarCampos(label, placa, capacidade);
        Veiculo v = new Veiculo(label.trim(), placa.trim().toUpperCase(), capacidade);
        v.setMarca(textoOpcional(marca));
        v.setTipo(textoOpcional(tipo));
        repository.salvar(v);
    }

    public void atualizar(Integer id, String label, String placa, Integer capacidade) {
        atualizar(id, label, placa, capacidade, null, null);
    }

    public void atualizar(Integer id, String label, String placa, Integer capacidade, String marca, String tipo) {
        if (id == null) throw new IllegalArgumentException("ID inválido.");
        validarCampos(label, placa, capacidade);

        Veiculo v = new Veiculo(label.trim(), placa.trim().toUpperCase(), capacidade);
        v.setId(id);
        v.setMarca(textoOpcional(marca));
        v.setTipo(textoOpcional(tipo));
        repository.atualizar(v);
    }

    private String textoOpcional(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return valor.trim();
    }

    public void excluir(Integer id) {
        if (id == null) throw new IllegalArgumentException("ID inválido.");
        repository.excluir(id);
    }

    public List<Veiculo> listarTodos() {
        return repository.listarTodos();
    }

    private void validarCampos(String label, String placa, Integer capacidade) {
        // Modelo
        if (label == null || label.trim().isEmpty())
            throw new IllegalArgumentException("Modelo é obrigatório.");
        if (label.trim().length() < 2 || label.trim().length() > 50)
            throw new IllegalArgumentException("Modelo deve ter entre 2 e 50 caracteres.");

        // Placa
        if (placa == null || placa.trim().isEmpty())
            throw new IllegalArgumentException("Placa é obrigatória.");

        String placaLimpa = placa.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        boolean placaAntiga   = placaLimpa.matches("[A-Z]{3}\\d{4}");          // ABC1234
        boolean placaMercosul = placaLimpa.matches("[A-Z]{3}\\d[A-Z]\\d{2}"); // ABC1D23
        if (!placaAntiga && !placaMercosul)
            throw new IllegalArgumentException("Placa inválida. Use o formato ABC1234 ou ABC1D23.");

        // Capacidade
        if (capacidade == null || capacidade <= 0)
            throw new IllegalArgumentException("Capacidade deve ser maior que zero.");
        if (capacidade > 200)
            throw new IllegalArgumentException("Capacidade máxima permitida é de 50 passageiros.");
    }
}