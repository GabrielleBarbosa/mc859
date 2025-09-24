# MO824 - Atividade 3: Metaheurística Tabu Search para o Problema do Set Covering com Fórmula Booleana Quantificada (SC-QBF)

Este projeto implementa uma metaheurística de Busca Tabu (Tabu Search) para resolver o Problema MAX-SC-QBF.

## Visão Geral

O objetivo deste projeto é explorar diferentes estratégias e configurações da metaheurística Tabu Search para o problema MAX-SC-QBF. O script principal para a execução dos experimentos computacionais é o `experiment.py`.

## Estrutura do Projeto

A estrutura do projeto está organizada da seguinte forma (desconsiderando as implementações já presentes no framework original):

```
ativ03/
├── experiment.py                  # Script principal para execução dos experimentos
├── main.py                        # Script para execução de uma única instância
├── instances/
│   └── sc_qbf/                    # Instâncias do problema SC-QBF
├── src/
│   ├── metaheuristics/
│   │   └── tabusearch/            # Implementação da metaheurística Tabu Search
│   └── problems/
│       └── sc_qbf/                # Implementação do problema SC-QBF
│           ├── sc_qbf.py          # Definição do problema SC-QBF (padrão do framework - minimização)
|           ├── sc_qbf_inverse.py  # SC-QBF para maximização (inversão dos sinais)
│           └── solvers/
│               └── ts_sc_qbf.py   # Solver Tabu Search para o SC-QBF
```

## Executando os Experimentos

O script `experiment.py` foi projetado para executar uma série de experimentos computacionais com diferentes configurações da metaheurística Tabu Search.

Para executar os experimentos, basta executar o seguinte comando na raiz do diretório `ativ03`:

```bash
python experiment.py
```

O script irá iterar sobre todas as instâncias localizadas no diretório `instances/sc_qbf/` e para cada instância, executará o Tabu Search com as seguintes configurações:

- **PADRÃO**: Configuração padrão com tenure de 0.2, busca local do tipo "first improving" e estratégia padrão.
- **PADRÃO+BEST**: Utiliza a busca local do tipo "best improving".
- **PADRÃO+TENURE**: Utiliza um tenure de 10.
- **PADRÃO+METHOD1**: Utiliza a estratégia "probabilistic".
- **PADRÃO+METHOD2**: Utiliza a estratégia "diversification by restart".
- **PADRÃO+METHOD3**: Utiliza a busca "best improving" com a estratégia "diversification by restart".

Ao final da execução, uma tabela com o resumo dos resultados é exibida no console.
