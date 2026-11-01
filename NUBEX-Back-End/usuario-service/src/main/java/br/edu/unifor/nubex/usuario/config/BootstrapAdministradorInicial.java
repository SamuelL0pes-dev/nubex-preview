package br.edu.unifor.nubex.usuario.config;

import br.edu.unifor.nubex.usuario.model.PerfilUsuario;
import br.edu.unifor.nubex.usuario.model.StatusUsuario;
import br.edu.unifor.nubex.usuario.model.TipoUsuario;
import br.edu.unifor.nubex.usuario.model.Usuario;
import br.edu.unifor.nubex.usuario.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria a primeira conta de ADMINISTRADOR na subida da aplicação, a partir de
 * variáveis de ambiente (RF074, e a premissa da seção 2.4.3 do documento de
 * requisitos: "a equipe do NUBEX será cadastrada pela equipe de desenvolvimento
 * antes da publicação do portal").
 *
 * POR QUE NÃO É UMA MIGRAÇÃO FLYWAY
 *
 * Migração é arquivo .sql versionado. Criar o administrador ali exigiria o hash
 * BCrypt escrito dentro do arquivo, que iria para o repositório e ficaria no
 * histórico do git para sempre -- qualquer pessoa com acesso ao código, hoje ou
 * daqui a anos, teria o hash da senha da conta mais poderosa do sistema. E o
 * Flyway não lê variável de ambiente. Por isso o seed vive aqui.
 *
 * COMO ELE SE COMPORTA
 *
 * A regra que vale para todos os casos: NENHUM deles impede a aplicação de
 * subir. Isso não é detalhe de conforto -- exceção lançada dentro de um
 * ApplicationRunner aborta a inicialização, e aborta DEPOIS de o contexto estar
 * pronto, produzindo um erro de subida sem ligação aparente com o seed. Por isso
 * cada caminho ruim registra log e retorna, em vez de estourar.
 *
 *  - já existe algum ADMINISTRADOR  -> não faz nada, e isso o torna idempotente:
 *                                      reiniciar a aplicação mil vezes não cria
 *                                      uma segunda conta nem bate no índice
 *                                      único do e-mail;
 *  - não existe e falta variável    -> registra aviso e deixa a aplicação subir,
 *                                      porque em ambiente de teste e no CI não
 *                                      há administrador para criar;
 *  - não existe e a senha é fraca   -> registra erro e NÃO cria, para não nascer
 *                                      uma conta de acesso total com senha curta;
 *  - o e-mail não parece um e-mail  -> registra erro e NÃO cria;
 *  - o e-mail já é de outra conta   -> registra erro e NÃO cria. A guarda do topo
 *                                      pergunta só pelo perfil, então a pessoa
 *                                      pode já existir como SOLICITANTE;
 *  - duas instâncias ao mesmo tempo -> a que perder no índice único registra
 *                                      aviso e segue, sem criar nada;
 *  - não existe e está tudo certo   -> cria com perfil ADMINISTRADOR, tipo
 *                                      INTERNO e status ATIVO.
 *
 * SOBRE O NOME DAS PROPRIEDADES
 *
 * A senha fica em "nubex.admin.senha", em português, para seguir o padrão do
 * projeto. Atenção: o Spring Boot mascara automaticamente propriedades cujo
 * nome contém "password", "secret", "token" ou "key" quando elas aparecem em
 * endpoint de diagnóstico -- e "senha" não está nessa lista. Hoje isso não é
 * problema, porque o nubex-common expõe apenas o /actuator/health. Se alguém
 * algum dia habilitar o /actuator/env ou o /actuator/configprops, esta senha
 * aparecerá em texto puro na resposta. Não habilite.
 */
@Component
public class BootstrapAdministradorInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdministradorInicial.class);

    /**
     * De 8 a 36 caracteres. Decisão de equipe, tomada antes deste PR: comprimento
     * protege mais que composição forçada.
     *
     * Esta é hoje a ÚNICA definição dessa regra no projeto -- o cadastro (RF054,
     * RF056) ainda não existe. Quando ele nascer, deve usar estes mesmos dois
     * números, e o lugar natural para eles passa a ser o nubex-common, junto do
     * PasswordEncoder, para não existirem duas fontes de verdade.
     */
    private static final int SENHA_MINIMA = 8;
    private static final int SENHA_MAXIMA = 36;

    private static final String NOME_PADRAO = "Administrador NUBEX";

    /**
     * Checagem mínima, só para barrar engano na variável de ambiente: sem espaço,
     * uma arroba e um ponto no domínio. A validação de verdade do e-mail é do
     * cadastro (RF056), com @Email no DTO -- aqui não há formulário nem usuário
     * para corrigir o valor, só uma variável que alguém digitou no .env.
     */
    private static final String FORMATO_EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String senha;
    private final String nome;

    public BootstrapAdministradorInicial(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${nubex.admin.email:}") String email,
            @Value("${nubex.admin.senha:}") String senha,
            @Value("${nubex.admin.nome:}") String nome) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;

        /*
         * O e-mail passa por trim porque espaço no fim passaria batido: o índice
         * único é sobre LOWER(email), que normaliza caixa e NÃO espaço. Sem isto,
         * "admin@unifor.br " e "admin@unifor.br" conviveriam como duas contas, e o
         * login por e-mail não acharia a primeira.
         *
         * A senha NÃO passa por trim, de propósito: cortar espaço de uma senha
         * mudaria a credencial em silêncio, e quem definiu a variável não
         * conseguiria entrar com o que digitou. O run() avisa, em vez de corrigir.
         *
         * O nome é tratado aqui, e não no valor padrão do @Value, porque o compose
         * repassa a variável com "${NUBEX_ADMIN_NOME:-}": isso define a
         * propriedade como string vazia, e não como ausente, então o padrão do
         * @Value nunca entraria em ação.
         */
        this.email = email.trim();
        this.senha = senha;
        this.nome = nome.isBlank() ? NOME_PADRAO : nome.trim();
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByPerfil(PerfilUsuario.ADMINISTRADOR)) {
            // info, e não debug: em produção o nível é INFO, e sem esta linha a
            // subida normal não deixaria rastro de que o seed rodou e decidiu
            // não criar nada.
            log.info("Já existe administrador cadastrado; o seed não criou nenhum.");
            return;
        }

        if (email.isBlank() || senha.isBlank()) {
            log.warn("Nenhum administrador cadastrado e as variáveis NUBEX_ADMIN_EMAIL e "
                    + "NUBEX_ADMIN_SENHA não estão definidas. Sem administrador, ninguém "
                    + "aprova pedido de acesso de solicitante (RF052).");
            return;
        }

        if (senha.length() < SENHA_MINIMA || senha.length() > SENHA_MAXIMA) {
            log.error("A senha em NUBEX_ADMIN_SENHA precisa ter de {} a {} caracteres. "
                            + "O administrador inicial NÃO foi criado.",
                    SENHA_MINIMA, SENHA_MAXIMA);
            return;
        }

        if (!senha.equals(senha.trim())) {
            // Avisa em vez de cortar: cortar mudaria a senha em silêncio. Sem o
            // aviso, a conta nasceria com uma senha que ninguém consegue digitar.
            log.warn("A senha em NUBEX_ADMIN_SENHA começa ou termina com espaço, e o "
                    + "espaço faz parte dela. Se não foi intencional, corrija a variável.");
        }

        if (!email.matches(FORMATO_EMAIL)) {
            log.error("O valor de NUBEX_ADMIN_EMAIL não parece um e-mail. O administrador "
                    + "inicial NÃO foi criado.");
            return;
        }

        /*
         * O e-mail pode já pertencer a outra conta -- por exemplo, a pessoa já
         * estar cadastrada como SOLICITANTE. A guarda do topo pergunta só pelo
         * perfil, então nesse caso o seed chegaria até aqui e o save() violaria o
         * índice único uq_usuario_email. E exceção em ApplicationRunner aborta a
         * inicialização DEPOIS de o contexto estar pronto: o serviço não subiria,
         * com um erro de inicialização sem ligação aparente com o seed.
         */
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            log.error("O e-mail de NUBEX_ADMIN_EMAIL já pertence a outra conta. O "
                    + "administrador inicial NÃO foi criado. Use outro e-mail, ou promova "
                    + "a conta existente a ADMINISTRADOR pelo painel.");
            return;
        }

        Usuario administrador = new Usuario();
        administrador.setNome(nome);
        administrador.setEmail(email);
        administrador.setSenhaHash(passwordEncoder.encode(senha));
        administrador.setPerfil(PerfilUsuario.ADMINISTRADOR);
        administrador.setTipo(TipoUsuario.INTERNO);
        administrador.setStatus(StatusUsuario.ATIVO);

        try {
            usuarioRepository.save(administrador);
        } catch (DataIntegrityViolationException colisao) {
            /*
             * Rede de segurança para a corrida entre duas instâncias: as duas
             * passam pelas guardas acima no mesmo instante, as duas salvam, e uma
             * perde no índice único. Sem este catch, a instância perdedora não
             * sobe. Com ele, ela apenas não cria nada -- que é o resultado certo,
             * porque a outra já criou.
             */
            log.warn("Outra instância criou o administrador inicial ao mesmo tempo; esta "
                    + "não criou nada. A aplicação segue normalmente.");
            return;
        }

        // O e-mail não entra no log de propósito: é dado pessoal, e o projeto
        // não registra dado pessoal em log (o StackTraceSanitizado do
        // nubex-common existe pela mesma razão).
        log.info("Administrador inicial criado. Troque a senha após o primeiro acesso: "
                + "o valor de NUBEX_ADMIN_SENHA tende a circular entre a equipe.");
    }
}
