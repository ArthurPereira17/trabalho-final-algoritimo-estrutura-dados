# Buscador de Arquivos v3 — Trabalho Final (Algoritmos e Estruturas de Dados — FURB)

Aplicação que **indexa palavras de arquivos `.txt`** (diretório + subdiretórios) usando um
**mapa de dispersão (hash)** como índice invertido, tornando a **busca instantânea**.
O índice é persistido em disco e recarregado na inicialização **sem reindexar**.

- **Backend:** Java puro (JDK), reutilizando as estruturas da disciplina
  (`MapaDispersao`, `ListaEncadeada`). API via `com.sun.net.httpserver` — **zero dependências**.
- **Frontend:** React mínimo (Vite), **1 tela**, apenas para visualização.

> 📄 Análise técnica completa, UML e plano: **[`docs/ANALISE-E-PLANO.md`](docs/ANALISE-E-PLANO.md)** ·
> Contrato da API: **[`docs/API.md`](docs/API.md)**

---

## Estrutura do projeto

```
trabalho-final-algoritimo-estrutura-dados/
├── backend/                     # Java puro (src/bin), sem Maven
│   ├── src/
│   │   ├── estruturas/          # estruturas REUTILIZADAS da disciplina (+ métodos novos)
│   │   ├── dominio/             # Documento
│   │   ├── indice/              # ExtratorPalavras, Indexador, IndiceInvertido, RepositorioIndice
│   │   ├── busca/               # MotorBusca (1 palavra e AND)
│   │   ├── api/                 # ServidorHttp, JsonUtil
│   │   └── App.java             # main
│   ├── test/                    # testes JUnit 5
│   └── lib/                     # junit-platform-console-standalone (.jar)
├── frontend/                    # Vite + React (1 tela)
├── exemplos/                    # massa de teste .txt (com subpasta sub/)
└── docs/                        # análise, UML, API
```

---

## Pré-requisitos

- **JDK 17+** (testado com JDK 21). ⚠️ No ambiente atual o `java` **não está no PATH** —
  use a extensão *Java* do VS Code ou configure o `JAVA_HOME`/PATH apontando para um JDK.
- **Node.js 18+** e **npm** (já instalados: Node v22) para o frontend.

---

## Como executar o backend

> ⚠️ **Não há JDK instalado no PATH desta máquina.** Os scripts abaixo já detectam o Java
> automaticamente (PATH → `JAVA_HOME` → JDK local em `C:\Berkan\Desenvolvimento\jdk-21`).
> Para o botão **Run** do VS Code funcionar, é preciso ter um JDK instalado/configurado.

### Opção A — Scripts prontos (recomendado)
Os scripts funcionam **de qualquer pasta** (usam o caminho do próprio script) e acham o JDK sozinhos:
```powershell
# compilar (src + test -> bin/)
.\backend\build.ps1

# rodar o servidor (compila antes, se necessário)
.\backend\run.ps1

# rodar já indexando um diretório
.\backend\run.ps1 --indexar "C:\caminho\para\seus\textos"
```
O servidor sobe em `http://localhost:8080`.

### Opção B — Linha de comando manual (PowerShell)
```powershell
cd backend
# compilar (saída em bin/)
javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse src -Filter *.java).FullName
# rodar (precisa estar DENTRO de backend/, com -cp bin)
java -cp bin App
```
> ❗ O erro `ClassNotFoundException: App` ocorre quando se roda de fora da pasta `backend/`
> ou sem `-cp bin`. Rode de dentro de `backend/` e sempre com `-cp bin` — ou use o `run.ps1`.

### Opção C — VS Code
Abra a pasta **`backend/`** como raiz do workspace (File → Open Folder → `backend`),
com a extensão *Extension Pack for Java* e um JDK configurado. Rode `App.java` (**Run**).

### Rodar os testes (JUnit 5)
```powershell
.\backend\build.ps1
# JUnit 6 usa o subcomando "execute":
java -jar backend\lib\junit-platform-console-standalone-6.0.0-RC2.jar execute -cp backend\bin --scan-class-path
```
(ou, com o JDK local: `& "C:\Berkan\Desenvolvimento\jdk-21\bin\java.exe" -jar ...`)

---

## Como executar o frontend

```powershell
cd frontend
npm install
npm run dev
```
Abra `http://localhost:5173`. Com o backend no ar, a tela permite **Indexar** um diretório
e **Buscar** palavras (busca ao vivo, com debounce).

---

## Demonstração rápida (sem frontend)

```powershell
cd backend
# indexa a massa de exemplo (4 arquivos, incluindo subpasta)
java -cp bin App --indexar "..\exemplos"
```
Depois, no navegador ou via curl:
```
http://localhost:8080/api/status
http://localhost:8080/api/buscar?q=tempo            # aparece nos 4 arquivos
http://localhost:8080/api/buscar?q=tempo%20pessoas  # AND: cidade.txt e tecnologia.txt e poema.txt
http://localhost:8080/api/buscar?q=água%20cidade    # AND: cidade.txt
```

> **Acentos:** a busca diferencia acentos (decisão documentada). Para encontrar `água`,
> digite com acento. Veja a justificativa na análise (§17).

---

## Como os requisitos foram atendidos

| Requisito | Onde |
|-----------|------|
| Varredura recursiva de `.txt` | `indice/Indexador.java` |
| Normalização (minúsculas, 3+ letras, sem dígitos/pontuação) | `indice/ExtratorPalavras.java` |
| Índice = mapa de dispersão (palavra → documentos) | `indice/IndiceInvertido.java` + `estruturas/MapaDispersao.java` |
| Persistência em disco + carga sem reindexar | `indice/RepositorioIndice.java` |
| Busca de 1 palavra e AND de N palavras | `busca/MotorBusca.java` |
| Sem coleções nativas; só estruturas da disciplina | `estruturas/` (núcleo do algoritmo) |

> Observação sobre a restrição: o **núcleo algorítmico** (índice e listas) usa **apenas**
> `MapaDispersao`/`ListaEncadeada` da disciplina. Classes utilitárias de **I/O, rede e String**
> (`File`, `BufferedReader`, `HttpServer`, `StringBuilder` na camada de API) não são
> "estruturas de dados" e são usadas apenas na infraestrutura.

---

## Status

🟢 Backend e frontend implementados · testes JUnit incluídos · massa de exemplo pronta.
