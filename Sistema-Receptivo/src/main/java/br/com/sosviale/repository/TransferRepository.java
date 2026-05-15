package br.com.sosviale.repository;

import br.com.sosviale.config.JPAUtil;
import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.service.StatusTransfer;
import jakarta.persistence.EntityManager;
import java.util.List;

// DAO responsável pelas operações de persistência da entidade Transfer.
public class TransferRepository {

    public void salvar(Transfer transfer) {
        if (transfer == null) throw new IllegalArgumentException("transfer não pode ser nulo.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(transfer);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            System.err.println("erro ao cadastrar transfer: " + e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    // Retorna todos os transfers ordenados por data e depois por hora.
    public List<Transfer> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT t FROM Transfer t ORDER BY t.dataTransfer ASC, t.horaTransfer ASC",
                    Transfer.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    /** Transfers com OS atribuída — usado na tela do motorista (ServicosPanel). */
    public List<Transfer> listarVinculadosOrdemServico() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT DISTINCT t FROM Transfer t " +
                            "JOIN FETCH t.ordemServico os " +
                            "LEFT JOIN FETCH os.motorista " +
                            "LEFT JOIN FETCH t.passageiros " +
                            "WHERE t.ordemServico IS NOT NULL " +
                            "ORDER BY os.id ASC, t.horaTransfer ASC",
                    Transfer.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public void atualizar(Transfer transfer) {
        if (transfer == null || transfer.getId() == null)
            throw new IllegalArgumentException("transfer inválido para atualização.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(transfer);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /*
     * Vincula um Transfer a uma OrdemServico de forma segura.
     *
     * Diferente do atualizar(), este método:
     *   - Abre seu próprio EntityManager e busca o Transfer pelo ID (objeto attached)
     *   - Seta apenas os campos necessários para a vinculação (ordemServico + status)
     *   - NÃO reprocessa a lógica financeira (valorBase, taxas, etc.)
     *   - Faz o merge dentro de uma única transação atômica
     *
     * @param transferId ID do transfer a ser vinculado
     * @param osId       ID da OrdemServico de destino
     */
    public void vincular(Integer transferId, Integer osId) {
        if (transferId == null || transferId <= 0)
            throw new IllegalArgumentException("ID do transfer inválido.");
        if (osId == null || osId <= 0)
            throw new IllegalArgumentException("ID da OS inválido.");

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // find() retorna objeto managed/attached — o merge vai ser limpo
            Transfer t = em.find(Transfer.class, transferId);
            if (t == null)
                throw new IllegalArgumentException("Transfer #" + transferId + " não encontrado.");

            OrdemServico os = em.find(OrdemServico.class, osId);
            if (os == null)
                throw new IllegalArgumentException("OS #" + osId + " não encontrada.");

            t.setOrdemServico(os);
            t.setStatus(StatusTransfer.NA_OS);

            // Como t é managed, o JPA detecta a mudança automaticamente.
            // O merge aqui é redundante mas explícito para clareza.
            em.merge(t);

            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Erro ao vincular transfer #" + transferId
                    + " à OS #" + osId + ": " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void excluir(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido para exclusão.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Transfer t = em.find(Transfer.class, id);
            if (t != null) em.remove(t);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Transfer buscarPorId(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Transfer> lista = em.createQuery(
                    "SELECT DISTINCT t FROM Transfer t " +
                            "LEFT JOIN FETCH t.passageiros " +
                            "LEFT JOIN FETCH t.ordemServico " +
                            "WHERE t.id = :id",
                    Transfer.class
            ).setParameter("id", id).getResultList();
            return lista.isEmpty() ? null : lista.get(0);
        } finally {
            em.close();
        }
    }

    public int contarPassageirosPorVeiculo(Integer veiculoId) {
        if (veiculoId == null || veiculoId <= 0)
            throw new IllegalArgumentException("ID de veículo inválido.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Long total = em.createQuery(
                    "SELECT COUNT(p) FROM Transfer t JOIN t.passageiros p " +
                            "WHERE t.ordemServico.veiculo.id = :veiculoId AND t.status = 'NA_OS'",
                    Long.class
            ).setParameter("veiculoId", veiculoId).getSingleResult();
            return total.intValue();
        } finally {
            em.close();
        }
    }

    public long contarSemOrdemServico() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT COUNT(t) FROM Transfer t WHERE t.ordemServico IS NULL",
                    Long.class
            ).getSingleResult();
        } finally {
            em.close();
        }
    }
}