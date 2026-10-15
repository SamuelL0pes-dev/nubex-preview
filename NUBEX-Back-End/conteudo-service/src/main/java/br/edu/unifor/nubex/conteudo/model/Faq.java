package br.edu.unifor.nubex.conteudo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "faq")
public class Faq {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, length = 80)
    private String categoria;

    @Column(nullable = false)
    private String pergunta;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resposta;

    @Column(nullable = false)
    private int ordem = 0;

    @Column(nullable = false)
    private boolean ativo = true;

    public Faq() {
    }

    public Long getId() {
        return id;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getPergunta() {
        return pergunta;
    }

    public String getResposta() {
        return resposta;
    }

    public int getOrdem() {
        return ordem;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setPergunta(String pergunta) {
        this.pergunta = pergunta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
