-- 1. Gestão de Passageiros (RF02)
CREATE TABLE passageiros (
                             id SERIAL PRIMARY KEY,
                             nome VARCHAR(100) NOT NULL,
                             documento VARCHAR(20) NOT NULL, -- RG ou Passaporte para fronteira
                             nacionalidade VARCHAR(50) DEFAULT 'Brasileira'
);

-- 2. Controle de Frota/Recursos (RF04)
CREATE TABLE motoristas (
                            id SERIAL PRIMARY KEY,
                            nome VARCHAR(100) NOT NULL,
                            cnh VARCHAR(20) UNIQUE NOT NULL,
                            latitude_atual DECIMAL(10,7),   -- posição atual do motorista
                            longitude_atual DECIMAL(10,7)   -- pode ser NULL até ele atualizar
);

CREATE TABLE veiculos (
                          id SERIAL PRIMARY KEY,
                          label VARCHAR(50) NOT NULL, -- Ex: "Van 01", "Sedan Executivo"
                          placa VARCHAR(10) UNIQUE NOT NULL,
                          capacidade INT NOT NULL
);

-- 3. Agendamento de Transfers (RF01 e RF05)
CREATE TABLE transfers (
                           id SERIAL PRIMARY KEY,
                           data_hora TIMESTAMP NOT NULL,
                           origem VARCHAR(100) NOT NULL,
                           destino VARCHAR(100) NOT NULL,
                           status VARCHAR(20) DEFAULT 'PENDENTE', -- PENDENTE, EM_TRANSITO, CONCLUIDO, CANCELADO
                           valor_base DECIMAL(10,2),
                           motorista_id INT REFERENCES motoristas(id),
                           veiculo_id INT REFERENCES veiculos(id)
);

-- 4. Logística de Múltiplos Pontos (RF03)
CREATE TABLE pontos_coleta (
                               id SERIAL PRIMARY KEY,
                               transfer_id INT REFERENCES transfers(id) ON DELETE CASCADE,
                               local_coleta VARCHAR(100) NOT NULL,
                               ordem_parada INT NOT NULL, -- Define a sequência lógica do pick-up
                               horario_previsto TIME,
                               latitude DECIMAL(10,7) NOT NULL,   -- necessário pro PathFinding
                               longitude DECIMAL(10,7) NOT NULL
);

-- 5. Tabela Associativa: Quem está em qual Transfer?
CREATE TABLE transfer_passageiros (
                                      transfer_id INT REFERENCES transfers(id) ON DELETE CASCADE,
                                      passageiro_id INT REFERENCES passageiros(id) ON DELETE CASCADE,
                                      PRIMARY KEY (transfer_id, passageiro_id)
);