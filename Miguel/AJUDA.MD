# 1️⃣ Estruturas de Dados

## 🔹 Bloco (Node)

```java
class Block {
    String id;              // hash do bloco
    String parentId;        // hash do pai
    String command;         // string a append (stage 1)
    int view;               // view onde foi proposto
    QC justify;             // QC que justifica este bloco
}
```

---

## 🔹 Voto

```java
class Vote {
    String blockId;
    int view;
    int voterId;
    byte[] signature;
}
```

---

## 🔹 Quorum Certificate (QC)

```java
class QC {
    String blockId;
    int view;
    Map<Integer, byte[]> signatures; // 2f+1 assinaturas
}
```

QC = prova criptográfica de que ≥2f+1 nós votaram.

---

## 🔹 Estado Local de Cada Nó

```java
class ReplicaState {
    int currentView;

    QC highQC;        // melhor QC conhecido
    QC lockedQC;      // QC que protege contra forks

    Map<String, Block> blockStore;

    Block lastVotedBlock;
}
```

---

# 2️⃣ Pseudocódigo do Basic HotStuff

Vou simplificar para a versão conceptual que vocês precisam no projeto.

---

## 🔹 Função: onReceiveProposal

```pseudo
onReceiveProposal(block B):

    if not verifySignature(B):
        return

    if not safeToVote(B):
        return

    sendVote(B)
    lastVotedBlock = B
```

---

## 🔹 Regra Safe

```pseudo
safeToVote(B):

    if B.parent extends lockedQC.block:
        return true

    if B.justify.view > lockedQC.view:
        return true

    return false
```

Isto garante safety.

---

## 🔹 Líder: propor bloco

```pseudo
leaderPropose():

    parent = highQC.block
    newBlock = createBlock(parent, command, highQC)

    broadcast PROPOSE(newBlock)
```

---

## 🔹 Ao receber votos

```pseudo
onReceiveVote(vote):

    collect vote

    if received >= 2f+1 votes for block B:

        qc = createQC(B)

        highQC = max(highQC, qc)

        advancePhase(B, qc)
```

---

## 🔹 Advance Phase (Prepare → PreCommit → Commit)

Basicamente:

```pseudo
advancePhase(B, qc):

    if phase == PREPARE:
        move to PRECOMMIT

    if phase == PRECOMMIT:
        move to COMMIT
        lockedQC = qc

    if phase == COMMIT:
        DECIDE(B)
```

---

## 🔹 DECIDE

```pseudo
DECIDE(B):

    apply(B.command)     // append string
    replyToClient(B)
```

---

# 3️⃣ Fluxo Completo de Mensagens

Vamos ilustrar com 4 nós (n=4, f=1).

---

## View 1

### 1️⃣ Líder envia

```
PROPOSE(B1, justify=genesisQC)
```

---

### 2️⃣ Réplicas respondem

```
VOTE_PREPARE(B1)
```

---

### 3️⃣ Líder forma

```
QC_PREPARE(B1)
```

---

## View 2

Líder seguinte usa:

```
PROPOSE(B2, justify=QC_PREPARE(B1))
```

Votos → QC_PRECOMMIT(B2)

---

## View 3

Novo líder:

```
PROPOSE(B3, justify=QC_PRECOMMIT(B2))
```

Votos → QC_COMMIT(B3)

---

## Agora ocorre DECIDE

Quando temos:

```
B1 ← B2 ← B3
```

com QCs encadeados,

decide-se B1.

---

# 4️⃣ Estrutura de uma Decisão

Uma decisão inclui:

```java
class Decision {
    String blockId;
    String command;
    int decidedView;
    QC proof;
}
```

O QC serve como prova verificável.

---

# Estrutura da Prova (Importante para Stage 1)

Quando o cliente recebe resposta, pode incluir:

```json
{
  "blockId": "hashB1",
  "command": "append('X')",
  "view": 3,
  "qc": {
      "blockId": "hashB1",
      "signatures": [ ... 2f+1 assinaturas ... ]
  }
}
```

Assim o cliente pode verificar:

* 2f+1 assinaturas válidas
* consenso legítimo

---

# Atualizações de highQC e lockedQC

| Evento            | highQC            | lockedQC |
| ----------------- | ----------------- | -------- |
| Recebe QC novo    | atualiza se maior | não      |
| Entra fase commit | mantém            | atualiza |
| View change       | usa maior highQC  | mantém   |

---

# Fluxo de View Change

Se timeout:

```
NEW_VIEW(highQC)
```

Novo líder:

```
collect 2f+1 NEW_VIEW
select best highQC
propose new block
```

---

# Visão Final do Algoritmo

```
Cliente
   ↓
Líder
   ↓ PROPOSE
Réplicas
   ↓ VOTE
Líder
   ↓ QC
Nova view
   ↓
Após 3 encadeamentos
   ↓
DECIDE
```

---