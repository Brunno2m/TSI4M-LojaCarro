package br.org.edu.ifrn.LojaCarro.repository;

import br.org.edu.ifrn.LojaCarro.model.LogOperacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogOperacaoRepository extends JpaRepository<LogOperacao, Long> {

    /**
     * Retorna todos os logs de operações ordenados do mais recente para o mais antigo.
     */
    List<LogOperacao> findAllByOrderByDataHoraDesc();

    /**
     * Retorna os logs filtrados por tipo de operação (CADASTRO, ATUALIZACAO, EXCLUSAO).
     */
    List<LogOperacao> findByTipoOperacaoOrderByDataHoraDesc(String tipoOperacao);
}
