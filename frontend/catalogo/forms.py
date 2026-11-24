from decimal import Decimal
from pathlib import Path

from django import forms


class FiltroVeiculos(forms.Form):
    marca = forms.CharField(required=False, max_length=60, label="Marca")
    modelo = forms.CharField(required=False, max_length=100, label="Modelo")
    precoMinimo = forms.DecimalField(required=False, min_value=0, max_digits=12, decimal_places=2, label="Preco minimo")
    precoMaximo = forms.DecimalField(required=False, min_value=0, max_digits=12, decimal_places=2, label="Preco maximo")
    anoModelo = forms.IntegerField(required=False, min_value=1886, max_value=2101, label="Ano do modelo")

    def clean(self):
        dados = super().clean()
        minimo, maximo = dados.get("precoMinimo"), dados.get("precoMaximo")
        if minimo is not None and maximo is not None and minimo > maximo:
            self.add_error("precoMaximo", "O preço máximo deve ser maior ou igual ao mínimo.")
        return dados

    def parametros(self):
        return {chave: str(valor) for chave, valor in self.cleaned_data.items()
                if valor is not None and valor != ""}


class LoginForm(forms.Form):
    email = forms.EmailField(label="E-mail", max_length=160,
                             widget=forms.EmailInput(attrs={"autocomplete": "username"}))
    senha = forms.CharField(label="Senha", max_length=72,
                            widget=forms.PasswordInput(attrs={"autocomplete": "current-password"}))


class CadastroForm(forms.Form):
    nome = forms.CharField(label="Nome completo", max_length=120)
    email = forms.EmailField(label="E-mail", max_length=160,
                             widget=forms.EmailInput(attrs={"autocomplete": "email"}))
    senha = forms.CharField(label="Senha (mínimo 8 caracteres)", min_length=8, max_length=72,
                            widget=forms.PasswordInput(attrs={"autocomplete": "new-password"}))
    confirmar_senha = forms.CharField(label="Confirmar senha",
                                      widget=forms.PasswordInput(attrs={"autocomplete": "new-password"}))

    def clean(self):
        dados = super().clean()
        senha = dados.get("senha")
        if senha and len(senha.encode("utf-8")) > 72:
            self.add_error("senha", "A senha deve possuir no máximo 72 bytes UTF-8.")
        if senha and dados.get("confirmar_senha") and senha != dados["confirmar_senha"]:
            self.add_error("confirmar_senha", "As senhas não coincidem.")
        return dados


class VeiculoForm(forms.Form):
    marca = forms.CharField(label="Marca", max_length=60)
    modelo = forms.CharField(label="Modelo", max_length=100)
    ano = forms.IntegerField(label="Ano de fabricação", min_value=1886, max_value=2100)
    anoModelo = forms.IntegerField(label="Ano do modelo", min_value=1886, max_value=2101)
    cor = forms.CharField(label="Cor", max_length=40)
    quilometragem = forms.IntegerField(label="Quilometragem", min_value=0)
    tipoCombustivel = forms.CharField(label="Combustível", max_length=30)
    tipoTransmissao = forms.CharField(label="Transmissão", max_length=30)
    preco = forms.DecimalField(label="Preço (R$)", min_value=Decimal("0.01"),
                               max_digits=12, decimal_places=2)
    descricao = forms.CharField(label="Descrição", max_length=2000,
                                widget=forms.Textarea(attrs={"rows": 4}))
    situacao = forms.ChoiceField(label="Situação", choices=[
        ("DISPONIVEL", "Disponível"), ("VENDIDO", "Vendido"), ("INATIVO", "Inativo")])
    foto = forms.FileField(label="Fotografia (JPG, PNG ou WebP, até 5 MB)", required=False)

    def __init__(self, *args, exige_foto=False, **kwargs):
        self.exige_foto = exige_foto
        super().__init__(*args, **kwargs)

    def clean_foto(self):
        foto = self.cleaned_data.get("foto")
        if not foto:
            if self.exige_foto:
                raise forms.ValidationError("Selecione uma fotografia do veículo.")
            return foto
        if foto.size > 5 * 1024 * 1024:
            raise forms.ValidationError("A fotografia deve ter até 5 MB.")
        extensao = Path(foto.name).suffix.lower()
        if extensao not in {".jpg", ".jpeg", ".png", ".webp"}:
            raise forms.ValidationError("Envie uma imagem JPG, PNG ou WebP.")
        if foto.content_type not in {"image/jpeg", "image/png", "image/webp"}:
            raise forms.ValidationError("Tipo de arquivo não permitido.")
        return foto

    def dados_api(self):
        campos = ("marca", "modelo", "ano", "anoModelo", "cor", "quilometragem",
                  "tipoCombustivel", "tipoTransmissao", "descricao", "situacao")
        dados = {campo: self.cleaned_data[campo] for campo in campos}
        dados["preco"] = float(self.cleaned_data["preco"])
        return dados
