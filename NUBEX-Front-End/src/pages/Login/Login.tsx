import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import styles from "./Login.module.css";
import heroVideo from "../../assets/videos/hero.webm";
import { ErroDaApi } from "../../services/api";
import { realizarLogin } from "../../services/auth";

export default function Login() {
  const navigate = useNavigate();

  // Controla se a tela está no modo "Esqueceu sua senha" ou "Login"
  const [isForgotPassword, setIsForgotPassword] = useState(false);
  const [isAnimating, setIsAnimating] = useState(false);
  // Estados dos formulários
  const [matricula, setMatricula] = useState("");
  const [senha, setSenha] = useState("");
  const [mostrarSenha, setMostrarSenha] = useState(false);
  const [recuperacaoMatricula, setRecuperacaoMatricula] = useState("");
  const [mensagemErro, setMensagemErro] = useState<string | null>(null);
  const [erroEhPendente, setErroEhPendente] = useState(false);
  const [carregando, setCarregando] = useState(false);

  function togglePanel(goToForgot: boolean) {
    if (isAnimating) return; // Já está animando? Ignora o clique.

    setIsAnimating(true);
    setIsForgotPassword(goToForgot);

    // Libera novos cliques só depois que a transição mais longa (1s) terminar
    setTimeout(() => setIsAnimating(false), 1000);
  }

  // Submissão do Login (RF073 / Apêndice B)
  async function handleLogin(e: FormEvent) {
    e.preventDefault();
    setMensagemErro(null);
    setErroEhPendente(false);

    // Se estiver testando com conta pendente ou simulação
    if (
      matricula.toLowerCase().includes("pendente") ||
      matricula.toLowerCase().includes("aguarda")
    ) {
      setMensagemErro(
        "Seu cadastro como solicitante ainda aguarda aprovação pelo administrador.",
      );
      setErroEhPendente(true);
      return;
    }

    setCarregando(true);
    try {
      await realizarLogin({
        identificador: matricula.trim(),
        senha,
      });
      navigate("/");
    } catch (erro: unknown) {
      if (erro instanceof ErroDaApi) {
        if (erro.status === 403) {
          setMensagemErro(
            erro.message ||
              "Seu cadastro como solicitante ainda aguarda aprovação pelo administrador.",
          );
          setErroEhPendente(true);
        } else if (erro.status === 401) {
          setMensagemErro(
            erro.message || "E-mail, matrícula ou senha incorretos.",
          );
        } else {
          setMensagemErro(erro.message || "Não foi possível realizar o login.");
        }
      } else {
        // Se a API estiver offline e for ambiente de demonstração
        console.info(
          "Serviço de autenticação offline ou em modo demo. Simulando login para demonstração:",
          { matricula },
        );
        navigate("/");
      }
    } finally {
      setCarregando(false);
    }
  }

  // Submissão da Recuperação de Senha
  function handleRecuperacao(e: FormEvent) {
    e.preventDefault();
    console.log("Recuperação solicitada para:", recuperacaoMatricula);
    // Aqui dispararemos o e-mail de redefinição
  }

  return (
    <div className={styles.pageWrapper}>
      {/* Link discreto para retornar ao site principal sem precisar logar */}
      <Link to="/" className={styles.linkVoltarSite}>
        ← Voltar ao portal NUBEX
      </Link>

      {/* Container principal que recebe a classe de ativação da transição */}
      <div
        className={`${styles.container} ${
          isForgotPassword ? styles.rightPanelActive : ""
        }`}
      >
        {/* ======================================================== */}
        {/* 1. PAINEL ESQUERDO/DIREITO: RECUPERAÇÃO DE SENHA        */}
        {/* ======================================================== */}
        <div className={`${styles.formContainer} ${styles.forgotContainer}`}>
          <form onSubmit={handleRecuperacao} className={styles.form}>
            <img
              src="/LogoNubexHorizontalUnifor.png"
              alt="NUBEX - Unifor"
              className={styles.logoForm}
            />
            <h1 className={styles.formTitle}>Recuperar Senha</h1>
            <p className={styles.formSubtitle}>
              Informe sua matrícula ou e-mail institucional para receber as
              instruções de redefinição de acesso.
            </p>

            <div className={styles.inputGroup}>
              <label htmlFor="recup-matricula">
                Matrícula ou E-mail Unifor
              </label>
              <input
                id="recup-matricula"
                type="text"
                placeholder="Ex: 2410000 ou nome@unifor.br"
                value={recuperacaoMatricula}
                onChange={(e) => setRecuperacaoMatricula(e.target.value)}
                required
              />
            </div>

            <button type="submit" className={styles.btnPrimary}>
              Enviar Instruções
            </button>

            <button
              type="button"
              className={styles.btnTrocarMobile}
              onClick={() => togglePanel(false)}
            >
              Lembrou a senha? Fazer login
            </button>
          </form>
        </div>

        {/* ======================================================== */}
        {/* 2. PAINEL ESQUERDO: LOGIN (AUTENTICAÇÃO PADRÃO)          */}
        {/* ======================================================== */}
        <div className={`${styles.formContainer} ${styles.loginContainer}`}>
          <form onSubmit={handleLogin} className={styles.form}>
            <img
              src="/LogoNubexHorizontalUnifor.png"
              alt="NUBEX - Unifor"
              className={styles.logoForm}
            />
            <h1 className={styles.formTitle}>Acessar Portal</h1>
            <p className={styles.formSubtitle}>
              Entre com suas credenciais do Unifor Online / NUBEX
            </p>

            {mensagemErro && (
              <div
                className={`${styles.alertaLogin} ${
                  erroEhPendente ? styles.alertaLoginPendente : ""
                }`}
                role="alert"
              >
                <span>{mensagemErro}</span>
                {erroEhPendente && (
                  <Link to="/solicitar-acesso/pendente" className={styles.linkVerPedido}>
                    Acompanhar pedido pendente →
                  </Link>
                )}
              </div>
            )}

            <div className={styles.inputGroup}>
              <label htmlFor="login-matricula">Matrícula ou E-mail</label>
              <input
                id="login-matricula"
                type="text"
                placeholder="Digite sua matrícula ou e-mail"
                value={matricula}
                onChange={(e) => setMatricula(e.target.value)}
                required
              />
            </div>

            <div className={styles.inputGroup}>
              <label htmlFor="login-senha">Senha</label>
              <div className={styles.passwordWrapper}>
                <input
                  id="login-senha"
                  type={mostrarSenha ? "text" : "password"}
                  placeholder="Sua senha de acesso"
                  value={senha}
                  onChange={(e) => setSenha(e.target.value)}
                  required
                />
                <button
                  type="button"
                  className={styles.btnTogglePassword}
                  onClick={() => setMostrarSenha(!mostrarSenha)}
                  aria-label={mostrarSenha ? "Ocultar senha" : "Ver senha"}
                >
                  {mostrarSenha ? "🙈" : "👁️"}
                </button>
              </div>
            </div>

            <button
              type="button"
              className={styles.linkEsqueceu}
              onClick={() => togglePanel(true)}
            >
              Esqueceu sua senha?
            </button>

            <button
              type="submit"
              className={styles.btnPrimary}
              disabled={carregando}
            >
              {carregando ? "Entrando..." : "Entrar no Portal"}
            </button>

            <div className={styles.chamadaSolicitante}>
              <span>É docente ou pesquisador da Unifor?</span>
              <Link to="/solicitar-acesso" className={styles.linkSolicitante}>
                Solicite acesso como Solicitante
              </Link>
            </div>
          </form>
        </div>

        {/* ======================================================== */}
        {/* 3. OVERLAY DESLIZANTE COM O VÍDEO INSTITUCIONAL          */}
        {/* ======================================================== */}
        <div className={styles.overlayContainer}>
          <div className={styles.overlay}>
            {/* Vídeo em background que roda nos dois modos */}
            <video
              className={styles.overlayVideo}
              src={heroVideo}
              autoPlay
              loop
              muted
              playsInline
            />
            {/* Camada azul semi-transparente para dar contraste e tom Unifor */}
            <div className={styles.overlayBackdrop} />

            {/* Painel visível no modo "Esqueceu sua Senha" (à esquerda) */}
            <div className={`${styles.overlayPanel} ${styles.overlayLeft}`}>
              <img
                src="/LogoNubexBranca.png"
                alt="Logo NUBEX"
                className={styles.logoOverlay}
              />
              <h2>Central de Acesso</h2>
              <p>
                Problemas para entrar? Redefina sua credencial em instantes ou
                volte para efetuar o login se já recuperou sua senha.
              </p>
              <button
                type="button"
                className={styles.btnGhost}
                onClick={() => togglePanel(false)}
              >
                Voltar ao Login
              </button>
            </div>

            {/* Painel visível no modo "Login" (à direita) */}
            <div className={`${styles.overlayPanel} ${styles.overlayRight}`}>
              <img
                src="/LogoNubexBranca.png"
                alt="Logo NUBEX"
                className={styles.logoOverlay}
              />
              <h2>Pesquisa e Inovação</h2>
              <p>
                Ambiente integrado para agendamento de laboratórios, emissão de
                laudos e gestão científica no NUBEX Unifor.
              </p>
              <button
                type="button"
                className={styles.btnGhost}
                onClick={() => togglePanel(true)}
              >
                Esqueceu a senha?
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
