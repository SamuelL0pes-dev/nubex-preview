package br.edu.unifor.nubex.usuario.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Token de uso único e com prazo, usado em dois fluxos:
 * confirmar o e-mail no cadastro (RF057) e redefinir a senha (RF086).
 *
 * SEGURANÇA: aqui fica apenas o HASH SHA-256 do token. O valor em claro é
 * gerado, enviado por e-mail e descartado — não existe coluna para ele. Se o
 * banco vazar, ninguém consegue redefinir a senha de ninguém.
 *
 * Por que SHA-256 e não BCrypt, se a senha usa BCrypt? Porque a validação
 * precisa ENCONTRAR o registro a partir do token recebido. O BCrypt gera um
 * sal diferente a cada chamada, então o mesmo token produziria hashes
 * diferentes e a busca seria impossível. O BCrypt existe para proteger
 * segredo de baixa entropia (senha que a pessoa escolheu, sujeita a ataque de
 * dicionário); o token aqui é aleatório e longo, e para ele o SHA-256 basta.
 */
@Entity
@Table(name = "token_verificacao")
public class TokenVerificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoToken tipo;

    /** Hash SHA-256 em hexadecimal: 64 caracteres. Nunca o token em claro. */
    @JsonIgnore
    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    /** Preenchido no momento do uso. Com valor, o token não vale mais. */
    @Column(name = "usado_em")
    private LocalDateTime usadoEm;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public TokenVerificacao() {
    }

    @PrePersist
    void aoCriar() {
        criadoEm = LocalDateTime.now();
    }

    /**
     * Vale só se ainda não foi usado E não venceu. As duas condições juntas
     * num método só para nenhum service esquecer uma delas (RF086).
     */
    public boolean estaValido() {
        return usadoEm == null && expiraEm.isAfter(LocalDateTime.now());
    }

    /** Marca o uso único. Depois disto, estaValido() devolve false para sempre. */
    public void marcarUsado() {
        usadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public LocalDateTime getUsadoEm() {
        return usadoEm;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setTipo(TipoToken tipo) {
        this.tipo = tipo;
    }

    /** Recebe o hash SHA-256, já calculado. Nunca o token em claro. */
    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public void setExpiraEm(LocalDateTime expiraEm) {
        this.expiraEm = expiraEm;
    }

    /** Só o id: o hash do token jamais vai para log. */
    @Override
    public String toString() {
        return "TokenVerificacao{id=" + id + "}";
    }
}
