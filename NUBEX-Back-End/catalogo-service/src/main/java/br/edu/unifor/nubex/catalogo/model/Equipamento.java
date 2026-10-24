package br.edu.unifor.nubex.catalogo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "equipamento")
public class Equipamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "laboratorio_id", nullable = false)
    private Laboratorio laboratorio;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEquipamento status = StatusEquipamento.DISPONIVEL;

    @Column(nullable = false)
    private boolean ativo = true;

    public Equipamento() {
    }

    public Long getId() {
        return id;
    }

    public Laboratorio getLaboratorio() {
        return laboratorio;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public StatusEquipamento getStatus() {
        return status;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setLaboratorio(Laboratorio laboratorio) {
        this.laboratorio = laboratorio;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setStatus(StatusEquipamento status) {
        this.status = status;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
