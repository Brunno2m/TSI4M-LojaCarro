
package br.org.edu.ifrn.LojaCarro.mensageria;

/**
 * Ponto de entrada alternativo da mensageria.
 *
 * A inicializacao do consumidor e gerenciada
 * pela classe Receber junto ao Spring Boot.
 */
public class MensageriaApplication {

    public static void main(String[] args) {

        Receber.main(args);

    }
}
