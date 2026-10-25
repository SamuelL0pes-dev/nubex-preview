package br.edu.unifor.nubex.usuario.repository;

import br.edu.unifor.nubex.usuario.model.PerfilUsuario;
import br.edu.unifor.nubex.usuario.model.StatusUsuario;
import br.edu.unifor.nubex.usuario.model.Usuario;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Consultas do domínio usuario. Cada método existe por causa de um requisito;
 * o comentário diz qual, para não sobrar consulta sem uso no serviço.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // ---- Login (RF086): e-mail para todos, matrícula para o discente ----

    /**
     * O IgnoreCase combina com o índice único sobre LOWER(email) da migração:
     * a mesma regra no Java e no banco, senão o login fica inconsistente com
     * o que o cadastro aceitou.
     */
    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByMatricula(String matricula);

    // ---- Cadastro: checar antes de gravar ----

    /*
     * O service chama estes antes de inserir e lança ConflitoException (409),
     * que o nubex-common converte em JSON com mensagem amigável. Sem isso, o
     * usuário receberia o erro cru de violação de índice único.
     *
     * As quatro colunas têm índice único no banco, que continua sendo a
     * garantia final em caso de duas requisições simultâneas.
     */
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByMatricula(String matricula);

    boolean existsByCpf(String cpf);

    boolean existsByCnpj(String cnpj);

    // ---- Painel administrativo (RF074) ----

    /** Lista por perfil e status, paginada: a lista de usuários cresce sem limite. */
    Page<Usuario> findByPerfilAndStatus(PerfilUsuario perfil, StatusUsuario status, Pageable pagina);

    Page<Usuario> findByStatus(StatusUsuario status, Pageable pagina);

    // ---- Lista de inclusão em agendamento (RF043) e em projeto (RF068) ----

    /**
     * Quem o solicitante interno pode incluir num agendamento ou na equipe de
     * um projeto. Chamada com [PARTICIPANTE, SOLICITANTE] e ATIVO.
     *
     * DIVERGE do RF043, que restringia a lista aos "participantes ativos
     * aprovados por ele": a equipe decidiu que o interno escolhe entre todos
     * os participantes e solicitantes ativos. O filtro de status continua
     * aqui, na consulta, porque o RF067 exige conferir perfil e status na API
     * a cada solicitação -- quem venceu o prazo deixa de aparecer sozinho.
     *
     * A rota que usa isto deve devolver apenas nome e vínculo de cada pessoa,
     * nunca e-mail, cpf ou telefone: o RNF010 manda tratar só o dado
     * necessário à finalidade, e a finalidade aqui é escolher um nome.
     */
    List<Usuario> findByPerfilInAndStatus(
            Collection<PerfilUsuario> perfis, StatusUsuario status);

    // ---- Gestão de quem eu aprovei (RF065, RF066) ----

    /**
     * As pessoas que este solicitante aprovou, para ele renovar o prazo
     * (RF065) ou desligar (RF066) -- os dois requisitos falam de
     * "vinculados a ele", e o vínculo é o solicitante_id.
     *
     * Não serve mais para montar a lista de inclusão, que passou a ser aberta.
     */
    List<Usuario> findBySolicitanteIdAndPerfilAndStatus(
            Long solicitanteId, PerfilUsuario perfil, StatusUsuario status);

    // ---- Rotinas de prazo de acesso ----

    /** Contas ativas cujo prazo venceu: passam a INATIVO (RF063). */
    List<Usuario> findByStatusAndExpiraEmBefore(StatusUsuario status, LocalDateTime limite);

    /** Contas que vencem numa janela, para o aviso de 15 dias antes (RF064). */
    List<Usuario> findByStatusAndExpiraEmBetween(
            StatusUsuario status, LocalDateTime inicio, LocalDateTime fim);
}
