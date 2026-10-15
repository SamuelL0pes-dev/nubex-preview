package br.edu.unifor.nubex.conteudo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.edu.unifor.nubex")
public class ConteudoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConteudoApplication.class, args);
    }
}
