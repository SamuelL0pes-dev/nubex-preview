package br.edu.unifor.nubex.usuario.dto;

import br.edu.unifor.nubex.usuario.model.VinculoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Corpo do POST /api/cadastro/solicitante: o pedido de acesso como solicitante
 * interno (RF052).
 *
 * <h4>O que NÃO está aqui, e é o ponto mais importante desta classe</h4>
 *
 * Não existe campo <b>perfil</b> nem <b>tipo</b>. A tela do portal envia os dois
 * hoje, e eles são simplesmente descartados: o Spring Boot ignora propriedade
 * desconhecida no JSON por padrão.
 *
 * <p>Isso é deliberado e é segurança, não limpeza. Se o perfil viesse do cliente,
 * qualquer pessoa mandaria {@code "perfil": "ADMINISTRADOR"} num curl e criaria
 * uma conta de acesso total -- sem login, sem aprovação, sem nada. O perfil aqui é
 * consequência da rota, decidido no servidor: esta é a rota de "solicitar acesso
 * como solicitante interno", então é SOLICITANTE e INTERNO, sempre.
 *
 * <p>Não declarar o campo é melhor que declarar e ignorar: quem lê a classe vê que
 * o servidor nunca teve onde guardar essa escolha.
 */
public record SolicitacaoAcessoRequisicao(

        @NotBlank(message = "Informe o nome completo")
        @Size(max = 150, message = "Nome muito longo")
        String nome,

        @NotBlank(message = "Informe o e-mail")
        @Email(message = "E-mail inválido")
        @Size(max = 150, message = "E-mail muito longo")
        String email,

        /*
         * De 8 a 36 caracteres, decisão de equipe. O limite superior é restrição
         * nossa, não do BCrypt, que corta em 72 bytes.
         *
         * A mesma regra está no BootstrapAdministradorInicial, e isso é duas
         * fontes de verdade: quando as duas se juntarem, as constantes vão para o
         * nubex-common, junto do PasswordEncoder. Levantado na revisão do PR #12.
         */
        @NotBlank(message = "Informe a senha")
        @Size(min = 8, max = 36, message = "A senha precisa ter de 8 a 36 caracteres")
        String senha,

        /** Só dígitos: a formatação é da interface (Apêndice A do documento). */
        @NotBlank(message = "Informe o telefone")
        @Pattern(regexp = "^[0-9]{10,11}$",
                message = "Telefone deve ter 10 ou 11 dígitos, somente números")
        String telefone,

        /**
         * O RF052 é para "docente ou pesquisador da Unifor". O enum tem seis
         * valores, porque serve também ao cadastro de participante -- então quais
         * valem NESTA rota é regra de negócio, conferida no service.
         */
        @NotNull(message = "Informe o vínculo")
        VinculoUsuario vinculo,

        @NotBlank(message = "Informe o endereço do currículo Lattes")
        @Pattern(regexp = LATTES,
                message = "Endereço do Lattes inválido. Exemplo: http://lattes.cnpq.br/0000000000000000")
        String lattesUrl,

        /**
         * Opcional (RF091). O padrão aceita vazio além de nulo, porque formulário
         * que não preenche manda string vazia tão facilmente quanto manda null, e
         * recusar por isso seria um 400 que o usuário não entende.
         */
        @Pattern(regexp = "^$|" + ORCID,
                message = "Endereço do ORCID inválido. Exemplo: https://orcid.org/0000-0000-0000-0000")
        String orcidUrl) {

    /** Lattes: o identificador tem exatamente 16 dígitos. */
    static final String LATTES = "^https?://lattes\\.cnpq\\.br/[0-9]{16}$";

    /** ORCID: quatro grupos de quatro, e o último dígito pode ser X. */
    static final String ORCID = "^https?://orcid\\.org/[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{3}[0-9X]$";

    /**
     * Sem isto, o toString() que o record gera sozinho imprimiria a senha em
     * texto, e um log de erro de validação citando o objeto a levaria para o
     * arquivo. O e-mail também fica fora: é dado pessoal (RNF010, RNF019).
     */
    @Override
    public String toString() {
        return "SolicitacaoAcessoRequisicao[vinculo=" + vinculo
                + ", nome=informado, email=omitido, senha=omitida]";
    }
}
