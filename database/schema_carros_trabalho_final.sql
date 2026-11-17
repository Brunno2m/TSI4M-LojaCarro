CREATE DATABASE IF NOT EXISTS carros
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE carros;

CREATE TABLE IF NOT EXISTS usuario (
                                       id BIGINT NOT NULL AUTO_INCREMENT,
                                       nome VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL DEFAULT 'CLIENTE',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT chk_usuario_perfil CHECK (perfil IN ('ADMIN', 'CLIENTE'))
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS carro (
                                     id BIGINT NOT NULL AUTO_INCREMENT,
                                     marca VARCHAR(60) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    ano INT NOT NULL COMMENT 'Ano de fabricacao (atributo ano da classe Carro)',
    ano_modelo INT NOT NULL,
    cor VARCHAR(40) NOT NULL,
    quilometragem BIGINT NOT NULL,
    tipo_combustivel VARCHAR(30) NOT NULL,
    tipo_transmissao VARCHAR(30) NOT NULL,
    preco DECIMAL(12, 2) NOT NULL,
    descricao TEXT NOT NULL,
    situacao VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL',
    foto_principal VARCHAR(255) NOT NULL COMMENT 'Caminho para arquivo validado e salvo em IMG/',
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_carro_marca_modelo (marca, modelo),
    INDEX idx_carro_preco (preco),
    INDEX idx_carro_ano_modelo (ano_modelo),
    CONSTRAINT chk_carro_ano CHECK (ano BETWEEN 1886 AND 2100),
    CONSTRAINT chk_carro_ano_modelo CHECK (ano_modelo BETWEEN 1886 AND 2101),
    CONSTRAINT chk_carro_km CHECK (quilometragem >= 0),
    CONSTRAINT chk_carro_preco CHECK (preco > 0),
    CONSTRAINT chk_carro_situacao CHECK (situacao IN ('DISPONIVEL', 'VENDIDO', 'INATIVO'))
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS agendamento (
                                           id BIGINT NOT NULL AUTO_INCREMENT,
                                           usuario_id BIGINT NOT NULL,
                                           carro_id BIGINT NOT NULL,
                                           inicio DATETIME NOT NULL,
                                           fim DATETIME NOT NULL,
                                           status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADO',
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelado_em DATETIME NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_agendamento_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuario (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_agendamento_carro FOREIGN KEY (carro_id)
    REFERENCES carro (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_agendamento_intervalo CHECK (fim > inicio),
    CONSTRAINT chk_agendamento_status CHECK (status IN ('CONFIRMADO', 'EM_ANDAMENTO', 'CANCELADO', 'REALIZADO')),
    INDEX idx_agendamento_carro_periodo (carro_id, status, inicio, fim),
    INDEX idx_agendamento_usuario_inicio (usuario_id, inicio)
    ) ENGINE=InnoDB;

-- O indice acima acelera a busca, mas NAO impede conflitos sozinho.
-- A API deve validar duracao de 30 minutos, horario de funcionamento,
-- data futura, situacao do carro e sobreposicao de intervalos.
-- A confirmacao deve ser transacional, com bloqueio adequado para
-- impedir agendamentos simultaneos no mesmo carro.

CREATE TABLE IF NOT EXISTS log_operacao (
                                            id BIGINT NOT NULL AUTO_INCREMENT,
                                            usuario_id BIGINT NULL,
                                            usuario_nome VARCHAR(120) NULL,
    entidade VARCHAR(40) NOT NULL,
    acao VARCHAR(50) NOT NULL,
    referencia_id BIGINT NULL,
    detalhes TEXT NOT NULL,
    data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_log_data_hora (data_hora),
    INDEX idx_log_usuario (usuario_id),
    INDEX idx_log_entidade_acao (entidade, acao)
    ) ENGINE=InnoDB;

-- Para confirmar a criacao das tabelas (consultas sem alteracao de dados):
SHOW TABLES;
SHOW COLUMNS FROM carro;
SHOW COLUMNS FROM agendamento;
