# devops-mock-AC2 · Ex_A3 Mock Objects (Grupo 7)

Projeto "Gestão de Jogos" (`mock_spring`) da disciplina de DevOps, usado no Ex_A3.
Foco da entrega: **Prática 2**, a regra de que o SMS ao vencedor só é enviado depois que o jogo finalizado e o vencedor forem salvos.

## Integrantes

| Integrante | Commits como piloto |
|---|---|
| Matheus de Alcantara | 01, 06 |
| Rafael Silva Fiamengue | 02, 03 |
| Henrique Bicudo Paulino | 04, 05 |

## Programação em pares com rodízio

Cada commit foi feito por uma dupla: o **piloto** no teclado (autor do commit) e o **navegador** revisando (`Co-authored-by`).
A dupla muda a cada commit, então todos parearam com todos.

| # | Piloto | Navegador | O que entrou |
|---|---|---|---|
| 01 | Matheus | Rafael | Projeto original da disciplina |
| 02 | Rafael | Matheus | Correção dos bugs da Prática 1 e GitHub Actions |
| 03 | Rafael | Henrique | RED: casos de teste da regra do SMS com mocks e stubs |
| 04 | Henrique | Rafael | GREEN: salva o jogo com o vencedor e só então envia o SMS |
| 05 | Henrique | Matheus | JaCoCo exigindo 100% de cobertura e os testes que faltavam |
| 06 | Matheus | Henrique | Práticas 3 e 4 adiantadas e este README |

O commit 03 fica vermelho no GitHub Actions de propósito (RED do TDD); o 04 fica verde.

## Como executar

```bash
./mvnw clean verify
```

Roda os 63 testes e falha se a cobertura do JaCoCo ficar abaixo de 100%. Relatório em `target/site/jacoco/index.html`.
