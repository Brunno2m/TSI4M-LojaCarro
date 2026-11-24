"""Views publicas e area administrativa; negocio/autorizacao de API ficam no Spring Boot."""
from functools import wraps
from urllib.parse import urlencode

from django.http import HttpResponse, HttpResponseForbidden
from django.shortcuts import redirect, render
from django.templatetags.static import static
from django.urls import reverse
from django.utils.http import url_has_allowed_host_and_scheme
from django.views.decorators.http import require_GET, require_http_methods, require_POST, require_safe
from django.middleware.csrf import rotate_token

from . import api
from .forms import CadastroForm, FiltroVeiculos, LoginForm, VeiculoForm


@require_safe
def inicio(request):
    try:
        veiculos = api.listar_veiculos()[:3]
        erro = False
    except (api.ApiIndisponivel, api.VeiculoNaoEncontrado):
        veiculos, erro = [], True
    return render(request, "catalogo/inicio.html", {"veiculos": veiculos, "erro": erro}, status=503 if erro else 200)


@require_safe
def listagem(request):
    formulario = FiltroVeiculos(request.GET)
    veiculos, erro = [], False
    if formulario.is_valid():
        try:
            veiculos = api.listar_veiculos(formulario.parametros())
        except (api.ApiIndisponivel, api.VeiculoNaoEncontrado):
            erro = True
    return render(request, "catalogo/listagem.html", {
        "formulario": formulario, "veiculos": veiculos, "erro": erro,
    }, status=503 if erro else 200)


@require_safe
def detalhes(request, carro_id):
    try:
        carro = api.buscar_veiculo(carro_id)
    except api.VeiculoNaoEncontrado:
        return render(request, "catalogo/erro.html", {"titulo": "Veículo não encontrado", "mensagem": "Este veículo não está mais disponível no catálogo."}, status=404)
    except api.ApiIndisponivel:
        return render(request, "catalogo/erro.html", {"titulo": "Catálogo temporariamente indisponível", "mensagem": "Não foi possível consultar o veículo. Tente novamente em instantes."}, status=503)
    return render(request, "catalogo/detalhes.html", {"carro": carro})


@require_safe
def foto(request, carro_id):
    try:
        conteudo, tipo = api.buscar_foto(carro_id)
    except (api.VeiculoNaoEncontrado, api.ApiIndisponivel):
        resposta = redirect(static("catalogo/sem-foto.svg"))
        resposta["Cache-Control"] = "no-store"
        return resposta
    resposta = HttpResponse(conteudo, content_type=tipo)
    resposta["X-Content-Type-Options"] = "nosniff"
    resposta["Cache-Control"] = "no-cache"
    return resposta


def _erro(request, titulo, mensagem, status):
    return render(request, "catalogo/erro.html", {"titulo": titulo, "mensagem": mensagem}, status=status)


def _acao_protegida(request, funcao):
    """Transforma erros de Spring Boot em respostas controladas no Django."""
    try:
        return funcao()
    except api.ErroAPI as exc:
        if exc.status == 401:
            request.session.flush()
            return redirect(reverse("catalogo:entrar") + "?expirou=1")
        if exc.status == 403:
            return _erro(request, "Acesso negado", "Você não possui permissão para esta operação.", 403)
        if exc.status == 404:
            return _erro(request, "Registro não encontrado", "Este registro não existe mais.", 404)
        if exc.status in {400, 409, 413, 415, 422}:
            return _erro(request, "Dados não aceitos", "Confira os dados do formulário e tente novamente.", exc.status)
        return _erro(request, "Falha na operação", "Não foi possível concluir esta operação.", 502)
    except api.ApiIndisponivel:
        return _erro(request, "Backend indisponível", "Não foi possível comunicar com o Spring Boot.", 503)


def _somente_admin(funcao):
    @wraps(funcao)
    def protegido(request, *args, **kwargs):
        usuario = request.session.get("usuario")
        token = request.session.get("jwt")
        if not usuario or not token:
            destino = reverse("catalogo:entrar")
            return redirect(destino + "?" + urlencode({"next": request.get_full_path()}))
        if usuario.get("perfil") != "ADMIN":
            return _erro(request, "Acesso negado", "Esta área é exclusiva do administrador.", 403)
        return funcao(request, *args, **kwargs)
    return protegido


def _destino_apos_login(request):
    proximo = request.POST.get("next", "") or request.GET.get("next", "")
    if proximo.startswith("/") and not proximo.startswith("//") and url_has_allowed_host_and_scheme(
        proximo, allowed_hosts={request.get_host()}, require_https=request.is_secure()
    ):
        return proximo
    return reverse("catalogo:painel") if request.session.get("usuario", {}).get("perfil") == "ADMIN" else reverse("catalogo:inicio")


@require_http_methods(["GET", "POST"])
def entrar(request):
    if request.method == "GET" and request.session.get("jwt"):
        return redirect(_destino_apos_login(request))
    formulario = LoginForm(request.POST or None)
    if request.method == "POST" and formulario.is_valid():
        try:
            resposta = api.autenticar(formulario.cleaned_data["email"], formulario.cleaned_data["senha"])
        except api.ErroAPI as exc:
            if exc.status == 401:
                formulario.add_error(None, "E-mail ou senha inválidos.")
            else:
                formulario.add_error(None, "Não foi possível entrar. Tente novamente.")
        except api.ApiIndisponivel:
            return _erro(request, "Backend indisponível", "Não foi possível comunicar com o Spring Boot.", 503)
        else:
            request.session.flush()
            rotate_token(request)
            request.session["jwt"] = resposta["token"]
            request.session["usuario"] = {
                "id": resposta["usuario"].get("id"),
                "nome": resposta["usuario"].get("nome"),
                "email": resposta["usuario"].get("email"),
                "perfil": resposta["usuario"]["perfil"],
            }
            request.session.set_expiry(min(3600, resposta["expiresIn"]))
            return redirect(_destino_apos_login(request))
    return render(request, "catalogo/entrar.html", {"formulario": formulario, "proximo": request.POST.get("next", "") or request.GET.get("next", ""), "cadastro_ok": request.GET.get("cadastro") == "ok", "expirou": request.GET.get("expirou") == "1"})


@require_http_methods(["GET", "POST"])
def cadastro(request):
    formulario = CadastroForm(request.POST or None)
    if request.method == "POST" and formulario.is_valid():
        try:
            api.cadastrar_cliente(formulario.cleaned_data["nome"], formulario.cleaned_data["email"], formulario.cleaned_data["senha"])
        except api.ErroAPI as exc:
            if exc.status == 409:
                formulario.add_error("email", "Este e-mail já está cadastrado.")
            elif exc.status in {400, 422}:
                formulario.add_error(None, "Os dados não foram aceitos pelo backend.")
            else:
                formulario.add_error(None, "Não foi possível concluir o cadastro.")
        except api.ApiIndisponivel:
            return _erro(request, "Backend indisponível", "Não foi possível comunicar com o Spring Boot.", 503)
        else:
            return redirect(reverse("catalogo:entrar") + "?cadastro=ok")
    return render(request, "catalogo/cadastro.html", {"formulario": formulario})


@require_POST
def sair(request):
    request.session.flush()
    return redirect("catalogo:inicio")


@_somente_admin
@require_GET
def painel(request):
    def carregar():
        veiculos = api.listar_admin(request.session["jwt"])
        dados = {"veiculos": veiculos, "total": len(veiculos),
                 "disponiveis": sum(v.get("situacao") == "DISPONIVEL" for v in veiculos),
                 "vendidos": sum(v.get("situacao") == "VENDIDO" for v in veiculos),
                 "inativos": sum(v.get("situacao") == "INATIVO" for v in veiculos)}
        return render(request, "catalogo/painel.html", dados)
    return _acao_protegida(request, carregar)


@_somente_admin
@require_GET
def painel_clientes(request):
    return _acao_protegida(request, lambda: render(request, "catalogo/clientes.html", {
        "clientes": api.listar_clientes_admin(request.session["jwt"])
    }))


@_somente_admin
@require_http_methods(["GET", "POST"])
def painel_cliente_novo(request):
    formulario = CadastroForm(request.POST or None)
    if request.method == "POST" and formulario.is_valid():
        def salvar():
            try:
                api.cadastrar_cliente_admin(
                    request.session["jwt"],
                    formulario.cleaned_data["nome"],
                    formulario.cleaned_data["email"],
                    formulario.cleaned_data["senha"],
                )
            except api.ErroAPI as exc:
                if exc.status == 409:
                    formulario.add_error("email", "Este e-mail já está cadastrado.")
                elif exc.status in (400, 422):
                    formulario.add_error(None, "Verifique os dados informados.")
                else:
                    raise
            else:
                return redirect("catalogo:painel_clientes")
            return render(request, "catalogo/cliente_form.html", {"formulario": formulario})
        return _acao_protegida(request, salvar)
    return render(request, "catalogo/cliente_form.html", {"formulario": formulario})


@_somente_admin
@require_POST
def painel_cliente_desativar(request, cliente_id):
    def desativar():
        api.desativar_cliente_admin(request.session["jwt"], cliente_id)
        return redirect("catalogo:painel_clientes")
    return _acao_protegida(request, desativar)


@_somente_admin
@require_http_methods(["GET", "POST"])
def painel_novo(request):
    formulario = VeiculoForm(request.POST or None, request.FILES or None, exige_foto=True)
    if request.method == "POST" and formulario.is_valid():
        return _acao_protegida(request, lambda: _salvar_novo(request, formulario))
    return render(request, "catalogo/veiculo_form.html", {"formulario": formulario, "editando": False})


def _salvar_novo(request, formulario):
    api.salvar_admin(request.session["jwt"], formulario.dados_api(), formulario.cleaned_data["foto"])
    return redirect("catalogo:painel")


@_somente_admin
@require_http_methods(["GET", "POST"])
def painel_editar(request, carro_id):
    def editar():
        carro = api.buscar_admin(request.session["jwt"], carro_id)
        formulario = VeiculoForm(request.POST or None, request.FILES or None, initial=carro)
        if request.method == "POST" and formulario.is_valid():
            api.atualizar_admin(request.session["jwt"], carro_id,
                                formulario.dados_api(), formulario.cleaned_data.get("foto"))
            return redirect("catalogo:painel")
        return render(request, "catalogo/veiculo_form.html", {
            "formulario": formulario, "editando": True, "carro": carro,
        })
    return _acao_protegida(request, editar)


@_somente_admin
@require_POST
def painel_desativar(request, carro_id):
    return _acao_protegida(request, lambda: _desativar(request, carro_id))


def _desativar(request, carro_id):
    api.desativar_admin(request.session["jwt"], carro_id)
    return redirect("catalogo:painel")


@_somente_admin
@require_POST
def painel_vender(request, carro_id):
    return _acao_protegida(request, lambda: _vender(request, carro_id))


def _vender(request, carro_id):
    api.vender_admin(request.session["jwt"], carro_id)
    return redirect("catalogo:painel")
