package br.edu.unifor.nubex.usuario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.edu.unifor.nubex")
public class UsuarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsuarioApplication.class, args);
    }
}
