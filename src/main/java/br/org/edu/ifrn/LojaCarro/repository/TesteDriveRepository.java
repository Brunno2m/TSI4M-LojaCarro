
package br.org.edu.ifrn.LojaCarro.repository;

import br.org.edu.ifrn.LojaCarro.model.TesteDrive;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TesteDriveRepository
        extends JpaRepository<TesteDrive, Long> {

    // Listagem administrativa
    List<TesteDrive> findAllByOrderByDataHoraDesc();

    // Consulta por status
    List<TesteDrive> findByStatusOrderByDataHoraDesc(
            String status
    );

    // Listagem dos agendamentos do cliente
    List<TesteDrive> findByUsuario_IdOrderByDataHoraDesc(
            Long usuarioId
    );

    // Consulta de agendamento pertencente ao cliente
    Optional<TesteDrive> findByIdAndUsuario_Id(
            Long id,
            Long usuarioId
    );

    // Verifica a existencia de horarios conflitantes
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

    // Verifica se o veiculo esta em Test Drive
    @Query("""
        SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
        FROM TesteDrive t
        WHERE t.carro.id = :carroId
          AND t.status = 'EM_ANDAMENTO'
        """)
    boolean existeTesteEmAndamento(
            @Param("carroId") Long carroId
    );

    // Bloqueia o agendamento durante alteracoes
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t FROM TesteDrive t
        WHERE t.id = :id
        """)
    Optional<TesteDrive> buscarParaAtualizacao(
            @Param("id") Long id
    );
}