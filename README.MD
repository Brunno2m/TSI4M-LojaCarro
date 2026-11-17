# LojaCarro — Trabalho Final

Sistema de gerenciamento de uma loja de veículos, desenvolvido para a disciplina de Desenvolvimento Web na Nuvem do IFRN.

**Branch de desenvolvimento:** `TrabalhoFinal`  
**Etapa atual:** Semana 1 — estruturação e implementação do backend  
**Última atualização deste documento:** 17/11/2026

## Sobre o projeto

O LojaCarro surgiu a partir de um projeto desenvolvido anteriormente na disciplina. Nesta nova etapa, a ideia é aproveitar a estrutura existente e adaptá-la aos requisitos do trabalho final, sem começar novamente do zero.

Na primeira semana, o foco foi reorganizar o backend, atualizar o banco de dados e preparar uma API REST que permita cadastrar veículos com fotografias, disponibilizar um catálogo público e controlar o acesso às operações administrativas. Também foi implementada a autenticação por JWT, diferenciando os usuários em dois perfis: administrador e cliente.

A interface Angular e as regras completas de agendamento de Test Drive serão desenvolvidas nas etapas seguintes.

## Tecnologias utilizadas

- Java 17 e Spring Boot 3.5.7;
- Spring Web, Spring Data JPA e Hibernate;
- Spring Security, JWT (HS256) e BCrypt;
- MySQL e H2 (ambiente de testes);
- RabbitMQ e Log4j2;
- Maven Wrapper;
- TwelveMonkeys ImageIO, para suporte a WebP;
- JUnit 5, Mockito, Postman e GitHub Actions.

O IntelliJ IDEA foi utilizado no desenvolvimento, e o MySQL Workbench auxiliou na criação e conferência do banco de dados.

## O que foi desenvolvido na Semana 1

### Reorganização do projeto

A branch `TrabalhoFinal` foi criada para manter as alterações do trabalho final separadas da branch `dev`. A primeira atividade foi revisar o código existente e retirar os módulos que não seriam mais utilizados, principalmente os de vendas, contabilidade e administração legada.

Ao todo, foram removidas 19 classes: `DataInitializer`; `AdmController`, `AdmWebController`, `ContabilController`, `ContabilWebController`, `VendaController` e `VendaWebController`; `Contabil` e `Venda`; `ContabilRepository` e `VendaRepository`; `ContabilService` e `VendaService`; `EnviarContabil` e `EnviarVenda`; além dos testes `AdmControllerTest`, `EnviarModulosTest`, `ContabilServiceTest` e `VendaServiceTest`.

As entidades, repositórios, serviços, controllers, classes de mensageria e testes restantes foram revisados para se adequar à nova proposta. Alguns componentes e templates antigos ainda permanecem no projeto por compatibilidade, mas as rotas legadas não previstas na configuração de segurança estão bloqueadas.

### RF01 — Gerenciamento de veículos

O cadastro de veículos passou a contemplar marca, modelo, ano de fabricação, ano do modelo, cor, quilometragem, combustível, transmissão, preço, descrição, situação e fotografia principal.

O administrador pode cadastrar, consultar, atualizar e inativar veículos. Também é possível registrar a situação `VENDIDO`. A exclusão é lógica: o registro permanece no banco com a situação `INATIVO`, mas deixa de aparecer no catálogo público. O catálogo apresenta apenas veículos `DISPONIVEL` e permite filtrar por marca, modelo, faixa de preço e ano do modelo.

### RF02 — Fotografias dos veículos

Foi criado o `ImagemService` para tratar os arquivos recebidos pelo backend. No cadastro, a fotografia é enviada junto com os dados do veículo por meio de uma requisição `multipart/form-data`.

São aceitas imagens JPG, JPEG, PNG e WebP de até 5 MB (5 MiB). O serviço verifica o conteúdo do arquivo, não apenas sua extensão, e gera um nome próprio para evitar colisões. As fotografias são armazenadas no diretório `IMG/`, e o banco guarda o caminho do arquivo. Na atualização de um veículo, a imagem existente pode ser mantida quando não for enviada uma nova fotografia.

O diretório `IMG/` está no `.gitignore`, pois seus arquivos são gerados durante a execução. Os formatos de imagem, o limite de tamanho, a rejeição de arquivos inválidos e a recuperação das fotografias foram testados no ambiente local.

### RF03 — Usuários e autenticação

A autenticação antiga foi substituída por login com e-mail e senha. As senhas são armazenadas como hashes BCrypt, e a senha original não é retornada pela API.

| Perfil | Permissões nesta etapa |
| --- | --- |
| `ADMIN` | Gerenciar veículos e consultar clientes e registros de auditoria. |
| `CLIENTE` | Realizar cadastro, autenticar-se e consultar o catálogo público. |

O cadastro público cria exclusivamente usuários com perfil `CLIENTE`. O administrador inicial é criado de forma controlada pelo `AdminInitializer`, quando habilitado por variáveis de ambiente.

Durante o login, o `JwtService` gera um token assinado com HS256, válido por uma hora, contendo a identificação e o perfil do usuário. O `SecurityConfig` protege as rotas administrativas. Também foram implementadas a normalização do e-mail, a validação dos dados, a rejeição de e-mails duplicados, de senhas incorretas e de usuários inativos.

### Mensageria e auditoria

As classes `Enviar`, `Receber`, `EnviarCarro`, `EnviarUsuario`, `EnviarTesteDrive` e `MensageriaApplication`, além do `LogOperacaoDTO`, foram adequadas à estrutura atual.

O produtor utiliza parâmetros externos para conexão com o RabbitMQ. O consumidor foi ajustado para gravar os eventos de auditoria pelo repositório JPA, sem o fallback JDBC antigo. As operações de usuários e veículos geram eventos com a identificação da entidade, da ação e do responsável.

Para permitir o desenvolvimento e a execução dos testes mesmo quando o servidor RabbitMQ não está acessível, a mensageria pode ser desativada com `RABBITMQ_ENABLED=false`. Nesse modo, não há entrega real das mensagens ao broker. A validação com o servidor do professor e a garantia de entrega de eventos ainda fazem parte das próximas etapas.

## Banco de dados

O banco MySQL recebeu o nome `carros`, conforme solicitado na disciplina. A estrutura anterior foi substituída por quatro tabelas:

| Tabela | Finalidade |
| --- | --- |
| `usuario` | Dados cadastrais, perfil, situação da conta e hash da senha. |
| `carro` | Informações do veículo, disponibilidade e caminho da fotografia. |
| `agendamento` | Associação entre cliente e veículo, período e estado do Test Drive. |
| `log_operacao` | Registro das operações para auditoria. |

O script [schema_carros_trabalho_final.sql](database/schema_carros_trabalho_final.sql) contém a criação do banco, das tabelas, dos índices e das restrições de integridade. A estrutura de agendamento foi preparada nesta semana, mas as regras completas de negócio ainda precisam ser implementadas.

No perfil `dev`, o Hibernate utiliza `ddl-auto=validate`, por isso o script precisa ser executado antes de iniciar a aplicação.

## Organização do código

```text
TSI4M-LojaCarro/
├── .github/workflows/maven.yml       # Compilação e testes no GitHub Actions
├── database/
│   └── schema_carros_trabalho_final.sql
├── src/
│   ├── main/
│   │   ├── java/br/org/edu/ifrn/LojaCarro/
│   │   │   ├── client/              # Integrações e compatibilidade
│   │   │   ├── config/              # Segurança e inicialização
│   │   │   ├── controllers/         # Rotas e requisições HTTP
│   │   │   ├── dto/                 # Transferência de dados
│   │   │   ├── exception/           # Tratamento de exceções
│   │   │   ├── mensageria/          # Envio e recebimento de eventos
│   │   │   ├── model/               # Entidades JPA
│   │   │   ├── repository/          # Consultas ao banco
│   │   │   ├── security/            # Emissão de tokens JWT
│   │   │   └── services/            # Regras de negócio e imagens
│   │   └── resources/              # Configurações e templates legados
│   └── test/java/                   # Testes automatizados
├── .gitignore
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

O fluxo principal segue a organização Controller → Service → Repository → MySQL. A autenticação e a autorização acontecem antes do acesso às rotas protegidas. O processamento das fotografias e o envio dos eventos possuem componentes próprios.

## Como executar localmente

### 1. Preparar o ambiente

É necessário ter o JDK 17, o Git e o MySQL instalado (MySQL 8.0.16 ou superior, conforme o script). O Maven Wrapper já está no projeto. O RabbitMQ é opcional para os testes locais da Semana 1.

```bash
git clone -b TrabalhoFinal https://github.com/Brunno2m/TSI4M-LojaCarro.git
cd TSI4M-LojaCarro
```

### 2. Configurar o banco

Execute o script `database/schema_carros_trabalho_final.sql` no MySQL Workbench. Em um banco que já possua tabelas antigas, faça backup e confira a estrutura antes de aplicar o script; ele não substitui automaticamente tabelas incompatíveis.

O perfil `dev` usa a conexão `jdbc:mysql://localhost:3306/carros` e o usuário `root`. Caso utilize outro usuário, ajuste a propriedade `spring.datasource.username` de forma local.

### 3. Configurar as variáveis de ambiente

As variáveis abaixo podem ser configuradas em **Run → Edit Configurations** no IntelliJ ou no terminal que iniciará o backend. Não publique senhas, chaves JWT ou tokens no GitHub.

| Variável | Descrição |
| --- | --- |
| `SERVER` | Perfil de execução. Utilize `dev` nesta etapa. |
| `DB_PASSWORD` | Senha local do MySQL. |
| `JWT_SECRET_BASE64` | Chave de assinatura JWT em Base64, com pelo menos 32 bytes após decodificação. |
| `ADMIN_BOOTSTRAP_ENABLED` | Ativa a criação inicial do administrador (`true` somente quando necessário). |
| `ADMIN_NOME` | Nome do administrador inicial. |
| `ADMIN_EMAIL` | E-mail do administrador inicial. |
| `ADMIN_SENHA` | Senha inicial do administrador. |
| `RABBITMQ_ENABLED` | Ativa a mensageria. Utilize `false` no desenvolvimento sem broker. |
| `RABBITMQ_HOST` | Endereço do broker, quando utilizado. |
| `RABBITMQ_USER` | Usuário autorizado no RabbitMQ. |
| `RABBITMQ_PASSWORD` | Senha do RabbitMQ. |
| `RABBITMQ_QUEUE` | Fila de envio. |
| `RABBITMQ_RECEIVE_QUEUE` | Fila de recebimento. |

Exemplo para PowerShell, na raiz do projeto. Substitua os valores ilustrativos do banco e da conta administrativa pelos seus próprios dados:

```powershell
$env:SERVER = "dev"
$env:DB_PASSWORD = "SUA_SENHA_LOCAL"
$env:RABBITMQ_ENABLED = "false"

# Gera uma chave aleatória para o ambiente local.
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
$env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)

# Habilite somente na primeira criação do administrador.
$env:ADMIN_BOOTSTRAP_ENABLED = "true"
$env:ADMIN_NOME = "Administrador"
$env:ADMIN_EMAIL = "admin@example.com"
$env:ADMIN_SENHA = "SUBSTITUA_POR_UMA_SENHA_FORTE"
```

Guarde a chave local de forma segura se precisar manter a validade dos tokens entre reinicializações. Após a criação do administrador, defina `ADMIN_BOOTSTRAP_ENABLED=false` e retire a senha inicial da configuração de execução.

### 4. Iniciar a aplicação

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS, após configurar as variáveis no shell:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`. Nesta etapa, a interação com ela é realizada pelo Postman; o frontend Angular ainda não está integrado. Para verificar o funcionamento, faça uma requisição `GET /carro` sem autenticação.

## Endpoints disponíveis

### Autenticação e usuários

| Método | Rota | Acesso |
| --- | --- | --- |
| POST | `/auth/cadastro` | Público |
| POST | `/auth/login` | Público |
| GET | `/auth/clientes` | ADMIN |

Exemplo de cadastro de cliente (`Content-Type: application/json`):

```json
{
  "nome": "Cliente de teste",
  "email": "cliente@example.com",
  "senha": "SenhaApenasDeExemplo123"
}
```

Para autenticar, envie o e-mail e a senha cadastrados para `POST /auth/login`. A resposta informa o `token`, o tipo `Bearer`, o tempo de expiração e os dados do usuário. Para acessar uma rota administrativa, utilize o token de uma conta `ADMIN` no cabeçalho `Authorization: Bearer <token>`.

### Veículos e auditoria

| Método | Rota | Acesso |
| --- | --- | --- |
| GET | `/carro` ou `/carro/listarCarros` | Público |
| GET | `/carro/{id}` | Público |
| GET | `/carro/{id}/foto` | Público |
| GET | `/carro/admin` | ADMIN |
| GET | `/carro/admin/{id}` | ADMIN |
| GET | `/carro/admin/{id}/foto` | ADMIN |
| POST | `/carro` ou `/carro/salvar` | ADMIN |
| PUT | `/carro/{id}` | ADMIN |
| DELETE | `/carro/{id}` | ADMIN |
| PATCH | `/carro/{id}/vender` | ADMIN |
| GET | `/carro/logs` | ADMIN |

O catálogo aceita os parâmetros opcionais `marca`, `modelo`, `precoMinimo`, `precoMaximo` e `anoModelo`.

Para cadastrar um veículo no Postman, utilize `POST /carro/salvar` com o token do administrador. Em **Body → form-data**, envie a parte `dados` do tipo texto com o Content-Type da parte definido como `application/json` e a parte `foto` do tipo File, contendo a imagem real. Não configure manualmente o Content-Type global da requisição: o Postman deve gerar o cabeçalho multipart com seu boundary.

Exemplo do conteúdo de `dados`:

```json
{
  "marca": "Honda",
  "modelo": "Civic",
  "ano": 2023,
  "anoModelo": 2024,
  "cor": "Prata",
  "quilometragem": 25000,
  "tipoCombustivel": "Gasolina",
  "tipoTransmissao": "Automatico",
  "preco": 150000.00,
  "descricao": "Veiculo de exemplo para testar o cadastro."
}
```

A resposta de criação é HTTP 201. Na atualização, envie `dados` e, caso deseje substituir a imagem, uma nova parte `foto`.

## Testes realizados

### Testes manuais

A API foi executada localmente e os seguintes cenários foram verificados pelo Postman:

| Cenário | Resultado observado |
| --- | --- |
| Listagem pública e consulta individual de veículos disponíveis | 200 |
| Cadastro de cliente | 201 |
| Login de administrador e cliente | 200 |
| Acesso administrativo com perfil ADMIN | 200 |
| Acesso administrativo com perfil CLIENTE | 403 |
| Cadastro de veículo com fotografia pelo administrador | 201 |
| Recuperação da fotografia | 200 |
| Atualização de veículo pelo administrador | 200 |
| Cadastro ou atualização de veículo pelo cliente | 403 |
| Exclusão lógica do veículo pelo administrador | 204 |
| Consulta pública de veículo inativo | 404 |
| Cadastro com e-mail já existente | 409 |
| Login com senha incorreta | 401 |

Também foram verificados o armazenamento da imagem na pasta `IMG`, a situação `INATIVO` no banco, a retirada do veículo do catálogo público e a validação dos formatos e do limite de tamanho das fotografias.

### Testes automatizados

Os testes existentes foram adaptados às alterações do projeto. A primeira execução apresentou falhas devido às referências à estrutura antiga e à falta de configuração JWT no ambiente de testes. Após a revisão, foi possível executar a suíte completa com sucesso.

| Classe | Testes aprovados |
| --- | ---: |
| `CarroControllerTest` | 8 |
| `EnviarTest` | 2 |
| `ReceberTest` | 6 |
| `UsuarioServiceTest` | 6 |
| `TesteDriveServiceTest` | 1 |
| `LojaCarroApplicationTests` | 1 |
| **Total** | **24** |

Para repetir a execução no Windows:

```powershell
$env:RABBITMQ_ENABLED = "false"
.\mvnw.cmd clean test
```

No Linux ou macOS:

```bash
RABBITMQ_ENABLED=false ./mvnw clean test
```

Resultado obtido na execução local:

```text
Tests run: 24, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Os testes unitários simulam as dependências do banco e da mensageria quando necessário. O `LojaCarroApplicationTests` verifica a inicialização do contexto Spring Boot com H2 e uma chave JWT exclusiva de teste. Isso não equivale à validação de todos os endpoints com MockMvc nem à comunicação real com o RabbitMQ.

### Integração contínua

O workflow [Java CI with Maven](.github/workflows/maven.yml) foi atualizado para executar a compilação e os testes nas branches `dev` e `TrabalhoFinal`, utilizando o JDK 17. A execução na branch do trabalho final foi concluída com sucesso.

[Consultar a execução do GitHub Actions](https://github.com/Brunno2m/TSI4M-LojaCarro/actions/runs/44103952781).

## Acompanhamento da entrega

| Atividade | Situação em 17/11/2026 |
| --- | --- |
| Criação da branch e reorganização das classes | Concluída |
| Modelagem e criação das quatro tabelas | Concluída |
| RF01 — CRUD de veículos | Implementado e testado nos fluxos principais |
| RF02 — Upload e validação de fotografias | Implementado e testado |
| RF03 — Cadastro, JWT e controle de acesso | Implementado e testado nos fluxos principais |
| Revisão dos testes existentes | 24 testes aprovados |
| GitHub Actions na branch TrabalhoFinal | Execução aprovada |
| Revisão das configurações legadas e do guia de mensageria | Pendente |
| Regras de agendamento, conflitos de horários e cancelamentos | Próximas etapas |
| Testes de integração com MockMvc (mínimo de cinco) | Pendente |
| Interface Angular e seus testes (mínimo de cinco) | Próximas etapas |
| Validação do RabbitMQ com o broker real | Pendente |
| Implantação em nuvem | Próximas etapas |

## Considerações sobre a versão atual

O objetivo desta primeira semana foi estabelecer uma base funcional para o restante do trabalho. O backend já permite executar os principais fluxos de veículos e usuários, com autenticação e persistência local no MySQL. A próxima etapa será avançar nas regras de agendamento de Test Drive e na integração com o frontend.

A configuração recomendada neste documento é a de desenvolvimento local. Antes de utilizar o perfil `prod` ou publicar o sistema na nuvem, é necessário revisar as configurações antigas, retirar referências a credenciais fixas, substituir credenciais reais que tenham sido expostas e validar a mensageria. O guia antigo de RabbitMQ ainda precisa ser atualizado para refletir a arquitetura atual. Nenhuma senha ou chave real deve ser compartilhada na documentação.

---

Projeto acadêmico em desenvolvimento — IFRN, disciplina Desenvolvimento Web na Nuvem.
