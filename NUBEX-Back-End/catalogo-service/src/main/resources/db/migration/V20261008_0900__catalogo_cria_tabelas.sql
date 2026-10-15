CREATE TABLE unidade_estrategica (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    slug VARCHAR(80) NOT NULL UNIQUE,
    descricao TEXT,
    imagem_url VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE laboratorio (
    id BIGSERIAL PRIMARY KEY,
    unidade_id BIGINT NOT NULL REFERENCES unidade_estrategica (id),
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE equipamento (
    id BIGSERIAL PRIMARY KEY,
    laboratorio_id BIGINT NOT NULL REFERENCES laboratorio(id),
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL' CHECK(status IN ('DISPONIVEL', 'MANUTENCAO', 'CALIBRACAO')),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE servico (
    id BIGSERIAL PRIMARY KEY,
    unidade_id BIGINT NOT NULL REFERENCES unidade_estrategica(id),
    categoria VARCHAR(100) NOT NULL,
    tecnicas TEXT NOT NULL,
    aplicacoes TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_laboratorio_unidade ON laboratorio(unidade_id);
CREATE INDEX idx_equipamento_laboratorio ON equipamento(laboratorio_id);
CREATE INDEX idx_servico_unidade ON servico(unidade_id);