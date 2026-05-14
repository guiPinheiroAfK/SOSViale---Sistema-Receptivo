package br.com.sosviale.model;

import br.com.sosviale.service.StatusTransfer;
import br.com.sosviale.service.Moeda;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "data_transfer", nullable = false)
    private LocalDate dataTransfer;

    @Column(name = "hora_transfer", nullable = false)
    private LocalTime horaTransfer;

    @Column(nullable = false, length = 100)
    private String origem;

    @Column(nullable = false, length = 100)
    private String destino;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StatusTransfer status = StatusTransfer.NA_OS;

    @Column(name = "valor_base", precision = 10, scale = 2)
    private BigDecimal valorBase;

    @Enumerated(EnumType.STRING)
    @Column(name = "moeda_origem", length = 5, nullable = false)
    private Moeda moedaOrigem = Moeda.BRL;

    @ManyToMany
    @JoinTable(
            name = "transfer_passageiro",
            joinColumns = @JoinColumn(name = "transfer_id"),
            inverseJoinColumns = @JoinColumn(name = "passageiro_id")
    )
    private List<Passageiro> passageiros = new ArrayList<>();

    /*@OneToMany(mappedBy = "transfer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderBy(" ASC")
    private List<PontoColeta> pontosColeta = new ArrayList<>();
    */
    @ManyToOne
    @JoinColumn(name = "os_id")
    private OrdemServico ordemServico;

    // Construtor padrão (JPA)
    public Transfer() {}

    // Construtor para novos agendamentos
    public Transfer(LocalDate dataTransfer, LocalTime horaTransfer, String origem, String destino, BigDecimal valorBase) {
        this.dataTransfer = dataTransfer;
        this.horaTransfer = horaTransfer;
        this.origem = origem;
        this.destino = destino;
        this.valorBase = valorBase;
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDate getDataTransfer() { return dataTransfer; }
    public void setDataTransfer(LocalDate dataTransfer) { this.dataTransfer = dataTransfer; }

    public LocalTime getHoraTransfer() { return horaTransfer; }
    public void setHoraTransfer(LocalTime horaTransfer) { this.horaTransfer = horaTransfer; }

    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public StatusTransfer getStatus() { return status; }
    public void setStatus(StatusTransfer status) { this.status = status; }

    public BigDecimal getValorBase() { return valorBase; }
    public void setValorBase(BigDecimal valorBase) { this.valorBase = valorBase; }

    public List<Passageiro> getPassageiros() { return passageiros; }
    public void setPassageiros(List<Passageiro> passageiros) { this.passageiros = passageiros; }

/*
    public List<PontoColeta> getPontosColeta() { return pontosColeta; }
    public void setPontosColeta(List<PontoColeta> pontosColeta) { this.pontosColeta = pontosColeta; }
*/
    public OrdemServico getOrdemServico() { return ordemServico; }
    public void setOrdemServico(OrdemServico ordemServico) { this.ordemServico = ordemServico; }

    public Moeda getMoedaOrigem() { return moedaOrigem; }
    public void setMoedaOrigem(Moeda moedaOrigem) { this.moedaOrigem = moedaOrigem; }

    public PontoColeta[] getPontosColeta() {
    return null;
    }
}