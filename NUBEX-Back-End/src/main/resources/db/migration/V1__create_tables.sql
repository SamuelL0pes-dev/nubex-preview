CREATE TABLE unidade_estrategica (
                                     id          BIGSERIAL PRIMARY KEY,
                                     nome        VARCHAR(120) NOT NULL,
                                     slug        VARCHAR(80)  NOT NULL UNIQUE,
                                     descricao   TEXT,
                                     imagem_url  VARCHAR(255),
                                     ativo       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE equipamento (
                             id          BIGSERIAL PRIMARY KEY,
                             unidade_id  BIGINT       NOT NULL REFERENCES unidade_estrategica (id),
                             nome        VARCHAR(150) NOT NULL,
                             descricao   TEXT,
                             status      VARCHAR(20)  NOT NULL DEFAULT 'DISPONIVEL'
                                 CHECK (status IN ('DISPONIVEL', 'MANUTENCAO', 'CALIBRACAO')),
                             ativo       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE usuario (
                         id            BIGSERIAL PRIMARY KEY,
                         nome          VARCHAR(150) NOT NULL,
                         email         VARCHAR(150) NOT NULL,
                         telefone      VARCHAR(20),
                         tipo          VARCHAR(10)  NOT NULL
                             CHECK (tipo IN ('INTERNO', 'EXTERNO')),
                         vinculo       VARCHAR(20)
                             CHECK (vinculo IN ('DOCENTE', 'PESQUISADOR', 'MESTRANDO',
                                                'DOUTORANDO', 'GRADUANDO', 'TECNICO')),
                         matricula     VARCHAR(30),
                         razao_social  VARCHAR(150),
                         cnpj          VARCHAR(18),
                         criado_em     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE agendamento (
                             id                   BIGSERIAL PRIMARY KEY,
                             codigo_rastreamento  VARCHAR(20)  NOT NULL UNIQUE,
                             usuario_id           BIGINT       NOT NULL REFERENCES usuario (id),
                             equipamento_id       BIGINT       NOT NULL REFERENCES equipamento (id),
                             data_agendamento     DATE         NOT NULL,
                             turno                VARCHAR(15)  NOT NULL
                                 CHECK (turno IN ('MANHA', 'TARDE', 'DIARIA', 'PERSONALIZADO')),
                             descricao_amostra    TEXT         NOT NULL,
                             protocolo_etica_url  VARCHAR(255),
                             status               VARCHAR(15)  NOT NULL DEFAULT 'PENDENTE'
                                 CHECK (status IN ('PENDENTE', 'CONFIRMADO', 'CANCELADO', 'CONCLUIDO')),
                             aceite_termos        BOOLEAN      NOT NULL DEFAULT FALSE,
                             criado_em            TIMESTAMP    NOT NULL DEFAULT NOW(),
                             atualizado_em        TIMESTAMP
);

CREATE TABLE pesquisador (
                             id         BIGSERIAL PRIMARY KEY,
                             nome       VARCHAR(150) NOT NULL,
                             cargo      VARCHAR(120),
                             bio        TEXT,
                             foto_url   VARCHAR(255),
                             lattes_url VARCHAR(255),
                             orcid_url  VARCHAR(255),
                             ativo      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE aviso (
                       id               BIGSERIAL PRIMARY KEY,
                       titulo           VARCHAR(150) NOT NULL,
                       conteudo         TEXT         NOT NULL,
                       tipo             VARCHAR(15)  NOT NULL
                           CHECK (tipo IN ('ALERTA', 'MANUTENCAO', 'CHAMADA', 'TREINAMENTO')),
                       data_publicacao  DATE         NOT NULL DEFAULT CURRENT_DATE,
                       data_expiracao   DATE,
                       ativo            BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE servico (
                         id          BIGSERIAL PRIMARY KEY,
                         unidade_id  BIGINT       NOT NULL REFERENCES unidade_estrategica (id),
                         categoria   VARCHAR(100) NOT NULL,
                         tecnicas    VARCHAR(255),
                         aplicacoes  TEXT,
                         ativo       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE faq (
                     id         BIGSERIAL PRIMARY KEY,
                     categoria  VARCHAR(80)  NOT NULL,
                     pergunta   VARCHAR(255) NOT NULL,
                     resposta   TEXT         NOT NULL,
                     ordem      INTEGER      NOT NULL DEFAULT 0,
                     ativo      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE contato (
                         id           BIGSERIAL PRIMARY KEY,
                         nome         VARCHAR(150) NOT NULL,
                         email        VARCHAR(150) NOT NULL,
                         instituicao  VARCHAR(150),
                         segmento     VARCHAR(15)  NOT NULL
                             CHECK (segmento IN ('PESQUISA', 'INDUSTRIA', 'MIDIA')),
                         mensagem     TEXT         NOT NULL,
                         criado_em    TIMESTAMP    NOT NULL DEFAULT NOW(),
                         lido         BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE feriado (
                         id         BIGSERIAL PRIMARY KEY,
                         data       DATE        NOT NULL UNIQUE,
                         descricao  VARCHAR(120) NOT NULL
);

-- Índices para as buscas mais frequentes
CREATE INDEX idx_equipamento_unidade   ON equipamento (unidade_id);
CREATE INDEX idx_servico_unidade       ON servico (unidade_id);
CREATE INDEX idx_agendamento_data      ON agendamento (data_agendamento);
CREATE INDEX idx_agendamento_status    ON agendamento (status);
CREATE INDEX idx_agendamento_equip_data ON agendamento (equipamento_id, data_agendamento);