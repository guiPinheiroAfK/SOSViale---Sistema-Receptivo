-- 1. MOTORISTAS
INSERT INTO motoristas (nome, cnh, telefone) VALUES
                                                 ('João Silva', '12345678901', '(45) 99988-7766'),
                                                 ('Maria Santos', '98765432109',  '(45) 98877-6655');

-- 2. VEÍCULOS
INSERT INTO veiculos (marca, label, placa, capacidade, tipo) VALUES
                                                                 ('Mercedes','Sprinter 515', 'ABC-1D23', 15, 'VAN'),
                                                                 ('Toyota','Corolla XEI', 'DEF-2G34', 4, 'SEDAN'),
                                                                 ('Jeep','Compass Longitude', 'GHI-3H45', 5, 'SUV');

-- 3. LOCAIS (Mantido igual)
INSERT INTO pontos_coleta (local_coleta, latitude, longitude) VALUES
                                                                  ('Aeroporto Internacional IGU', -25.5947, -54.4907),
                                                                  ('Parque Nacional (Cataratas)', -25.6888, -54.4447),
                                                                  ('Hotel Bourbon Cataratas', -25.5583, -54.5580),
                                                                  ('Marco das 3 Fronteiras', -25.5886, -54.5910),
                                                                  ('Terminal Rodoviário', -25.5117, -54.5721),
                                                                  ('Itaipu Binacional', -25.4481, -54.5912);

-- 4. TRANSFERS
INSERT INTO transfers (data_transfer, hora_transfer, origem, destino, status, valor_base, moeda_origem) VALUES
    (CURRENT_DATE + 1, '10:00:00', 'Aeroporto Internacional IGU', 'Hotel Bourbon Cataratas', 'AGUARDANDO_OS', 120.00, 'BRL'),
    (CURRENT_DATE + 1, '14:00:00', 'Hotel Bourbon Cataratas', 'Parque Nacional (Cataratas)', 'AGUARDANDO_OS', 80.00, 'BRL');

-- 5. ORDENS DE SERVIÇO
INSERT INTO ordens_servico (data_servico, motorista_id, veiculo_id, status) VALUES
                                                                                (CURRENT_DATE + 1, 1, 1, 'ABERTA'),
                                                                                (CURRENT_DATE + 2, 2, 3, 'ABERTA');

-- 6. PASSAGEIROS
-- Corrigido: Removido o campo 'id' para evitar o erro de duplicidade (o banco gera o ID 1, 2, 3...)
-- Ajustado: Usando 'documento' em vez de 'num_documento' conforme seu log
INSERT INTO passageiros (nome, documento, tipo_documento, nacionalidade) VALUES
                                                                             ('Guilherme Pinheiro', '11144477735', 'CPF', 'BRASILEIRO'),
                                                                             ('Beatriz Oliveira', 'BR123456', 'PASSAPORTE', 'BRASILEIRO');