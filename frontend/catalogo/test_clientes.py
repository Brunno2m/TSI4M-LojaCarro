"""Testes de criacao/exclusao logica de clientes via API simulada."""
from unittest.mock import patch

from django.test import Client, TestCase
from django.urls import reverse

from . import api

ADMIN = {"id": 1, "nome": "Administrador", "email": "admin@exemplo.com", "perfil": "ADMIN", "ativo": True}
CLIENTE = {"id": 2, "nome": "Cliente", "email": "cliente@exemplo.com", "perfil": "CLIENTE", "ativo": True}


class ClienteAdminTests(TestCase):
    def autenticar(self, usuario=ADMIN, client=None):
        client = client or self.client
        sessao = client.session
        sessao["jwt"] = "jwt-de-teste"
        sessao["usuario"] = usuario
        sessao.set_expiry(3600)
        sessao.save()
        client.cookies["sessionid"] = sessao.session_key
        return client

    def dados(self):
        return {"nome": "Ana Cliente", "email": "ana@exemplo.com", "senha": "SenhaSegura123",
                "confirmar_senha": "SenhaSegura123"}

    @patch("catalogo.api.cadastrar_cliente_admin", return_value={"id": 11, "perfil": "CLIENTE"})
    def test_admin_pode_criar_cliente(self, chamar):
        self.autenticar()
        resposta = self.client.post(reverse("catalogo:painel_cliente_novo"), self.dados())
        self.assertRedirects(resposta, reverse("catalogo:painel_clientes"), fetch_redirect_response=False)
        chamar.assert_called_once_with("jwt-de-teste", "Ana Cliente", "ana@exemplo.com", "SenhaSegura123")

    @patch("catalogo.api.cadastrar_cliente_admin")
    def test_cliente_nao_pode_criar_outro_cliente_no_painel(self, chamar):
        self.autenticar(CLIENTE)
        resposta = self.client.post(reverse("catalogo:painel_cliente_novo"), self.dados())
        self.assertEqual(resposta.status_code, 403)
        chamar.assert_not_called()

    @patch("catalogo.api.cadastrar_cliente_admin")
    def test_visitante_nao_pode_criar_cliente_no_painel(self, chamar):
        resposta = self.client.post(reverse("catalogo:painel_cliente_novo"), self.dados())
        self.assertEqual(resposta.status_code, 302)
        chamar.assert_not_called()

    @patch("catalogo.api.cadastrar_cliente_admin", side_effect=api.ErroAPI(409))
    def test_email_duplicado_mostra_erro(self, chamar):
        self.autenticar()
        resposta = self.client.post(reverse("catalogo:painel_cliente_novo"), self.dados())
        self.assertContains(resposta, "Este e-mail já está cadastrado.")

    @patch("catalogo.api.desativar_cliente_admin", return_value=None)
    def test_admin_pode_desativar_cliente(self, chamar):
        self.autenticar()
        resposta = self.client.post(reverse("catalogo:painel_cliente_desativar", args=[2]))
        self.assertRedirects(resposta, reverse("catalogo:painel_clientes"), fetch_redirect_response=False)
        chamar.assert_called_once_with("jwt-de-teste", 2)

    @patch("catalogo.api.desativar_cliente_admin")
    def test_cliente_nao_pode_desativar_cliente(self, chamar):
        self.autenticar(CLIENTE)
        resposta = self.client.post(reverse("catalogo:painel_cliente_desativar", args=[2]))
        self.assertEqual(resposta.status_code, 403)
        chamar.assert_not_called()

    @patch("catalogo.api.desativar_cliente_admin")
    def test_exclusao_por_get_nao_e_permitida(self, chamar):
        self.autenticar()
        resposta = self.client.get(reverse("catalogo:painel_cliente_desativar", args=[2]))
        self.assertEqual(resposta.status_code, 405)
        chamar.assert_not_called()

    @patch("catalogo.api.desativar_cliente_admin")
    def test_exclusao_exige_csrf(self, chamar):
        client = Client(enforce_csrf_checks=True)
        self.autenticar(client=client)
        resposta = client.post(reverse("catalogo:painel_cliente_desativar", args=[2]))
        self.assertEqual(resposta.status_code, 403)
        chamar.assert_not_called()

    @patch("catalogo.api.listar_clientes_admin", return_value=[CLIENTE, {**CLIENTE, "id": 3, "ativo": False}])
    def test_listagem_destaca_inativos(self, chamar):
        self.autenticar()
        resposta = self.client.get(reverse("catalogo:painel_clientes"))
        self.assertContains(resposta, "INATIVO")
        self.assertContains(resposta, "Cadastrar cliente")
        self.assertEqual(resposta.content.decode().count("Excluir acesso"), 1)
