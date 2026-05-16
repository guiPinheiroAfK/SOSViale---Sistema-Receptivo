-- Ajusta os tipos numéricos para casar nativamente com o Double do Java
ALTER TABLE paradas_os ALTER COLUMN latitude TYPE DOUBLE PRECISION;
ALTER TABLE paradas_os ALTER COLUMN longitude TYPE DOUBLE PRECISION;