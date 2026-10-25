package br.edu.unifor.nubex.projeto.model;

import jakarta.persistence.*;

/**
 * Pessoa que faz parte da equipe de um projeto (RF019, RF068).
 *
 * O integrante é SELECIONADO entre as contas do portal, nunca digitado. Por
 * isso usuarioId é obrigatório: quem entra na equipe tem conta, e o service
 * confere pela API do usuario-service que ela existe e está ativa antes de
 * gravar.
 *
 * Duas referências convivem aqui, e elas são diferentes de propósito:
 *  - projeto   -> @ManyToOne, porque é o mesmo banco e há chave estrangeira;
 *  - usuarioId -> Long simples, porque a conta está em outro banco.
 */
@Entity
@Table(name = "projeto_integrante")
public class ProjetoIntegrante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    /** Id da conta no usuario-service. Sem FK: outro banco. */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    /**
     * Cópia do nome no momento da inclusão, preenchida pelo service a partir
     * da conta selecionada. Nunca digitada por quem usa o sistema.
     *
     * Existe para a página pública de projetos (RF019) ser montada sem uma
     * chamada ao usuario-service por integrante -- a meta de LCP do RNF007 é
     * abaixo de 2 segundos. O usuarioId é a verdade; este campo é etiqueta.
     */
    @Column(nullable = false, length = 150)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private PapelIntegrante papel;

    public ProjetoIntegrante() {
    }

    public Long getId() {
        return id;
    }

    public Projeto getProjeto() {
        return projeto;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getNome() {
        return nome;
    }

    public PapelIntegrante getPapel() {
        return papel;
    }

    public void setProjeto(Projeto projeto) {
        this.projeto = projeto;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    /** Recebe o nome lido da conta selecionada, não um texto digitado. */
    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setPapel(PapelIntegrante papel) {
        this.papel = papel;
    }

    /** Só o id: a linha carrega nome de pessoa, que não vai para log. */
    @Override
    public String toString() {
        return "ProjetoIntegrante{id=" + id + "}";
    }
}
