package br.org.edu.ifrn.LojaCarro.mensageria;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Mensageria Application:
 * Inicializador da mensageria RabbitMQ.
 * Ao iniciar o Spring Boot, inicia a escuta assíncrona de Receber.lerMensagem()
 * em uma thread de background conectada ao IP 15.228.215.36.
 */
@Component
public class MensageriaApplication implements CommandLineRunner {

    public static void main(String[] args) {
        System.out.println("Iniciando Módulo de Mensageria (Receber) no IP 15.228.215.36...");
        try {
            Receber.lerMensagem();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(String... args) {
        // Inicia o consumidor RabbitMQ em uma thread separada para não bloquear a inicialização da aplicação web
        Thread consumerThread = new Thread(() -> {
            try {
                System.out.println("Iniciando escuta do RabbitMQ no IP 15.228.215.36 via MensageriaApplication...");
                Receber.lerMensagem();
            } catch (Exception e) {
                System.err.println("Erro ao iniciar Receber.lerMensagem(): " + e.getMessage());
            }
        });
        consumerThread.setName("RabbitMQ-Receber-Thread");
        consumerThread.setDaemon(true);
        consumerThread.start();
    }
}
