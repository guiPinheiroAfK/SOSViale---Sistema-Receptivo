package br.com.sosviale.model;

import br.com.sosviale.service.StatusTransfer;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 100)
    private String origem;

    @Column(nullable = false, length = 100)
    private String destino;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StatusTransfer status = StatusTransfer.AGENDADO;

    @Column(name = "valor_base", precision = 10, scale = 2)
    private BigDecimal valorBase;

    // @JoinTable REMOVIDO daqui — o PontoColeta já tem o @JoinColumn
    @OneToMany(mappedBy = "transfer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy("ordemParada ASC")
    private List<PontoColeta> pontosColeta = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "os_id")
    private OrdemServico ordemServico;

    // @ManyToMany RESTAURADO — estava faltando
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "transfer_passageiros",
            joinColumns = @JoinColumn(name = "transfer_id"),
            inverseJoinColumns = @JoinColumn(name = "passageiro_id")
    )
    private List<Passageiro> passageiros = new ArrayList<>();

    // Construtor vazio obrigatório para o JPA
    public Transfer() {
    }

    // Novo construtor enxuto (apenas dados do agendamento)
    public Transfer(LocalDateTime dataHora, String origem, String destino, BigDecimal valorBase) {
        this.dataHora = dataHora;
        this.origem = origem;
        this.destino = destino;
        this.valorBase = valorBase;
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public StatusTransfer getStatus() {return status;}
    public void setStatus(StatusTransfer status) {this.status = status;}

    public BigDecimal getValorBase() { return valorBase; }
    public void setValorBase(BigDecimal valorBase) { this.valorBase = valorBase; }

    public List<Passageiro> getPassageiros() { return passageiros; }
    public void setPassageiros(List<Passageiro> passageiros) { this.passageiros = passageiros; }

    public List<PontoColeta> getPontosColeta() {
        return pontosColeta;
    }
    public void setPontosColeta(List<PontoColeta> pontosColeta) {
        this.pontosColeta = pontosColeta;
    }

    public OrdemServico getOrdemServico() {
        return ordemServico;
    }
    public void setOrdemServico(OrdemServico ordemServico) {
        this.ordemServico = ordemServico;
    }
}