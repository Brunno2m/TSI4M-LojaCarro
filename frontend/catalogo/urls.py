from django.urls import path
from . import views

app_name = "catalogo"
urlpatterns = [
    path("", views.inicio, name="inicio"),
    path("veiculos/", views.listagem, name="listagem"),
    path("veiculos/<int:carro_id>/", views.detalhes, name="detalhes"),
    path("veiculos/<int:carro_id>/foto/", views.foto, name="foto"),
    path("entrar/", views.entrar, name="entrar"),
    path("cadastro/", views.cadastro, name="cadastro"),
    path("sair/", views.sair, name="sair"),
    path("painel/", views.painel, name="painel"),
    path("painel/clientes/", views.painel_clientes, name="painel_clientes"),
    path("painel/clientes/novo/", views.painel_cliente_novo, name="painel_cliente_novo"),
    path("painel/clientes/<int:cliente_id>/desativar/", views.painel_cliente_desativar, name="painel_cliente_desativar"),
    path("painel/veiculos/novo/", views.painel_novo, name="painel_novo"),
    path("painel/veiculos/<int:carro_id>/editar/", views.painel_editar, name="painel_editar"),
    path("painel/veiculos/<int:carro_id>/desativar/", views.painel_desativar, name="painel_desativar"),
    path("painel/veiculos/<int:carro_id>/vender/", views.painel_vender, name="painel_vender"),
]
