package br.com.valueprojects.mock_spring.builder;

import java.util.Calendar;

import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;
import br.com.valueprojects.mock_spring.model.Resultado;

public class CriadorDeJogo {

	private Jogo jogo;
    private boolean finaliza;

    public CriadorDeJogo() { }

    public CriadorDeJogo para(String descricao) {
        this.jogo = new Jogo(descricao);
        return this;
    }

    public CriadorDeJogo resultado(Participante participante, double metrica) {
        jogo.anota(new Resultado(participante, metrica));
        return this;
    }

    public CriadorDeJogo naData(Calendar data) {
        this.jogo.setData(data);
        return this;
    }

    public CriadorDeJogo finaliza() {
        this.finaliza = true;
        return this;
    }

    public boolean isFinalizado() {
        return finaliza;
    }

    public CriadorDeJogo comId(int id) {
        this.jogo.setId(id);
        return this;
    }

    public int getId() {
        return jogo.getId();
    }

    public Jogo constroi() {
        // antes o flag "finaliza" era ignorado e o jogo saía sempre em andamento
        if (finaliza) jogo.finaliza();
        return jogo;
    }
}
