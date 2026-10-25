package br.edu.unifor.nubex.projeto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.edu.unifor.nubex")
public class ProjetoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjetoApplication.class, args);
    }
}
