-- ====================================================================
-- Script SQL: Adicionar 'codigo_modelo' único para cada produto
-- Banco de dados: estoque
-- Tabela: produtos
-- ====================================================================

-- 1. Se a tabela já existir e você quiser adicionar a coluna diretamente:
ALTER TABLE produtos 
ADD COLUMN codigo_modelo VARCHAR(100) UNIQUE AFTER id;

-- ====================================================================
-- DICA: Se a sua tabela já tiver produtos cadastrados e der erro ao 
-- aplicar o UNIQUE direto (por valores nulos/vazios duplicados), 
-- execute os comandos abaixo na ordem:
-- ====================================================================
-- Passo A: Adiciona a coluna permitindo nulo
-- ALTER TABLE produtos ADD COLUMN codigo_modelo VARCHAR(100) AFTER id;

-- Passo B: Gera um código provisório para os produtos existentes
-- UPDATE produtos SET codigo_modelo = CONCAT('MOD-', LPAD(id, 4, '0')) WHERE codigo_modelo IS NULL;

-- Passo C: Adiciona a restrição UNIQUE e NOT NULL
-- ALTER TABLE produtos MODIFY COLUMN codigo_modelo VARCHAR(100) NOT NULL;
-- ALTER TABLE produtos ADD CONSTRAINT uk_produtos_codigo_modelo UNIQUE (codigo_modelo);
