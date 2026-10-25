-- =====================================================================
-- Domínio projeto: projetos de pesquisa, integrantes da equipe e
-- solicitações de compra de reagentes, insumos e equipamentos.
-- Base: Apêndice A do Documento de Requisitos v2.0.
--
-- Os pontos marcados com DIVERGE foram decididos em equipe e saem do
-- que está escrito no Apêndice A. Precisam entrar na próxima versão
-- do documento.
-- =====================================================================

CREATE TABLE projeto (
    id             BIGSERIAL PRIMARY KEY,

    -- Solicitante interno que cadastrou o projeto (RF068). O RF051 lista
    -- "cadastra projetos" apenas no perfil interno.
    -- BIGINT solto, sem chave estrangeira: a conta vive no banco do
    -- usuario-service. A existência é verificada pela aplicação antes de
    -- gravar, como o Apêndice A determina (regra de ouro).
    solicitante_id BIGINT       NOT NULL,

    titulo         VARCHAR(200) NOT NULL,
    subtitulo      VARCHAR(255),
    conteudo       TEXT,

    -- Listas guardadas como TEXT: nenhum requisito pede busca ou filtro por
    -- elas, é conteúdo de exibição na página pública (RF019). Mesmo caminho
    -- que o catalogo-service adotou em servico.tecnicas.
    links            TEXT,
    linhas_pesquisa  TEXT,
    producoes        TEXT,

    -- DIVERGE (coluna acrescentada): o Apêndice A não previa, mas o RF069
    -- exige vínculo a "projeto de pesquisa ativo" e sem isto não há como
    -- validar. Atende também o "remova seus projetos" do RF068 por
    -- desativação, sem deixar solicitação de compra apontando para projeto
    -- que deixou de existir.
    ativo          BOOLEAN      NOT NULL DEFAULT TRUE,

    criado_em      TIMESTAMP    NOT NULL DEFAULT NOW(),
    -- DIVERGE (coluna acrescentada): o RF068 permite editar o projeto.
    atualizado_em  TIMESTAMP
);

-- "Meus projetos" do solicitante (RF068) e o CRUD do Apêndice B.
CREATE INDEX idx_projeto_solicitante ON projeto (solicitante_id);

-- A listagem pública do RF019 mostra só projeto ativo.
CREATE INDEX idx_projeto_ativo ON projeto (ativo);


-- =====================================================================
-- Equipe do projeto (RF019, RF068).
-- O integrante é SELECIONADO entre as contas do portal, nunca digitado.
-- =====================================================================

CREATE TABLE projeto_integrante (
    id         BIGSERIAL PRIMARY KEY,

    -- Chave estrangeira de verdade: mesmo banco, mesmo serviço.
    -- CASCADE porque integrante não existe fora do projeto.
    projeto_id BIGINT       NOT NULL REFERENCES projeto (id) ON DELETE CASCADE,

    -- DIVERGE (coluna acrescentada): o Apêndice A tinha apenas "nome", em
    -- texto livre. A equipe decidiu que o integrante é escolhido entre as
    -- contas existentes, então aqui fica a referência à pessoa.
    -- BIGINT solto: a conta vive no usuario-service.
    usuario_id BIGINT       NOT NULL,

    -- Cópia do nome no momento da inclusão, preenchida PELO SISTEMA a partir
    -- da conta selecionada, nunca digitada. Existe para a página pública de
    -- projetos (RF019) ser montada sem uma chamada ao usuario-service por
    -- integrante: a meta de LCP do RNF007 é abaixo de 2 segundos.
    -- O usuario_id é a verdade; este campo é etiqueta de exibição.
    nome       VARCHAR(150) NOT NULL,

    -- Função da pessoa NESTE projeto (RF068: "o papel de cada um, como
    -- coordenador ou pesquisador"). Não confundir com usuario.perfil, que é
    -- o que ela pode fazer na API, nem com usuario.vinculo, que é o que ela
    -- é na universidade. A mesma pessoa pode coordenar um projeto e ser
    -- pesquisadora em outro.
    -- Os valores seguem a organização do RF018 e o padrão já usado em
    -- conteudo.pesquisador.secao. OUTROS é a saída para o caso imprevisto,
    -- que evita migração a cada papel novo.
    papel      VARCHAR(25)  NOT NULL
        CHECK (papel IN ('COORDENADOR', 'PESQUISADOR',
                         'INICIACAO_CIENTIFICA', 'OUTROS')),

    -- A mesma pessoa não entra duas vezes na equipe do mesmo projeto.
    -- Só é possível garantir isto porque o integrante é selecionado: com
    -- nome digitado, "Marina Souza" e "Marina Sousa" passariam as duas.
    CONSTRAINT uq_projeto_integrante UNIQUE (projeto_id, usuario_id)
);

-- Não há índice avulso para projeto_id de propósito: o índice único acima
-- começa por essa coluna, então o PostgreSQL já o usa para buscar a equipe
-- de um projeto. Um segundo índice seria custo de escrita sem ganho.


-- =====================================================================
-- Solicitação de compra de reagentes, insumos e equipamentos
-- (RF069, RF070, RF075).
-- =====================================================================

CREATE TABLE solicitacao_compra (
    id             BIGSERIAL PRIMARY KEY,

    -- Quem pediu (RF069). BIGINT solto: outro banco.
    solicitante_id BIGINT        NOT NULL,

    -- DIVERGE: o Apêndice A lista esta referência entre as "sem chave
    -- estrangeira", mas projeto e solicitacao_compra estão no MESMO banco e
    -- no mesmo serviço. Pela própria regra do documento, relação interna ao
    -- serviço continua sendo chave estrangeira. Sem ON DELETE: projeto com
    -- compra registrada não se apaga, se desativa.
    projeto_id     BIGINT        NOT NULL REFERENCES projeto (id),

    -- DIVERGE (colunas removidas): o Apêndice A tinha nome_projeto e
    -- projeto_em_andamento. Com projeto_id obrigatório e com chave
    -- estrangeira, as duas são cópia de projeto.titulo e projeto.ativo --
    -- dado duplicado que sai de sincronia na primeira edição do projeto.

    -- DIVERGE (coluna renomeada): o Apêndice A chama "reagente", mas o
    -- próprio título do RF069 é "reagentes, insumos e equipamentos" e o
    -- texto pede "a descrição do item". O nome antigo excluía dois dos
    -- três casos que o requisito cobre.
    item           VARCHAR(255)  NOT NULL,

    -- Justificativa acadêmica, obrigatória (RF069): produtos e impactos
    -- científicos esperados da aquisição.
    finalidade     TEXT          NOT NULL,

    -- NUMERIC e não inteiro porque reagente se compra em fração: 2,5 L.
    quantidade     NUMERIC(12,3) NOT NULL CHECK (quantidade > 0),

    -- DIVERGE (coluna acrescentada): o Apêndice A tinha quantidade sem
    -- unidade nenhuma, e "500" não diz se são 500 ml, 500 g ou 500 frascos.
    unidade_medida VARCHAR(20)   NOT NULL,

    -- Unidade estratégica de destino e equipamento relacionado (RF069).
    -- Os dois são do catalogo-service: BIGINT solto. O equipamento é
    -- opcional, porque o RF069 diz "quando houver".
    unidade_id     BIGINT        NOT NULL,
    equipamento_id BIGINT,

    -- DIVERGE (quatro valores em vez de três): o Apêndice A parava em
    -- RECUSADA. O RF070 e o RF075 preveem a supervisão administrativa
    -- encaminhar a solicitação aprovada ao sistema de compras da Unifor,
    -- que é um quarto estado. ENCAMINHADA é o estado final: acompanhar a
    -- compra está no escopo negativo do documento.
    status         VARCHAR(15)   NOT NULL DEFAULT 'AGUARDANDO'
        CHECK (status IN ('AGUARDANDO', 'APROVADA', 'RECUSADA', 'ENCAMINHADA')),

    -- DIVERGE (colunas acrescentadas): o RF070 manda registrar a decisão
    -- "com justificativa", e sem avaliado_por não há rastro de quem decidiu.
    -- avaliado_por é BIGINT solto: a conta do avaliador vive em outro banco.
    justificativa_decisao TEXT,
    avaliado_por          BIGINT,

    criado_em      TIMESTAMP     NOT NULL DEFAULT NOW(),
    avaliado_em    TIMESTAMP,

    -- Mesma regra do pedido_aprovacao no usuario-service: a decisão é tudo
    -- ou nada. Ou está aguardando, sem avaliador nem data, ou foi decidida,
    -- com os dois preenchidos. Evita registro meio gravado.
    CONSTRAINT ck_compra_decisao_completa
        CHECK ((status =  'AGUARDANDO' AND avaliado_em IS NULL
                                       AND avaliado_por IS NULL)
            OR (status <> 'AGUARDANDO' AND avaliado_em IS NOT NULL
                                       AND avaliado_por IS NOT NULL))
);

-- "Minhas solicitações" do solicitante (Apêndice B, /api/portal/compras).
CREATE INDEX idx_compra_solicitante ON solicitacao_compra (solicitante_id);

-- As compras de um projeto, e a validação do vínculo obrigatório do RF069.
CREATE INDEX idx_compra_projeto ON solicitacao_compra (projeto_id);

-- A diretoria avalia e a supervisão despacha, as duas filtrando por status
-- (RF070, RF075, e /api/admin/compras do Apêndice B).
CREATE INDEX idx_compra_status ON solicitacao_compra (status);
