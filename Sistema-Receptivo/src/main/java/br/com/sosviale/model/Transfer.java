package br.com.sosviale.model;

import br.com.sosviale.enums.StatusTransfer;
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

    @Enumerated(EnumType.STRING) // Salva o nome do Enum no banco
    @Column(name = "status", length = 20, nullable = false)
    private StatusTransfer status = StatusTransfer.AGENDADO;

    @Column(name = "valor_base", precision = 10, scale = 2)
    private BigDecimal valorBase;

    // Relacionamento: Muitos transfers para um motorista
    @ManyToOne
    @JoinColumn(name = "motorista_id") // Nome da coluna FK no banco
    private Motorista motorista;

    // Relacionamento: Muitos transfers para um veículo
    @ManyToOne
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "transfer_passageiros",
            joinColumns = @JoinColumn(name = "transfer_id"),
            inverseJoinColumns = @JoinColumn(name = "passageiro_id")
    )

    private List<Passageiro> passageiros = new ArrayList<>();

    public Transfer() {
    }

    public Transfer(LocalDateTime dataHora, String origem, String destino, BigDecimal valorBase, Motorista motorista, Veiculo veiculo) {
        this.dataHora = dataHora;
        this.origem = origem;
        this.destino = destino;
        this.valorBase = valorBase;
        this.motorista = motorista;
        this.veiculo = veiculo;
        this.status = StatusTransfer.AGENDADO;
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
    public void setStatus(StatusTransfer status) { this.status = status; }

    public BigDecimal getValorBase() { return valorBase; }
    public void setValorBase(BigDecimal valorBase) { this.valorBase = valorBase; }

    public Motorista getMotorista() { return motorista; }
    public void setMotorista(Motorista motorista) { this.motorista = motorista; }

    public Veiculo getVeiculo() { return veiculo; }
    public void setVeiculo(Veiculo veiculo) { this.veiculo = veiculo; }

    public List<Passageiro> getPassageiros() { return passageiros; }
    public void setPassageiros(List<Passageiro> passageiros) { this.passageiros = passageiros; }
}