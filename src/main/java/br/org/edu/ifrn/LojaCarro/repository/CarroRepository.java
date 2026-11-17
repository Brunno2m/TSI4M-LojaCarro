package br.org.edu.ifrn.LojaCarro.repository;

import br.org.edu.ifrn.LojaCarro.model.Carro;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarroRepository extends JpaRepository<Carro, Long> {

    Optional<Carro> findFirstByModelo(String modelo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Carro c WHERE c.id = :id")
    Optional<Carro> buscarParaAgendamento(
            @Param("id") Long id
    );
}
