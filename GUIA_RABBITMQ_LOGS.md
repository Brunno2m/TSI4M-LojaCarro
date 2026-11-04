# 🚗 Guia de Integração RabbitMQ - LojaCarro (IP: 15.228.215.36)

Este documento descreve a arquitetura e o funcionamento dos novos CRUDs desenvolvidos para a evolução da atividade solicitada pelo professor, com conexão ao broker RabbitMQ no IP **`15.228.215.36`** e credenciais **`aluno` / `12345`** na fila **`guilherme_brunno`**.

---

## 📁 1. Módulos Separados de Envio na Mensageria

Para permitir a **execução e teste de forma totalmente separada e independente**, o pacote **`br.org.edu.ifrn.LojaCarro.mensageria`** agora contém módulos dedicados para cada CRUD, cada um com sua própria classe e seu próprio método `main(String[] args)` executável:

| Classe / Módulo de Envio | Entidade | Métodos de Envio Isolado | Como Executar |
| :--- | :--- | :--- | :--- |
| **[`EnviarCarro.java`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/EnviarCarro.java)** | `CARRO` | `enviarCadastro`, `enviarAtualizacao`, `enviarExclusao` | Clique com o botão direito e selecione **Run 'EnviarCarro.main()'** |
| **[`EnviarVenda.java`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/EnviarVenda.java)** | `VENDAS` | `enviarCadastro`, `enviarAtualizacao`, `enviarExclusao` | Clique com o botão direito e selecione **Run 'EnviarVenda.main()'** |
| **[`EnviarContabil.java`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/EnviarContabil.java)** | `CONTABIL` | `enviarEntrada`, `enviarSaida`, `enviarExclusao` | Clique com o botão direito e selecione **Run 'EnviarContabil.main()'** |
| **[`EnviarTesteDrive.java`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/EnviarTesteDrive.java)** | `TESTE_DRIVE` | `enviarAgendamento`, `enviarStatus`, `enviarCancelamento` | Clique com o botão direito e selecione **Run 'EnviarTesteDrive.main()'** |
| **[`EnviarUsuario.java`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/EnviarUsuario.java)** | `USUARIO` | `enviarCadastro`, `enviarAtualizacao`, `enviarExclusao` | Clique com o botão direito e selecione **Run 'EnviarUsuario.main()'** |
| **[`Enviar.java`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/Enviar.java)** | *(Base / Todos)* | Executa uma bateria completa de testes disparando mensagens de todos os CRUDs | Clique com o botão direito e selecione **Run 'Enviar.main()'** |

---

## 📌 2. Visão Geral dos CRUDs e Regras de Negócio

| Módulo | Endpoint Base / Rota Web | Descrição e Regra de Negócio | Módulo de Mensageria Utilizado |
| :--- | :--- | :--- | :--- |
| **`ADM / Proprietário`** | `/adm` | **Apenas leitura de todas as outras entidades.** Exclusivo para o perfil `PROPRIETARIO`. Dashboard executivo consolidando Carros, Vendas, Contábil, Test Drives, Usuários e Logs. Não permite inserção, alteração ou exclusão. | Consulta centralizada dos logs e dados em tempo real. |
| **`Carro`** | `/estoque` e `/carro` | Gestão completa do catálogo e estoque de veículos (cadastro, atualização de preço, consulta e exclusão). | Publica eventos via [`EnviarCarro`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/EnviarCarro.java). |
| **`Vendas`** | `/vendas` | Registro de vendas de veículos. **Espelhamento Contábil e Comissões:** Gera `ENTRADA` no valor total da venda e lança `SAIDA` automática de comissão por cargo: **Vendedor (3%)**, **Gerente (2%)**, **Superusuário (1%)** e demais (0%). | Publica eventos detalhados com comissão via [`EnviarVenda`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/EnviarVenda.java) e evento contábil da comissão via [`EnviarContabil`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/EnviarContabil.java). |
| **`Contábil`** | `/contabil` | Controle de fluxo de caixa da loja com receitas (`ENTRADA`), despesas (`SAIDA`) e comissões automáticas (`COMISSAO`), cálculo automático de saldo e categorização. | Publica eventos via [`EnviarContabil`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/EnviarContabil.java). |
| **`Teste Drive`** | `/teste-drive` | Agendamento e acompanhamento de test drives de clientes em veículos do estoque. | Publica eventos via [`EnviarTesteDrive`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/EnviarTesteDrive.java). |
| **`Usuário`** | `/usuarios` e `/usuario` | Gerenciamento de equipe e permissões (Proprietário, Superusuário, Gerente, Vendedor, Contábil). | Publica eventos via [`EnviarUsuario`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/EnviarUsuario.java). |
| **`Logs / Mensageria`** | `/estoque/logs` | Consumidor que escuta a fila RabbitMQ e salva os logs auditados na tabela `log_operacao`. | Consome da fila `guilherme_brunno` via [`Receber.java`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/Receber.java). |

---

## 🔑 3. Credenciais Pré-configuradas (Senha Padrão: `12345`)

| Login | Perfil / Cargo | Permissões |
| :--- | :--- | :--- |
| **`proprietario`** | **`PROPRIETARIO`** | **Apenas Leitura Global (ADM):** Visualiza todas as tabelas e métricas da loja no painel `/adm`, sem permissão de alteração (conforme pedido do professor). |
| **`admin`** | **`SUPERUSUARIO`** | Acesso operacional completo (leitura e escrita em todos os CRUDs). |
| **`gerente`** | **`GERENTE`** | Gestão de carros, vendas, test drives e usuários subordinados. |
| **`vendedor`** | **`VENDEDOR`** | Registro de vendas, agendamento de test drives e consulta ao estoque. |
| **`contador`** | **`CONTABIL`** | Lançamentos contábeis, receitas e despesas. |

---

## 🐇 4. Configuração do RabbitMQ no IP `15.228.215.36`

As propriedades de conexão estão centralizadas em `application.properties`:

```properties
spring.rabbitmq.host=${RABBITMQ_HOST:15.228.215.36}
spring.rabbitmq.port=${RABBITMQ_PORT:5672}
spring.rabbitmq.username=${RABBITMQ_USERNAME:aluno}
spring.rabbitmq.password=${RABBITMQ_PASSWORD:12345}
rabbitmq.queue.log=guilherme_brunno
```

---

## 🚀 5. Como Executar os Envios Separadamente

Você pode testar cada módulo de envio isolado sem precisar rodar o Spring Boot:

1. Abra a classe do módulo desejado:
   - Para Carros: [`EnviarCarro.java`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/EnviarCarro.java)
   - Para Vendas: [`EnviarVenda.java`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/EnviarVenda.java)
   - Para Contábil: [`EnviarContabil.java`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/EnviarContabil.java)
   - Para Test Drive: [`EnviarTesteDrive.java`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/EnviarTesteDrive.java)
   - Para Usuários: [`EnviarUsuario.java`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/EnviarUsuario.java)
2. Execute o método `main(String[] args)` da classe escolhida.
3. No terminal aparecerá a mensagem sendo publicada no RabbitMQ (`15.228.215.36`).
4. O consumidor [`Receber.java`](file:///C:/Users/brunn_mexf451/Downloads/TSI4M-LojaCarro/src/main/java/br/org/edu/ifrn/LojaCarro/mensageria/Receber.java) capturará a mensagem e a persistirá na tabela `log_operacao`.
