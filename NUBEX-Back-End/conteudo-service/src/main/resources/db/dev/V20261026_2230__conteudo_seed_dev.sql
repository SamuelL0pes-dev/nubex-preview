-- Dados de desenvolvimento. So rodam no perfil dev (ver spring.flyway.locations).
-- Servem para o NewsBoard ter o que exibir enquanto a tela de publicacao do
-- administrador nao existe. Descartavel.

INSERT INTO noticia (titulo, resumo, conteudo, unidade, data_publicacao) VALUES
('Noticia de exemplo 1', 'Resumo de exemplo da primeira noticia.',
 'Texto completo de exemplo da primeira noticia.',
 'Unidade de exemplo', DATE '2026-10-12'),
('Noticia de exemplo 2', 'Resumo de exemplo da segunda noticia.',
 'Texto completo de exemplo da segunda noticia.',
 'Unidade de exemplo', DATE '2026-10-08'),
('Noticia de exemplo 3', 'Resumo de exemplo da terceira noticia.',
 'Texto completo de exemplo da terceira noticia.',
 'Unidade de exemplo', DATE '2026-09-18');

INSERT INTO edital (titulo, resumo, conteudo, unidade, data_publicacao, data_encerramento) VALUES
('Edital de exemplo aberto', 'Resumo de exemplo de um edital com inscricoes abertas.',
 'Texto completo de exemplo do edital aberto.',
 'Unidade de exemplo', DATE '2026-10-10', DATE '2026-11-14'),
('Edital de exemplo encerrado', 'Resumo de exemplo de um edital proximo do encerramento.',
 'Texto completo de exemplo do edital encerrado.',
 'Unidade de exemplo', DATE '2026-09-29', DATE '2026-10-30');
