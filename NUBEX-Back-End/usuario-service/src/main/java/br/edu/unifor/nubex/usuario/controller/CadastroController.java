package br.edu.unifor.nubex.usuario.controller;

import br.edu.unifor.nubex.usuario.dto.SolicitacaoAcessoRequisicao;
import br.edu.unifor.nubex.usuario.dto.SolicitacaoAcessoResposta;
import br.edu.unifor.nubex.usuario.service.SolicitacaoAcessoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cadastro de contas no portal.
 *
 * <p>O caminho segue o Apêndice B do documento de requisitos, que já especifica a
 * família de cadastro: {@code /participante}, {@code /confirmar-email},
 * {@code /solicitante} e {@code /solicitante-externo}. Por isso a classe se chama
 * Cadastro e não Solicitacao -- as outras três entram aqui.
 *
 * <p>Todas são rotas abertas, pelo motivo óbvio: quem está se cadastrando não tem
 * token. A liberação não está escrita aqui, e sim em
 * {@code nubex.security.public-routes}, no application.properties, para as rotas
 * públicas do serviço ficarem listadas num lugar só.
 */
@RestController
@RequestMapping("/api/cadastro")
public class CadastroController {

    private final SolicitacaoAcessoService solicitacaoAcessoService;

    public CadastroController(SolicitacaoAcessoService solicitacaoAcessoService) {
        this.solicitacaoAcessoService = solicitacaoAcessoService;
    }

    /**
     * POST /api/cadastro/solicitante
     *
     * <p>Registra o pedido de acesso como solicitante interno (RF052). A conta
     * nasce em AGUARDANDO_APROVACAO e o pedido fica aberto para o administrador
     * decidir no painel.
     *
     * <p>O {@code @Valid} aplica as regras do DTO antes de o método rodar: campo
     * faltando, e-mail mal formado, senha fora de 8 a 36, telefone com letra ou
     * Lattes com formato errado já voltam 400 com a lista de campos, sem chegar ao
     * banco. É o que o RNF013 pede ao mandar validar na API tudo que chega,
     * independentemente do que a interface já validou.
     *
     * <p>Respostas: 201 com o protocolo; 400 se algum campo não passar ou se o
     * vínculo não for docente nem pesquisador; 409 se o e-mail já tiver conta.
     */
    @PostMapping("/solicitante")
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitacaoAcessoResposta solicitarAcesso(
            @Valid @RequestBody SolicitacaoAcessoRequisicao requisicao) {
        return solicitacaoAcessoService.registrar(requisicao);
    }
}
