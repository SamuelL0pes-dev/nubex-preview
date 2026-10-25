package br.edu.unifor.nubex.usuario.repository;

import br.edu.unifor.nubex.usuario.model.TipoToken;
import br.edu.unifor.nubex.usuario.model.TokenVerificacao;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Consultas dos tokens de confirmação de e-mail (RF057) e de redefinição
 * de senha (RF086).
 */
public interface TokenVerificacaoRepository extends JpaRepository<TokenVerificacao, Long> {

    /**
     * Validação do token recebido. O service calcula o SHA-256 do valor que
     * chegou e busca por ele: o token em claro nunca é comparado com o banco,
     * porque o banco não o tem.
     */
    Optional<TokenVerificacao> findByTokenHash(String tokenHash);

    /**
     * Token ainda não usado de um usuário. Serve para não emitir um segundo
     * código enquanto o primeiro vale, e para invalidar o anterior quando a
     * pessoa pede outro.
     */
    Optional<TokenVerificacao> findByUsuarioIdAndTipoAndUsadoEmIsNull(Long usuarioId, TipoToken tipo);

    /**
     * Limpeza dos vencidos. Token expirado não serve para nada e é dado ligado
     * a pessoa: guardar sem finalidade contraria o RNF010 (LGPD).
     * Quem chamar precisa estar numa transação.
     */
    long deleteByExpiraEmBefore(LocalDateTime limite);
}
