package br.com.valueprojects.mock_spring.model;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
public class Jogo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String descricao;
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar data;
	// a ordem importa: a regra de "resultados consecutivos" depende do último anotado
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "jogo_id")
	@OrderColumn(name = "ordem")
	private List<Resultado> resultados;
	private boolean finalizado;
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Participante vencedor;

	protected Jogo() {
		// exigido pela JPA
	}

	public Jogo(String descricao) {
		this(descricao, Calendar.getInstance());
	}

	public Jogo(String descricao, Calendar data) {
		this.descricao = descricao;
		this.data = data;
		this.resultados = new ArrayList<Resultado>();
	}

	public void anota(Resultado resultado) {
		if(resultados.isEmpty() || podeTerResultado(resultado.getParticipante())) {
			resultados.add(resultado);
		}
	}

	private boolean podeTerResultado(Participante participante) {
		return !ultimoResultadoRecebido().getParticipante().equals(participante) && qtdDeResultadosDo(participante) <5;
	}

	private int qtdDeResultadosDo(Participante participante) {
		int total = 0;
		for(Resultado l : resultados) {
			if(l.getParticipante().equals(participante)) total++;
		}
		return total;
	}

	private Resultado ultimoResultadoRecebido() {
		return resultados.get(resultados.size()-1);
	}

	public String getDescricao() {
		return descricao;
	}

	public List<Resultado> getResultados() {
		return Collections.unmodifiableList(resultados);
	}

	public Calendar getData() {
		return (Calendar) data.clone();
	}

	public void finaliza() {
		this.finalizado = true;
	}

	public boolean isFinalizado() {
		return finalizado;
	}

	public void defineVencedor(Participante vencedor) {
		this.vencedor = vencedor;
	}

	public Participante getVencedor() {
		return vencedor;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	 public void setData(Calendar data) {
	        this.data = data;
	    }
}
