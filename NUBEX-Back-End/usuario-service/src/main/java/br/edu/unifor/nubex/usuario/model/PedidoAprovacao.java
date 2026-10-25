package br.edu.unifor.nubex.usuario.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Pedido de aprovação de um cadastro (RF059 a RF061, RF088).
 *
 * Dois casos usam a mesma tabela:
 *  - participante ou solicitante externo: quem decide é o solicitante interno
 *    indicado no cadastro, guardado em {@code solicitante};
 *  - pedido de acesso como solicitante interno (RF052): qualquer
 *    ADMINISTRADOR decide, então {@code solicitante} fica nulo.
 *
 * Decidido, o pedido não aceita nova deliberação (RF060).
 */
@Entity
@Table(name = "pedido_aprovacao")
public class PedidoAprovacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Quem está pedindo acesso. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** Solicitante interno que deve decidir. Nulo quando quem decide é um administrador. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id")
    private Usuario solicitante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusPedido status = StatusPedido.AGUARDANDO;

    /**
     * Quem de fato decidiu. Importa porque vários administradores podem ver o
     * mesmo pedido e o RF088 exige mostrar aos outros quem decidiu.
     *
     * DIVERGE do Apêndice A: coluna acrescentada, não estava no documento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decidido_por")
    private Usuario decididoPor;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "decidido_em")
    private LocalDateTime decididoEm;

    public PedidoAprovacao() {
    }

    @PrePersist
    void aoCriar() {
        criadoEm = LocalDateTime.now();
    }

    public boolean estaAguardando() {
        return status == StatusPedido.AGUARDANDO;
    }

    /**
     * Registra a decisão. Existe como método único porque o banco tem um CHECK
     * exigindo que status, decididoPor e decididoEm andem juntos: ou os três
     * indicam decisão, ou os três indicam pendência. Com setters separados seria
     * fácil gravar metade e levar erro do banco; aqui não há como.
     *
     * @param decisao APROVADO ou RECUSADO
     * @param quem    usuário que decidiu
     */
    public void decidir(StatusPedido decisao, Usuario quem) {
        if (decisao == StatusPedido.AGUARDANDO) {
            throw new IllegalArgumentException("A decisão precisa ser APROVADO ou RECUSADO");
        }
        this.status = decisao;
        this.decididoPor = quem;
        this.decididoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public Usuario getDecididoPor() {
        return decididoPor;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getDecididoEm() {
        return decididoEm;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    /** Só o id: o pedido carrega dado pessoal de quem se cadastrou. */
    @Override
    public String toString() {
        return "PedidoAprovacao{id=" + id + "}";
    }
}
