package br.edu.unifor.nubex.common.exception;

import br.edu.unifor.nubex.common.util.StackTraceSanitizado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Tratamento global de erros (RNF013/RNF018). Qualquer exceção que escapar de um
 * controller cai aqui e vira um ApiError. Assim o cliente nunca recebe stack trace,
 * nome de classe, SQL ou outro detalhe interno.
 *
 * Os erros 401/403 que acontecem ANTES do controller (no filtro de segurança) são
 * tratados em ApiAuthenticationEntryPoint e ApiAccessDeniedHandler.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---- Erros esperados (culpa do cliente ou regra de negócio) ----

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ApiError> tratarNegocio(NegocioException ex, HttpServletRequest req) {
        return montar(ex.getStatus(), ex.getMessage(), req);
    }

    /** Falha de @Valid no corpo da requisição: devolve a lista de campos inválidos. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> tratarValidacaoCorpo(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiError.CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ApiError.CampoInvalido(e.getField(), e.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "Dados inválidos", req.getRequestURI())
                        .comCampos(campos));
    }

    /** Falha de validação em parâmetro de URL ou de método (@Validated). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> tratarValidacaoParametro(ConstraintViolationException ex, HttpServletRequest req) {
        List<ApiError.CampoInvalido> campos = ex.getConstraintViolations().stream()
                .map(v -> new ApiError.CampoInvalido(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "Dados inválidos", req.getRequestURI())
                        .comCampos(campos));
    }

    /** Validação de @RequestParam/@PathVariable (@Min, @NotBlank etc.): devolve a lista de campos, como no corpo. */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> tratarValidacaoMetodo(HandlerMethodValidationException ex, HttpServletRequest req) {
        List<ApiError.CampoInvalido> campos = ex.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream()
                        .map(erro -> new ApiError.CampoInvalido(
                                resultado.getMethodParameter().getParameterName(),
                                erro.getDefaultMessage())))
                .toList();
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "Dados inválidos", req.getRequestURI())
                        .comCampos(campos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> tratarCorpoIlegivel(HttpMessageNotReadableException ex, HttpServletRequest req) {
        // Não repete o conteúdo recebido: o corpo pode ter senha ou outro dado sensível.
        return montar(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou mal formatado", req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> tratarTipoParametro(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + ex.getName() + "'", req);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> tratarParametroAusente(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente: " + ex.getParameterName(), req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> tratarMetodoNaoPermitido(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return montar(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não permitido nesta rota", req);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> tratarTipoConteudo(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return montar(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de conteúdo não suportado", req);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> tratarRotaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, "Recurso não encontrado", req);
    }

    // ---- Segurança lançada dentro do controller (ex.: @PreAuthorize) ----

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> tratarAcessoNegado(AccessDeniedException ex, HttpServletRequest req) {
        return montar(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> tratarNaoAutenticado(AuthenticationException ex, HttpServletRequest req) {
        return montar(HttpStatus.UNAUTHORIZED, "Autenticação necessária ou inválida", req);
    }

    // ---- Qualquer outro erro: bug nosso ----

    /**
     * O cliente recebe só uma mensagem genérica e um código. No log vai o mesmo
     * código e o stack trace SEM as mensagens das exceções (StackTraceSanitizado):
     * a equipe vê onde o erro aconteceu, mas valores como e-mail ou CPF, que
     * exceções de banco costumam trazer no texto, não vão para o log. A exceção
     * completa, com mensagem, só aparece em DEBUG (perfil dev).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> tratarInesperado(Exception ex, HttpServletRequest req) {
        String codigo = UUID.randomUUID().toString();
        log.error("[{}] Erro inesperado em {} {}\n{}",
                codigo, req.getMethod(), req.getRequestURI(), StackTraceSanitizado.de(ex));
        log.debug("[{}] Detalhes do erro", codigo, ex);

        ApiError corpo = ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erro interno. Informe o código ao suporte.", req.getRequestURI())
                .comCodigo(codigo);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(corpo);
    }

    private ResponseEntity<ApiError> montar(HttpStatus status, String mensagem, HttpServletRequest req) {
        return ResponseEntity.status(status).body(ApiError.of(status, mensagem, req.getRequestURI()));
    }
}
