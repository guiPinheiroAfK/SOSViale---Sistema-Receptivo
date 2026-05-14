package br.com.sosviale.model;

import br.com.sosviale.service.StatusTransfer;
import jakarta.persistence.Transient;
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

    @Transient
    private boolean notificado = false;

    public boolean isNotificado() { return notificado; }
    public void setNotificado(boolean notificado) { this.notificado = notificado; }

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
    private StatusTransfer status = StatusTransfer.AGUARDANDO_OS;

    // Este será o valor digitado pelo usuário no painel
    @Column(name = "valor_original", precision = 10, scale = 2)
    private BigDecimal valorOriginal;

    // Este será o valor convertido + taxas, que usaremos para relatórios em BRL
    @Column(name = "valor_base", precision = 10, scale = 2)
    private BigDecimal valorBase;

    @Enumerated(EnumType.STRING)
    @Column(name = "moeda_origem", length = 5, nullable = false)
    private Moeda moedaOrigem = Moeda.BRL;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "transfer_passageiro",
            joinColumns = @JoinColumn(name = "transfer_id"),
            inverseJoinColumns = @JoinColumn(name = "passageiro_id")
    )
    private List<Passageiro> passageiros = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "os_id")
    private OrdemServico ordemServico;

    public Transfer() {}

    public Transfer(LocalDate dataTransfer, LocalTime horaTransfer, String origem, String destino, BigDecimal valorOriginal, Moeda moedaOrigem) {
        this.dataTransfer = dataTransfer;
        this.horaTransfer = horaTransfer;
        this.origem = origem;
        this.destino = destino;
        this.valorOriginal = valorOriginal;
        this.moedaOrigem = moedaOrigem;
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

    public BigDecimal getValorOriginal() { return valorOriginal; }
    public void setValorOriginal(BigDecimal valorOriginal) { this.valorOriginal = valorOriginal; }

    public BigDecimal getValorBase() { return valorBase; }
    public void setValorBase(BigDecimal valorBase) { this.valorBase = valorBase; }

    public Moeda getMoedaOrigem() { return moedaOrigem; }
    public void setMoedaOrigem(Moeda moedaOrigem) { this.moedaOrigem = moedaOrigem; }

    public List<Passageiro> getPassageiros() { return passageiros; }
    public void setPassageiros(List<Passageiro> passageiros) { this.passageiros = passageiros; }

    public OrdemServico getOrdemServico() { return ordemServico; }
    public void setOrdemServico(OrdemServico ordemServico) { this.ordemServico = ordemServico; }

    public PontoColeta[] getPontosColeta() {
        return null;
    }
}