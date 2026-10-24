package br.edu.unifor.nubex.catalogo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "servico")
public class Servico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidade_id", nullable = false)
    private UnidadeEstrategica unidade;

    @Column(nullable = false, length = 100)
    private String categoria;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String tecnicas;

    @Column(columnDefinition = "TEXT")
    private String aplicacoes;

    @Column(nullable = false)
    private boolean ativo = true;

    public Servico() {
    }

    public Long getId() {
        return id;
    }

    public UnidadeEstrategica getUnidade() {
        return unidade;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getTecnicas() {
        return tecnicas;
    }

    public String getAplicacoes() {
        return aplicacoes;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setUnidade(UnidadeEstrategica unidade) {
        this.unidade = unidade;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setTecnicas(String tecnicas) {
        this.tecnicas = tecnicas;
    }

    public void setAplicacoes(String aplicacoes) {
        this.aplicacoes = aplicacoes;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
