package br.edu.unifor.nubex.usuario.service;

import br.edu.unifor.nubex.common.config.ChavesRsa;
import br.edu.unifor.nubex.common.config.NubexJwtProperties;
import br.edu.unifor.nubex.common.exception.NegocioException;
import br.edu.unifor.nubex.usuario.model.Usuario;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

/**
 * Assina o token do usuário que acabou de entrar.
 *
 * <p>Esta classe só existe no usuario-service, e é essa a razão de a assinatura
 * ser assimétrica: a chave privada, que <em>produz</em> assinatura, mora aqui, no
 * único serviço que faz login. Os outros quatro recebem apenas a chave pública,
 * que <em>confere</em> e não produz. Se a configuração do catalogo-service vazar,
 * ninguém consegue fabricar um token de ADMINISTRADOR com ela.
 */
@Component
public class EmissorDeToken {

    private static final Logger log = LoggerFactory.getLogger(EmissorDeToken.class);

    private final JwtEncoder assinador;
    private final String emissor;
    private final Duration validade;

    public EmissorDeToken(NubexJwtProperties propriedades) {
        this.emissor = propriedades.emissor();
        this.validade = propriedades.validade();

        if (propriedades.temChavePrivada() && propriedades.temChavePublica()) {
            this.assinador = NimbusJwtEncoder
                    .withKeyPair(
                            ChavesRsa.publica(propriedades.chavePublica()),
                            ChavesRsa.privada(propriedades.chavePrivada()))
                    .algorithm(SignatureAlgorithm.RS256)
                    .build();
        } else {
            /*
             * Sem chave não há como assinar, mas a aplicação sobe: o
             * /actuator/health precisa responder, e o job do CI não define as
             * chaves. Quem tentar fazer login recebe 503, e não um 500 sem
             * explicação. É a mesma escolha de falhar fechado que o
             * ConferenciaDeTokenConfig faz do lado de quem confere.
             */
            this.assinador = null;
            log.error("nubex.jwt.chave-privada ou chave-publica não definidas: o login "
                    + "responderá 503. Defina JWT_CHAVE_PRIVADA e JWT_CHAVE_PUBLICA.");
        }
    }

    public long validadeEmSegundos() {
        return validade.toSeconds();
    }

    /**
     * Monta e assina o token.
     *
     * <p>O que vai escrito dentro dele:
     *
     * <ul>
     *   <li><b>iss</b>: quem emitiu. Todo serviço confere este campo, senão um
     *       token assinado pela mesma chave para outra finalidade seria aceito;
     *   <li><b>sub</b>: o id do usuário. É o que {@code authentication.getName()}
     *       devolve dentro de um controller;
     *   <li><b>exp</b>: o vencimento, duas horas à frente;
     *   <li><b>perfil</b> e <b>tipo</b>: viram as autoridades que o
     *       {@code @PreAuthorize} compara (RF073, RNF013);
     *   <li><b>nome</b>: para a tela escrever "Olá, Fulano" sem outra chamada.
     * </ul>
     *
     * <p>O que <b>não</b> vai: e-mail, cpf, matrícula, telefone. O token fica
     * guardado no navegador e viaja em toda requisição; quanto menos dado pessoal
     * nele, menor o estrago se ele vazar (RNF010).
     *
     * <p>O <b>status</b> também não vai, e isso é uma limitação conhecida: o
     * RF067 pede conferir perfil e status a cada solicitação. O perfil está aqui,
     * mas o status muda depois que o token foi emitido -- desativar alguém agora
     * não invalida o token dela, que vale por até duas horas. Fechar essa janela
     * é consultar este serviço a cada requisição, que é o cartão "Comunicação
     * entre serviços".
     */
    public String emitir(Usuario usuario) {
        if (assinador == null) {
            throw new NegocioException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Serviço de autenticação indisponível no momento");
        }

        Instant agora = Instant.now();
        JwtClaimsSet campos = JwtClaimsSet.builder()
                .issuer(emissor)
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(agora)
                .expiresAt(agora.plus(validade))
                .claim("perfil", usuario.getPerfil().name())
                .claim("tipo", usuario.getTipo().name())
                .claim("nome", usuario.getNome())
                .build();

        return assinador.encode(JwtEncoderParameters.from(campos)).getTokenValue();
    }
}
