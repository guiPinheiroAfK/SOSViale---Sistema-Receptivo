-- 1. Adicionar telefone ao motorista
ALTER TABLE motoristas ADD COLUMN telefone VARCHAR(20);

-- 2. Adicionar tipo ao veículo (e marca para facilitar filtros)
ALTER TABLE veiculos ADD COLUMN tipo VARCHAR(50); -- Ex: Van, SUV, Sedan
ALTER TABLE veiculos ADD COLUMN marca VARCHAR(50); -- Ex: Chery, Toyota

-- 3. Tabela de ligação Transfer <-> Passageiro (Muitos-para-Muitos)
CREATE TABLE transfer_passageiro (
                                     transfer_id INTEGER NOT NULL,
                                     passageiro_id INTEGER NOT NULL,
                                     PRIMARY KEY (transfer_id, passageiro_id),
                                     CONSTRAINT fk_transfer FOREIGN KEY (transfer_id) REFERENCES transfers(id) ON DELETE CASCADE,
                                     CONSTRAINT fk_passageiro FOREIGN KEY (passageiro_id) REFERENCES passageiros(id) ON DELETE CASCADE
);