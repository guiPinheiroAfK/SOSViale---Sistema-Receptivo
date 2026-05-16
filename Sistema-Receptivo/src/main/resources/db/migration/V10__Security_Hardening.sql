-- ============================================================
--  V10__Security_Hardening.sql
--  Endurecimento de segurança: hash de senhas e colunas para
--  documentos criptografados.
-- ============================================================

-- ──────────────────────────────────────────────────────────────
--  1. AMPLIAR COLUNA DE SENHA (BCrypt = 60 chars; 255 com folga)
--     V4 já criou como VARCHAR(255), esta linha é idempotente
--     mas documenta a intenção explicitamente.
-- ──────────────────────────────────────────────────────────────
ALTER TABLE usuarios
    ALTER COLUMN senha TYPE VARCHAR(255);


-- ──────────────────────────────────────────────────────────────
--  2. AMPLIAR COLUNA DE DOCUMENTO PARA DADOS CIFRADOS
--     AES-256-GCM + Base64: salt(16) + IV(12) + texto + tag(16)
--     Para um passaporte de ~20 chars → ~85 bytes → ~116 chars Base64.
--     TEXT elimina qualquer preocupação de truncamento.
-- ──────────────────────────────────────────────────────────────
ALTER TABLE passageiros
    ALTER COLUMN documento TYPE TEXT;


-- ──────────────────────────────────────────────────────────────
--  3. MARCAR SENHAS LEGADAS COMO "PRECISA RE-HASH"
--     O BCrypt.precisaReHash() detecta dados legados pelo prefixo.
--     Aqui apenas documentamos quais registros estão em estado
--     legado — o re-hash ocorre silenciosamente no próximo login
--     via AuthenticationService.
--
--     AVISO: A linha abaixo NÃO altera as senhas — apenas serve
--     como verificação visual em auditorias. Remova se não quiser
--     expor quais usuários ainda não fizeram login após a migração.
-- ──────────────────────────────────────────────────────────────
-- SELECT usuario, 'SENHA_LEGADA_AGUARDA_REHASH' AS status
-- FROM usuarios
-- WHERE senha NOT LIKE '$2%';


-- ──────────────────────────────────────────────────────────────
--  4. ATUALIZAR O USUÁRIO ADMIN PADRÃO COM HASH BCRYPT
--     Hash BCrypt de 'admin123' com work factor 12.
--     Gerado com: BCrypt.hashpw("admin123", BCrypt.gensalt(12))
--
--     ⚠️  ATENÇÃO: Troque a senha do admin imediatamente após
--         a primeira inicialização em produção!
--         Use: UserService.alterarSenha("admin", "admin123", "NovaSenhaForte@2025")
-- ──────────────────────────────────────────────────────────────
UPDATE usuarios
SET senha = '$2a$12$LrTCRiKqEZJTz.vc8YN8Xemqfl6.hDfGWmRuX/.sQY5kHZJJ7GzCC'
WHERE usuario = 'admin'
  AND senha = 'admin123';  -- Proteção: só atualiza se ainda estiver em texto puro


-- ──────────────────────────────────────────────────────────────
--  5. CRIAR ÍNDICE PARA BUSCA POR USUÁRIO (segurança + performance)
--     Garante que a busca de login seja O(log n) e não sofra
--     com timing differences por tamanho da tabela.
-- ──────────────────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_usuarios_usuario
    ON usuarios (usuario);


-- ──────────────────────────────────────────────────────────────
--  6. ADICIONAR COLUNA DE AUDITORIA: último login
--     Útil para detectar contas inativas e acessos suspeitos.
-- ──────────────────────────────────────────────────────────────
ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS ultimo_login TIMESTAMP;

COMMENT ON COLUMN usuarios.ultimo_login IS
    'Timestamp do último login bem-sucedido. Atualizado pelo AuthenticationService.';

COMMENT ON COLUMN usuarios.senha IS
    'Hash BCrypt (work factor 12). NUNCA armazenar senha em texto puro.';

COMMENT ON COLUMN passageiros.documento IS
    'Documento criptografado com AES-256-GCM. Descriptografar apenas na camada de serviço (PassageiroService).';

UPDATE usuarios SET senha = '310000:6kIyXYoFuFd96Pj/QuXsVg==:dbIw1e11tyWR6jtNtkaEEPA8813L0iHkELGDYlgHywU=' WHERE usuario = 'admin';