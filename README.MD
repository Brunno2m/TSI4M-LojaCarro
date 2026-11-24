# LojaCarro — Trabalho Final

Projeto desenvolvido para a disciplina **Desenvolvimento Web na Nuvem**, do IFRN.

**Branch utilizada:** `TrabalhoFinal`  
**Situação:** backend da Semana 1 concluído; frontend Django e integração da Semana 2 implementados e testados nos principais fluxos.  
**Atualização:** 23/11/2026.

## 1. Sobre o projeto

O LojaCarro é um sistema para uma loja de veículos. O visitante pode consultar os carros disponíveis, ver fotografias e pesquisar por marca, modelo e preço. Um cliente pode criar sua conta e entrar no sistema. Já o administrador pode gerenciar os veículos e os cadastros de clientes.

O trabalho aproveita um projeto Java existente, que foi reorganizado para atender aos requisitos atuais da disciplina. Em vez de utilizar Angular, escolhemos **Django** para a interface, conforme a liberdade de escolha informada pelo professor.

## 2. Como o sistema funciona

```text
Navegador
    |
    v
Django (http://127.0.0.1:8000)
    |  Requisições HTTP/JSON e JWT
    v
Spring Boot (http://localhost:8080)
    |                 |
    v                 v
MySQL (carros)    Pasta IMG/ (fotografias)

Spring Boot também pode enviar eventos de auditoria ao RabbitMQ.
```

O **Django mostra as páginas e recebe os formulários**. O **Spring Boot aplica as regras do sistema, valida o JWT e acessa o MySQL**. Os dados de usuários e veículos não são gravados diretamente pelo Django.

O Django utiliza um pequeno banco SQLite local **somente para sessões de acesso**. Esse arquivo não substitui o MySQL da aplicação.

### Tecnologias

| Parte | Tecnologias |
| --- | --- |
| Backend | Java 17, Spring Boot 3.5.7, Spring Security, JPA/Hibernate |
| Frontend | Python, Django 5.2 LTS, HTML, CSS e JavaScript |
| Banco principal | MySQL (`carros`) |
| Sessões do frontend | SQLite local do Django |
| Segurança | Login por e-mail e senha, BCrypt, JWT (1 hora), perfis ADMIN e CLIENTE |
| Imagens | Arquivos JPG, JPEG, PNG e WebP, armazenados em `IMG/` |
| Mensageria | RabbitMQ e registros de auditoria |
| Testes | JUnit, Mockito, H2, Django Test Framework, Postman |
| Versionamento e automação | Git, GitHub e GitHub Actions |

## 3. O que já foi feito

### Semana 1 — Backend

- Reorganização das classes Java e retirada dos módulos antigos que não fazem parte do trabalho final.
- Criação do banco `carros` com quatro tabelas: `usuario`, `carro`, `agendamento` e `log_operacao`.
- Cadastro, consulta, edição, venda e desativação lógica de veículos.
- Upload e validação de fotografias. A foto é mantida na edição quando nenhuma imagem nova é enviada.
- Catálogo público com filtros e exibição apenas dos veículos `DISPONIVEL`.
- Cadastro de clientes, autenticação JWT e proteção das rotas por perfil.
- Preparação das classes de envio e recebimento de eventos RabbitMQ.
- Revisão dos testes Java e configuração de execução automática no GitHub Actions.

### Semana 2 — Frontend Django

- Página inicial e catálogo público com fotografias, detalhes e filtros.
- Integração real com a API Spring Boot e o banco MySQL.
- Cadastro público de clientes e tela de login.
- Sessão no Django utilizando o token JWT gerado pelo Spring Boot.
- Painel administrativo com informações do estoque.
- Cadastro e edição de veículos com fotografia, marcação como vendido e desativação lógica.
- Atualização do catálogo após as alterações feitas pelo administrador.
- Listagem de clientes, cadastro administrativo de clientes e opção **Excluir acesso**.
- Bloqueio do painel para usuários com perfil `CLIENTE`.

**Importante:** “Excluir acesso” não apaga o cliente do banco. A ação altera `ativo` para `false` (ou `0` no MySQL), preservando o histórico. O cliente desativado não consegue realizar um novo login. Tokens emitidos antes da desativação ainda podem continuar válidos até expirar; a revogação imediata é uma melhoria futura.

## 4. Estrutura das pastas

```text
TSI4M-LojaCarro/
├── .github/workflows/        # Automação do backend
├── database/                 # Script do MySQL
├── src/
│   ├── main/java/            # Spring Boot: controllers, services, models etc.
│   ├── main/resources/       # Configurações Java
│   └── test/java/            # Testes Java
├── frontend/
│   ├── catalogo/
│   │   ├── templates/        # Páginas HTML
│   │   ├── static/           # CSS e imagens estáticas
│   │   ├── api.py            # Comunicação com o Spring Boot
│   │   ├── forms.py          # Formulários
│   │   ├── views.py          # Páginas e ações
│   │   └── test*.py          # Testes Django
│   ├── config/               # Configurações do Django
│   ├── manage.py
│   ├── requirements.txt
│   └── README.md             # Detalhes específicos do frontend
├── IMG/                      # Fotos enviadas (não versionar)
├── pom.xml
├── mvnw.cmd                   # Maven Wrapper para Windows
└── README.MD                 # Este guia
```

## 5. Como executar no Windows (IntelliJ + PowerShell)

Para trabalhar com o projeto, vamos usar **dois servidores ao mesmo tempo**: Java na porta `8080` e Django na porta `8000`.

### Passo 1 — O que precisa estar instalado

- Java JDK 17;
- MySQL e MySQL Workbench;
- Python 3.14 (ou outra versão compatível com o Django indicado em `requirements.txt`);
- IntelliJ IDEA ou outro editor;
- Git.

O projeto já possui o Maven Wrapper. Não é necessário instalar Maven separadamente.

### Passo 2 — Preparar o MySQL

Abra o MySQL Workbench e confira se o banco `carros` existe. Em uma **instalação nova**, execute o arquivo:

`database/schema_carros_trabalho_final.sql`

**Atenção:** se você já criou as tabelas e cadastrou veículos ou clientes, **não apague nem recrie o banco**. Basta mantê-lo iniciado.

Para conferir os veículos:

```sql
USE carros;
SELECT id, marca, modelo, preco, situacao FROM carro ORDER BY id;
```

### Passo 3 — Configurar o Spring Boot

No IntelliJ, abra **Run → Edit Configurations → Environment variables** e confira:

| Variável | Para que serve |
| --- | --- |
| `SERVER` | Perfil de execução; para desenvolvimento, `dev` |
| `DB_PASSWORD` | Senha da conexão MySQL local |
| `JWT_SECRET_BASE64` | Chave JWT em Base64 (mínimo de 32 bytes quando decodificada) |
| `ADMIN_BOOTSTRAP_ENABLED` | `false` quando o administrador já existe |
| `RABBITMQ_ENABLED` | `false` para rodar sem broker; `true` para usar RabbitMQ |
| `RABBITMQ_HOST` | Endereço do servidor RabbitMQ, quando utilizado |
| `RABBITMQ_USER` | Usuário fornecido para o RabbitMQ |
| `RABBITMQ_PASSWORD` | Senha fornecida para o RabbitMQ |
| `RABBITMQ_QUEUE` | Fila de envio, conforme configuração da disciplina |
| `RABBITMQ_RECEIVE_QUEUE` | Fila de recebimento, conforme configuração da disciplina |

**Não coloque senhas reais, tokens ou chaves no README ou no GitHub.** Preserve a mesma chave JWT entre reinicializações quando necessário. Se o administrador já foi cadastrado, não é preciso cadastrá-lo outra vez.

O RabbitMQ não é necessário para consultar o catálogo ou usar o painel local. Para testes sem broker, utilize `RABBITMQ_ENABLED=false`. O projeto usa a porta AMQP `5672` na implementação atual.

### Passo 4 — Iniciar o backend

Pelo IntelliJ, execute a classe principal `LojaCarroApplication` com as variáveis configuradas. Como alternativa, no terminal com as mesmas variáveis definidas:

```powershell
.\mvnw.cmd spring-boot:run
```

Confirme que a API responde em:

- [http://localhost:8080/carro](http://localhost:8080/carro) — lista pública em JSON;
- `http://localhost:8080/carro/1/foto` — fotografia do veículo de ID 1, se ele existir e estiver disponível.

Se a API não iniciar, confira primeiro o MySQL, a variável `DB_PASSWORD` e a chave `JWT_SECRET_BASE64`.

### Passo 5 — Preparar o frontend Django

Abra **outro terminal PowerShell**, a partir da raiz do repositório:

```powershell
cd frontend
```

Se ainda não existir a pasta `.venv`, crie o ambiente virtual:

```powershell
py -3.14 -m venv .venv
```

Instale as dependências:

```powershell
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
```

Na primeira execução do Django, crie as tabelas **de sessão** locais:

```powershell
.\.venv\Scripts\python.exe manage.py migrate
```

Essa migration é do SQLite do Django. **Ela não altera as tabelas `usuario`, `carro`, `agendamento` ou `log_operacao` do MySQL.** Nas próximas execuções, normalmente não é necessário repetir esse passo.

Confira a configuração e inicie o frontend:

```powershell
.\.venv\Scripts\python.exe manage.py check
.\.venv\Scripts\python.exe manage.py runserver
```

Agora acesse [http://127.0.0.1:8000/](http://127.0.0.1:8000/).

> **Dica:** deixe o Spring Boot rodando na porta `8080` e o Django na `8000`. O Django busca os carros pela API `http://localhost:8080`. Se a API estiver desligada, o catálogo não conseguirá carregar os dados.

### Passo 6 — Entrar no sistema

- **Visitante:** pode abrir o catálogo sem login.
- **Cliente:** usa a página `/cadastro/` para criar conta e `/entrar/` para fazer login.
- **Administrador:** entra em `/entrar/` com o **mesmo e-mail e senha já usados no Postman**. Não existe uma senha separada para o Django.

Endereços úteis:

| Página | URL local |
| --- | --- |
| Início | `http://127.0.0.1:8000/` |
| Catálogo | `http://127.0.0.1:8000/veiculos/` |
| Login | `http://127.0.0.1:8000/entrar/` |
| Cadastro público | `http://127.0.0.1:8000/cadastro/` |
| Painel do administrador | `http://127.0.0.1:8000/painel/` |
| Clientes cadastrados | `http://127.0.0.1:8000/painel/clientes/` |
| Novo cliente (admin) | `http://127.0.0.1:8000/painel/clientes/novo/` |
| Novo veículo (admin) | `http://127.0.0.1:8000/painel/veiculos/novo/` |

O administrador pode gerenciar os carros e os clientes. Um cliente comum não pode entrar no painel.

**Sobre o login:** o JWT do backend dura uma hora. Ao expirar, entre novamente. Não precisa criar outra conta. Em desenvolvimento, reiniciar o Django pode invalidar sessões caso não esteja definida uma `DJANGO_SECRET_KEY` estável.

## 6. Principais endpoints do Spring Boot

| Método | Endpoint | Quem pode usar |
| --- | --- | --- |
| GET | `/carro` | Público |
| GET | `/carro/{id}` e `/carro/{id}/foto` | Público (somente disponível) |
| POST | `/auth/cadastro` | Público (cria CLIENTE) |
| POST | `/auth/login` | Público |
| GET | `/auth/clientes` | ADMIN |
| POST | `/auth/clientes` | ADMIN (cria CLIENTE) |
| DELETE | `/auth/clientes/{id}` | ADMIN (desativa CLIENTE) |
| GET | `/carro/admin` | ADMIN |
| POST | `/carro` ou `/carro/salvar` | ADMIN (dados + fotografia) |
| PUT | `/carro/{id}` | ADMIN (atualiza veículo) |
| PATCH | `/carro/{id}/vender` | ADMIN |
| DELETE | `/carro/{id}` | ADMIN (desativação lógica) |
| GET | `/carro/logs` | ADMIN |

O cadastro e a edição de carros enviam duas partes `multipart/form-data`: `dados`, com JSON e tipo `application/json`, e `foto`, com a imagem (obrigatória no cadastro e opcional na edição).

## 7. Testes realizados e como repetir

### O que já foi confirmado manualmente

- A API Java retornou seis veículos que também apareciam no MySQL e no catálogo Django, com preços, fotos e filtros corretos.
- O catálogo apresentou detalhes dos veículos e ocultou os inativos.
- O login do administrador abriu o painel; o cliente não teve acesso administrativo.
- O cadastro de veículo com imagem funcionou e apareceu no catálogo público.
- A edição do veículo, a mudança para `VENDIDO`, a desativação e a volta para `DISPONIVEL` funcionaram.
- A listagem de clientes funcionou.
- A opção **Excluir acesso** desativou uma conta de teste: o campo `ativo` passou a `0` no MySQL e uma nova tentativa de login foi rejeitada.
- A criação administrativa de clientes foi implementada, mas deve ser validada separadamente no navegador caso ainda não tenha sido testada.

### Testes Java

Na raiz do projeto, com o ambiente Java configurado:

```powershell
$env:RABBITMQ_ENABLED = "false"
.\mvnw.cmd clean test
```

Na Semana 1, **24 testes Java passaram**. Depois disso foram acrescentados testes para a gestão de clientes e corrigida uma chamada ambígua ao método `enviar`. Por isso, o total atualizado deve ser conferido na próxima execução completa do Maven; não reutilize o número antigo como se fosse o resultado atual.

### Testes Django

No terminal da pasta `frontend`:

```powershell
.\.venv\Scripts\python.exe manage.py test catalogo -v 2
```

A primeira versão do catálogo passou em **8 testes**. Após login e painel administrativo, **26 testes passaram**, sem falhas. A atualização de gestão de clientes acrescentou mais nove testes; execute novamente o comando para confirmar a versão atual.

Os testes automatizados utilizam dados e respostas simuladas quando apropriado. Por isso, também foram feitos os testes reais no navegador com Spring Boot e MySQL.

### GitHub Actions

O workflow `.github/workflows/maven.yml` executa compilação e testes **Java** na branch `TrabalhoFinal`. A última execução publicada antes desta atualização passou: [ver GitHub Actions](https://github.com/Brunno2m/TSI4M-LojaCarro/actions/runs/44107279404).

O pipeline ainda precisa ser ampliado para executar também os testes do Django.

## 8. O que falta desenvolver

- Regras completas de agendamento de Test Drive: criação, horários, conflitos, cancelamento e permissões.
- Integração das telas de Test Drive no Django.
- Mais testes de integração com MockMvc no backend.
- Testes automatizados Django no GitHub Actions.
- Verificação ponta a ponta da mensageria RabbitMQ e da persistência dos eventos de auditoria.
- Revisão de configurações antigas que possam conter credenciais e preparação para implantação em nuvem.

## 9. Observações importantes

Este é um projeto acadêmico em desenvolvimento. O ambiente descrito neste README é o **ambiente local de testes**, não uma configuração pronta para produção.

Antes de publicar a aplicação em um servidor, será necessário configurar HTTPS, `DJANGO_SECRET_KEY` estável, `DJANGO_DEBUG=false`, hosts permitidos, armazenamento de imagens, proteção de sessões e credenciais externas seguras. A aplicação Java também precisa passar por uma revisão do perfil `prod` e das configurações legadas do RabbitMQ.

---

**Repositório:** [Brunno2m/TSI4M-LojaCarro](https://github.com/Brunno2m/TSI4M-LojaCarro/tree/TrabalhoFinal)  
**Disciplina:** Desenvolvimento Web na Nuvem — IFRN.
