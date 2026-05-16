package br.com.sosviale.service;

import br.com.sosviale.model.Transfer;
import br.com.sosviale.repository.TransferRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class TransferService {

    private final TransferRepository repository = new TransferRepository();

    // Cotações fixas (Exemplo: 1 USD = 5.00 BRL | 1 BRL = 1470 PYG approx)
    private static final BigDecimal CAMBIO_USD = new BigDecimal("5.00");
    private static final BigDecimal CAMBIO_PYG = new BigDecimal("0.00068");

    public void cadastrar(Transfer transfer) {
        validarTransfer(transfer);
        processarFinanceiro(transfer);
        repository.salvar(transfer);
    }

    public void atualizar(Transfer transfer) {
        if (transfer == null || transfer.getId() == null)
            throw new IllegalArgumentException("Transfer inválido para atualização.");

        validarTransfer(transfer);
        processarFinanceiro(transfer);
        repository.atualizar(transfer);
    }

    /**
     * Realiza a conversão e aplica taxas internacionais.
     * O resultado final é salvo em valorBase (BRL).
     */
    private void processarFinanceiro(Transfer transfer) {
        BigDecimal valorOriginal = transfer.getValorOriginal();
        Moeda moeda = transfer.getMoedaOrigem();

        if (valorOriginal == null || valorOriginal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor do transfer inválido.");
        }

        // 1. Converter para Real (BRL)
        BigDecimal valorEmReais = converterParaBRL(valorOriginal, moeda);

        // 2. Aplicar Taxas Internacionais baseadas na Moeda
        BigDecimal valorComTaxa = aplicarTaxas(valorEmReais, moeda);

        // 3. Atribuir ao valorBase (o que o banco usará como referência)
        transfer.setValorBase(valorComTaxa.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal converterParaBRL(BigDecimal valor, Moeda moeda) {
        return switch (moeda) {
            case USD -> valor.multiply(CAMBIO_USD);
            case PYG -> valor.multiply(CAMBIO_PYG);
            case BRL -> valor;
            default -> valor;
        };
    }

    private BigDecimal aplicarTaxas(BigDecimal valorEmReais, Moeda moeda) {
        if (moeda == Moeda.USD) {
            // Aplica 12% (Taxa Argentina associada ao USD neste contexto)
            BigDecimal taxa = BigDecimal.valueOf(TaxaInternacional.ARGENTINA.getTaxa());
            return valorEmReais.multiply(BigDecimal.ONE.add(taxa));
        } else if (moeda == Moeda.PYG) {
            // Aplica 10% (Taxa Paraguai)
            BigDecimal taxa = BigDecimal.valueOf(TaxaInternacional.PARAGUAI.getTaxa());
            return valorEmReais.multiply(BigDecimal.ONE.add(taxa));
        }
        return valorEmReais;
    }

    private void validarTransfer(Transfer transfer) {
        if (transfer == null) throw new IllegalArgumentException("Transfer não pode ser nulo.");
        if (transfer.getOrigem() == null || transfer.getOrigem().isBlank())
            throw new IllegalArgumentException("Origem é obrigatória.");
        if (transfer.getDestino() == null || transfer.getDestino().isBlank())
            throw new IllegalArgumentException("Destino é obrigatório.");
        if (transfer.getDataTransfer() == null)
            throw new IllegalArgumentException("Data é obrigatória.");
        if (transfer.getHoraTransfer() == null)
            throw new IllegalArgumentException("Hora é obrigatória.");
    }

    public List<Transfer> listarTodos() {
        return repository.listarTodos();
    }

    public void excluir(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        repository.excluir(id);
    }

    public Transfer buscarPorId(Integer id) {
        return repository.buscarPorId(id);
    }

    public int contarPassageirosPorVeiculo(Integer veiculoId) {
        return repository.contarPassageirosPorVeiculo(veiculoId);
    }

    public long contarSemOrdemServico() {
        return repository.contarSemOrdemServico();
    }
}