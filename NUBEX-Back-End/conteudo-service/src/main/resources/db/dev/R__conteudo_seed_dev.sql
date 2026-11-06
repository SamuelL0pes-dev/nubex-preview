DELETE FROM noticia WHERE titulo LIKE 'Noticia de exemplo%';
DELETE FROM edital  WHERE titulo LIKE 'Edital de exemplo%';

INSERT INTO noticia (titulo, resumo, conteudo, unidade, data_publicacao) VALUES
('Noticia de exemplo 1', 'Resumo de exemplo da primeira noticia.',
 'Texto completo de exemplo da primeira noticia.',
 'Unidade de exemplo', CURRENT_DATE - 2),
('Noticia de exemplo 2', 'Resumo de exemplo da segunda noticia.',
 'Texto completo de exemplo da segunda noticia.',
 'Unidade de exemplo', CURRENT_DATE - 9),
('Noticia de exemplo 3', 'Resumo de exemplo da terceira noticia.',
 'Texto completo de exemplo da terceira noticia.',
 'Unidade de exemplo', CURRENT_DATE - 24),
-- Esta nao pode aparecer no site: serve de prova viva da regra de publicacao.
('Noticia de exemplo 4 agendada, nao deve aparecer no mural',
 'Resumo de exemplo de uma noticia ainda nao publicada.',
 'Texto completo de exemplo da noticia agendada.',
 'Unidade de exemplo', CURRENT_DATE + 30);

INSERT INTO edital (titulo, resumo, conteudo, unidade, data_publicacao, data_encerramento) VALUES
('Edital de exemplo aberto', 'Resumo de exemplo de um edital com inscricoes abertas.',
 'Texto completo de exemplo do edital aberto.',
 'Unidade de exemplo', CURRENT_DATE - 21, CURRENT_DATE + 14),
('Edital de exemplo encerrado', 'Resumo de exemplo de um edital ja encerrado.',
 'Texto completo de exemplo do edital encerrado.',
 'Unidade de exemplo', CURRENT_DATE - 32, CURRENT_DATE - 1);
