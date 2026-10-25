-- =====================================================================
-- Domínio usuario: contas de acesso, tokens de verificação e
-- pedidos de aprovação de cadastro.
-- Base: Apêndice A do Documento de Requisitos v2.0.
--
-- Os pontos marcados com DIVERGE foram decididos em equipe e saem do
-- que está escrito no Apêndice A. Precisam entrar na próxima versão
-- do documento.
-- =====================================================================

CREATE TABLE usuario (
    id            BIGSERIAL PRIMARY KEY,

    nome          VARCHAR(150) NOT NULL,
    email         VARCHAR(150) NOT NULL,

    -- Só o hash BCrypt, que tem sempre 60 caracteres.
    -- 72 deixa folga para uma troca futura de algoritmo (ex.: Argon2id).
    senha_hash    VARCHAR(72)  NOT NULL,

    -- DIVERGE: guardado só com dígitos, sem formatação. O RF056 define
    -- "(XX) XXXXX-XXXX" como formato de ENTRADA, validado na API; no banco
    -- o dígito puro evita que o mesmo número exista em duas grafias.
    -- Aceita 10 ou 11 dígitos: o RF056 previu só 11 (celular), mas o próprio
    -- telefone do NUBEX no RF030, (85) 3477-3000, é fixo e tem 10.
    telefone      VARCHAR(11)
        CHECK (telefone ~ '^[0-9]{10,11}$'),

    -- O que a pessoa PODE fazer na API. Alimenta o @PreAuthorize de cada rota.
    -- DIVERGE: o Apêndice A previa PESQUISADOR_EXTERNO como perfil. Agora
    -- "solicitante externo" é perfil=SOLICITANTE + tipo=EXTERNO, o que deixa
    -- as duas dimensões independentes.
    perfil        VARCHAR(30)  NOT NULL
        CHECK (perfil IN ('PARTICIPANTE', 'SOLICITANTE', 'ADMINISTRADOR',
                          'SUPERVISAO_ADMINISTRATIVA', 'ADMINISTRADOR_CONSULTA')),

    -- De ONDE a pessoa vem.
    -- DIVERGE: o Apêndice A tinha o valor EMPRESA, que misturava origem com
    -- natureza jurídica. Pessoa física x jurídica agora sai de cpf x cnpj.
    tipo          VARCHAR(10)  NOT NULL
        CHECK (tipo IN ('INTERNO', 'EXTERNO')),

    -- Em que ponto do fluxo de cadastro a conta está (RF058 a RF061, RF074).
    -- Nasce PENDENTE_EMAIL: sem confirmar o e-mail, o cadastro não anda.
    status        VARCHAR(25)  NOT NULL DEFAULT 'PENDENTE_EMAIL'
        CHECK (status IN ('PENDENTE_EMAIL', 'AGUARDANDO_APROVACAO',
                          'ATIVO', 'RECUSADO', 'INATIVO')),

    -- Vínculo ACADÊMICO. Não confundir com perfil: vínculo é o que a pessoa
    -- é na universidade, perfil é o que ela pode fazer na API.
    vinculo       VARCHAR(20)
        CHECK (vinculo IN ('DOCENTE', 'PESQUISADOR', 'MESTRANDO',
                           'DOUTORANDO', 'GRADUANDO', 'TECNICO')),

    -- Discente da Unifor (RF054).
    matricula     VARCHAR(30),
    curso         VARCHAR(120),

    -- Quem vem de fora (RF055): pessoa física usa cpf, jurídica usa cnpj.
    -- DIVERGE: só dígitos, pelo mesmo motivo do telefone. Os dígitos
    -- verificadores são validados na API (RF056); em CHECK ficaria ilegível.
    cpf           VARCHAR(11)
        CHECK (cpf ~ '^[0-9]{11}$'),
    instituicao   VARCHAR(150),
    razao_social  VARCHAR(150),
    cnpj          VARCHAR(14)
        CHECK (cnpj ~ '^[0-9]{14}$'),

    -- Identificação acadêmica (RF091). A obrigatoriedade varia por perfil
    -- (Lattes+ORCID no interno, só ORCID no externo, nenhum no participante),
    -- então fica na validação da API: é regra condicional e muda sem migração.
    lattes_url            VARCHAR(255),
    orcid_url             VARCHAR(255),
    google_academico_url  VARCHAR(255),

    -- Solicitante interno que aprovou esta conta (RF060).
    -- FK de verdade: é auto-referência, mesmo banco e mesmo serviço.
    solicitante_id BIGINT REFERENCES usuario (id),

    aprovado_em   TIMESTAMP,

    -- Fim do prazo de acesso (RF063). Vencido, a conta passa a INATIVO.
    expira_em     TIMESTAMP,

    criado_em     TIMESTAMP    NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP,

    -- DIVERGE: o participante também pode ser EXTERNO. O RF089 previa só
    -- discente da Unifor, mas a equipe confirmou que participante externo
    -- existe -- ele pertence a um solicitante externo e é aprovado pelo
    -- solicitante interno da cadeia.
    --
    -- A matrícula fica exigida apenas do participante INTERNO, porque o RF056
    -- pede matrícula "dos alunos da Unifor". O participante externo se
    -- identifica por cpf, o que ck_usuario_externo_cpf_ou_cnpj já garante.
    CONSTRAINT ck_usuario_participante_interno_tem_matricula
        CHECK (perfil <> 'PARTICIPANTE'
               OR tipo <> 'INTERNO'
               OR matricula IS NOT NULL),

    -- A equipe do NUBEX é de dentro da Unifor.
    CONSTRAINT ck_usuario_admin_interno
        CHECK (perfil NOT IN ('ADMINISTRADOR', 'SUPERVISAO_ADMINISTRATIVA',
                              'ADMINISTRADOR_CONSULTA')
               OR tipo = 'INTERNO'),

    -- Quem é EXTERNO se identifica por cpf OU cnpj, exatamente um dos dois.
    -- O "<>" entre dois booleanos é o ou-exclusivo: barra os dois preenchidos
    -- e barra os dois vazios.
    CONSTRAINT ck_usuario_externo_cpf_ou_cnpj
        CHECK (tipo <> 'EXTERNO'
               OR ((cpf IS NOT NULL) <> (cnpj IS NOT NULL)))
);

-- O e-mail é identidade de login, então é único de verdade. Sobre LOWER()
-- para que "Ana@unifor.br" e "ana@unifor.br" não virem duas contas.
CREATE UNIQUE INDEX uq_usuario_email ON usuario (LOWER(email));

-- A matrícula também é identidade de login, mas só o discente tem.
-- Índice PARCIAL: cobre quem tem e ignora as linhas nulas.
CREATE UNIQUE INDEX uq_usuario_matricula ON usuario (matricula)
    WHERE matricula IS NOT NULL;

-- CPF e CNPJ são identidade: não repetem. Parcial porque a maioria das contas
-- não tem nem um nem outro (participante e docente interno).
CREATE UNIQUE INDEX uq_usuario_cpf  ON usuario (cpf)  WHERE cpf  IS NOT NULL;
CREATE UNIQUE INDEX uq_usuario_cnpj ON usuario (cnpj) WHERE cnpj IS NOT NULL;

-- O painel administrativo lista usuários por perfil e por status (RF074).
CREATE INDEX idx_usuario_status      ON usuario (status);
CREATE INDEX idx_usuario_perfil_tipo ON usuario (perfil, tipo);

-- "Meus participantes aprovados", usado na etapa 3 do agendamento (RF043).
CREATE INDEX idx_usuario_solicitante ON usuario (solicitante_id);

-- Suporta a rotina que expira acessos (RF063) e o aviso de 15 dias (RF064).
CREATE INDEX idx_usuario_expira_em ON usuario (expira_em)
    WHERE expira_em IS NOT NULL;


-- =====================================================================
-- Código de confirmação de e-mail (RF057) e link de redefinição de
-- senha (RF086). Os dois são de uso único e têm prazo de validade.
-- =====================================================================

CREATE TABLE token_verificacao (
    id         BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT      NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,

    tipo       VARCHAR(20) NOT NULL
        CHECK (tipo IN ('CODIGO_EMAIL', 'RECUPERACAO_SENHA')),

    -- Só o HASH SHA-256 do token (64 caracteres em hexadecimal). O valor em
    -- claro vai no e-mail e não fica guardado: se o banco vazar, ninguém
    -- consegue redefinir a senha de ninguém.
    token_hash VARCHAR(64) NOT NULL,

    expira_em  TIMESTAMP   NOT NULL,

    -- Marca o uso único: preenchido, o token não vale mais (RF086).
    usado_em   TIMESTAMP,

    -- DIVERGE (coluna acrescentada): o Apêndice A não previa, mas sem ela
    -- não há como auditar quando o token foi emitido.
    criado_em  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- A validação busca pelo hash, então ele é indexado e único.
CREATE UNIQUE INDEX uq_token_verificacao_hash ON token_verificacao (token_hash);

-- Buscar o token vigente de um usuário e limpar os vencidos.
CREATE INDEX idx_token_verificacao_usuario ON token_verificacao (usuario_id, tipo);


-- =====================================================================
-- Pedido de aprovação de cadastro (RF059 a RF061, RF088).
-- =====================================================================

CREATE TABLE pedido_aprovacao (
    id             BIGSERIAL PRIMARY KEY,
    usuario_id     BIGINT      NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,

    -- Quem deve decidir: o solicitante interno indicado no cadastro (RF059).
    -- Nulo quando o pedido é de acesso como solicitante interno, caso em que
    -- qualquer ADMINISTRADOR decide (RF052, RF088).
    solicitante_id BIGINT      REFERENCES usuario (id),

    status         VARCHAR(15) NOT NULL DEFAULT 'AGUARDANDO'
        CHECK (status IN ('AGUARDANDO', 'APROVADO', 'RECUSADO')),

    -- DIVERGE (coluna acrescentada): o RF088 exige que o aviso dos outros
    -- administradores exiba "quem decidiu, qual foi a decisão e quando".
    -- Sem esta coluna não há como cumprir.
    decidido_por   BIGINT      REFERENCES usuario (id),

    criado_em      TIMESTAMP   NOT NULL DEFAULT NOW(),
    decidido_em    TIMESTAMP,

    -- Decisão é tudo ou nada: ou está aguardando, sem autor nem data, ou foi
    -- decidida, com os dois preenchidos. Evita registro meio gravado.
    CONSTRAINT ck_pedido_decisao_completa
        CHECK ((status =  'AGUARDANDO' AND decidido_em IS NULL
                                       AND decidido_por IS NULL)
            OR (status <> 'AGUARDANDO' AND decidido_em IS NOT NULL
                                       AND decidido_por IS NOT NULL))
);

-- Um usuário não pode ter dois pedidos em aberto. Índice parcial: a restrição
-- vale só para AGUARDANDO, então o histórico de recusados fica preservado e um
-- novo cadastro pode ser pedido depois (RF061).
CREATE UNIQUE INDEX uq_pedido_aprovacao_aberto ON pedido_aprovacao (usuario_id)
    WHERE status = 'AGUARDANDO';

-- "Pedidos aguardando a minha decisão", na aba de avisos (RF060).
CREATE INDEX idx_pedido_aprovacao_solicitante
    ON pedido_aprovacao (solicitante_id, status);
