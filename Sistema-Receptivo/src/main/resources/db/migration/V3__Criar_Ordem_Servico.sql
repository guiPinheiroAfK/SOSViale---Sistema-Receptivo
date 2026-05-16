SET search_path TO public;

-- Criação da tabela de Ordens de Serviço
CREATE TABLE ordens_servico (
                                id SERIAL PRIMARY KEY,
                                data_servico DATE NOT NULL, -- O dia que o motorista vai fazer esses transfers
                                motorista_id INT REFERENCES motoristas(id),
                                veiculo_id INT REFERENCES veiculos(id),
                                status VARCHAR(20) DEFAULT 'ABERTA'
);

-- Alteração na tabela de transfers
ALTER TABLE transfers
DROP CONSTRAINT transfers_motorista_id_fkey,
    DROP CONSTRAINT transfers_veiculo_id_fkey,
    DROP COLUMN motorista_id,
    DROP COLUMN veiculo_id,
    ADD COLUMN os_id INT REFERENCES ordens_servico(id);