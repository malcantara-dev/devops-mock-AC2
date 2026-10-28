package br.com.valueprojects.mock_spring.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.valueprojects.mock_spring.model.Participante;

public interface ParticipanteRepository extends JpaRepository<Participante, Integer> {

	Optional<Participante> findFirstByNome(String nome);
}
