package br.edu.unifor.nubex.usuario.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Conta de acesso do Portal NUBEX. É ao mesmo tempo a identidade de login e o
 * registro da pessoa nos outros domínios: o agendamento-service e o
 * projeto-service guardam apenas o id desta linha, num BIGINT solto, porque
 * estão em bancos diferentes (regra de ouro).
 *
 * Três dimensões independentes descrevem o usuário:
 *  - {@link PerfilUsuario} - o que ele pode fazer na API;
 *  - {@link TipoUsuario}   - de onde ele vem (dentro ou fora da Unifor);
 *  - {@link VinculoUsuario}- o que ele é na universidade.
 *
 * O login aceita dois identificadores, os dois únicos no banco: e-mail
 * (todos) e matrícula (discente). Ver RF086.
 *
 * A senha existe aqui SÓ como hash BCrypt, em senhaHash. Não há campo de
 * senha em texto em lugar nenhum da entidade.
 */
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 150)
    private String email;

    /**
     * Hash BCrypt da senha. O @JsonIgnore é uma segunda trava: mesmo que alguém
     * devolva a entidade direto de um controller por engano, o hash não vai no
     * JSON. A primeira trava é não expor a entidade (usar DTO de resposta).
     */
    @JsonIgnore
    @Column(name = "senha_hash", nullable = false, length = 72)
    private String senhaHash;

    /** Só dígitos, 10 ou 11. A máscara "(XX) XXXXX-XXXX" do RF056 é da entrada e da exibição. */
    @Column(length = 11)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PerfilUsuario perfil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoUsuario tipo;

    /** Nasce PENDENTE_EMAIL: sem confirmar o e-mail, o cadastro não anda (RF057). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private StatusUsuario status = StatusUsuario.PENDENTE_EMAIL;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VinculoUsuario vinculo;

    // ---- Discente da Unifor (RF054) ----

    @Column(length = 30)
    private String matricula;

    @Column(length = 120)
    private String curso;

    // ---- Quem vem de fora (RF055): pessoa física usa cpf, jurídica usa cnpj ----

    /** Só dígitos, 11. Os dígitos verificadores são validados na API (RF056). */
    @Column(length = 11)
    private String cpf;

    @Column(length = 150)
    private String instituicao;

    @Column(name = "razao_social", length = 150)
    private String razaoSocial;

    /** Só dígitos, 14. Os dígitos verificadores são validados na API (RF056). */
    @Column(length = 14)
    private String cnpj;

    // ---- Identificação acadêmica (RF091) ----

    @Column(name = "lattes_url", length = 255)
    private String lattesUrl;

    @Column(name = "orcid_url", length = 255)
    private String orcidUrl;

    @Column(name = "google_academico_url", length = 255)
    private String googleAcademicoUrl;

    /**
     * Solicitante interno que aprovou esta conta (RF060). Nulo em quem não
     * passa por aprovação de solicitante, como os perfis administrativos.
     *
     * É @ManyToOne de verdade, e não um Long solto, porque é auto-referência:
     * mesmo banco e mesmo serviço, então a chave estrangeira existe.
     * LAZY para não puxar o aprovador em toda leitura de usuário.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id")
    private Usuario solicitante;

    @Column(name = "aprovado_em")
    private LocalDateTime aprovadoEm;

    /** Fim do prazo de acesso (RF063). Vencido, a conta passa a INATIVO. */
    @Column(name = "expira_em")
    private LocalDateTime expiraEm;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public Usuario() {
    }

    /**
     * O banco tem DEFAULT NOW() como rede de segurança, mas quem insere aqui é
     * o JPA: sem isto ele mandaria NULL e bateria no NOT NULL da coluna.
     */
    @PrePersist
    void aoCriar() {
        criadoEm = LocalDateTime.now();
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    /**
     * Único status que entra no portal (RF067). Deixar a regra aqui evita que
     * cada service repita a comparação e erre em um dos lugares.
     */
    public boolean estaAtivo() {
        return status == StatusUsuario.ATIVO;
    }

    /** Só o solicitante INTERNO aprova cadastro de outros usuários (RF055, RF090). */
    public boolean podeAprovarCadastros() {
        return perfil == PerfilUsuario.SOLICITANTE && tipo == TipoUsuario.INTERNO;
    }

    // ---- Leitura ----

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    /** Devolve o HASH, não a senha. Usado só pelo login, para comparar. */
    public String getSenhaHash() {
        return senhaHash;
    }

    public String getTelefone() {
        return telefone;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public StatusUsuario getStatus() {
        return status;
    }

    public VinculoUsuario getVinculo() {
        return vinculo;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getCurso() {
        return curso;
    }

    public String getCpf() {
        return cpf;
    }

    public String getInstituicao() {
        return instituicao;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
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

    public Usuario getSolicitante() {
        return solicitante;
    }

    public LocalDateTime getAprovadoEm() {
        return aprovadoEm;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    // ---- Escrita ----

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Recebe um valor JÁ codificado com o PasswordEncoder do nubex-common.
     * Nunca passe senha em texto por aqui.
     */
    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setPerfil(PerfilUsuario perfil) {
        this.perfil = perfil;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    public void setStatus(StatusUsuario status) {
        this.status = status;
    }

    public void setVinculo(VinculoUsuario vinculo) {
        this.vinculo = vinculo;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public void setInstituicao(String instituicao) {
        this.instituicao = instituicao;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
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

    public void setSolicitante(Usuario solicitante) {
        this.solicitante = solicitante;
    }

    public void setAprovadoEm(LocalDateTime aprovadoEm) {
        this.aprovadoEm = aprovadoEm;
    }

    public void setExpiraEm(LocalDateTime expiraEm) {
        this.expiraEm = expiraEm;
    }

    /**
     * Só o id, de propósito. Se um log imprimir a entidade, não sai hash, CPF,
     * e-mail nem telefone (RNF010 e a regra de nada sensível em log).
     */
    @Override
    public String toString() {
        return "Usuario{id=" + id + "}";
    }
}
