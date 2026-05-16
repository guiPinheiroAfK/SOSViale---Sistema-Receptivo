package br.com.sosviale.offline;

import br.com.sosviale.model.*;
import br.com.sosviale.offline.dto.*;
import br.com.sosviale.service.StatusTransfer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// jpa entities <-> json snapshot; uns patches só mexem no dto antes de salvar de novo

public final class OfflineEntityMapper {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private OfflineEntityMapper() {}

    // persistencia server -> disco

    public static OfflineSnapshot fromOrdens(List<OrdemServico> ordens, String usuario) {
        OfflineSnapshot snap = new OfflineSnapshot();
        snap.setUsuario(usuario);
        snap.setSyncedAt(java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        for (OrdemServico os : ordens) {
            snap.getOrdens().add(fromOrdem(os));
        }
        return snap;
    }

    public static OfflineOrdemServicoDto fromOrdem(OrdemServico os) {
        OfflineOrdemServicoDto dto = new OfflineOrdemServicoDto();
        dto.setId(os.getId());
        dto.setDataServico(os.getDataServico() != null ? os.getDataServico().format(DATE) : null);
        dto.setStatus(os.getStatus());
        if (os.getMotorista() != null) {
            OfflineMotoristaDto m = new OfflineMotoristaDto();
            m.setId(os.getMotorista().getId());
            m.setNome(os.getMotorista().getNome());
            m.setCnh(os.getMotorista().getCnh());
            m.setTelefone(os.getMotorista().getTelefone());
            dto.setMotorista(m);
        }
        if (os.getVeiculo() != null) {
            OfflineVeiculoDto v = new OfflineVeiculoDto();
            v.setId(os.getVeiculo().getId());
            v.setLabel(os.getVeiculo().getLabel());
            v.setPlaca(os.getVeiculo().getPlaca());
            v.setCapacidade(os.getVeiculo().getCapacidade());
            v.setMarca(os.getVeiculo().getMarca());
            v.setTipo(os.getVeiculo().getTipo());
            dto.setVeiculo(v);
        }
        if (os.getTransfers() != null) {
            for (Transfer t : os.getTransfers()) {
                dto.getTransfers().add(fromTransfer(t, os.getId()));
            }
        }
        return dto;
    }

    public static OfflineTransferDto fromTransfer(Transfer t, Integer osId) {
        OfflineTransferDto dto = new OfflineTransferDto();
        dto.setId(t.getId());
        dto.setOsId(osId);
        dto.setOrigem(t.getOrigem());
        dto.setDestino(t.getDestino());
        dto.setDataTransfer(t.getDataTransfer() != null ? t.getDataTransfer().format(DATE) : null);
        dto.setHoraTransfer(t.getHoraTransfer() != null ? t.getHoraTransfer().format(TIME) : null);
        dto.setStatus(t.getStatus() != null ? t.getStatus().name() : null);
        if (t.getPassageiros() != null) {
            for (Passageiro p : t.getPassageiros()) {
                OfflinePassageiroDto pd = new OfflinePassageiroDto();
                pd.setId(p.getId());
                pd.setNome(p.getNome());
                pd.setDocumento(p.getDocumento());
                pd.setTipoDocumento(p.getTipoDocumento() != null ? p.getTipoDocumento().name() : null);
                pd.setNacionalidade(p.getNacionalidade());
                dto.getPassageiros().add(pd);
            }
        }
        return dto;
    }

    // json -> objetos pra tela (sem round-trip no banco)

    public static List<Transfer> toTransfers(OfflineSnapshot snapshot) {
        List<Transfer> result = new ArrayList<>();
        if (snapshot == null) return result;
        for (OfflineOrdemServicoDto osDto : snapshot.getOrdens()) {
            OrdemServico os = toOrdem(osDto);
            for (OfflineTransferDto td : osDto.getTransfers()) {
                Transfer t = toTransfer(td, os);
                result.add(t);
            }
        }
        return result;
    }

    public static OrdemServico toOrdem(OfflineOrdemServicoDto dto) {
        OrdemServico os = new OrdemServico();
        os.setId(dto.getId());
        if (dto.getDataServico() != null) {
            os.setDataServico(LocalDate.parse(dto.getDataServico(), DATE));
        }
        os.setStatus(dto.getStatus());
        if (dto.getMotorista() != null) {
            Motorista m = new Motorista();
            m.setId(dto.getMotorista().getId());
            m.setNome(dto.getMotorista().getNome());
            m.setCnh(dto.getMotorista().getCnh());
            m.setTelefone(dto.getMotorista().getTelefone());
            os.setMotorista(m);
        }
        if (dto.getVeiculo() != null) {
            Veiculo v = new Veiculo();
            v.setId(dto.getVeiculo().getId());
            v.setLabel(dto.getVeiculo().getLabel());
            v.setPlaca(dto.getVeiculo().getPlaca());
            v.setCapacidade(dto.getVeiculo().getCapacidade());
            v.setMarca(dto.getVeiculo().getMarca());
            v.setTipo(dto.getVeiculo().getTipo());
            os.setVeiculo(v);
        }
        List<Transfer> transfers = new ArrayList<>();
        for (OfflineTransferDto td : dto.getTransfers()) {
            transfers.add(toTransfer(td, os));
        }
        os.setTransfers(transfers);
        return os;
    }

    public static Transfer toTransfer(OfflineTransferDto dto, OrdemServico os) {
        Transfer t = new Transfer();
        t.setId(dto.getId());
        t.setOrigem(dto.getOrigem());
        t.setDestino(dto.getDestino());
        if (dto.getDataTransfer() != null) {
            t.setDataTransfer(LocalDate.parse(dto.getDataTransfer(), DATE));
        }
        if (dto.getHoraTransfer() != null) {
            t.setHoraTransfer(LocalTime.parse(dto.getHoraTransfer(), TIME));
        }
        if (dto.getStatus() != null) {
            t.setStatus(StatusTransfer.valueOf(dto.getStatus()));
        }
        t.setOrdemServico(os);
        List<Passageiro> pax = new ArrayList<>();
        for (OfflinePassageiroDto pd : dto.getPassageiros()) {
            Passageiro p = new Passageiro();
            p.setId(pd.getId());
            p.setNome(pd.getNome());
            p.setDocumento(pd.getDocumento());
            if (pd.getTipoDocumento() != null) {
                p.setTipoDocumento(TipoDocumento.valueOf(pd.getTipoDocumento()));
            }
            p.setNacionalidade(pd.getNacionalidade());
            pax.add(p);
        }
        t.setPassageiros(pax);
        return t;
    }

    // atualiza status no dto antes de rewrite do arquivo

    public static OfflineSnapshot mergeStatusUpdate(OfflineSnapshot snap, int transferId, StatusTransfer status) {
        for (OfflineOrdemServicoDto os : snap.getOrdens()) {
            for (OfflineTransferDto t : os.getTransfers()) {
                if (transferId == t.getId()) {
                    t.setStatus(status.name());
                }
            }
        }
        return snap;
    }

    public static OfflineSnapshot removeTransfer(OfflineSnapshot snap, int transferId) {
        for (OfflineOrdemServicoDto os : new ArrayList<>(snap.getOrdens())) {
            os.getTransfers().removeIf(t -> transferId == t.getId());
        }
        snap.getOrdens().removeIf(o -> o.getTransfers().isEmpty());
        return snap;
    }

    // agrupa transfers que ja tem mesmo os attached (atalho de dominio)

    public static List<OrdemServico> groupTransfersToOrdens(List<Transfer> transfers) {
        Map<Integer, OrdemServico> map = new LinkedHashMap<>();
        for (Transfer t : transfers) {
            if (t.getOrdemServico() == null) continue;
            Integer osId = t.getOrdemServico().getId();
            OrdemServico os = map.computeIfAbsent(osId, id -> {
                OrdemServico o = t.getOrdemServico();
                o.setTransfers(new ArrayList<>());
                return o;
            });
            os.getTransfers().add(t);
        }
        return new ArrayList<>(map.values());
    }
}
