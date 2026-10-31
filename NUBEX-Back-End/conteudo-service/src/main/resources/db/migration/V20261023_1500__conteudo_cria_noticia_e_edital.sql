CREATE TABLE noticia (
                       id BIGSERIAL PRIMARY KEY,
                       titulo VARCHAR(150) NOT NULL,
                       resumo VARCHAR(300),
                       conteudo TEXT NOT NULL,
                       unidade VARCHAR(120),
                       imagem_url VARCHAR(255),
                       link VARCHAR(255),
                       autor_id BIGINT,
                       data_publicacao DATE NOT NULL DEFAULT CURRENT_DATE,
                       destaque BOOLEAN NOT NULL DEFAULT FALSE,
                       ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE edital (
                      id BIGSERIAL PRIMARY KEY,
                      titulo VARCHAR(150) NOT NULL,
                      resumo VARCHAR(300),
                      conteudo TEXT NOT NULL,
                      unidade VARCHAR(120),
                      imagem_url VARCHAR(255),
                      link VARCHAR(255),
                      autor_id BIGINT,
                      data_publicacao DATE NOT NULL DEFAULT CURRENT_DATE,
                      data_encerramento DATE,
                      destaque BOOLEAN NOT NULL DEFAULT FALSE,
                      ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_noticia_publicacao ON noticia (data_publicacao DESC);
CREATE INDEX idx_edital_encerramento ON edital (data_encerramento);
