from unittest.mock import patch
from django.test import SimpleTestCase
from django.urls import reverse
from . import api

CARRO = {"id": 7, "marca": "Honda", "modelo": "Civic", "ano": 2023,
         "anoModelo": 2024, "cor": "Prata", "quilometragem": 20000,
         "tipoCombustivel": "Flex", "tipoTransmissao": "Automático",
         "preco": 120000.00, "descricao": "Veículo revisado", "situacao": "DISPONIVEL"}


class CatalogoTests(SimpleTestCase):
    @patch("catalogo.api.listar_veiculos", return_value=[CARRO])
    def test_inicio(self, consulta):
        resposta = self.client.get(reverse("catalogo:inicio"))
        self.assertContains(resposta, "Civic")
        consulta.assert_called_once_with()

    @patch("catalogo.api.listar_veiculos", return_value=[CARRO])
    def test_filtros(self, consulta):
        resposta = self.client.get(reverse("catalogo:listagem"), {"marca": "Honda", "precoMaximo": "150000", "anoModelo": "2024"})
        self.assertContains(resposta, "Civic")
        consulta.assert_called_once_with({"marca": "Honda", "precoMaximo": "150000", "anoModelo": "2024"})

    @patch("catalogo.api.listar_veiculos", return_value=[])
    def test_catalogo_vazio(self, consulta):
        self.assertContains(self.client.get(reverse("catalogo:listagem")), "Nenhum veículo encontrado")

    @patch("catalogo.api.listar_veiculos")
    def test_faixa_invalida(self, consulta):
        resposta = self.client.get(reverse("catalogo:listagem"), {"precoMinimo": "200000", "precoMaximo": "100000"})
        self.assertContains(resposta, "Revise os filtros")
        consulta.assert_not_called()

    @patch("catalogo.api.buscar_veiculo", return_value=CARRO)
    def test_detalhes(self, consulta):
        resposta = self.client.get(reverse("catalogo:detalhes", args=[7]))
        self.assertContains(resposta, "Veículo revisado")
        consulta.assert_called_once_with(7)

    @patch("catalogo.api.buscar_veiculo", side_effect=api.VeiculoNaoEncontrado)
    def test_veiculo_inexistente(self, consulta):
        self.assertEqual(self.client.get(reverse("catalogo:detalhes", args=[7])).status_code, 404)

    @patch("catalogo.api.listar_veiculos", side_effect=api.ApiIndisponivel)
    def test_api_indisponivel(self, consulta):
        self.assertContains(self.client.get(reverse("catalogo:listagem")), "Não foi possível acessar a API", status_code=503)

    @patch("catalogo.api.buscar_foto", return_value=(b"foto-de-teste", "image/jpeg"))
    def test_foto(self, consulta):
        resposta = self.client.get(reverse("catalogo:foto", args=[7]))
        self.assertEqual(resposta.status_code, 200)
        self.assertEqual(resposta["Content-Type"], "image/jpeg")
        self.assertEqual(resposta.content, b"foto-de-teste")
