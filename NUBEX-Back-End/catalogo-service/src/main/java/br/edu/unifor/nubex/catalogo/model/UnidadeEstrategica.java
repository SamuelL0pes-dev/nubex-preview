package br.edu.unifor.nubex.catalogo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "unidade_estrategica")
public class UnidadeEstrategica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 80, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "imagem_url")
    private String imagemUrl;

    @Column(nullable = false)
    private boolean ativo = true;

    public UnidadeEstrategica() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
