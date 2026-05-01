## Ethnos (JavaFX)

Projeto Java do jogo de tabuleiro **Ethnos**, com interface em JavaFX.

## Requisitos

- Java 17+ (recomendado)
- Maven 3.8+

## Como rodar (passo a passo)

1. Abra um terminal na raiz do projeto.
2. Compile o projeto:
   - `mvn clean compile`
3. Rode a aplicação:
   - `mvn javafx:run`

Se preferir, também pode executar a classe principal `com.projeto.ethnos.EthnosApp` pela IDE.

## Configuração inicial da partida

Ao abrir o jogo, você escolhe:

1. **Modo de jogo**:
   - `Contra IA`
   - `Local (turnos)` (todos humanos no mesmo computador, alternando turnos)
2. **Quantidade de jogadores**:
   - de **2 a 6**

## Como jogar (fluxo atual)

### Objetivo

Fazer mais pontos até o fim das eras.

### Turno do jogador

No seu turno, você pode:

1. **Jogar Bando**
   - selecione cartas da mão clicando nelas;
   - escolha um **líder** no seletor da mão;
   - escolha a **região** clicando no tabuleiro;
   - clique em `Jogar Bando`.

2. **Recrutar Aliado**
   - clique em uma carta do mercado para comprar aquela carta;
   - ou clique no deck de compra para comprar do topo.

### Regras implementadas no fluxo

- O líder escolhido precisa estar entre as cartas selecionadas.
- O bando precisa respeitar combinação por **cor ou tribo** com o líder.
- Região selecionada recebe a ficha do jogador.
- Poder do líder é aplicado durante a jogada do bando.
- O mercado inicial é previsível (setup fixo), sem sorteio aleatório inicial.
- Em modo contra IA, os turnos da IA são executados automaticamente.
- Limite de mão: **10 cartas** (se atingir, é necessário jogar bando antes de recrutar).

### Transição de era (essencial)

- A era termina quando **3 dragões** são revelados (ou se não houver mais cartas para revelar).
- No fim da era:
  - é calculada a pontuação por região usando o **ranking de fichas** (1º/2º/3º) e os `valoresPontuacao` da região;
  - o **mercado é limpo**;
  - as **mãos dos jogadores são descartadas**;
  - as cartas descartadas podem ser **recicladas** para o baralho automaticamente quando o baralho acabar.

### Baralho

- O baralho é **embaralhado** no início da partida.

### Rodadas

- A aplicação exibe um contador de **rodada** (a rodada aumenta quando todos os jogadores jogam uma vez).

## Interface

- **Topo**: mercado + deck de compra
- **Centro**: tabuleiro (seleção de região)
- **Base**: mão do jogador da vez (seleção de cartas/líder)
- **Direita**: status da era, dragões, turno atual e pontuação

## Estrutura principal

- `src/main/java/com/projeto/ethnos/EthnosApp.java` - entrada da aplicação
- `src/main/java/com/projeto/ethnos/controller/JogoController.java` - fluxo de ações da UI
- `src/main/java/com/projeto/ethnos/model/*` - regras e estado da partida
- `src/main/java/com/projeto/ethnos/view/*` - componentes visuais JavaFX

## Observações

- O projeto segue os diagramas presentes na raiz como referência de arquitetura.
- Há espaço para evolução de regras específicas de tribos e pontuação avançada por dominância.
