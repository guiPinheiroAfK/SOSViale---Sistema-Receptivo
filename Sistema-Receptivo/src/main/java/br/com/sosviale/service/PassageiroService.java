package br.com.sosviale.service;

import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.TipoDocumento;
import br.com.sosviale.repository.PassageiroRepository;
import br.com.sosviale.util.CryptoUtil;
import br.com.sosviale.util.DocumentoValidator;

import java.util.List;

// serviço de passageiro: versão com criptografia de documentos sensíveis.

public class PassageiroService {

    private final PassageiroRepository repository = new PassageiroRepository();
    //  salvar

     // salva um passageiro com o documento criptografado.
     //a validação de formato é feita ANTES da criptografia (sobre o texto puro).

    public void salvar(String nome, String documento, TipoDocumento tipo, String nacionalidade) {
        validarEntrada(nome, documento, tipo);

        String documentoCriptografado = CryptoUtil.encrypt(documento.trim(), isPassaporte(tipo));

        repository.salvar(new Passageiro(
                nome.trim(),
                documentoCriptografado,
                tipo,
                nacionalidade
        ));
    }

    //atualizar
    public void atualizar(Integer id, String nome, String documento,
                          TipoDocumento tipo, String nacionalidade) {
        if (id == null) throw new IllegalArgumentException("ID inválido.");
        validarEntrada(nome, documento, tipo);

        String documentoCriptografado = CryptoUtil.encrypt(documento.trim(), isPassaporte(tipo));

        Passageiro p = new Passageiro(nome.trim(), documentoCriptografado, tipo, nacionalidade);
        p.setId(id);
        repository.atualizar(p);
    }

    // ecluir

    public void excluir(Integer id) {
        if (id == null) throw new IllegalArgumentException("ID inválido.");
        repository.excluir(id);
    }


    //  conssultas — descriptografam antes de retornar à UI
    // lista todos os passageiros com documentos descriptografados.

    public List<Passageiro> listarTodos() {
        List<Passageiro> passageiros = repository.listarTodos();
        passageiros.forEach(this::descriptografarDocumento);
        return passageiros;
    }

    //busca um passageiro por ID com documento descriptografado.

    public Passageiro buscarPorId(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID do passageiro inválido.");
        Passageiro p = repository.buscarPorId(id);
        if (p != null) descriptografarDocumento(p);
        return p;
    }

    //  metodos privados
    private void descriptografarDocumento(Passageiro p) {
        if (p.getDocumento() == null) return;
        try {
            String decriptado = CryptoUtil.decrypt(p.getDocumento(), isPassaporte(p.getTipoDocumento()));
            p.setDocumento(decriptado);
        } catch (CryptoUtil.CryptoException e) {
            // se falhar a descriptografia, mantém o valor cifrado e loga o problema.
            // nap lança exceção para não quebrar a listagem inteira por um registro corrompido.
            System.err.println("[CRYPTO] Falha ao descriptografar documento do passageiro id="
                    + p.getId() + ": " + e.getMessage());
        }
    }

    private boolean isPassaporte(TipoDocumento tipo) {
        return tipo == TipoDocumento.PASSAPORTE;
    }

    private void validarEntrada(String nome, String documento, TipoDocumento tipo) {
        if (nome == null || nome.trim().isEmpty())
            throw new IllegalArgumentException("Nome é obrigatório.");
        if (tipo == null)
            throw new IllegalArgumentException("Tipo de documento é obrigatório.");
        if (documento == null || documento.trim().isEmpty())
            throw new IllegalArgumentException("Documento é obrigatório.");

        // validação de formato sobre o texto puro (antes de criptografar)
        if (!DocumentoValidator.isValido(documento.trim(), tipo))
            throw new IllegalArgumentException("O formato do documento é inválido para " + tipo);
    }
}
