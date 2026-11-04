package br.org.edu.ifrn.LojaCarro.repository;

import br.org.edu.ifrn.LojaCarro.model.TesteDrive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TesteDriveRepository extends JpaRepository<TesteDrive, Long> {

    List<TesteDrive> findAllByOrderByDataHoraDesc();

    List<TesteDrive> findByStatusOrderByDataHoraDesc(String status);
}
