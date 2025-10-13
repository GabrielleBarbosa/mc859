# GRASP MAX-SC-QBF

Uma implementação modular do algoritmo GRASP (Greedy Randomized Adaptive Search Procedure) para resolver o problema MAX-SC-QBF (Maximization of Quadratic Binary Function with Set Cover constraints).

## 📋 Descrição do Problema

O problema MAX-SC-QBF consiste em:
- **Maximizar** uma função binária quadrática: `f(x) = x'.A.x`
- **Sujeito a** restrições de cobertura de conjuntos (todos os elementos devem ser cobertos)

Onde:
- `x` é um vetor binário de variáveis
- `A` é uma matriz de coeficientes
- Cada variável binária corresponde a um subconjunto que cobre determinados elementos

### Algoritmo GRASP
- **Fase Construtiva**: Constrói soluções usando RCL com randomização controlada
- **Fase de Busca Local**: Melhora soluções através de movimentos de vizinhança
- **Multi-start**: Executa múltiplas iterações independentes

### Movimentos de Busca Local
- **Inserção**: Adiciona elemento à solução
- **Remoção**: Remove elemento (mantendo factibilidade)
- **Troca**: Substitui um elemento por outro


## 🏗️ Estrutura do Projeto

```
grasp_maxsc_qbf/
├── main.py                    # Script principal de execução
├── core/                      # Componentes base do framework
│   ├── __init__.py
│   ├── solution.py           # Classe Solution genérica
│   ├── evaluator.py          # Interface abstrata Evaluator
│   └── abstract_grasp.py     # Implementação base do GRASP
├── problems/                  # Implementações específicas de problemas
│   ├── __init__.py
│   └── qbf_sc.py            # Avaliador para MAX-SC-QBF
├── algorithms/               # Algoritmos específicos
│   ├── __init__.py
│   └── grasp_qbf_sc.py      # GRASP especializado para QBF-SC
└── utils/                    # Utilitários
    ├── __init__.py
    └── instance_generator.py # Gerador de instâncias de teste
```

## 📦 Módulos

### 🔧 Core (`core/`)

#### `solution.py`
Classe genérica para representar soluções de problemas de otimização:
- Armazena elementos da solução e seu custo
- Operações básicas: adicionar, remover, verificar pertencimento
- Suporte para construtor de cópia e iteração

#### `evaluator.py`
Interface abstrata para avaliadores de problemas:
- `evaluate()`: Avalia uma solução completa
- `evaluate_insertion_cost()`: Custo de inserir um elemento
- `evaluate_removal_cost()`: Custo de remover um elemento
- `evaluate_exchange_cost()`: Custo de trocar elementos
- `is_feasible()`: Verifica factibilidade

#### `abstract_grasp.py`
Implementação base do algoritmo GRASP:
- **Métodos de construção**: Standard, Random+Greedy, Sampled Greedy
- **Busca local**: First Improving, Best Improving

### 🎯 Problems (`problems/`)

#### `qbf_sc.py`
Implementação específica do avaliador MAX-SC-QBF:
- Lê instâncias no formato especificado
- Avalia função quadrática binária eficientemente
- Verifica restrições de cobertura de conjuntos

### 🚀 Algorithms (`algorithms/`)

#### `grasp_qbf_sc.py`
GRASP especializado para o problema MAX-SC-QBF:
- Implementa operações específicas do problema
- Busca local com inserção, remoção e troca
- Estratégias para manter factibilidade

### 🛠️ Utils (`utils/`)

#### `instance_generator.py`
Gerador de instâncias para teste e desenvolvimento

## 📊 Formato da Instância

```
<n>                           # número de variáveis binárias
<s1> <s2> ... <sn>           # tamanhos dos subconjuntos
<elementos de S1>            # elementos cobertos por S1
<elementos de S2>            # elementos cobertos por S2
...
<elementos de Sn>            # elementos cobertos por Sn
<a11> <a12> ... <a1n>       # matriz QBF (triangular superior)
<a22> ... <a2n>
...
<ann>
```

**Exemplo:**
```
4
2 3 2 1
1 2
2 3 4
1 4
3
10 -2 3 1
5 0 -1
8 4
-2
```

## 🚀 Como Usar

### Execução Básica
```bash
# Executar com instância específica
python main.py instancia.txt

# Com parâmetros personalizados
python main.py instancia.txt 0.3 100 standard first_improving
```

## ⚙️ Configurações Disponíveis

### Métodos de Construção
- **`standard`**: GRASP clássico com RCL baseado em α
- **`random_plus_greedy`**: 30% seleção aleatória + conclusão gulosa
- **`sampled_greedy`**: Amostra 50% dos candidatos e escolhe o melhor

### Métodos de Busca Local
- **`first_improving`**: Para no primeiro movimento que melhora
- **`best_improving`**: Avalia todos os movimentos e escolhe o melhor

### Parâmetros
- **`alpha`** (0.0-1.0): Controla ganância vs aleatoriedade (0=guloso, 1=aleatório)
- **`iterations`**: Número de iterações do GRASP