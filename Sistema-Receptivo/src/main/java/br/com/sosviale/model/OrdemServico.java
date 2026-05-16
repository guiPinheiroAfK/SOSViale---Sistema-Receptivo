package br.com.sosviale.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// os com motorista/veículo + lista eager de transfers e paradas (subselect pra nao brigar fetch)

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

    @Column(length = 20)
    private String status = "ABERTA";

    @OneToMany(mappedBy = "ordemServico", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("horaTransfer ASC")
    private List<Transfer> transfers = new ArrayList<>();

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("ordemParada ASC")
    private List<ParadaOS> paradasRota = new ArrayList<>();

    public List<ParadaOS> getParadasRota() {
        return paradasRota;
    }

    public void setParadasRota(List<ParadaOS> paradasRota) {
        this.paradasRota = paradasRota;
    }

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