package br.edu.unifor.nubex.conteudo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contato")
public class Contato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 150)
    private String instituicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private SegmentoContato segmento;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private boolean lido = false;

    public Contato() {
    }

    @PrePersist
    void aoCriar(){
        if (criadoEm == null){
            criadoEm = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getInstituicao() {
        return instituicao;
    }

    public SegmentoContato getSegmento() {
        return segmento;
    }

    public String getMensagem() {
        return mensagem;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public boolean isLido() {
        return lido;
    }

    public void setName(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setInstituicao(String instituicao) {
        this.instituicao = instituicao;
    }

    public void setSegmento(SegmentoContato segmento) {
        this.segmento = segmento;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public void setLido(boolean lido) {
        this.lido = lido;
    }
}
