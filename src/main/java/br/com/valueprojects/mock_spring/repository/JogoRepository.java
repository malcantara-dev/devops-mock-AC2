package br.com.valueprojects.mock_spring.repository;

import java.util.Calendar;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.valueprojects.mock_spring.model.Jogo;

public interface JogoRepository extends JpaRepository<Jogo, Integer> {

	// query derivada do nome do método
	List<Jogo> findByFinalizado(boolean finalizado);

	// mesma regra do FinalizaJogo (iniciados há 7 dias ou mais), mas filtrada no banco
	@Query("select j from Jogo j where j.finalizado = false and j.data <= :limite")
	List<Jogo> emAndamentoIniciadosAte(@Param("limite") Calendar limite);
}
