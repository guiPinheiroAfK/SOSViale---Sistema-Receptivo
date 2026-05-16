package br.com.sosviale.service;

import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.TipoDocumento;
import br.com.sosviale.repository.PassageiroRepository;
import br.com.sosviale.util.CryptoUtil;
import br.com.sosviale.util.DocumentoValidator;

import java.util.List;

/*
 * Serviço de Passageiros — versão com criptografia de documentos sensíveis.
 *
 * ESTRATÉGIA DE CRIPTOGRAFIA:
 *   - Passaportes (TipoDocumento.PASSAPORTE): sempre criptografados com AES-256-GCM.
 *   - CPF: criptografado (dado pessoal sensível por LGPD).
 *   - Outros documentos: criptografados por padrão.
 *
 * O BANCO DE DADOS NUNCA VÊ O NÚMERO REAL do documento.
 * A descriptografia ocorre apenas na camada de serviço, ao retornar dados para a UI.
 *
 * LGPD (Lei Geral de Proteção de Dados):
 *   Passaportes e CPFs são dados pessoais sensíveis. A criptografia em repouso
 *   é uma medida técnica obrigatória para conformidade com o Art. 46 da LGPD.
 */
public class PassageiroService {

    private final PassageiroRepository repository = new PassageiroRepository();

    // ════════════════════════════════════════════════════════════════════════
    //  SALVAR
    // ════════════════════════════════════════════════════════════════════════

    /*
     * Salva um passageiro com o documento criptografado.
     * A validação de formato é feita ANTES da criptografia (sobre o texto puro).
     */
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

    // ════════════════════════════════════════════════════════════════════════
    //  ATUALIZAR
    // ════════════════════════════════════════════════════════════════════════

    public void atualizar(Integer id, String nome, String documento,
                          TipoDocumento tipo, String nacionalidade) {
        if (id == null) throw new IllegalArgumentException("ID inválido.");
        validarEntrada(nome, documento, tipo);

        String documentoCriptografado = CryptoUtil.encrypt(documento.trim(), isPassaporte(tipo));

        Passageiro p = new Passageiro(nome.trim(), documentoCriptografado, tipo, nacionalidade);
        p.setId(id);
        repository.atualizar(p);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  EXCLUIR
    // ════════════════════════════════════════════════════════════════════════

    public void excluir(Integer id) {
        if (id == null) throw new IllegalArgumentException("ID inválido.");
        repository.excluir(id);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CONSULTAS — descriptografam antes de retornar à UI
    // ════════════════════════════════════════════════════════════════════════

    /*
     * Lista todos os passageiros com documentos descriptografados.
     * Use este metodo para exibição na interface.
     */
    public List<Passageiro> listarTodos() {
        List<Passageiro> passageiros = repository.listarTodos();
        passageiros.forEach(this::descriptografarDocumento);
        return passageiros;
    }

    /*
     * Busca um passageiro por ID com documento descriptografado.
     */
    public Passageiro buscarPorId(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID do passageiro inválido.");
        Passageiro p = repository.buscarPorId(id);
        if (p != null) descriptografarDocumento(p);
        return p;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MÉTODOS PRIVADOS
    // ════════════════════════════════════════════════════════════════════════

    /*
     * Descriptografa o campo documento de um Passageiro in-place.
     * Trata dados legados (texto puro) com graceful fallback.
     */
    private void descriptografarDocumento(Passageiro p) {
        if (p.getDocumento() == null) return;
        try {
            String decriptado = CryptoUtil.decrypt(p.getDocumento(), isPassaporte(p.getTipoDocumento()));
            p.setDocumento(decriptado);
        } catch (CryptoUtil.CryptoException e) {
            // Se falhar a descriptografia, mantém o valor cifrado e loga o problema.
            // NÃO lança exceção para não quebrar a listagem inteira por um registro corrompido.
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

        // Validação de formato sobre o texto puro (antes de criptografar)
        if (!DocumentoValidator.isValido(documento.trim(), tipo))
            throw new IllegalArgumentException("O formato do documento é inválido para " + tipo);
    }
}
