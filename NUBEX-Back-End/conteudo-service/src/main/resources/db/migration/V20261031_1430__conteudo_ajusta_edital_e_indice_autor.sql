UPDATE edital
   SET data_encerramento = data_publicacao + 30
 WHERE data_encerramento IS NULL;

ALTER TABLE edital
    ALTER COLUMN data_encerramento SET NOT NULL;

CREATE INDEX idx_noticia_autor ON noticia (autor_id)
    WHERE autor_id IS NOT NULL;

CREATE INDEX idx_edital_autor ON edital (autor_id)
    WHERE autor_id IS NOT NULL;
