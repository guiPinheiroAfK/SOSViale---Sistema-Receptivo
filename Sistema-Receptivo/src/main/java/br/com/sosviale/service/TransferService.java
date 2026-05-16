package br.com.sosviale.service;

import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.repository.TransferRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class TransferService {

    private final TransferRepository repository = new TransferRepository();

    // Cotações fixas (Exemplo: 1 USD = 5.00 BRL | 1 PYG ≈ 0.00068 BRL)
    private static final BigDecimal CAMBIO_USD = new BigDecimal("5.00");
    private static final BigDecimal CAMBIO_PYG = new BigDecimal("0.00068");

    public void cadastrar(Transfer transfer) {
        validarTransfer(transfer);
        processarFinanceiro(transfer);
        repository.salvar(transfer);
    }

    /*
     * Atualiza todos os dados de um transfer existente, incluindo reprocessamento
     * financeiro. Use este método apenas quando dados do transfer em si mudam
     * (origem, destino, valor, etc.).
     *
     * Para apenas vincular um transfer a uma OS, use vincularAOS().
     */
    public void atualizar(Transfer transfer) {
        if (transfer == null || transfer.getId() == null)
            throw new IllegalArgumentException("Transfer inválido para atualização.");
        validarTransfer(transfer);
        processarFinanceiro(transfer);
        repository.atualizar(transfer);
    }

    /*
     * Vincula um Transfer a uma OrdemServico de forma segura e atômica.
     *
     * Por que este método existe separado do atualizar()?
     *   - atualizar() reprocessa valorBase, taxas e câmbio — desnecessário e
     *     potencialmente destrutivo para uma simples vinculação.
     *   - vincularAOS() delega ao repositório um find() com objeto attached,
     *     garantindo que só os campos de vínculo (ordemServico + status) são
     *     alterados, dentro de uma transação própria e limpa.
     *
     * @param transferId ID do transfer a ser vinculado
     * @param os         OrdemServico de destino (usamos apenas o ID internamente)
     */
    public void vincularAOS(Integer transferId, OrdemServico os) {
        if (transferId == null || transferId <= 0)
            throw new IllegalArgumentException("ID do transfer inválido.");
        if (os == null || os.getId() == null)
            throw new IllegalArgumentException("OS inválida para vinculação.");

        repository.vincular(transferId, os.getId());
    }

    private void processarFinanceiro(Transfer transfer) {
        BigDecimal valorOriginal = transfer.getValorOriginal();
        Moeda moeda = transfer.getMoedaOrigem();

        if (valorOriginal == null || valorOriginal.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Valor do transfer inválido.");

        BigDecimal valorEmReais    = converterParaBRL(valorOriginal, moeda);
        BigDecimal valorComTaxa    = aplicarTaxas(valorEmReais, moeda);
        transfer.setValorBase(valorComTaxa.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal converterParaBRL(BigDecimal valor, Moeda moeda) {
        return switch (moeda) {
            case USD -> valor.multiply(CAMBIO_USD);
            case PYG -> valor.multiply(CAMBIO_PYG);
            case BRL -> valor;
            default  -> valor;
        };
    }

    private BigDecimal aplicarTaxas(BigDecimal valorEmReais, Moeda moeda) {
        if (moeda == Moeda.USD) {
            BigDecimal taxa = BigDecimal.valueOf(TaxaInternacional.ARGENTINA.getTaxa());
            return valorEmReais.multiply(BigDecimal.ONE.add(taxa));
        } else if (moeda == Moeda.PYG) {
            BigDecimal taxa = BigDecimal.valueOf(TaxaInternacional.PARAGUAI.getTaxa());
            return valorEmReais.multiply(BigDecimal.ONE.add(taxa));
        }
        return valorEmReais;
    }

    private void validarTransfer(Transfer transfer) {
        if (transfer == null)
            throw new IllegalArgumentException("Transfer não pode ser nulo.");
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

    public List<Transfer> listarVinculadosOrdemServico() {
        return repository.listarVinculadosOrdemServico();
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