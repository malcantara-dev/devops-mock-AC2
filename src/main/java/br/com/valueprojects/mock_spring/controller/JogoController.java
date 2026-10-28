package br.com.valueprojects.mock_spring.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.service.JogoService;
import br.com.valueprojects.mock_spring.service.ResultadoJulgamento;

import java.net.URI;
import java.util.List;

@RestController

@RequestMapping("/jogos")
public class JogoController {

    // antes: lista em memória + Juiz único (estado compartilhado entre requisições)
    private final JogoService service;

    public JogoController(JogoService service) {
        this.service = service;
    }

    // Criar um novo jogo
    @PostMapping
    public ResponseEntity<Jogo> criarJogo(@RequestBody NovoJogoRequest request) {
        Jogo jogo = service.cria(request.descricao());
        URI local = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(jogo.getId()).toUri();
        return ResponseEntity.created(local).body(jogo);
    }

    // Buscar um jogo
    @GetMapping("/{id}")
    public Jogo buscarJogo(@PathVariable int id) {
        return service.busca(id);
    }

    // Anotar o resultado de um participante
    @PostMapping("/{id}/resultados")
    public Jogo anotarResultado(@PathVariable int id, @RequestBody NovoResultadoRequest request) {
        return service.anotaResultado(id, request.participante(), request.metrica());
    }

    // Julgar um jogo
    @PostMapping("/{id}/julgar")
    public ResultadoJulgamento julgarJogo(@PathVariable int id) {
        return service.julga(id);
    }

    // Listar todos os jogos
    @GetMapping
    public List<Jogo> listarJogos() {
        return service.lista();
    }

}
