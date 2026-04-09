--Log de alteracoes de preco
CREATE TABLE IF NOT EXISTS logs_sistema (
    id SERIAL PRIMARY KEY,
    tabela_afetada VARCHAR(50),
    registro_id INT,
    mensagem TEXT,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION fn_log_preco_transfer()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.valor_base <> NEW.valor_base THEN
        INSERT INTO logs_sistema (tabela_afetada, registro_id, mensagem)
        VALUES ('transfers', NEW.id, 'Preço alterado de R$' || OLD.valor_base || ' para R$' || NEW.valor_base);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_preco
AFTER UPDATE ON transfers
FOR EACH ROW EXECUTE FUNCTION fn_log_preco_transfer();


--Nosso log de cancelamento
CREATE OR REPLACE FUNCTION fn_log_cancelamento()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO logs_sistema (tabela_afetada, registro_id, mensagem)
    VALUES ('transfers', OLD.id, 'Agendamento removido: ' || OLD.origem || ' -> ' || OLD.destino);
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_delete_transfer
AFTER DELETE ON transfers
FOR EACH ROW EXECUTE FUNCTION fn_log_cancelamento();


--Trigger para impedir cadastrar uma transfer no mesmo dia e o horario de uma ja cadastrada
--Um motorista nao pode estar em 2 lugares ao mesmo tempo
CREATE OR REPLACE FUNCTION fn_check_conflito_motorista()
RETURNS TRIGGER AS $$
BEGIN
    -- Verifica se o motorista já tem um transfer no mesmo dia e hora
    IF EXISTS (
        SELECT 1 FROM transfers
        WHERE motorista_id = NEW.motorista_id
        AND data_hora = NEW.data_hora
        AND id <> COALESCE(NEW.id, 0) -- Garante que não está comparando com ele mesmo no update
    ) THEN
        RAISE EXCEPTION 'Conflito de Agenda: O motorista já possui um transfer agendado para este horário!';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_agenda_motorista
BEFORE INSERT OR UPDATE ON transfers
FOR EACH ROW EXECUTE FUNCTION fn_check_conflito_motorista();