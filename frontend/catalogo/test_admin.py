"""Testes de autenticacao e administracao (com API Java simulada)."""
from unittest.mock import patch

from django.core.files.uploadedfile import SimpleUploadedFile
from django.test import Client, TestCase
from django.urls import reverse

from . import api

ADMIN = {"id": 1, "nome": "Administrador", "email": "admin@exemplo.com", "perfil": "ADMIN", "ativo": True}
CLIENTE = {"id": 2, "nome": "Cliente", "email": "cliente@exemplo.com", "perfil": "CLIENTE", "ativo": True}
CARRO = {"id": 7, "marca": "Honda", "modelo": "Civic", "ano": 2023,
         "anoModelo": 2024, "cor": "Prata", "quilometragem": 20000,
         "tipoCombustivel": "FLEX", "tipoTransmissao": "AUTOMATICO",
         "preco": 120000.00, "descricao": "Veículo revisado", "situacao": "DISPONIVEL",
         "fotoPrincipal": "IMG/foto.jpg"}


def dados_formulario():
    return {k: str(CARRO[k]) for k in (
        "marca", "modelo", "ano", "anoModelo", "cor", "quilometragem",
        "tipoCombustivel", "tipoTransmissao", "preco", "descricao", "situacao"
    )}


class AutenticacaoAdminTests(TestCase):
    def autenticar_sessao(self, usuario=ADMIN, client=None):
        client = client or self.client
        sessao = client.session
        sessao["jwt"] = "token-teste-secreto"
        sessao["usuario"] = usuario
        sessao.set_expiry(3600)
        sessao.save()
        client.cookies["sessionid"] = sessao.session_key
        return client

    @patch("catalogo.api.autenticar", return_value={"token": "token-teste-secreto", "expiresIn": 3600, "usuario": ADMIN})
    def test_login_admin_cria_sessao_servidor(self, autenticar):
        resposta = self.client.post(reverse("catalogo:entrar"),
                                    {"email": ADMIN["email"], "senha": "senha-supersegura"})
        self.assertRedirects(resposta, reverse("catalogo:painel"), fetch_redirect_response=False)
        self.assertEqual(self.client.session["jwt"], "token-teste-secreto")
        self.assertEqual(self.client.session["usuario"]["perfil"], "ADMIN")
        self.assertLessEqual(self.client.session.get_expiry_age(), 3600)
        html = self.client.get(reverse("catalogo:entrar")).content.decode()
        self.assertNotIn("token-teste-secreto", html)
        autenticar.assert_called_once()

    @patch("catalogo.api.autenticar", return_value={"token": "token-teste-secreto", "expiresIn": 3600, "usuario": CLIENTE})
    def test_login_cliente_volta_inicio(self, autenticar):
        resposta = self.client.post(reverse("catalogo:entrar"),
                                    {"email": CLIENTE["email"], "senha": "senha-supersegura"})
        self.assertRedirects(resposta, reverse("catalogo:inicio"), fetch_redirect_response=False)

    @patch("catalogo.api.autenticar", side_effect=api.ErroAPI(401))
    def test_senha_invalida_nao_cria_sessao(self, autenticar):
        resposta = self.client.post(reverse("catalogo:entrar"),
                                    {"email": "admin@exemplo.com", "senha": "senha-incorreta"})
        self.assertContains(resposta, "E-mail ou senha inválidos")
        self.assertNotIn("jwt", self.client.session)

    @patch("catalogo.api.cadastrar_cliente", return_value={"id": 3, "perfil": "CLIENTE"})
    def test_cadastro_cliente_envia_dados_api(self, cadastrar):
        resposta = self.client.post(reverse("catalogo:cadastro"), {
            "nome": "Nova Cliente", "email": "nova@exemplo.com",
            "senha": "senha-valida-123", "confirmar_senha": "senha-valida-123",
        })
        self.assertRedirects(resposta, reverse("catalogo:entrar") + "?cadastro=ok", fetch_redirect_response=False)
        cadastrar.assert_called_once_with("Nova Cliente", "nova@exemplo.com", "senha-valida-123")
        self.assertNotIn("jwt", self.client.session)

    @patch("catalogo.api.cadastrar_cliente", side_effect=api.ErroAPI(409))
    def test_cadastro_email_duplicado(self, cadastrar):
        resposta = self.client.post(reverse("catalogo:cadastro"), {
            "nome": "Nova Cliente", "email": "nova@exemplo.com",
            "senha": "senha-valida-123", "confirmar_senha": "senha-valida-123",
        })
        self.assertContains(resposta, "Este e-mail já está cadastrado")

    @patch("catalogo.api.listar_admin", return_value=[CARRO])
    def test_painel_admin_lista_estoque(self, listar):
        self.autenticar_sessao()
        resposta = self.client.get(reverse("catalogo:painel"))
        self.assertContains(resposta, "Civic")
        listar.assert_called_once_with("token-teste-secreto")
        self.assertNotIn("token-teste-secreto", resposta.content.decode())

    @patch("catalogo.api.listar_admin")
    def test_visitante_redirecionado_para_login(self, listar):
        resposta = self.client.get(reverse("catalogo:painel"))
        self.assertEqual(resposta.status_code, 302)
        self.assertIn(reverse("catalogo:entrar"), resposta["Location"])
        listar.assert_not_called()

    @patch("catalogo.api.listar_admin")
    def test_cliente_nao_acessa_painel(self, listar):
        self.autenticar_sessao(CLIENTE)
        resposta = self.client.get(reverse("catalogo:painel"))
        self.assertEqual(resposta.status_code, 403)
        listar.assert_not_called()

    @patch("catalogo.api.listar_admin", side_effect=api.ErroAPI(401))
    def test_token_expirado_encerra_sessao(self, listar):
        self.autenticar_sessao()
        resposta = self.client.get(reverse("catalogo:painel"))
        self.assertEqual(resposta.status_code, 302)
        self.assertIn("expirou=1", resposta["Location"])
        self.assertNotIn("jwt", self.client.session)

    @patch("catalogo.api.salvar_admin", return_value=CARRO)
    def test_admin_cria_veiculo_com_foto(self, salvar):
        self.autenticar_sessao()
        dados = dados_formulario()
        dados["foto"] = SimpleUploadedFile("corolla.jpg", b"\xff\xd8\xff\xe0foto", content_type="image/jpeg")
        resposta = self.client.post(reverse("catalogo:painel_novo"), dados)
        self.assertRedirects(resposta, reverse("catalogo:painel"), fetch_redirect_response=False)
        salvar.assert_called_once()
        self.assertEqual(salvar.call_args.args[1]["modelo"], "Civic")
        self.assertEqual(salvar.call_args.args[2].name, "corolla.jpg")

    @patch("catalogo.api.salvar_admin")
    def test_cadastro_sem_foto_rejeitado(self, salvar):
        self.autenticar_sessao()
        resposta = self.client.post(reverse("catalogo:painel_novo"), dados_formulario())
        self.assertContains(resposta, "Selecione uma fotografia")
        salvar.assert_not_called()

    @patch("catalogo.api.atualizar_admin", return_value=CARRO)
    @patch("catalogo.api.buscar_admin", return_value=CARRO)
    def test_edicao_sem_trocar_foto(self, buscar, atualizar):
        self.autenticar_sessao()
        resposta = self.client.post(reverse("catalogo:painel_editar", args=[7]), dados_formulario())
        self.assertRedirects(resposta, reverse("catalogo:painel"), fetch_redirect_response=False)
        atualizar.assert_called_once()
        self.assertIsNone(atualizar.call_args.args[3])

    @patch("catalogo.api.desativar_admin")
    def test_desativacao_por_post(self, desativar):
        self.autenticar_sessao()
        resposta = self.client.post(reverse("catalogo:painel_desativar", args=[7]))
        self.assertRedirects(resposta, reverse("catalogo:painel"), fetch_redirect_response=False)
        desativar.assert_called_once_with("token-teste-secreto", 7)

    @patch("catalogo.api.vender_admin")
    def test_marcar_vendido(self, vender):
        self.autenticar_sessao()
        resposta = self.client.post(reverse("catalogo:painel_vender", args=[7]))
        self.assertRedirects(resposta, reverse("catalogo:painel"), fetch_redirect_response=False)
        vender.assert_called_once_with("token-teste-secreto", 7)

    @patch("catalogo.api.listar_clientes_admin", return_value=[CLIENTE])
    def test_lista_clientes_somente_admin(self, listar):
        self.autenticar_sessao()
        resposta = self.client.get(reverse("catalogo:painel_clientes"))
        self.assertContains(resposta, CLIENTE["email"])
        listar.assert_called_once_with("token-teste-secreto")

    def test_exclusao_por_get_nao_permitida(self):
        self.autenticar_sessao()
        resposta = self.client.get(reverse("catalogo:painel_desativar", args=[7]))
        self.assertEqual(resposta.status_code, 405)

    def test_logout_via_post(self):
        self.autenticar_sessao()
        resposta = self.client.post(reverse("catalogo:sair"))
        self.assertRedirects(resposta, reverse("catalogo:inicio"), fetch_redirect_response=False)
        self.assertNotIn("jwt", self.client.session)

    def test_csrf_em_acao_desativar(self):
        outro_cliente = Client(enforce_csrf_checks=True)
        self.autenticar_sessao(client=outro_cliente)
        resposta = outro_cliente.post(reverse("catalogo:painel_desativar", args=[7]))
        self.assertEqual(resposta.status_code, 403)
