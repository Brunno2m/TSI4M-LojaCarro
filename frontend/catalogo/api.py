"""Cliente REST: a fonte de verdade de usuarios e veiculos e o Spring Boot."""
import json
import os

import requests
from django.conf import settings


class ApiIndisponivel(Exception):
    pass


class VeiculoNaoEncontrado(Exception):
    pass


class ErroAPI(Exception):
    """Erro HTTP do backend com codigo de status seguro para a UI."""
    def __init__(self, status):
        super().__init__(f"O backend retornou HTTP {status}.")
        self.status = status


def _get(caminho, **kwargs):
    try:
        resposta = requests.get(
            f"{settings.API_BASE_URL}{caminho}", timeout=(3, 8),
            allow_redirects=False, **kwargs
        )
    except requests.RequestException as exc:
        raise ApiIndisponivel() from exc
    if resposta.status_code == 404:
        resposta.close()
        raise VeiculoNaoEncontrado()
    if resposta.status_code != 200:
        resposta.close()
        raise ApiIndisponivel()
    return resposta


def _json(caminho, params=None):
    with _get(caminho, params=params) as resposta:
        try:
            return resposta.json()
        except ValueError as exc:
            raise ApiIndisponivel() from exc


def listar_veiculos(filtros=None):
    dados = _json("/carro", params=filtros or {})
    if not isinstance(dados, list) or any(
        not isinstance(item, dict) or not isinstance(item.get("id"), int)
        or item["id"] <= 0 for item in dados
    ):
        raise ApiIndisponivel()
    return dados


def buscar_veiculo(carro_id):
    dados = _json(f"/carro/{carro_id}")
    if not isinstance(dados, dict) or dados.get("id") != carro_id:
        raise ApiIndisponivel()
    return dados


def buscar_foto(carro_id):
    # O caminho e criado pelo Django, nunca por uma URL enviada pelo visitante.
    with _get(f"/carro/{carro_id}/foto", stream=True) as resposta:
        tipo = resposta.headers.get("Content-Type", "").split(";")[0].strip().lower()
        if tipo not in {"image/jpeg", "image/png", "image/webp"}:
            raise ApiIndisponivel()
        conteudo = bytearray()
        try:
            for bloco in resposta.iter_content(65536):
                conteudo.extend(bloco)
                if len(conteudo) > 5 * 1024 * 1024:
                    raise ApiIndisponivel()
        except requests.RequestException as exc:
            raise ApiIndisponivel() from exc
        if not conteudo:
            raise ApiIndisponivel()
        return bytes(conteudo), tipo


def _request(metodo, caminho, *, token=None, json_dados=None, arquivos=None):
    cabecalhos = {"Accept": "application/json"}
    if token:
        cabecalhos["Authorization"] = f"Bearer {token}"
    try:
        resposta = requests.request(
            metodo,
            f"{settings.API_BASE_URL}{caminho}",
            headers=cabecalhos,
            json=json_dados,
            files=arquivos,
            timeout=(3, 20),
            allow_redirects=False,
        )
    except requests.RequestException as exc:
        raise ApiIndisponivel() from exc
    try:
        if not 200 <= resposta.status_code < 300:
            raise ErroAPI(resposta.status_code)
        if resposta.status_code == 204 or not resposta.content:
            return None
        try:
            return resposta.json()
        except ValueError as exc:
            raise ApiIndisponivel() from exc
    finally:
        resposta.close()


def autenticar(email, senha):
    dados = _request("POST", "/auth/login", json_dados={"email": email, "senha": senha})
    if not isinstance(dados, dict) or not isinstance(dados.get("token"), str) or not dados["token"]:
        raise ApiIndisponivel()
    usuario = dados.get("usuario")
    if not isinstance(usuario, dict) or usuario.get("perfil") not in {"ADMIN", "CLIENTE"}:
        raise ApiIndisponivel()
    if not isinstance(dados.get("expiresIn"), int) or dados["expiresIn"] < 1:
        raise ApiIndisponivel()
    return dados


def cadastrar_cliente(nome, email, senha):
    return _request("POST", "/auth/cadastro", json_dados={"nome": nome, "email": email, "senha": senha})


def listar_admin(token):
    dados = _request("GET", "/carro/admin", token=token)
    if not isinstance(dados, list):
        raise ApiIndisponivel()
    return dados


def buscar_admin(token, carro_id):
    dados = _request("GET", f"/carro/admin/{int(carro_id)}", token=token)
    if not isinstance(dados, dict):
        raise ApiIndisponivel()
    return dados


def _partes_veiculo(dados, foto):
    # @RequestPart("dados") no Spring Boot EXIGE Content-Type application/json.
    partes = {"dados": ("dados.json", json.dumps(dados, ensure_ascii=False), "application/json")}
    if foto is not None:
        partes["foto"] = (os.path.basename(foto.name), foto, foto.content_type or "application/octet-stream")
    return partes


def salvar_admin(token, dados, foto):
    return _request("POST", "/carro", token=token, arquivos=_partes_veiculo(dados, foto))


def atualizar_admin(token, carro_id, dados, foto=None):
    return _request("PUT", f"/carro/{int(carro_id)}", token=token,
                    arquivos=_partes_veiculo(dados, foto))


def desativar_admin(token, carro_id):
    return _request("DELETE", f"/carro/{int(carro_id)}", token=token)


def vender_admin(token, carro_id):
    return _request("PATCH", f"/carro/{int(carro_id)}/vender", token=token)


def listar_clientes_admin(token):
    return _request("GET", "/auth/clientes", token=token)


def cadastrar_cliente_admin(token, nome, email, senha):
    # Endpoint exclusivo ADMIN: o backend sempre atribui perfil CLIENTE.
    return _request("POST", "/auth/clientes", token=token,
                    json_dados={"nome": nome, "email": email, "senha": senha})


def desativar_cliente_admin(token, cliente_id):
    # Exclusao logica: preserva historico de agendamentos e auditoria.
    return _request("DELETE", f"/auth/clientes/{int(cliente_id)}", token=token)
