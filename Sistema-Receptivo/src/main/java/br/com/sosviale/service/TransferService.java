package br.com.sosviale.service;

import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.repository.TransferRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class TransferService {

    private final TransferRepository repository = new TransferRepository();

    // COTAÇÕES VARIAVEIS!!! Removido campo "final" dos cambios para adicionar conexão a API
    private static BigDecimal CAMBIO_USD = new BigDecimal("5.00");
    private static BigDecimal CAMBIO_PYG = new BigDecimal("0.00068");

    // "trava" para saber se já buscamos hoje. antes ele pesquisava varias vezes por causa das telas
    private static boolean cotacoesAtualizadas = false;

    public BigDecimal getCotacaoUsd() {
        return CAMBIO_USD;
    }

    public BigDecimal getCotacaoPyg() {
        return CAMBIO_PYG;
    }

    public TransferService() {
        // Atualiza sempre que abre o sistema :>
        if (!cotacoesAtualizadas) { // a trava aqui
            atualizarCotacoes();
        }
    }

    /**
     * Busca as cotações mais recentes na AwesomeAPI.
     */
    public void atualizarCotacoes() {
        try {
            // Cria o cliente HTTP
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5)) // Timeout rápido pra não travar a tela
                    .build();

            // Configura a requisição para USD e PYG simultaneamente
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://economia.awesomeapi.com.br/last/USD-BRL,PYG-BRL"))
                    .GET()
                    .build();

            // Envia a requisição
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Lê o retorno da API
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

                // Extrai o valor de compra ("bid") do JSON
                String bidUsd = json.getAsJsonObject("USDBRL").get("bid").getAsString();
                String bidPyg = json.getAsJsonObject("PYGBRL").get("bid").getAsString();

                // Atualiza as variáveis estáticas do sistema
                CAMBIO_USD = new BigDecimal(bidUsd);
                CAMBIO_PYG = new BigDecimal(bidPyg);

                // o boolean para garantir :>
                cotacoesAtualizadas = true;

                System.out.println("✅ Cotações atualizadas com sucesso: USD = " + CAMBIO_USD + " | PYG = " + CAMBIO_PYG);
            } else {
                System.err.println("⚠️ Falha ao buscar cotação. Status: " + response.statusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Erro de conexão com a API de câmbio. Usando cotações de fallback (5.00 e 0.00068). Erro: " + e.getMessage());
            // Como não tem 'final' mais, se der erro ele apenas mantém o valor anterior/padrão e o sistema não quebra.
        }
    }

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