package br.org.edu.ifrn.LojaCarro.repository;

import br.org.edu.ifrn.LojaCarro.model.Contabil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContabilRepository extends JpaRepository<Contabil, Long> {

    List<Contabil> findAllByOrderByDataLancamentoDesc();

    List<Contabil> findByTipoOrderByDataLancamentoDesc(String tipo);
}
