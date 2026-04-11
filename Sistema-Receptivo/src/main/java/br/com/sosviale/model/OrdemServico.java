package br.com.sosviale.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordens_servico")
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "data_servico", nullable = false)
    private LocalDate dataServico;

    @ManyToOne
    @JoinColumn(name = "motorista_id")
    private Motorista motorista;

    @ManyToOne
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    // status padrão ao criar uma nova OS
    @Column(length = 20)
    private String status = "ABERTA";

    // transfers já vêm ordenados por horário para facilitar a exibição da rota
    @OneToMany(mappedBy = "ordemServico", fetch = FetchType.EAGER)
    @OrderBy("dataHora ASC")
    private List<Transfer> transfers = new ArrayList<>();

    // getters e setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Motorista getMotorista() { return motorista; }
    public void setMotorista(Motorista motorista) { this.motorista = motorista; }

    public LocalDate getDataServico() { return dataServico; }
    public void setDataServico(LocalDate dataServico) { this.dataServico = dataServico; }

    public Veiculo getVeiculo() { return veiculo; }
    public void setVeiculo(Veiculo veiculo) { this.veiculo = veiculo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Transfer> getTransfers() { return transfers; }
    public void setTransfers(List<Transfer> transfers) { this.transfers = transfers; }
}
