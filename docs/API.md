# Contrato da API HTTP

Servidor `com.sun.net.httpserver` na porta **8080**. Respostas em JSON (UTF-8), montadas à mão.
Todos os endpoints enviam cabeçalhos **CORS** (`Access-Control-Allow-Origin: *`) e tratam `OPTIONS`.

> O diretório e a consulta vêm como **query params URL-encoded** (não no corpo), para evitar
> parsing manual de JSON com as barras invertidas dos caminhos do Windows.

---

## GET `/api/status`
Estado do índice em memória.

```json
{ "indiceCarregado": true, "documentos": 4, "palavras": 57, "fatorCarga": 0.00057 }
```

---

## GET ou POST `/api/indexar?dir=<caminho>`
Indexa o diretório (recursivo, só `.txt`) e **persiste** o índice em `./dados`.
Substitui o índice em memória pelo novo.

`dir` é URL-encoded. Ex.: `dir=C%3A%5Cmeus-textos`

**200**
```json
{ "ok": true, "arquivos": 4, "documentos": 4, "palavras": 57, "fatorCarga": 0.00057, "tempoMs": 12 }
```
**400** (diretório ausente/inválido)
```json
{ "ok": false, "erro": "Diretório inválido: ..." }
```

---

## GET `/api/buscar?q=<consulta>`
Busca AND. Retorna apenas documentos que contêm **todas** as palavras.
A consulta passa pela mesma normalização da indexação (minúsculas, 3+ letras, sem pontuação).

`q` é URL-encoded. Múltiplas palavras separadas por espaço (`%20`).

**200**
```json
{
  "consulta": ["tempo", "pessoas"],
  "totalDocumentos": 2,
  "documentos": [
    { "id": 1, "caminho": "C:\\exemplos\\cidade.txt" },
    { "id": 2, "caminho": "C:\\exemplos\\sub\\tecnologia.txt" }
  ]
}
```

Se o índice ainda não foi carregado:
```json
{ "consulta": [], "totalDocumentos": 0, "documentos": [], "erro": "indice nao carregado" }
```
