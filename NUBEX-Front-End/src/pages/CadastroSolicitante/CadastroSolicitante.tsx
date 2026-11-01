import { useState, type FormEvent, type ChangeEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import styles from "./CadastroSolicitante.module.css";
import { enviarSolicitacaoAcesso } from "../../services/solicitante";
import type { SolicitacaoSolicitanteInput } from "../../types/usuario";

const CENTROS_ACADEMICOS = [
  "Centro de Ciências Tecnológicas (CCT)",
  "Centro de Ciências da Saúde (CCS)",
  "Centro de Ciências da Comunicação e Gestão (CCG)",
  "Centro de Ciências Jurídicas (CCJ)",
  "Núcleo de Biologia Experimental (NUBEX)",
  "Pós-Graduação Stricto Sensu / Interdisciplinar",
  "Outro Departamento",
];

function formatarTelefone(valor: string): string {
  const digitos = valor.replace(/\D/g, "").slice(0, 11);
  if (digitos.length <= 2) return digitos ? `(${digitos}` : "";
  if (digitos.length <= 6) return `(${digitos.slice(0, 2)}) ${digitos.slice(2)}`;
  if (digitos.length <= 10) {
    return `(${digitos.slice(0, 2)}) ${digitos.slice(2, 6)}-${digitos.slice(6)}`;
  }
  return `(${digitos.slice(0, 2)}) ${digitos.slice(2, 7)}-${digitos.slice(7, 11)}`;
}

export default function CadastroSolicitante() {
  const navigate = useNavigate();

  // Estados do formulário
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [vinculo, setVinculo] = useState<"DOCENTE" | "PESQUISADOR">("DOCENTE");
  const [centro, setCentro] = useState(CENTROS_ACADEMICOS[0]);
  const [telefone, setTelefone] = useState("");
  const [lattesUrl, setLattesUrl] = useState("");
  const [orcidUrl, setOrcidUrl] = useState("");
  const [senha, setSenha] = useState("");
  const [confirmacaoSenha, setConfirmacaoSenha] = useState("");
  const [termosAceitos, setTermosAceitos] = useState(false);

  // Estados de UI e controle
  const [mostrarSenha, setMostrarSenha] = useState(false);
  const [mostrarConfirmacao, setMostrarConfirmacao] = useState(false);
  const [carregando, setCarregando] = useState(false);
  const [erroGeral, setErroGeral] = useState<string | null>(null);
  const [errosCampos, setErrosCampos] = useState<Record<string, string>>({});

  function handleTelefoneChange(e: ChangeEvent<HTMLInputElement>) {
    setTelefone(formatarTelefone(e.target.value));
    if (errosCampos.telefone) {
      setErrosCampos((prev) => ({ ...prev, telefone: "" }));
    }
  }

  function validarFormulario(): boolean {
    const novosErros: Record<string, string> = {};

    // 1. Validação de Nome
    const nomeLimpo = nome.trim();
    if (!nomeLimpo) {
      novosErros.nome = "Informe seu nome completo.";
    } else if (nomeLimpo.split(/\s+/).length < 2) {
      novosErros.nome = "Informe ao menos nome e sobrenome.";
    }

    // 2. Validação de E-mail Institucional
    const emailLimpo = email.trim().toLowerCase();
    if (!emailLimpo) {
      novosErros.email = "Informe seu e-mail institucional Unifor.";
    } else if (!emailLimpo.includes("@")) {
      novosErros.email = "Formato de e-mail inválido.";
    } else if (!emailLimpo.endsWith("@unifor.br") && !emailLimpo.endsWith("@edu.unifor.br")) {
      novosErros.email =
        "O perfil de solicitante interno exige e-mail institucional Unifor (@unifor.br ou @edu.unifor.br).";
    }

    // 3. Validação de Telefone
    const digitosTelefone = telefone.replace(/\D/g, "");
    if (digitosTelefone.length < 10) {
      novosErros.telefone = "Informe um telefone válido com DDD (mínimo 10 dígitos).";
    }

    // 4. Validação de Currículo Lattes (RF091)
    const lattesLimpo = lattesUrl.trim();
    if (!lattesLimpo) {
      novosErros.lattesUrl = "O link do Currículo Lattes é obrigatório para solicitantes internos (RF091).";
    } else if (!lattesLimpo.startsWith("http://") && !lattesLimpo.startsWith("https://")) {
      novosErros.lattesUrl = "Informe uma URL válida iniciando com http:// ou https://";
    }

    // 5. Validação de Senha (alinhada com regra do projeto: 8 a 36 caracteres)
    if (!senha) {
      novosErros.senha = "Crie uma senha de acesso.";
    } else if (senha.length < 8 || senha.length > 36) {
      novosErros.senha = "A senha deve ter entre 8 e 36 caracteres.";
    }

    // 6. Confirmação de Senha
    if (!confirmacaoSenha) {
      novosErros.confirmacaoSenha = "Confirme sua senha.";
    } else if (senha !== confirmacaoSenha) {
      novosErros.confirmacaoSenha = "As senhas digitadas não coincidem.";
    }

    // 7. Termos
    if (!termosAceitos) {
      novosErros.termos = "Você deve concordar com os termos para continuar.";
    }

    setErrosCampos(novosErros);
    return Object.keys(novosErros).length === 0;
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setErroGeral(null);

    if (!validarFormulario()) {
      return;
    }

    setCarregando(true);

    try {
      const dadosInput: SolicitacaoSolicitanteInput = {
        nome,
        email,
        senha,
        confirmacaoSenha,
        telefone,
        vinculo,
        centro,
        lattesUrl,
        orcidUrl,
        termosAceitos,
      };

      await enviarSolicitacaoAcesso(dadosInput);
      navigate("/solicitar-acesso/pendente");
    } catch (erro: unknown) {
      console.error("Erro ao enviar solicitação:", erro);
      setErroGeral(
        erro instanceof Error
          ? erro.message
          : "Não foi possível concluir a solicitação. Verifique sua conexão e tente novamente.",
      );
    } finally {
      setCarregando(false);
    }
  }

  // Verificações visuais de requisitos de senha
  const temOitoCaracteres = senha.length >= 8 && senha.length <= 36;
  const temMaiuscula = /[A-Z]/.test(senha);
  const temNumeroOuEspecial = /[0-9!@#$%^&*(),.?":{}|<>]/.test(senha);

  return (
    <div className={styles.pagina}>
      {/* ======================================================== */}
      {/* 1. HERO INSTITUCIONAL UNIFOR / NUBEX                     */}
      {/* ======================================================== */}
      <section className={styles.hero}>
        <div className={styles.heroConteudo}>
          <span className={styles.eyebrow}>Portal NUBEX • Requisito RF052</span>
          <h1 className={styles.titulo}>Solicitar Acesso como Solicitante</h1>
          <p className={styles.descricao}>
            Exclusivo para <strong>docentes e pesquisadores</strong> da Universidade de Fortaleza
            (Unifor). Este perfil autoriza a submissão de projetos, o agendamento de equipamentos
            dos laboratórios do NUBEX e a liberação de acesso aos discentes participantes.
          </p>

          {/* Destaque do fluxo bloqueante explicado ao usuário */}
          <div className={styles.alertaBloqueante} role="region" aria-label="Aviso de aprovação de perfil">
            <svg
              className={styles.alertaIcone}
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
            >
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
            </svg>
            <div className={styles.alertaTexto}>
              <strong>Por que esta solicitação requer aprovação prévia?</strong>
              <br />
              Conforme as diretrizes institucionais do NUBEX (RF052 e RF054), os discentes e
              bolsistas dependem do e-mail de um <strong>solicitante ativo</strong> para realizar o
              cadastro no sistema. Por segurança e governança acadêmica, seu pedido será avaliado e
              homologado pela <strong>Administração do NUBEX</strong> antes da liberação do login.
            </div>
          </div>
        </div>
      </section>

      {/* ======================================================== */}
      {/* 2. FORMULÁRIO DE CADASTRO E SOLICITAÇÃO                   */}
      {/* ======================================================== */}
      <main className={styles.conteudoPrincipal}>
        <div className={styles.formCard}>
          {erroGeral && (
            <div className={styles.erroGeral} role="alert">
              <span aria-hidden="true">⚠️</span>
              <span>{erroGeral}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} noValidate>
            {/* SEÇÃO 1: DADOS PESSOAIS E VÍNCULO */}
            <fieldset className={styles.secaoFormulario}>
              <legend className={styles.secaoTitulo}>
                <span className={styles.secaoNumero}>1</span>
                Dados Pessoais e Vínculo Institucional
              </legend>
              <p className={styles.secaoDescricao}>
                Identificação do docente ou pesquisador com vínculo ativo na Unifor.
              </p>

              <div className={styles.gridCampos}>
                {/* Nome Completo */}
                <div className={`${styles.grupoCampo} ${styles.campoTotal}`}>
                  <label htmlFor="campo-nome" className={styles.rotulo}>
                    Nome Completo <span className={styles.obrigatorio}>*</span>
                  </label>
                  <input
                    id="campo-nome"
                    name="nome"
                    type="text"
                    value={nome}
                    onChange={(e) => setNome(e.target.value)}
                    placeholder="Ex: Prof. Dr. Carlos Eduardo de Albuquerque"
                    className={`${styles.inputTexto} ${errosCampos.nome ? styles.inputInvalido : ""}`}
                    aria-required="true"
                    aria-invalid={!!errosCampos.nome}
                    aria-describedby={errosCampos.nome ? "erro-nome" : undefined}
                    maxLength={150}
                  />
                  {errosCampos.nome && (
                    <span id="erro-nome" className={styles.mensagemErro} role="alert">
                      {errosCampos.nome}
                    </span>
                  )}
                </div>

                {/* E-mail Institucional */}
                <div className={styles.grupoCampo}>
                  <label htmlFor="campo-email" className={styles.rotulo}>
                    E-mail Institucional Unifor <span className={styles.obrigatorio}>*</span>
                  </label>
                  <input
                    id="campo-email"
                    name="email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="seu.nome@unifor.br"
                    className={`${styles.inputTexto} ${errosCampos.email ? styles.inputInvalido : ""}`}
                    aria-required="true"
                    aria-invalid={!!errosCampos.email}
                    aria-describedby={errosCampos.email ? "erro-email" : "ajuda-email"}
                    maxLength={150}
                  />
                  {errosCampos.email ? (
                    <span id="erro-email" className={styles.mensagemErro} role="alert">
                      {errosCampos.email}
                    </span>
                  ) : (
                    <span id="ajuda-email" className={styles.textoAjuda}>
                      Domínio @unifor.br ou @edu.unifor.br
                    </span>
                  )}
                </div>

                {/* Telefone */}
                <div className={styles.grupoCampo}>
                  <label htmlFor="campo-telefone" className={styles.rotulo}>
                    Telefone / Celular (WhatsApp) <span className={styles.obrigatorio}>*</span>
                  </label>
                  <input
                    id="campo-telefone"
                    name="telefone"
                    type="tel"
                    value={telefone}
                    onChange={handleTelefoneChange}
                    placeholder="(85) 99999-0000"
                    className={`${styles.inputTexto} ${errosCampos.telefone ? styles.inputInvalido : ""}`}
                    aria-required="true"
                    aria-invalid={!!errosCampos.telefone}
                    aria-describedby={errosCampos.telefone ? "erro-telefone" : "ajuda-tel"}
                    maxLength={15}
                  />
                  {errosCampos.telefone ? (
                    <span id="erro-telefone" className={styles.mensagemErro} role="alert">
                      {errosCampos.telefone}
                    </span>
                  ) : (
                    <span id="ajuda-tel" className={styles.textoAjuda}>
                      Para contato da coordenação do laboratório
                    </span>
                  )}
                </div>

                {/* Vínculo na Unifor (Docente / Pesquisador) */}
                <div className={`${styles.grupoCampo} ${styles.campoTotal}`}>
                  <span className={styles.rotulo}>
                    Vínculo na Universidade <span className={styles.obrigatorio}>*</span>
                  </span>
                  <div className={styles.opcoesVinculo} role="radiogroup" aria-label="Vínculo acadêmico">
                    <label
                      className={`${styles.opcaoCard} ${
                        vinculo === "DOCENTE" ? styles.opcaoCardSelecionada : ""
                      }`}
                    >
                      <input
                        type="radio"
                        name="vinculo"
                        value="DOCENTE"
                        checked={vinculo === "DOCENTE"}
                        onChange={() => setVinculo("DOCENTE")}
                        className={styles.radioInput}
                      />
                      <div className={styles.opcaoTextos}>
                        <span className={styles.opcaoTitulo}>Docente / Professor(a)</span>
                        <span className={styles.opcaoSubtitulo}>
                          Professor do corpo docente dos cursos de graduação ou pós-graduação
                        </span>
                      </div>
                    </label>

                    <label
                      className={`${styles.opcaoCard} ${
                        vinculo === "PESQUISADOR" ? styles.opcaoCardSelecionada : ""
                      }`}
                    >
                      <input
                        type="radio"
                        name="vinculo"
                        value="PESQUISADOR"
                        checked={vinculo === "PESQUISADOR"}
                        onChange={() => setVinculo("PESQUISADOR")}
                        className={styles.radioInput}
                      />
                      <div className={styles.opcaoTextos}>
                        <span className={styles.opcaoTitulo}>Pesquisador(a) Institucional</span>
                        <span className={styles.opcaoSubtitulo}>
                          Pós-doutorando ou pesquisador alocado em projetos de pesquisa do NUBEX
                        </span>
                      </div>
                    </label>
                  </div>
                </div>

                {/* Centro / Departamento */}
                <div className={`${styles.grupoCampo} ${styles.campoTotal}`}>
                  <label htmlFor="campo-centro" className={styles.rotulo}>
                    Centro ou Departamento Acadêmico <span className={styles.obrigatorio}>*</span>
                  </label>
                  <select
                    id="campo-centro"
                    name="centro"
                    value={centro}
                    onChange={(e) => setCentro(e.target.value)}
                    className={styles.select}
                  >
                    {CENTROS_ACADEMICOS.map((c) => (
                      <option key={c} value={c}>
                        {c}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </fieldset>

            {/* SEÇÃO 2: IDENTIFICAÇÃO CIENTÍFICA (RF091) */}
            <fieldset className={styles.secaoFormulario}>
              <legend className={styles.secaoTitulo}>
                <span className={styles.secaoNumero}>2</span>
                Identificação Científica e Produção Acadêmica (RF091)
              </legend>
              <p className={styles.secaoDescricao}>
                Informações para verificação da produção científica e validação pela administração.
              </p>

              <div className={styles.gridCampos}>
                {/* Lattes URL */}
                <div className={`${styles.grupoCampo} ${styles.campoTotal}`}>
                  <label htmlFor="campo-lattes" className={styles.rotulo}>
                    Link do Currículo Lattes <span className={styles.obrigatorio}>*</span>
                  </label>
                  <input
                    id="campo-lattes"
                    name="lattesUrl"
                    type="url"
                    value={lattesUrl}
                    onChange={(e) => setLattesUrl(e.target.value)}
                    placeholder="http://lattes.cnpq.br/1234567890123456"
                    className={`${styles.inputTexto} ${errosCampos.lattesUrl ? styles.inputInvalido : ""}`}
                    aria-required="true"
                    aria-invalid={!!errosCampos.lattesUrl}
                    aria-describedby={errosCampos.lattesUrl ? "erro-lattes" : "ajuda-lattes"}
                  />
                  {errosCampos.lattesUrl ? (
                    <span id="erro-lattes" className={styles.mensagemErro} role="alert">
                      {errosCampos.lattesUrl}
                    </span>
                  ) : (
                    <span id="ajuda-lattes" className={styles.textoAjuda}>
                      Obrigatório para conferência do histórico de pesquisa pela equipe do NUBEX.
                    </span>
                  )}
                </div>

                {/* ORCID URL */}
                <div className={`${styles.grupoCampo} ${styles.campoTotal}`}>
                  <label htmlFor="campo-orcid" className={styles.rotulo}>
                    Identificador ORCID (Opcional)
                  </label>
                  <input
                    id="campo-orcid"
                    name="orcidUrl"
                    type="text"
                    value={orcidUrl}
                    onChange={(e) => setOrcidUrl(e.target.value)}
                    placeholder="https://orcid.org/0000-0002-1825-0097"
                    className={styles.inputTexto}
                    aria-describedby="ajuda-orcid"
                  />
                  <span id="ajuda-orcid" className={styles.textoAjuda}>
                    Recomendado para integração de publicações científicas e relatórios de projetos.
                  </span>
                </div>
              </div>
            </fieldset>

            {/* SEÇÃO 3: CREDENCIAIS DE ACESSO */}
            <fieldset className={styles.secaoFormulario}>
              <legend className={styles.secaoTitulo}>
                <span className={styles.secaoNumero}>3</span>
                Definição de Senha de Acesso
              </legend>
              <p className={styles.secaoDescricao}>
                Esta credencial será utilizada para acessar o Portal NUBEX após a aprovação da conta.
              </p>

              <div className={styles.gridCampos}>
                {/* Senha */}
                <div className={styles.grupoCampo}>
                  <label htmlFor="campo-senha" className={styles.rotulo}>
                    Senha de Acesso <span className={styles.obrigatorio}>*</span>
                  </label>
                  <div className={styles.inputSenhaContainer}>
                    <input
                      id="campo-senha"
                      name="senha"
                      type={mostrarSenha ? "text" : "password"}
                      value={senha}
                      onChange={(e) => setSenha(e.target.value)}
                      placeholder="Mínimo 8 caracteres"
                      className={`${styles.inputTexto} ${errosCampos.senha ? styles.inputInvalido : ""}`}
                      aria-required="true"
                      aria-invalid={!!errosCampos.senha}
                      aria-describedby={errosCampos.senha ? "erro-senha" : undefined}
                      maxLength={36}
                    />
                    <button
                      type="button"
                      className={styles.btnMostrarSenha}
                      onClick={() => setMostrarSenha(!mostrarSenha)}
                      aria-label={mostrarSenha ? "Ocultar senha" : "Ver senha"}
                    >
                      {mostrarSenha ? "🙈" : "👁️"}
                    </button>
                  </div>
                  {errosCampos.senha && (
                    <span id="erro-senha" className={styles.mensagemErro} role="alert">
                      {errosCampos.senha}
                    </span>
                  )}
                  {/* Requisitos visuais */}
                  <div className={styles.requisitosSenha} aria-label="Requisitos de segurança da senha">
                    <span
                      className={`${styles.requisitoItem} ${
                        temOitoCaracteres ? styles.requisitoCumprido : ""
                      }`}
                    >
                      {temOitoCaracteres ? "✓" : "○"} 8 a 36 caracteres
                    </span>
                    <span
                      className={`${styles.requisitoItem} ${
                        temMaiuscula ? styles.requisitoCumprido : ""
                      }`}
                    >
                      {temMaiuscula ? "✓" : "○"} Letra maiúscula
                    </span>
                    <span
                      className={`${styles.requisitoItem} ${
                        temNumeroOuEspecial ? styles.requisitoCumprido : ""
                      }`}
                    >
                      {temNumeroOuEspecial ? "✓" : "○"} Número ou caractere especial
                    </span>
                  </div>
                </div>

                {/* Confirmação de Senha */}
                <div className={styles.grupoCampo}>
                  <label htmlFor="campo-confirmacao" className={styles.rotulo}>
                    Confirmar Senha <span className={styles.obrigatorio}>*</span>
                  </label>
                  <div className={styles.inputSenhaContainer}>
                    <input
                      id="campo-confirmacao"
                      name="confirmacaoSenha"
                      type={mostrarConfirmacao ? "text" : "password"}
                      value={confirmacaoSenha}
                      onChange={(e) => setConfirmacaoSenha(e.target.value)}
                      placeholder="Repita a senha"
                      className={`${styles.inputTexto} ${
                        errosCampos.confirmacaoSenha ? styles.inputInvalido : ""
                      }`}
                      aria-required="true"
                      aria-invalid={!!errosCampos.confirmacaoSenha}
                      aria-describedby={
                        errosCampos.confirmacaoSenha ? "erro-confirmacao" : undefined
                      }
                      maxLength={36}
                    />
                    <button
                      type="button"
                      className={styles.btnMostrarSenha}
                      onClick={() => setMostrarConfirmacao(!mostrarConfirmacao)}
                      aria-label={mostrarConfirmacao ? "Ocultar confirmação" : "Ver confirmação"}
                    >
                      {mostrarConfirmacao ? "🙈" : "👁️"}
                    </button>
                  </div>
                  {errosCampos.confirmacaoSenha && (
                    <span id="erro-confirmacao" className={styles.mensagemErro} role="alert">
                      {errosCampos.confirmacaoSenha}
                    </span>
                  )}
                </div>
              </div>
            </fieldset>

            {/* SEÇÃO 4: TERMOS E DECLARAÇÃO */}
            <fieldset className={styles.secaoFormulario}>
              <div className={styles.termoContainer}>
                <input
                  id="campo-termos"
                  type="checkbox"
                  checked={termosAceitos}
                  onChange={(e) => setTermosAceitos(e.target.checked)}
                  className={styles.checkbox}
                  aria-required="true"
                  aria-invalid={!!errosCampos.termos}
                  aria-describedby={errosCampos.termos ? "erro-termos" : undefined}
                />
                <label htmlFor="campo-termos" className={styles.termoTexto}>
                  Declaro que as informações prestadas são verdadeiras e estou ciente de que a
                  liberação do meu acesso ao Portal NUBEX está sujeita à <strong>homologação prévia</strong>{" "}
                  pela Administração e ao cumprimento das normas de biossegurança dos laboratórios.
                </label>
              </div>
              {errosCampos.termos && (
                <span id="erro-termos" className={styles.mensagemErro} role="alert">
                  {errosCampos.termos}
                </span>
              )}
            </fieldset>

            {/* AÇÕES DE SUBMISSÃO */}
            <div className={styles.acoesFormulario}>
              <button
                type="submit"
                disabled={carregando}
                className={styles.btnEnviar}
                aria-busy={carregando}
              >
                {carregando ? (
                  <>
                    <span className={styles.spinner} aria-hidden="true" />
                    <span>Enviando solicitação...</span>
                  </>
                ) : (
                  <>
                    <span>Submeter Solicitação de Acesso</span>
                    <span aria-hidden="true">→</span>
                  </>
                )}
              </button>

              <div className={styles.linkLogin}>
                Já possui uma conta ativa no Portal NUBEX?
                <Link to="/login">Fazer Login</Link>
              </div>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}
