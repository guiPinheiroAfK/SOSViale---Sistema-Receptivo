-- 1. Tabela de Logs (Onde os VALUES são guardados)
CREATE TABLE IF NOT EXISTS logs_transfers(
                                             id SERIAL PRIMARY KEY,
                                             tabela_afetada VARCHAR(50),
                                             registro_id INT,
                                             mensagem TEXT,
                                             data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Função para UPDATE (Preço e Status)
CREATE OR REPLACE FUNCTION fn_log_update_transfer()
    RETURNS TRIGGER AS $$
BEGIN
    -- Se mudou o preço, gera log
    IF OLD.valor_base <> NEW.valor_base THEN
        INSERT INTO logs_transfers (tabela_afetada, registro_id, mensagem)
        VALUES ('transfers', NEW.id, 'FINANCEIRO: Preço alterado de R$' || OLD.valor_base || ' para R$' || NEW.valor_base);
    END IF;

    -- Se mudou o status, gera log
    IF OLD.status <> NEW.status THEN
        INSERT INTO logs_transfers (tabela_afetada, registro_id, mensagem)
        VALUES ('transfers', NEW.id, 'STATUS: Mudou de ' || OLD.status || ' para ' || NEW.status);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger de Update
DROP TRIGGER IF EXISTS trg_audit_update ON transfers;
CREATE TRIGGER trg_audit_update
    AFTER UPDATE ON transfers
    FOR EACH ROW EXECUTE FUNCTION fn_log_update_transfer();


-- 3. Função para DELETE (Cancelamento)
CREATE OR REPLACE FUNCTION fn_log_cancelamento()
    RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO logs_transfers (tabela_afetada, registro_id, mensagem)
    VALUES ('transfers', OLD.id, 'REMOÇÃO: Agendamento removido: ' || OLD.origem || ' -> ' || OLD.destino);
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Trigger de Delete
DROP TRIGGER IF EXISTS trg_delete_transfer ON transfers;
CREATE TRIGGER trg_delete_transfer
    AFTER DELETE ON transfers
    FOR EACH ROW EXECUTE FUNCTION fn_log_cancelamento();