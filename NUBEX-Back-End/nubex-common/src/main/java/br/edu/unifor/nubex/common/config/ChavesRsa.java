package br.edu.unifor.nubex.common.config;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Transforma o texto base64 das propriedades nubex.jwt em objetos de chave RSA
 * que o Spring Security entende.
 *
 * <p>Os dois formatos não são intercambiáveis, e é por isso que há dois métodos:
 * uma chave pública RSA vem no formato X.509 SubjectPublicKeyInfo e uma privada
 * no PKCS#8. São exatamente os formatos que o openssl gera com
 * {@code genpkey} e {@code rsa -pubout}.
 *
 * <p>Nenhuma mensagem de erro aqui inclui o conteúdo da chave: uma exceção de
 * subida da aplicação vai para o log, e chave em log é o que queremos evitar.
 */
public final class ChavesRsa {

    private ChavesRsa() {
    }

    /** Chave pública, formato X.509 SubjectPublicKeyInfo (openssl rsa -pubout). */
    public static RSAPublicKey publica(String texto) {
        try {
            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(semMolduraPem(texto)));
        } catch (Exception causa) {
            throw new IllegalStateException(
                    "nubex.jwt.chave-publica inválida: esperado o base64 de uma chave "
                            + "pública RSA no formato X.509 (openssl rsa -pubout)", causa);
        }
    }

    /** Chave privada, formato PKCS#8 (openssl genpkey). */
    public static RSAPrivateKey privada(String texto) {
        try {
            return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(semMolduraPem(texto)));
        } catch (Exception causa) {
            throw new IllegalStateException(
                    "nubex.jwt.chave-privada inválida: esperado o base64 de uma chave "
                            + "privada RSA no formato PKCS#8 (openssl genpkey)", causa);
        }
    }

    /**
     * Aceita tanto o base64 puro numa linha quanto o PEM inteiro colado, com as
     * linhas "-----BEGIN ...-----" e as quebras de linha. Sem isto, quem copiasse
     * o arquivo .pem direto receberia um erro de base64 difícil de entender.
     */
    private static byte[] semMolduraPem(String texto) {
        String limpo = texto
                .replaceAll("-----[A-Z ]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(limpo);
    }
}
