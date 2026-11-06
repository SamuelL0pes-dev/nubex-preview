# Padrões do projeto

Onde cada coisa fica e como se chama. Para instalar, rodar e abrir PR, veja o
[README](../README.md).

Este documento descreve o que o código já faz. Quando uma regra mudar, ela muda
aqui no mesmo PR.

---

## 1. Repositório

Duas pastas irmãs no mesmo repositório. Não renomear.

| Pasta | O que é |
|---|---|
| `NUBEX-Back-End/` | Maven multimódulo, Spring Boot 4.1, Java 17 |
| `NUBEX-Front-End/` | React 19 + TypeScript + Vite |
| `docs/` | documentos do projeto |
| `.github/` | workflows da CI e modelo de PR |

### Os seis módulos do back

| Módulo | Porta | Responsabilidade |
|---|---|---|
| `nubex-common` | — | segurança, CORS, tratamento de erro. Não sobe sozinho |
| `usuario-service` | 8081 | contas, perfis, tokens, pedidos de aprovação |
| `agendamento-service` | 8082 | reservas e feriados |
| `catalogo-service` | 8083 | unidades, laboratórios, equipamentos, serviços |
| `projeto-service` | 8084 | projetos, integrantes, compras |
| `conteudo-service` | 8085 | notícias, editais, FAQ, pesquisadores, parceiros |

Cada serviço tem **banco próprio**. Um serviço nunca lê a tabela de outro: se
precisar de um dado alheio, chama a API dele.

---

## 2. Onde colocar o código

### Dentro de um serviço

```
br.edu.unifor.nubex.<servico>/
    controller/   recebe HTTP, devolve DTO. Não tem regra de negócio
    service/      a regra de negócio
    mapper/       converte entidade em DTO
    dto/          o que entra e o que sai pela API
    model/        entidades JPA, espelho das tabelas
    repository/   interfaces do Spring Data
```

Um controller chama um service; um service chama um repository. Nunca o
contrário, e nunca pulando camada.

### Dentro do front

```
src/
    assets/       imagens, vídeos, logos
    components/   layout | ui | features
    constants/    valores fixos do domínio
    hooks/        lógica com estado
    mocks/        dados falsos, descartáveis
    pages/        uma pasta por rota
    routes/       mapa de URLs
    services/     única parte que fala HTTP
    styles/       CSS global: reset e tokens
    types/        contratos com o back
    utils/        funções puras
```

**Onde vai um componente?** A pergunta é o quanto ele sabe sobre o NUBEX:

| Se ele… | vai em |
|---|---|
| aparece em toda página (Header, Footer, Breadcrumb) | `components/layout/` |
| serviria em qualquer site (Button, Card, Modal) | `components/ui/` |
| sabe o que é unidade, edital, agendamento | `components/features/` |

Cada componente é uma pasta: `Nome.tsx` + `Nome.module.css` + `index.ts`.

---

## 3. Nomes

### Java

| Coisa | Padrão | Exemplo |
|---|---|---|
| entidade | singular | `Noticia`, `Edital` |
| repository | entidade + `Repository` | `NoticiaRepository` |
| controller | entidade + `Controller` | `NoticiaController` |
| DTO de lista | entidade + `Resumo` | `NoticiaResumo`, `EditalResumo` |
| DTO completo | entidade + `Response` | `FaqResponse`, `PesquisadorResponse` |
| pacote | minúsculo, sem acento | `br.edu.unifor.nubex.conteudo` |

`Resumo` é recorte: leva só o que o cartão da lista mostra. `NoticiaResumo`
não tem `conteudo`, `autorId` nem `ativo`. `Response` leva tudo que a tela
precisa daquele recurso. Quando a lista e a tela de detalhe mostram a mesma
coisa, existe só o `Response`.

### Banco

Tudo em `snake_case`: `data_publicacao`, `imagem_url`. A entidade traduz com
`@Column(name = "...")` quando o nome Java difere.

### Front

| Coisa | Padrão | Exemplo |
|---|---|---|
| componente | PascalCase | `NewsCard.tsx` |
| estilo | mesmo nome + `.module.css` | `NewsCard.module.css` |
| hook | `use` + descrição | `useMural.ts` |
| serviço, tipo, util | camelCase | `conteudo.ts`, `datas.ts` |

Identificadores dentro do código em português: `listarNoticias`, `carregando`,
`erro`. Nomes de API e bibliotecas ficam como são.

### Branches e commits

Branch a partir da `develop`: `feature/<escopo>`, `fix/<escopo>`.

Commits em português, Conventional Commits:

```
feat(conteudo): endpoints publicos de noticias e editais
fix(front): corrige roteamento de servico com query string
perf(front): reduz custo de pintura da home
docs: padroes do projeto
```

Um commit por assunto. Mudança de desempenho não entra no commit da
funcionalidade.

---

## 4. Regras que não se quebram

### Back

- Controller não tem regra de negócio e não fala com o repository direto.
- A API devolve **DTO**, nunca entidade. A entidade pode ter campos internos.
- Entidade nova exige migração Flyway na mesma PR.
- `ddl-auto` é sempre `validate`. Quem cria tabela é o Flyway.
- Data de "hoje" sai de `FusoNubex.hoje()`. `LocalDate.now()` solto usa o fuso
  do servidor e, num servidor em UTC, publica a notícia de hoje três horas antes.

### Front

- Componente **nunca** chama a API direto. Recebe por props; quem busca é a
  página ou um hook.
- Nenhum `fetch` fora de `services/`.
- Header e Footer são montados uma vez em `App.tsx`.
- Sem componente com `variant="home" | "unidade"`. Componentes separados.
- Cor nunca é o único sinal de estado (WCAG 2.1 AA).
- CSS Modules. Sem Tailwind, sem styled-components.
- Rota de destino não fica escrita dentro do componente: vem de `constants/`.
- Link cujo texto visível é genérico ("Leia", "Veja mais") precisa de
  `aria-label` com o título do item (WCAG 2.4.4).

---

## 5. API

### Formato

| Regra | Exemplo |
|---|---|
| prefixo `/api` | `/api/noticias` |
| recurso no plural | `/api/editais` |
| filtro por query | `/api/noticias?limite=3` |
| parâmetro numérico tem padrão e teto | `limite` vale 3 por omissão e é preso entre 1 e 20 |

O teto fica no service, não no controller: é regra de negócio, e o controller
não tem regra. O controller só declara o `defaultValue`.

### Status

| Código | Quando |
|---|---|
| `200` | deu certo, com corpo |
| `204` | deu certo, e a resposta é "não há" |
| `400` | o cliente mandou errado |
| `401` | falta autenticação |
| `403` | autenticado, sem permissão |
| `404` | o recurso não existe |

A diferença entre `204` e `404` importa: `/api/editais/aberto` devolve `204`
quando não há edital aberto, porque a pergunta foi respondida.

Nada impede dois editais abertos ao mesmo tempo. `/api/editais/aberto` devolve
**um**: o de `data_publicacao` mais recente, pelo `Pageable` de tamanho 1 no
`buscarAbertos`. O mural tem um cartão de edital, então quem escolhe é o back,
não o front.

### Rotas públicas

Tudo exige autenticação por padrão. Cada serviço declara as exceções no próprio
`application.properties`:

```properties
nubex.security.public-routes=GET /api/faq/**,GET /api/noticias/**
```

Sempre com o método na frente. Sem ele, `POST` e `DELETE` entram junto.

---

## 6. Banco e migrações

Nome do arquivo:

```
V<AAAAMMDD>_<HHMM>__<servico>_<descricao>.sql
V20261023_1500__conteudo_cria_noticia_e_edital.sql
```

| Regra | Motivo |
|---|---|
| migração aplicada **não se edita** | o Flyway guarda o checksum e recusa subir |
| mudou de ideia? nova migração com `ALTER TABLE` | o histórico é o registro da obra |
| dados de exemplo vão em `db/dev/` | só rodam no perfil `dev`, nunca em produção |
| seed de exemplo usa `R__` | é repetível: roda de novo quando o arquivo muda, sem nova versão |

As duas pastas são ligadas por um bloco de perfil dentro do próprio
`application.properties` do serviço, separado por `#---`:

```properties
#---
spring.config.activate.on-profile=dev
spring.flyway.locations=classpath:db/migration,classpath:db/dev
```

Fora do perfil `dev` a pasta `db/dev` não é nem lida. O `#---` é obrigatório:
sem ele o arquivo inteiro passa a valer só em `dev`, e produção sobe sem porta
nem banco.

---

## 7. Antes de abrir o PR

| Lado | Comandos |
|---|---|
| back | `./mvnw clean verify` |
| front | `npm run lint`, `npm run build`, `npm test` |

Os três do front precisam sair sem erro — é o que a CI roda. Traga a `develop`
para dentro da sua branch antes de abrir o PR, para o revisor ver só o seu
trabalho no diff.
