package br.com.valueprojects.mock_spring.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Resultado {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Participante participante;
	private double metrica;

	protected Resultado() {
		// exigido pela JPA
	}

	public Resultado(Participante participante, double metrica) {
		this.participante = participante;
		this.metrica = metrica;
	}

	public Participante getParticipante() {
		return participante;
	}

	public double getMetrica() {
		return metrica;
	}



}
