import { useState } from "react";
import { Link } from "react-router-dom";
import styles from "./PedidoPendente.module.css";
import {
  obterPedidoPendente,
  formatarDataHoraAtual,
  atualizarStatusDemonstracao,
} from "../../services/solicitante";
import type { PedidoPendenteInfo, StatusUsuario } from "../../types/usuario";

const DADOS_EXEMPLO: PedidoPendenteInfo = {
  protocolo: "SOL-20261101-7824",
  nome: "Prof. Dr. Samuel Lopes",
  email: "samuel.lopes@unifor.br",
  telefone: "(85) 99876-5432",
  vinculo: "DOCENTE",
  centro: "Centro de Ciências Tecnológicas (CCT)",
  lattesUrl: "http://lattes.cnpq.br/4928172910492817",
  orcidUrl: "https://orcid.org/0000-0002-1825-0097",
  criadoEm: formatarDataHoraAtual(),
  status: "AGUARDANDO_APROVACAO",
};

export default function PedidoPendente() {
  const [pedido, setPedido] = useState<PedidoPendenteInfo>(() => {
    return obterPedidoPendente() || DADOS_EXEMPLO;
  });

  const [verificando, setVerificando] = useState(false);
  const [mensagemFeedback, setMensagemFeedback] = useState<string | null>(null);

  function handleVerificarStatus() {
    setVerificando(true);
    setMensagemFeedback(null);

    // Simula consulta de status na API
    setTimeout(() => {
      setVerificando(false);
      if (pedido.status === "AGUARDANDO_APROVACAO") {
        setMensagemFeedback(
          "Status verificado: sua solicitação continua sob avaliação da Administração do NUBEX. Prazo médio: até 24 horas úteis.",
        );
      } else if (pedido.status === "ATIVO") {
        setMensagemFeedback("Status verificado: sua conta está APROVADA e pronta para uso!");
      } else {
        setMensagemFeedback("Status verificado: seu cadastro foi indeferido pela administração.");
      }
    }, 700);
  }

  function handleTrocarStatusDemo(novoStatus: StatusUsuario) {
    const atualizado = atualizarStatusDemonstracao(novoStatus, pedido);
    setPedido(atualizado);
    setMensagemFeedback(
      novoStatus === "ATIVO"
        ? "Ambiente Demo: Simulada aprovação do pedido pelo Administrador!"
        : novoStatus === "RECUSADO"
          ? "Ambiente Demo: Simulada recusa do pedido pelo Administrador."
          : "Ambiente Demo: Retornado para Aguardando Aprovação.",
    );
  }

  const ehPendente = pedido.status === "AGUARDANDO_APROVACAO";
  const ehAprovado = pedido.status === "ATIVO";
  const ehRecusado = pedido.status === "RECUSADO";

  return (
    <div className={styles.pagina}>
      {/* ======================================================== */}
      {/* 1. HERO INSTITUCIONAL                                    */}
      {/* ======================================================== */}
      <section className={styles.hero}>
        <div className={styles.heroConteudo}>
          <span className={styles.eyebrow}>
            {ehAprovado
              ? "Acesso Homologado"
              : ehRecusado
                ? "Solicitação Encerrada"
                : "Solicitação Registrada • RF052"}
          </span>
          <h1 className={styles.titulo}>
            {ehAprovado
              ? "Acesso Liberado como Solicitante!"
              : ehRecusado
                ? "Solicitação de Acesso Não Homologada"
                : "Acompanhamento de Solicitação de Acesso"}
          </h1>
          <p className={styles.descricao}>
            {ehAprovado
              ? "Seu perfil foi homologado com sucesso. Você já possui acesso completo para cadastrar projetos, aprovar alunos e agendar laboratórios."
              : ehRecusado
                ? "A administração do NUBEX analisou o pedido e constatou inconsistências com os critérios institucionais."
                : "Seus dados foram enviados com sucesso e estão na fila de deliberação da Diretoria e Administração do NUBEX."}
          </p>
        </div>
      </section>

      {/* ======================================================== */}
      {/* 2. CONTEÚDO PRINCIPAL                                     */}
      {/* ======================================================== */}
      <main className={styles.conteudoPrincipal}>
        {/* CARD PRINCIPAL DE STATUS */}
        <div
          className={`${styles.cardStatus} ${
            ehAprovado
              ? styles.statusAprovado
              : ehRecusado
                ? styles.statusRecusado
                : styles.statusPendente
          }`}
          role="region"
          aria-label="Estado atual do pedido"
        >
          <div className={styles.statusHeader}>
            <div
              className={`${styles.badgeStatus} ${
                ehAprovado
                  ? styles.badgeAprovado
                  : ehRecusado
                    ? styles.badgeRecusado
                    : styles.badgePendente
              }`}
            >
              {ehPendente && <span className={styles.pulsoPendente} aria-hidden="true" />}
              {ehAprovado && <span aria-hidden="true">✓</span>}
              {ehRecusado && <span aria-hidden="true">✕</span>}
              <span>
                {ehAprovado
                  ? "STATUS: APROVADO (ATIVO)"
                  : ehRecusado
                    ? "STATUS: RECUSADO"
                    : "STATUS: AGUARDANDO APROVAÇÃO"}
              </span>
            </div>

            <div className={styles.protocoloTag}>
              Protocolo: <strong>#{pedido.protocolo}</strong>
            </div>
          </div>

          <p className={styles.statusMensagemPrincipal}>
            {ehAprovado ? (
              <>
                Olá, <strong>{pedido.nome}</strong>! Sua solicitação de acesso como{" "}
                <strong>Solicitante Interno ({pedido.vinculo === "DOCENTE" ? "Docente" : "Pesquisador"})</strong>{" "}
                foi <strong>aprovada</strong> pela Administração do NUBEX. Você já pode acessar o
                portal utilizando suas credenciais institucionais.
              </>
            ) : ehRecusado ? (
              <>
                Prezado(a) <strong>{pedido.nome}</strong>, seu pedido de acesso como Solicitante
                Interno foi indeferido. Motivo:{" "}
                <em>{pedido.motivoRecusa || "Vínculo institucional não localizado na base da Unifor."}</em>
              </>
            ) : (
              <>
                Olá, <strong>{pedido.nome}</strong>! Sua solicitação de acesso como{" "}
                <strong>Solicitante Interno ({pedido.vinculo === "DOCENTE" ? "Docente" : "Pesquisador"})</strong>{" "}
                foi registrada e está aguardando deliberação de um <strong>Administrador do NUBEX</strong>.
                Assim que aprovada, você receberá um e-mail de notificação em{" "}
                <strong>{pedido.email}</strong>.
              </>
            )}
          </p>

          {/* RESUMO DOS DADOS INFORMADOS */}
          <div className={styles.resumoGrid}>
            <div className={styles.resumoItem}>
              <span className={styles.resumoRotulo}>Solicitante</span>
              <span className={styles.resumoValor}>{pedido.nome}</span>
            </div>

            <div className={styles.resumoItem}>
              <span className={styles.resumoRotulo}>E-mail Institucional</span>
              <span className={styles.resumoValor}>{pedido.email}</span>
            </div>

            <div className={styles.resumoItem}>
              <span className={styles.resumoRotulo}>Vínculo na Unifor</span>
              <span className={styles.resumoValor}>
                {pedido.vinculo === "DOCENTE" ? "Docente / Professor" : "Pesquisador Institucional"}
              </span>
            </div>

            <div className={styles.resumoItem}>
              <span className={styles.resumoRotulo}>Centro / Departamento</span>
              <span className={styles.resumoValor}>{pedido.centro}</span>
            </div>

            <div className={styles.resumoItem}>
              <span className={styles.resumoRotulo}>Data do Envio</span>
              <span className={styles.resumoValor}>{pedido.criadoEm}</span>
            </div>

            <div className={styles.resumoItem}>
              <span className={styles.resumoRotulo}>Currículo Lattes</span>
              <span className={styles.resumoValor}>
                <a
                  href={pedido.lattesUrl}
                  target="_blank"
                  rel="noreferrer"
                  className={styles.linkLattes}
                >
                  Abrir Lattes ↗
                </a>
              </span>
            </div>
          </div>
        </div>

        {/* FEEDBACK DE CHECAGEM DE STATUS */}
        {mensagemFeedback && (
          <div className={styles.toastFeedback} role="status" aria-live="polite">
            <span aria-hidden="true">ℹ️</span>
            <span>{mensagemFeedback}</span>
          </div>
        )}

        {/* BLOCO EXPLICATIVO: POR QUE É BLOQUEANTE? (RF054 & RF052) */}
        <section className={styles.cardBloqueanteInfo} aria-label="Informações sobre o fluxo do sistema">
          <div className={styles.bloqueanteCabecalho}>
            <svg
              className={styles.bloqueanteIcone}
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
            <span>Por que esta tela é a porta de entrada de toda a cadeia do NUBEX?</span>
          </div>
          <p className={styles.bloqueanteTexto}>
            O requisito de negócio <strong>RF054</strong> determina que qualquer aluno ou participante
            só consegue se cadastrar no portal fornecendo o e-mail de um <strong>solicitante ativo</strong>.
            Sem solicitantes cadastrados e homologados pela administração (<strong>RF052</strong>), nenhum
            discente ou bolsista consegue entrar no sistema. Por esta razão, o atendimento a este pedido
            possui caráter prioritário na rotina da administração do NUBEX.
          </p>
        </section>

        {/* LINHA DO TEMPO DOS PASSOS */}
        <section className={styles.secaoTimeline} aria-label="Etapas do processo de aprovação">
          <h2 className={styles.timelineTitulo}>Próximas Etapas do Acesso</h2>
          <div className={styles.timelineLista}>
            {/* Passo 1 */}
            <div className={`${styles.timelineItem} ${styles.itemConcluido}`}>
              <div className={styles.timelineMarcador} aria-hidden="true">
                ✓
              </div>
              <div className={styles.timelineItemTitulo}>1. Solicitação Enviada</div>
              <div className={styles.timelineItemDescricao}>
                Seus dados cadastrais e link do Currículo Lattes foram submetidos com sucesso.
              </div>
            </div>

            {/* Passo 2 */}
            <div
              className={`${styles.timelineItem} ${
                ehPendente ? styles.itemEmProgresso : ehAprovado ? styles.itemConcluido : ""
              }`}
            >
              <div className={styles.timelineMarcador} aria-hidden="true">
                {ehAprovado ? "✓" : "2"}
              </div>
              <div className={styles.timelineItemTitulo}>
                2. Deliberação Administrativa (RF052 / RF088)
              </div>
              <div className={styles.timelineItemDescricao}>
                Um Administrador do NUBEX valida o vínculo com a Unifor e homologa o perfil de
                Solicitante no painel interno.
              </div>
            </div>

            {/* Passo 3 */}
            <div
              className={`${styles.timelineItem} ${
                ehAprovado ? styles.itemConcluido : ""
              }`}
            >
              <div className={styles.timelineMarcador} aria-hidden="true">
                {ehAprovado ? "✓" : "3"}
              </div>
              <div className={styles.timelineItemTitulo}>3. Notificação por E-mail</div>
              <div className={styles.timelineItemDescricao}>
                Você receberá um e-mail de confirmação assim que o administrador emitir a decisão.
              </div>
            </div>

            {/* Passo 4 */}
            <div
              className={`${styles.timelineItem} ${
                ehAprovado ? styles.itemConcluido : ""
              }`}
            >
              <div className={styles.timelineMarcador} aria-hidden="true">
                {ehAprovado ? "✓" : "4"}
              </div>
              <div className={styles.timelineItemTitulo}>4. Login e Desbloqueio da Equipe</div>
              <div className={styles.timelineItemDescricao}>
                Com a conta ativa, você poderá aprovar seus discentes (RF054), submeter projetos e
                solicitar agendamento de laboratórios do NUBEX.
              </div>
            </div>
          </div>
        </section>

        {/* BOTÕES DE AÇÃO */}
        <div className={styles.acoesContainer}>
          {ehAprovado ? (
            <Link to="/login" className={styles.btnLoginAprovado}>
              <span>Acessar Portal Agora</span>
              <span aria-hidden="true">→</span>
            </Link>
          ) : (
            <button
              type="button"
              onClick={handleVerificarStatus}
              disabled={verificando}
              className={styles.btnChecarStatus}
            >
              {verificando ? (
                <>
                  <span aria-hidden="true">⏳</span>
                  <span>Verificando na API...</span>
                </>
              ) : (
                <>
                  <span aria-hidden="true">🔄</span>
                  <span>Verificar Status Agora</span>
                </>
              )}
            </button>
          )}

          <Link to="/" className={styles.btnSecundario}>
            ← Voltar ao Portal NUBEX
          </Link>

          <Link to="/login" className={styles.btnSecundario}>
            Ir para a Tela de Login
          </Link>
        </div>

        {/* ======================================================== */}
        {/* 3. SIMULADOR DE DEMO (EXCLUSIVO PARA AVALIAÇÃO DA SPRINT) */}
        {/* ======================================================== */}
        <section className={styles.painelDemo} aria-label="Ferramenta de demonstração interativa">
          <p className={styles.demoTitulo}>
            <span aria-hidden="true">🧪</span>
            <strong>Simulador de Estados (Demonstração da Sprint 2)</strong>
          </p>
          <div className={styles.demoBotoes}>
            <button
              type="button"
              className={`${styles.btnDemoAcao} ${ehPendente ? styles.btnDemoAcaoAtivo : ""}`}
              onClick={() => handleTrocarStatusDemo("AGUARDANDO_APROVACAO")}
            >
              ⏳ Simular: Aguardando Decisão
            </button>

            <button
              type="button"
              className={`${styles.btnDemoAcao} ${ehAprovado ? styles.btnDemoAcaoAtivo : ""}`}
              onClick={() => handleTrocarStatusDemo("ATIVO")}
            >
              ✓ Simular: Administrador Aprovou
            </button>

            <button
              type="button"
              className={`${styles.btnDemoAcao} ${ehRecusado ? styles.btnDemoAcaoAtivo : ""}`}
              onClick={() => handleTrocarStatusDemo("RECUSADO")}
            >
              ✕ Simular: Administrador Recusou
            </button>
          </div>
        </section>
      </main>
    </div>
  );
}
