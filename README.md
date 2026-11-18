# Portal NUBEX

> [!NOTE]
> **Repositório de Demonstração / Preview (Em Desenvolvimento)**  
> Este repositório é uma versão de demonstração técnica e portfólio do **Portal NUBEX** (Núcleo de Biologia Experimental – UNIFOR), um projeto institucional atualmente em desenvolvimento ativo. Apresenta a arquitetura de microsserviços (Java 17 / Spring Boot) e interface web (React 19 / TypeScript).

Portal do Núcleo de Biologia Experimental da Unifor.

## Estrutura atual

- `NUBEX-Front-End`: React 19, TypeScript, React Router e Vite.
- `NUBEX-Back-End`: Java 17, Spring Boot 4.1.1 e Maven, com os módulos
  `nubex-common`, `usuario-service`, `agendamento-service`, `catalogo-service`,
  `projeto-service` e `conteudo-service`.
- PostgreSQL 16, com um banco por serviço e migrações Flyway.

Usuários e projetos foram ativados na atualização do backend (PR #9). O Compose
inicia os cinco serviços ativos. As versões descritas são as do código.

## Requisitos

- Docker com Compose, instalado e em execução, para o banco e as APIs.
- Node.js 24 com npm para o frontend.
- JDK 17 para compilar/testar Java fora do Docker; Maven é baixado pelo Wrapper.

## Iniciar banco e APIs

A partir da raiz do repositório:

```sh
cd NUBEX-Back-End
cp .env.example .env
```

Abra `.env` e preencha `POSTGRES_PASSWORD` com uma senha local não vazia. O arquivo
`.env` é ignorado pelo Git. Nunca inclua a senha no código nem no PR.

No mesmo `.env`, preencha `NUBEX_ADMIN_EMAIL` e `NUBEX_ADMIN_SENHA` para que o
`usuario-service` crie a primeira conta de administrador ao subir. A senha precisa
ter de 8 a 36 caracteres, e `NUBEX_ADMIN_NOME` é opcional. Sem administrador
ninguém aprova pedido de acesso de solicitante, e o painel administrativo não tem
como ser acessado. O seed não cria um segundo administrador: se já existe um no
banco, deixe as duas variáveis em branco. Cada pessoa tem o próprio banco local,
então cada uma define a própria senha — não é preciso compartilhar.

Preencha também `JWT_CHAVE_PRIVADA` e `JWT_CHAVE_PUBLICA`, o par que assina e
confere o token de acesso. Gere o seu par **fora da pasta do repositório**, para não
haver risco de commitar uma chave. No Windows, use o Git Bash, que já traz o
`openssl`:

```sh
cd ~
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out privada.pem
openssl rsa -in privada.pem -pubout -out publica.pem
grep -v -- "-----" privada.pem | tr -d '\n'   # cole em JWT_CHAVE_PRIVADA
grep -v -- "-----" publica.pem | tr -d '\n'   # cole em JWT_CHAVE_PUBLICA
```

Cada chave vai numa linha só, sem as linhas `-----BEGIN-----` e `-----END-----`.
Apenas o `usuario-service` recebe a privada, porque é o único que faz login e,
portanto, o único que precisa assinar; os outros quatro recebem só a pública, que
confere a assinatura e não produz nenhuma. Cada pessoa gera o próprio par: o token
emitido na sua máquina vale na sua máquina, e não é preciso compartilhar chave.
Faltando as duas, a aplicação sobe normalmente, mas o login responde 503 e as rotas
protegidas respondem 401.

```sh
docker compose config --quiet
docker compose up --build --detach --wait --wait-timeout 240
docker compose ps
```

O primeiro build baixa dependências e pode demorar. O Compose aguarda o banco
ficar pronto e inicia as cinco APIs. Os seis serviços devem aparecer `healthy`.

| Componente | Endereço local padrão | Banco utilizado |
|---|---|---|
| PostgreSQL | localhost:5433 | nubex, usuario, agendamento, catalogo, projeto e conteudo |
| Usuário | http://localhost:8081 | usuario |
| Agendamento | http://localhost:8082 | agendamento |
| Catálogo | http://localhost:8083 | catalogo |
| Projeto | http://localhost:8084 | projeto |
| Conteúdo | http://localhost:8085 | conteudo |

As portas são publicadas somente neste computador. Para mudar as portas, ajuste
`POSTGRES_PORT`, `USUARIO_PORT`, `AGENDAMENTO_PORT`, `CATALOGO_PORT`,
`PROJETO_PORT` e `CONTEUDO_PORT` em `.env`.
Dentro do Docker, cada API acessa `banco:5432` pelas variáveis `DB_URL`, `DB_USER`
e `DB_PASSWORD` que já são usadas pelo código da equipe.

O script `docker/postgres/init.sql` cria os bancos na primeira inicialização de
um volume vazio. Cada API aplica suas próprias migrações. O volume `nubex-dados`
conserva os dados. Se você reutilizar um volume antigo que não contenha os bancos,
o script de inicialização não será repetido: crie os bancos faltantes ou use outro
projeto Compose/volume para testes. Não apague dados existentes para resolver isso.
Alterar a senha em `.env` também não altera a senha de um banco já inicializado.

O conteudo-service traz um seed de exemplo em `db/dev`, carregado somente no
perfil `dev` e como migração repetível (`R__`). Quem aplicou a versão anterior
desse seed, que era numerada, tem no histórico uma versão sem arquivo
correspondente: o perfil `dev` ignora esse caso e o serviço sobe normalmente.
Fora do perfil `dev` a validação do Flyway continua completa.

Verifique o estado das APIs:

```sh
curl --fail http://localhost:8081/actuator/health
curl --fail http://localhost:8082/actuator/health
curl --fail http://localhost:8083/actuator/health
curl --fail http://localhost:8084/actuator/health
curl --fail http://localhost:8085/actuator/health
```

A resposta deve indicar `UP`. O healthcheck verifica a saúde da aplicação e do
banco; não comprova todas as funcionalidades. O Compose usa o perfil `dev`, com
as regras locais já definidas em `nubex-common.yml`. Não é configuração de produção.

Para diagnóstico ou encerramento, ainda em `NUBEX-Back-End`:

```sh
docker compose logs --tail 100 banco usuario agendamento catalogo projeto conteudo
docker compose down
```

O encerramento conserva o volume. `down --volumes` apaga os dados e só deve ser
usado num ambiente temporário que você queira descartar.

## Compilar e testar o backend

Na pasta `NUBEX-Back-End`, com JDK 17 instalado e Docker em execução:

```sh
sh mvnw -B -ntp clean verify
```

No Windows, use `mvnw.cmd` no lugar de `sh mvnw`.
O comando compila e empacota os módulos ativos, além de executar os testes.
Há testes do módulo comum e dos controllers. Os testes de usuário e projeto usam
Testcontainers: criam PostgreSQL 16 temporário automaticamente, aplicam migrações
e verificam a inicialização e as respostas HTTP previstas. Eles precisam de Docker
ativo, mas não de banco manual nem de senha local. Não pule esses testes para
contornar uma falha de acesso ao Docker. A inicialização conjunta dos cinco
serviços também é verificada separadamente pelo Compose. Os relatórios ficam em cada módulo, em `target/surefire-reports`.

Para executar uma API fora do Docker, inicie apenas o banco pelo Compose e
configure `DB_URL`, `DB_USER` e `DB_PASSWORD` no terminal ou no arquivo local
`application-dev.properties`, usando os exemplos de cada serviço. O endereço
local do banco usa a porta publicada, normalmente 5433. Não envie configurações
locais com senhas ao GitHub. Evite executar duas instâncias na mesma porta.

Rodando assim, o `.env` não é lido: ele só vale para o Compose. Para o
`usuario-service` criar a primeira conta de administrador fora do Docker, preencha
`nubex.admin.email` e `nubex.admin.senha` no `application-dev.properties` dele, como
mostra o `application-dev.properties.example`.

## Rodar o frontend

A partir da raiz, em outro terminal:

```sh
cd NUBEX-Front-End
cp .env.example .env
npm ci
npm run dev
```

Abra o endereço informado pelo Vite, normalmente http://localhost:5173.
O `.env.example` aponta os serviços para as portas 8081 a 8085. Ajuste a cópia
local se tiver alterado essas portas no backend. A existência do serviço não
significa que todas as funcionalidades planejadas já foram implementadas. As variáveis `VITE_*`
são públicas no navegador; nunca coloque credenciais nelas.

Para conferir o código, os testes e a compilação:

```sh
npm run lint
npm test
npm run build
```

Os arquivos de distribuição são gerados em `dist/`. Lint, testes automatizados
e build verificam aspectos diferentes e não substituem a revisão das telas.

## Verificações no GitHub

Os arquivos ficam em `.github/workflows/`, na raiz. São executados em PRs para
`develop` e `main`, nas alterações dessas branches e por acionamento manual.

- **Frontend - lint e build:** Node.js 24, instalação pelo lockfile, lint, testes
  e build. A configuração é para revisão em par com Matheus.
- **Backend - testes e build:** Java 17 e `mvnw clean verify` no projeto multimódulo.
- **Compose - banco e servicos:** após os testes Java, constrói as imagens, inicia
  o PostgreSQL e os cinco serviços, e verifica `/actuator/health` em cada API.
  O ambiente e a credencial são temporários e descartados ao final do job.

O Dockerfile não executa testes durante o build da imagem; o job de testes roda
antes da validação do Compose. O backend e os testes são preservados a partir da
base da equipe, incluindo usuário e projeto, sem reintroduzir a antiga API única.

## Fluxo de contribuição

Crie uma branch a partir da `develop` atualizada, faça suas alterações e abra PR
para `develop`. O modelo fica em `.github/pull_request_template.md`. Confira
`Files changed` para garantir que não haja pastas de pacotes de download nem
arquivos antigos substituindo trabalho atual. Aguarde revisão e checks antes
da integração. As proteções das branches são configuradas no GitHub por alguém
com as permissões necessárias; os arquivos deste PR não ativam essas regras.
