package br.com.brainstorming.cliente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * A aplicação da solução do cliente: a Athena inteira (pacote br.com.brainstorming.athena) mais o que é só deste
 * cliente (este pacote — renomeie para o do cliente, ex.: br.com.brainstorming.cliente.acme).
 */
@SpringBootApplication(scanBasePackages = {"br.com.brainstorming.athena", "br.com.brainstorming.cliente"})
public class SolucaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SolucaoApplication.class, args);
    }
}
