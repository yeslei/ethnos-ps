# Ethnos — Projeto de Software (UFF)

Implementação do jogo de tabuleiro **Ethnos** em Java + JavaFX, desenvolvido
como trabalho prático da disciplina de Projeto de Software (6º período –
Sistemas de Informação / UFF).

> A descrição do trabalho pede, na Parte 2, "**a versão final do produto,
> diagramas atualizados, pelo menos 2 padrões GRASP e 2 padrões GoF
> apropriados, e um detalhamento das dificuldades encontradas e decisões
> tomadas durante o desenvolvimento**". Este documento cobre todos esses
> pontos.

---

## Sumário

1. [Como executar](#como-executar)
2. [Como o jogo funciona](#como-o-jogo-funciona)
3. [Arquitetura e diagramas](#arquitetura-e-diagramas)
4. [Padrões GoF aplicados](#padrões-gof-aplicados)
5. [Padrões GRASP aplicados](#padrões-grasp-aplicados)
6. [Decisões de projeto](#decisões-de-projeto)
7. [Dificuldades encontradas](#dificuldades-encontradas)
8. [Testes](#testes)
9. [Estrutura de pastas](#estrutura-de-pastas)

---

## Como executar

Requisitos: **Java 17+** e **Maven 3.8+**.

```bash
# Compilar
mvn clean compile

# Rodar com interface gráfica (JavaFX)
mvn javafx:run

# Rodar testes
mvn test

# Rodar a demo de console (sem JavaFX — útil em máquinas sem o módulo)
mvn compile
java -cp target/classes com.projeto.ethnos.DemoConsole
```

A demo de console existe para provar uma propriedade importante do projeto:
**o modelo do jogo é independente da interface gráfica.** Trocar a UI para
qualquer outra tecnologia (console, web, mobile) não exige tocar em uma
linha de regra de negócio. Isso é a aplicação prática de GRASP **Low
Coupling** e do padrão **Observer**.

---

## Como o jogo funciona

### Objetivo

Fazer mais pontos do que os adversários ao longo de três Eras.

### Setup

- 2 a 6 jogadores (humanos ou IA).
- **Antes de começar, o jogador escolhe quais tribos vão participar** (regra
  fiel ao jogo original). Mínimo de 3 tribos. Os 3 Dragões sempre entram.
- Cada jogador recebe 4 a 6 cartas iniciais (depende da quantidade de
  jogadores) e um estoque de 15 fichas.
- O mercado é preenchido com 5 cartas viradas para cima.

### No seu turno, escolha uma de duas ações

**1. Recrutar Aliado** — pegue uma carta:
- do mercado (clique numa das cartas viradas), **ou**
- do topo do baralho (clique no "Comprar").

**2. Jogar Bando** — baixe um grupo de cartas:
- Selecione cartas da sua mão clicando nelas.
- Selecione UMA das cartas e clique em **"Marcar como líder ★"** —
  uma estrela amarela aparece sobre ela.
- Escolha uma **região** clicando no tabuleiro.
- Clique em **Jogar Bando**.

Regras do bando:
- Todas as cartas precisam combinar **em cor OU em tribo** com o líder.
- **Regra do N-ésimo marcador** (regra fiel ao jogo original): para
  plantar o N-ésimo marcador na região, o bando precisa ter no mínimo N
  cartas. Ou seja, para o 1º marcador basta 1 carta, mas para o 2º na
  mesma região o bando precisa ter pelo menos 2 cartas.
- Minotauro como líder reduz esse requisito em 1.
- Cartas restantes na mão voltam para o mercado.

### Pontuação do bando (tabela oficial)

Pontos ganhos ao baixar um bando, em função do seu tamanho:

| Tamanho | Pontos |
|---------|--------|
| 1       | 0      |
| 2       | 1      |
| 3       | 3      |
| 4       | 6      |
| 5       | 10     |
| 6+      | 15     |

Anão como líder conta como se o bando tivesse +1 carta para esse
cálculo (bônus do anão).

### Poderes dos líderes

Cada tribo tem um poder específico que dispara quando uma carta dela
lidera um bando:

| Tribo      | Efeito                                                             |
|------------|--------------------------------------------------------------------|
| Anão       | +1 ponto + bônus na tabela de pontuação do bando                   |
| Elfo       | Recupera uma carta do bando para a mão                             |
| Gigante    | +2 pontos                                                          |
| Mago       | Compra 1 carta do baralho                                          |
| Troll      | +1 ponto por carta do bando (mín. 1)                               |
| Minotauro  | Coloca uma ficha adicional na região e reduz requisito de tamanho  |
| Centauro   | Compra 1 carta do baralho                                          |
| Esqueleto  | Drena 1 ponto do líder em pontos                                   |
| Dragão     | Carta especial, não vai para a mão                                 |

### Fim de Era

Uma Era termina quando **3 Dragões são revelados** (durante compras ou
ao completar o mercado). Os 2 primeiros dragões revelados ficam visíveis
mas **não têm efeito** — apenas o terceiro inicia a fase de pontuação.

Quando a era termina:

1. Para cada região, contamos os marcadores de cada jogador.
2. Pontuação por era (regra fiel ao jogo original):
   - **Era 1**: apenas o 1º colocado pontua, ganhando 1 ponto.
   - **Era 2**: 1º colocado ganha 3, 2º ganha 1.
   - **Era 3**: 1º colocado ganha 6, 2º ganha 3, 3º ganha 1.
3. Mercado e mãos são descartados.
4. Começa a próxima Era (são 3 no total).

Ao fim da terceira Era, o jogador com mais pontos vence.

---

## Arquitetura e diagramas

A arquitetura segue **MVC**:

- **Model** (`com.projeto.ethnos.model`): regras, estado e padrões.
- **View** (`com.projeto.ethnos.view`): JavaFX, implementam `Assinante`.
- **Controller** (`com.projeto.ethnos.controller.JogoController`):
  traduz eventos JavaFX em chamadas ao Model.

Os diagramas atualizados estão na raiz:

- `diagramadeclasses.png` — diagrama de classes (Parte 2, com `Assinante`,
  `Carta` abstrata e operações de Observer em `Partida`).
- `diagramadesequencia.jpeg` — diagrama de sequência da jogada de bando.
- `diagramadecomunicacao.jpeg` — diagrama de comunicação correspondente.

> **Importante para a apresentação:** o diagrama de classes mostra a
> classe abstrata **Assinante** com a operação `atualiza(p: Partida)`,
> que é o papel de Observer no padrão. **Partida** tem
> `adicionaAssinante`, `removeAssinante` e `notifica` — papel de Subject.

---

## Padrões GoF aplicados

> O enunciado pede **pelo menos 2 padrões GoF**. Aplicamos **3**: Observer,
> Strategy e Factory Method.

### 1. Observer — `Assinante` / `Partida`

**Onde:**
- `com.projeto.ethnos.observer.Assinante` (interface, papel de Observer)
- `com.projeto.ethnos.model.Partida` (papel de Subject)
- Implementado por `MercadoView`, `MaoView`, `TabuleiroView`, `StatusView`

**Por que aplicar aqui:**
A Partida muda de estado constantemente (jogada feita, era trocada,
dragão revelado…), e várias regiões da tela precisam refletir essa
mudança ao mesmo tempo. Sem Observer, a Partida teria que conhecer e
chamar diretamente cada `view.atualizar()`, importando classes do JavaFX
— um forte acoplamento entre Model e View.

Com Observer:
- A Partida só conhece a interface `Assinante`.
- Adicionar uma nova View (ex.: um histórico de jogadas) não exige
  modificar Partida — basta a nova view se inscrever no construtor.
- O modelo pode ser executado **sem JavaFX nenhum**, como prova a
  classe `DemoConsole`.

**Trecho-chave:**
```java
// Partida.java
public void adicionaAssinante(Assinante obs) { ... }
public void removeAssinante(Assinante obs) { ... }
public void notifica() {
    for (Assinante a : new ArrayList<>(assinantes)) a.atualiza(this);
}
```

### 2. Strategy — `PoderDoLider` e as subclasses de `Carta`

**Onde:**
- `com.projeto.ethnos.model.poder.PoderDoLider` (interface)
- Cada subclasse de `Carta` (`Anao`, `Elfo`, `Gigante`, `Mago`, `Troll`,
  `Minotauro`, `Centauro`, `Esqueleto`) declara seu próprio `PoderDoLider`.
- `Partida.aplicarPoderDoLider` delega via `lider.getPoder().executar(...)`.

**Por que aplicar aqui:**
Na primeira versão (Parte 1) os poderes viviam dentro de um `switch` na
classe `Partida`. Isso significava que (a) toda nova tribo exigia
modificar a Partida (violando Open/Closed), (b) testar um poder isolado
era trabalhoso, e (c) a coesão de Partida ficava baixa porque ela tomava
conta tanto do fluxo do jogo quanto das regras de cada tribo.

Com Strategy, cada poder é um objeto encapsulado. A Partida agora apenas
delega: `lider.getPoder().executar(partida, jogador, bando, regiao)`. O
`switch` desapareceu.

**Trecho-chave:**
```java
// Anao.java
public PoderDoLider getPoder() {
    return (partida, jogador, bando, regiao) -> {
        jogador.adicionarPontos(1);
        return "Poder (Anão): +1 ponto para " + jogador.getNome();
    };
}
```

(No código real preferimos uma classe nomeada `PoderAnao` em vez de
lambda, para facilitar debug e stack traces — mas o efeito é o mesmo.)

### 3. Factory Method — `CartaFactory`

**Onde:** `com.projeto.ethnos.factory.CartaFactory`

**Por que aplicar aqui:**
Antes, o setup do jogo instanciava manualmente cada tribo com
`new Anao(...)`, `new Elfo(...)`, etc. Isso espalhava o conhecimento
sobre as classes concretas pelo `EthnosApp` e pelos testes.

A Factory centraliza isso. Adicionar uma nova tribo passou a exigir
apenas: (1) criar a subclasse, (2) registrar no switch da Factory. O
restante do sistema permanece intacto.

Também ganhamos um método utilitário `montarBaralhoPadrao()` que
encapsula a distribuição de cartas e a posição dos Dragões. Se quisermos
suportar variantes de tema do jogo (Archeos Society, por exemplo), basta
criar uma segunda fábrica e selecionar pela configuração — caminho
natural para evoluir para um **Abstract Factory** no futuro.

**Trecho-chave:**
```java
// EthnosApp.java
Baralho baralho = CartaFactory.montarBaralhoPadrao();
// EthnosApp não importa nenhuma classe concreta de tribo.
```

---

## Padrões GRASP aplicados

> O enunciado pede **pelo menos 2 padrões GRASP**. Aplicamos **5**:
> Controller, Creator, Information Expert, Low Coupling, High Cohesion
> (Polymorphism também aparece implicitamente via Strategy).

### 1. Controller — `Partida`

`Partida` é o objeto que recebe as **operações de sistema** (jogar bando,
recrutar aliado, iniciar/terminar era). Ela não é um objeto da UI nem
representa um conceito do mundo real do jogo — é o "controlador de
fachada" que coordena os demais objetos.

Note que o projeto tem **dois "Controllers"** com papéis diferentes:

| Classe          | Papel                                                      |
|-----------------|------------------------------------------------------------|
| `Partida`       | Controller no sentido GRASP — operações de sistema         |
| `JogoController`| Controller no sentido MVC — traduz eventos da UI           |

`JogoController` é uma fina camada de adaptação JavaFX → Partida. Ele
não conhece as regras do jogo.

### 2. Creator — `Jogador` cria `Bando`

Quem deve instanciar `Bando`? O padrão Creator diz: aquele que (a) agrega
o objeto criado, (b) contém ou conhece a informação necessária para
criar, ou (c) usa de perto o objeto.

`Jogador` cumpre todos os três critérios:
- O diagrama mostra a composição `Jogador ◆--Possui-- 1..* Bando`.
- `Jogador` possui a mão de onde saem as cartas.
- `Jogador` usa o `Bando` (mantém a lista de bandos baixados).

Por isso o método é `Jogador.jogarBando(cartas, lider)` — e não
`Partida.criarBando(jogador, cartas, lider)`. O diagrama da Parte 1 já
marca esse método com `<<create>>`, confirmando que foi uma decisão
consciente.

### 3. Information Expert — `Regiao` e `Bando`

Quem é o "especialista" para calcular o ranking de uma região? A própria
região — porque ela detém a lista de marcadores. Por isso
`Regiao.rankingJogadoresPorMarcadores()` mora em `Regiao` e não em
`Partida`.

O mesmo vale para `Bando.calcularPontos()`: o bando sabe quantas cartas
tem e quem é o líder, então é quem deve calcular sua própria pontuação
base.

Resultado: a Partida fica mais enxuta e cada classe tem responsabilidade
clara.

### 4. Low Coupling — Model independente de JavaFX

A Partida **não importa nada de javafx**. Toda comunicação com a UI
acontece via interface `Assinante`. Isso é prova viva:
- A demo de console (`DemoConsole`) substitui as views JavaFX por um
  Assinante que imprime no terminal — o resto do código não muda.
- Os 16 testes JUnit rodam sem JavaFX no classpath.

### 5. High Cohesion — Cada classe tem uma responsabilidade

| Classe       | Responsabilidade única                               |
|--------------|------------------------------------------------------|
| `Baralho`    | Gerenciar monte e descarte                           |
| `Mercado`    | Gerenciar cartas abertas                             |
| `Tabuleiro`  | Gerenciar conjunto de regiões                        |
| `Regiao`     | Gerenciar marcadores e calcular pontuação posicional |
| `Bando`      | Representar uma jogada feita e seu valor base        |
| `Jogador`    | Estado do jogador (mão, fichas, pontos)              |
| `Partida`    | Coordenar o fluxo do jogo                            |

Quando a Partida da Parte 1 acumulava o `switch` dos poderes + o cálculo
de pontuação + o controle de turno, sua coesão era baixa. Com Strategy
extraindo os poderes e Information Expert deslocando cálculos para
Regiao/Bando, a coesão melhora.

---

## Decisões de projeto

### 1. `Carta` virou classe abstrata (não enum)

Uma alternativa para representar tribos seria um `enum Tribo`. Optamos
por classes abstratas + subclasses porque:
- Permite que cada tribo carregue seu próprio comportamento (Strategy
  via `getPoder()`).
- Combina com a anotação de "abstract" no diagrama de classes (Carta em
  itálico).
- Permite futura especialização visual (ex.: ícones, animações
  diferentes por subclasse) sem `instanceof`.

### 2. `Dragao` não tem poder, mas é uma `Carta`

Em vez de tratar dragão como uma flag em `Carta`, ele é uma subclasse
com `getPoder()` devolvendo `null`. A Partida usa
`Dragao.ehDragao(carta)` (que internamente faz `instanceof`) em poucos
pontos bem específicos: revelação, fim de era, recrutamento. Esse é o
único `instanceof` do projeto. Avaliamos alternativas como Visitor mas
elas trariam complexidade desnecessária para apenas um caso especial.

### 3. Poderes implementados como **classes internas estáticas**, não lambdas

Cada subclasse de `Carta` declara seu `PoderDoLider` como uma classe
interna nomeada (`PoderAnao`, `PoderElfo`…). Lambdas seriam mais
curtas, mas as classes nomeadas:
- Aparecem com nome próprio em stack traces.
- São naturalmente testáveis isoladas.
- Combinam melhor com o diagrama UML (ficam como classes do modelo
  visíveis no projeto).

### 4. `Partida` usa `Assinante` em vez de PropertyChangeListener do JDK

O JDK já fornece `java.util.Observable` (depreciada) e
`PropertyChangeListener`. Não usamos por dois motivos:
- O diagrama da Parte 2 mostra explicitamente a classe `Assinante` com
  a operação `atualiza(p: Partida): Void` — implementar fielmente o
  diagrama era requisito.
- A interface customizada deixa o padrão visível no código, o que ajuda
  na argumentação durante a apresentação.

### 5. IA simples mas não trivial

A IA escolhe o bando montando a maior combinação possível a partir de
qualquer carta da mão (procura o melhor "centro de cor/tribo"). Não é
ótima, mas evita o caso degenerado da Parte 1 onde a IA jogava sempre
a primeira carta da mão.

### 6. Mercado iniciado vazio em vez de pré-populado

Na Parte 1 o mercado era preenchido com cartas "hard-coded" para
estabilidade. Agora ele é populado via `revelarCartasRaca()` no início
da era — mesmo método usado durante o jogo. Reduz duplicação e garante
que o setup é uma operação real, não um caso especial.

### 7. Limite de mão = 10 cartas

Mantido da Parte 1. É a regra do jogo original. Tentar recrutar com mão
cheia lança `IllegalArgumentException`, que o JogoController traduz num
alerta da UI.

---

## Dificuldades encontradas

### Dificuldade 1: switch gigante de poderes vs. Open/Closed

**Problema:** A Parte 1 implementava todos os poderes num único método
`aplicarPoderDoLider` com `switch(tribo.toLowerCase())`. Adicionar uma
nova tribo exigia ler e modificar 50+ linhas. Testar um poder isolado
exigia montar uma Partida inteira.

**Solução:** Extraímos Strategy. Cada tribo tem seu `PoderDoLider`.
Adicionar uma tribo agora é uma nova subclasse + um case na Factory.
Testar um poder agora é instanciar a Strategy e chamar `executar(...)`.

### Dificuldade 2: Views ficaram presas ao ciclo do Controller

**Problema:** Na Parte 1, o JogoController chamava manualmente
`mercadoView.atualizarVisualizacao()`, `maoView.atualizarVisualizacao()`,
etc. depois de cada ação. Adicionar uma 5ª view exigia caçar todos esses
pontos. Pior: quando a era trocava por revelação de dragão durante um
recrutamento, o Controller não sabia que precisava redesenhar.

**Solução:** Aplicamos Observer. As views se inscrevem na Partida no
construtor, e a Partida chama `notifica()` no fim de cada operação. O
Controller continua existindo, mas só para tratar input — não para
disparar refresh.

### Dificuldade 3: Modelo testável sem JavaFX

**Problema:** Quando model e UI estão acoplados, escrever testes
unitários exige inicializar a JavaFX Toolkit (`Application.launch()`),
o que é frágil em CI/CD e brutal para rodar.

**Solução:** Como consequência da Low Coupling + Observer, hoje os
testes ficam em `src/test/java` e rodam em **~200ms** sem inicializar
nada de JavaFX. 16 testes cobrindo Factory, Strategy, Observer, Creator
e Information Expert.

### Dificuldade 4: Dragão revelado durante setup quebrava o jogo

**Problema:** No setup, ao distribuir as mãos iniciais, o baralho podia
revelar um Dragão. A Parte 1 começava a era 2 antes do jogo começar.

**Solução:** Mantemos uma flag `setupInicialAtivo` que faz dragões
revelados durante a distribuição irem direto para o descarte, sem contar
para o fim de era. A flag é desligada após `distribuirMaosIniciais()`.

### Dificuldade 5: Reciclagem do descarte

**Problema:** Em partidas longas (3 eras × ~6 jogadores) o baralho
zerava antes da terceira era e o jogo travava.

**Solução:** `Baralho.comprarDoTopo()` agora detecta monte vazio com
descarte cheio e recicla automaticamente (re-embaralha o descarte e
move para o monte). `semCartasDisponiveis()` só retorna true quando
ambos estão vazios.

---

## Testes

Rodamos com:
```bash
mvn test
```

**Cobertura atual: 16 testes, 100% passando**, cobrindo:

| Padrão / regra            | Teste                                                    |
|---------------------------|----------------------------------------------------------|
| Factory Method            | `factoryCriaCartasConcretasCorretas`                     |
| Factory Method (erros)    | `factoryLancaParaTriboInexistente`                       |
| Factory (composição)      | `factoryMontaBaralhoComTresDragoes`                      |
| Strategy (Anão)           | `poderAnaoSomaUmPonto`                                   |
| Strategy (Gigante)        | `poderGiganteSomaDoisPontos`                             |
| Strategy (Troll)          | `poderTrollEscalaComBando`                               |
| Strategy (Dragão)         | `dragaoNaoTemPoder`                                      |
| Observer (notificação)    | `observerNotificaAssinantesEmJogadas`                    |
| Observer (remoção)        | `observerRemovecaoFunciona`                              |
| Creator                   | `jogadorCriaBandoAoJogar`                                |
| Information Expert (B.)   | `bandoCalculaPontosCorretamente`                         |
| Information Expert (R.)   | `regiaoOrdenaRankingPorMarcadores`                       |
| Information Expert (R.)   | `regiaoDevolveZeroParaPosicaoForaDoArray`                |
| Validação de regras       | `rejeitaBandoSemCombinacaoComLider`                      |
| Validação de regras       | `rejeitaJogadaForaDoTurno`                               |
| Reciclagem do baralho     | `baralhoRecicliaDescarteQuandoZera`                      |

---

## Estrutura de pastas

```
src/main/java/com/projeto/ethnos/
├── EthnosApp.java                 # main do JavaFX
├── DemoConsole.java               # demo sem UI (prova de Low Coupling)
├── controller/
│   └── JogoController.java        # MVC Controller (traduz JavaFX → Partida)
├── factory/
│   └── CartaFactory.java          # GoF Factory Method
├── model/
│   ├── Carta.java                 # abstrata
│   ├── Bando.java
│   ├── Jogador.java
│   ├── Baralho.java
│   ├── Mercado.java
│   ├── Tabuleiro.java
│   ├── Regiao.java
│   ├── Partida.java               # GRASP Controller + Observer Subject
│   ├── carta/                     # subclasses concretas de Carta
│   │   ├── Anao.java
│   │   ├── Centauro.java
│   │   ├── Dragao.java
│   │   ├── Elfo.java
│   │   ├── Esqueleto.java
│   │   ├── Gigante.java
│   │   ├── Mago.java
│   │   ├── Minotauro.java
│   │   └── Troll.java
│   └── poder/
│       └── PoderDoLider.java      # GoF Strategy (interface)
├── observer/
│   └── Assinante.java             # GoF Observer (interface)
└── view/
    ├── CartaView.java
    ├── MaoView.java               # implementa Assinante
    ├── MercadoView.java           # implementa Assinante
    ├── StatusView.java            # implementa Assinante
    └── TabuleiroView.java         # implementa Assinante

src/test/java/com/projeto/ethnos/
└── PartidaTest.java               # 16 testes JUnit 5
```

---

## Autoria

Grupo de Projeto de Software — 6º período, Sistemas de Informação,
Universidade Federal Fluminense. Implementação evolutiva a partir do
esqueleto inicial do trabalho da Parte 1.
