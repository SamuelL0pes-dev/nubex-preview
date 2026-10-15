package br.edu.unifor.nubex.conteudo.model;


import jakarta.persistence.*;

@Entity
@Table(name = "pesquisador")
public class Pesquisador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 120)
    private String cargo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private SecaoPesquisador secao;

    @Column(name = "linha_pesquisa")
    private String linhaPesquisa;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "lattes_url")
    private String lattesUrl;

    @Column(name = "orcid_url")
    private String orcidUrl;

    @Column(name = "google_academico_url")
    private String googleAcademicoUrl;

    @Column(nullable = false)
    private boolean ativo = true;

    public Pesquisador() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCargo() {
        return cargo;
    }

    public SecaoPesquisador getSecao() {
        return secao;
    }

    public String getLinhaPesquisa() {
        return linhaPesquisa;
    }

    public String getBio() {
        return bio;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public String getLattesUrl() {
        return lattesUrl;
    }

    public String getOrcidUrl() {
        return orcidUrl;
    }

    public String getGoogleAcademicoUrl() {
        return googleAcademicoUrl;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public void setSecao(SecaoPesquisador secao) {
        this.secao = secao;
    }

    public void setLinhaPesquisa(String linhaPesquisa) {
        this.linhaPesquisa = linhaPesquisa;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public void setLattesUrl(String lattesUrl) {
        this.lattesUrl = lattesUrl;
    }

    public void setOrcidUrl(String orcidUrl) {
        this.orcidUrl = orcidUrl;
    }

    public void setGoogleAcademicoUrl(String googleAcademicoUrl) {
        this.googleAcademicoUrl = googleAcademicoUrl;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
