package br.edu.unifor.nubex.projeto.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Projeto de pesquisa cadastrado por um solicitante interno (RF068) e exibido
 * na página pública de projetos (RF019).
 *
 * Repare no solicitanteId: é um Long simples, não um @ManyToOne. É a regra de
 * ouro em ação -- a conta vive no banco do usuario-service, então não existe
 * chave estrangeira possível. Quem confere que o solicitante existe é o
 * service, chamando a API do outro domínio antes de gravar.
 */
@Entity
@Table(name = "projeto")
public class Projeto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Id da conta no usuario-service. Sem FK: outro banco. */
    @Column(name = "solicitante_id", nullable = false)
    private Long solicitanteId;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 255)
    private String subtitulo;

    @Column(columnDefinition = "TEXT")
    private String conteudo;

    // Listas em texto: conteúdo de exibição, sem busca nem filtro em
    // requisito nenhum.

    @Column(columnDefinition = "TEXT")
    private String links;

    @Column(name = "linhas_pesquisa", columnDefinition = "TEXT")
    private String linhasPesquisa;

    @Column(columnDefinition = "TEXT")
    private String producoes;

    /**
     * Desativar em vez de apagar. O RF069 exige que a compra se vincule a um
     * projeto ATIVO, e apagar deixaria solicitações apontando para o nada.
     */
    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public Projeto() {
    }

    /**
     * O banco tem DEFAULT NOW() como rede de segurança, mas quem insere aqui
     * é o JPA: sem isto ele mandaria NULL e bateria no NOT NULL da coluna.
     */
    @PrePersist
    void aoCriar() {
        criadoEm = LocalDateTime.now();
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getSolicitanteId() {
        return solicitanteId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public String getLinks() {
        return links;
    }

    public String getLinhasPesquisa() {
        return linhasPesquisa;
    }

    public String getProducoes() {
        return producoes;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setSolicitanteId(Long solicitanteId) {
        this.solicitanteId = solicitanteId;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setSubtitulo(String subtitulo) {
        this.subtitulo = subtitulo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public void setLinks(String links) {
        this.links = links;
    }

    public void setLinhasPesquisa(String linhasPesquisa) {
        this.linhasPesquisa = linhasPesquisa;
    }

    public void setProducoes(String producoes) {
        this.producoes = producoes;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    /** Só o id: o projeto não carrega dado pessoal, mas o padrão do projeto é não imprimir entidade em log. */
    @Override
    public String toString() {
        return "Projeto{id=" + id + "}";
    }
}
