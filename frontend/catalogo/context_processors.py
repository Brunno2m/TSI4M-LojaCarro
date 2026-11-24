def sessao_usuario(request):
    # Nunca envia o JWT ao HTML; somente nome/perfil para navegacao visual.
    return {"usuario_sessao": request.session.get("usuario")}
