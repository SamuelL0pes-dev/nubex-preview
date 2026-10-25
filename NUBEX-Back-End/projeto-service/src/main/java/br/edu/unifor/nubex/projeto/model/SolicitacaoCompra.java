package br.edu.unifor.nubex.projeto.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Solicitação de compra de reagente, insumo ou equipamento (RF069), avaliada
 * pela diretoria (RF070) e encaminhada pela supervisão administrativa (RF075).
 *
 * O vínculo com o projeto é obrigatório e é chave estrangeira de verdade:
 * projeto e solicitação vivem no mesmo banco. Já solicitanteId, unidadeId,
 * equipamentoId e avaliadoPor são Long simples, porque apontam para outros
 * serviços.
 */
@Entity
@Table(name = "solicitacao_compra")
public class SolicitacaoCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Id da conta no usuario-service. Sem FK: outro banco. */
    @Column(name = "solicitante_id", nullable = false)
    private Long solicitanteId;

    /**
     * Projeto ao qual a compra se vincula. O RF069 exige que seja um projeto
     * ATIVO cadastrado pelo próprio solicitante -- as duas condições são
     * verificadas pelo service antes de gravar.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    /** Descrição do item: reagente, insumo ou equipamento (RF069). */
    @Column(nullable = false, length = 255)
    private String item;

    /**
     * Justificativa acadêmica do solicitante, obrigatória (RF069): produtos e
     * impactos científicos esperados da aquisição. Não confundir com a
     * justificativa de quem avalia, que fica em justificativaDecisao.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String finalidade;

    /** BigDecimal porque reagente se compra em fração: 2,5 L. */
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "unidade_medida", nullable = false, length = 20)
    private String unidadeMedida;

    /** Unidade estratégica de destino, no catalogo-service. Sem FK: outro banco. */
    @Column(name = "unidade_id", nullable = false)
    private Long unidadeId;

    /** Equipamento relacionado, no catalogo-service. Opcional: o RF069 diz "quando houver". */
    @Column(name = "equipamento_id")
    private Long equipamentoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusCompra status = StatusCompra.AGUARDANDO;

    /** Parecer de quem avaliou (RF070). */
    @Column(name = "justificativa_decisao", columnDefinition = "TEXT")
    private String justificativaDecisao;

    /** Id da conta de quem avaliou, no usuario-service. Sem FK: outro banco. */
    @Column(name = "avaliado_por")
    private Long avaliadoPor;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "avaliado_em")
    private LocalDateTime avaliadoEm;

    public SolicitacaoCompra() {
    }

    @PrePersist
    void aoCriar() {
        criadoEm = LocalDateTime.now();
    }

    public boolean estaAguardando() {
        return status == StatusCompra.AGUARDANDO;
    }

    /**
     * Registra a avaliação da diretoria (RF070). Existe como método único
     * porque o banco tem um CHECK exigindo que status, avaliadoPor e
     * avaliadoEm andem juntos: ou os três indicam decisão, ou os três
     * indicam pendência. Com setters separados seria fácil gravar metade e
     * levar erro do banco; aqui não há como.
     *
     * @param decisao      APROVADA ou RECUSADA
     * @param avaliadorId  conta de quem decidiu
     * @param justificativa parecer, exigido pelo RF070
     */
    public void avaliar(StatusCompra decisao, Long avaliadorId, String justificativa) {
        if (decisao != StatusCompra.APROVADA && decisao != StatusCompra.RECUSADA) {
            throw new IllegalArgumentException("A avaliação precisa ser APROVADA ou RECUSADA");
        }
        this.status = decisao;
        this.avaliadoPor = avaliadorId;
        this.justificativaDecisao = justificativa;
        this.avaliadoEm = LocalDateTime.now();
    }

    /**
     * A supervisão administrativa registra o encaminhamento ao sistema de
     * compras da Unifor (RF075). Só vale sobre solicitação já aprovada.
     */
    public void encaminhar() {
        if (status != StatusCompra.APROVADA) {
            throw new IllegalStateException("Só solicitação aprovada pode ser encaminhada");
        }
        this.status = StatusCompra.ENCAMINHADA;
    }

    public Long getId() {
        return id;
    }

    public Long getSolicitanteId() {
        return solicitanteId;
    }

    public Projeto getProjeto() {
        return projeto;
    }

    public String getItem() {
        return item;
    }

    public String getFinalidade() {
        return finalidade;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public Long getUnidadeId() {
        return unidadeId;
    }

    public Long getEquipamentoId() {
        return equipamentoId;
    }

    public StatusCompra getStatus() {
        return status;
    }

    public String getJustificativaDecisao() {
        return justificativaDecisao;
    }

    public Long getAvaliadoPor() {
        return avaliadoPor;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAvaliadoEm() {
        return avaliadoEm;
    }

    public void setSolicitanteId(Long solicitanteId) {
        this.solicitanteId = solicitanteId;
    }

    public void setProjeto(Projeto projeto) {
        this.projeto = projeto;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public void setFinalidade(String finalidade) {
        this.finalidade = finalidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public void setUnidadeId(Long unidadeId) {
        this.unidadeId = unidadeId;
    }

    public void setEquipamentoId(Long equipamentoId) {
        this.equipamentoId = equipamentoId;
    }

    @Override
    public String toString() {
        return "SolicitacaoCompra{id=" + id + "}";
    }
}
