# LojaCarro — Frontend Django (catalogo e administracao)

Frontend desenvolvido em Django 5.2 LTS para consumir a API REST Spring Boot 3 do projeto. O MySQL `carros` e os arquivos `IMG/` permanecem sob responsabilidade do backend Java. O Django **nao acessa essas tabelas diretamente**.

## O que ja foi validado

O usuario verificou manualmente a comunicacao Django → Spring Boot → MySQL, listagem de seis veiculos, recuperacao de fotografias, filtros, detalhes e ocultacao de veiculos inativos. Os oito testes iniciais do catalogo tambem passaram no Windows.

## Novidade desta entrega

- Login `/entrar/` usando `POST /auth/login` do backend.
- Cadastro publico de clientes `/cadastro/` usando `POST /auth/cadastro` (nunca cria ADMIN).
- Logout por POST com CSRF.
- JWT guardado no **servidor Django** em uma sessao apoiada por SQLite (somente sessoes, nao dados de negocio). O JWT **nao e enviado ao HTML**.
- Sessao com duracao maxima de 3600 segundos, alinhada com a validade do JWT do Spring Boot.
- `/painel/`: estoque completo para ADMIN, inclusive VENDIDO/INATIVO, com indicadores.
- `/painel/veiculos/novo/`: cadastro com dados JSON e fotografia em partes multipart.
- `/painel/veiculos/<id>/editar/`: edicao com troca opcional de foto.
- Acoes POST com CSRF para marcar como vendido e desativar logicamente.
- `/painel/clientes/`: listagem administrativa de clientes.
- Somente o Spring Boot autoriza as chamadas administrativas. O frontend verifica a sessao/perfil apenas para navegacao e experiencia de uso.

## Preparacao no Windows / IntelliJ

Na raiz do projeto, **mantenha Spring Boot iniciado** na porta 8080. Em outro terminal:

```powershell
cd frontend
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
.\.venv\Scripts\python.exe manage.py migrate --noinput
.\.venv\Scripts\python.exe manage.py check
.\.venv\Scripts\python.exe manage.py test catalogo -v 2
.\.venv\Scripts\python.exe manage.py runserver 127.0.0.1:8000
```

**IMPORTANTE:** esta versao usa SQLite exclusivamente para armazenar sessoes Django; por isso e necessario executar `manage.py migrate` **uma unica vez** na primeira instalacao. Essa migracao nao altera nenhuma tabela do MySQL `carros` e nao realiza nenhuma operacao sobre dados do Java. O arquivo `frontend/db.sqlite3` esta no `.gitignore`.

Se estiver iniciando pela raiz do repositorio, use `cd frontend`; se ja estiver dentro da pasta `frontend`, nao execute `cd` outra vez.

Acesse http://127.0.0.1:8000/. Utilize um ADMIN que ja exista no MySQL para acessar `/painel/`. Um cliente cadastrado publicamente nao pode entrar na administracao (403). **Nao crie nova conta ADMIN pelo Django**.

## Seguranca

- Senhas enviadas somente ao backend no momento do login/cadastro e nao armazenadas no Django.
- JWT salvo em sessao do servidor Django; cookie de sessao HttpOnly, SameSite=Lax, com CSRF nos formularios POST.
- O backend faz a verificacao JWT e da role ADMIN em todas as operacoes do estoque.
- Se a API retorna 401 (token expirado ou invalido), a sessao Django e encerrada e o visitante redirecionado para login.
- Operacoes mutantes (cadastro, edicao, venda, exclusao logica) usam metodos HTTP apropriados.
- No ambiente local `DEBUG=true` com HTTP. Para producao, defina chave secreta estavel `DJANGO_SECRET_KEY`, `DJANGO_DEBUG=false`, HTTPS, hosts permitidos, protecao de upload e configuracao adequada de sessao/CSRF. **Nao publique segredos no GitHub**.
- O Spring Boot continua responsavel por validar conteudo real e formato das fotografias.

## Testes e observacoes

```powershell
.\.venv\Scripts\python.exe manage.py test catalogo -v 2
```

Os testes de auth/admin usam mocks das respostas HTTP e sessoes em banco de teste. Para integracao real, no navegador: entre como ADMIN, abra o painel, cadastre carro com foto, edite, marque vendido/desative, e confira os dados no Workbench. Faça logout e tente acessar `/painel/` como CLIENTE (403). A instalacao de dependencias nao estava disponivel no ambiente gerador; execute os testes no seu computador antes do commit.

Se ao reiniciar o Django suas sessoes forem invalidadas, defina uma `DJANGO_SECRET_KEY` estavel como variavel de ambiente local (sem versiona-la). Em desenvolvimento, a geracao de uma chave efemera sem essa variavel e intencional.


## Gestão administrativa de clientes (versão 3)

- `GET /painel/clientes/`: lista clientes ativos e inativos.
- `GET/POST /painel/clientes/novo/`: formulário administrativo para cadastro CLIENTE.
- `POST /painel/clientes/<id>/desativar/`: desativação lógica com confirmação e CSRF.
- Endpoints Spring Boot exigem JWT com `ROLE_ADMIN`: `POST /auth/clientes` e `DELETE /auth/clientes/<id>`.
- `ativo=false` impede novos logins. A exclusão não remove o registro e preserva relações de agendamentos.
- A senha não aparece na listagem; BCrypt continua no backend Java.
- Não permite criar administradores pelo painel.
- Execute os testes: `.\.venv\Scripts\python.exe manage.py test catalogo -v 2`.
