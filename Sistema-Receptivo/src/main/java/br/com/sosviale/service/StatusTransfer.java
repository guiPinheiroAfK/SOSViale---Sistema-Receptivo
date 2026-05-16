package br.com.sosviale.service;

// Representa os possíveis estados de ciclo de vida de um transfer
public enum StatusTransfer {
    AGUARDANDO_OS("Aguardando OS"),  // Cadastrado, mas solto (sem rota)
    NA_OS("Na OS"),                  // Já foi atribuído a uma Ordem de Serviço
    EM_EXECUCAO("Em Execução"),      // Motorista iniciou a rota
    CONCLUIDO("Concluído"),          // Passageiro entregue
    CANCELADO("Cancelado");

    private final String descricao;

    StatusTransfer(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}