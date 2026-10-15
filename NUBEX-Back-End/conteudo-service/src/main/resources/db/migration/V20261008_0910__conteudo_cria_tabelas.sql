CREATE TABLE pesquisador (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cargo VARCHAR(120),
    secao VARCHAR(25) NOT NULL CHECK(secao IN ('COORDENACAO', 'PESQUISADOR', 'INICIACAO_CIENTIFICA', 'OUTROS')),
    linha_pesquisa VARCHAR(255),
    bio TEXT,
    foto_url VARCHAR(255),
    lattes_url VARCHAR(255),
    orcid_url VARCHAR(255),
    google_academico_url VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE faq (
    id BIGSERIAL PRIMARY KEY,
    categoria VARCHAR(80) NOT NULL,
    pergunta VARCHAR(255) NOT NULL,
    resposta TEXT NOT NULL,
    ordem INTEGER NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE contato (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    instituicao VARCHAR(150),
    segmento VARCHAR(15) NOT NULL CHECK (segmento IN ('PESQUISA', 'INDUSTRIA', 'MIDIA')),
    mensagem TEXT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    lido BOOLEAN NOT NULL DEFAULT FALSE
);