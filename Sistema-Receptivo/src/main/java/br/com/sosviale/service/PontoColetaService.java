package br.com.sosviale.service;

import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.repository.PontoColetaRepository;

import java.util.List;

public class PontoColetaService {

    private final PontoColetaRepository repository = new PontoColetaRepository();

    public List<PontoColeta> listarTodos() {
        // Se estiver usando JPA/Hibernate:
        return repository.listarTodos();
        // Ou se estiver usando EntityManager diretamente:
        // return em.createQuery("FROM PontoColeta", PontoColeta.class).getResultList();
    }

    public void cadastrar(PontoColeta pc) {
        repository.salvar(pc);
    }

    public void atualizar(PontoColeta pc) {
        repository.atualizar(pc);
    }

    public void excluir(Long id) {
        repository.excluir(id); // Ajuste o cast se o ID no banco for Integer
    }

    public PontoColeta buscarPorId(Long id) {
        return repository.buscarPorId(id);
    }
}