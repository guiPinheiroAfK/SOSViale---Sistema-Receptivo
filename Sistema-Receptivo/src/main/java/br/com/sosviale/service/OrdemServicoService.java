package br.com.sosviale.service;

import br.com.sosviale.config.JPAUtil;
import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.repository.OrdemServicoRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class OrdemServicoService {

    public void cadastrar(OrdemServico os) {
        if (os == null) throw new IllegalArgumentException("Ordem de serviço não pode ser nula.");
        if (os.getMotorista() == null) throw new IllegalArgumentException("Motorista é obrigatório.");
        if (os.getVeiculo() == null) throw new IllegalArgumentException("Veículo é obrigatório.");

        EntityManager em = JPAUtil.getEntityManager();
        try {
            new OrdemServicoRepository(em).salvar(os);
        } finally {
            em.close();
        }
    }

    public OrdemServico buscarPorId(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return new OrdemServicoRepository(em).buscarPorId(id);
        } finally {
            em.close();
        }
    }

    public List<OrdemServico> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return new OrdemServicoRepository(em).listarTodos();
        } finally {
            em.close();
        }
    }

    public void atualizar(OrdemServico os) {
        if (os == null || os.getId() == null)
            throw new IllegalArgumentException("Ordem de serviço inválida para atualização.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            new OrdemServicoRepository(em).atualizar(os);
        } finally {
            em.close();
        }
    }
}