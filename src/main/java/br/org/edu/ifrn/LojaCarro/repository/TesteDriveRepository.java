package br.org.edu.ifrn.LojaCarro.repository;

import br.org.edu.ifrn.LojaCarro.model.TesteDrive;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface TesteDriveRepository
        extends JpaRepository<TesteDrive, Long> {

    // Consultas existentes
    List<TesteDrive> findAllByOrderByDataHoraDesc();

    List<TesteDrive> findByStatusOrderByDataHoraDesc(
            String status
    );

    // Verifica se existe reserva em horário conflitante
    @Query("""
        SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
        FROM TesteDrive t
        WHERE t.carro.id = :carroId
          AND t.status IN (:statusAtivos)
          AND t.dataHora < :fim
          AND t.fim > :inicio
        """)
    boolean existeConflito(
            @Param("carroId") Long carroId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("statusAtivos") Collection<String> statusAtivos
    );

    // Verifica se o veículo está em teste neste momento
    @Query("""
        SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
        FROM TesteDrive t
        WHERE t.carro.id = :carroId
          AND t.status = 'EM_ANDAMENTO'
        """)
    boolean existeTesteEmAndamento(
            @Param("carroId") Long carroId
    );
}
