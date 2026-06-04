package api;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import busca.MotorBusca;
import dominio.Documento;
import estruturas.ListaEncadeada;
import estruturas.NoLista;
import indice.IndiceInvertido;
import indice.Indexador;
import indice.RepositorioIndice;

/**
 * Servidor HTTP embutido (com.sun.net.httpserver) — zero dependências externas.
 *
 * Endpoints:
 *   GET/POST  /api/indexar?dir=<caminho>   -> indexa o diretório e persiste
 *   GET       /api/buscar?q=<consulta>     -> busca AND, devolve documentos
 *   GET       /api/status                  -> estado do índice em memória
 *
 * O diretório e a consulta vêm como query params (URL-encoded). Optou-se por
 * query param em vez de corpo JSON para evitar parsing manual de JSON com as
 * barras invertidas dos caminhos do Windows.
 */
public class ServidorHttp {

    private final int porta;
    private final String dirDados;
    private final RepositorioIndice repositorio = new RepositorioIndice();
    private final Indexador indexador = new Indexador();
    private volatile IndiceInvertido indice; // estado em memória (pode ser null)

    public ServidorHttp(int porta, String dirDados, IndiceInvertido indiceInicial) {
        this.porta = porta;
        this.dirDados = dirDados;
        this.indice = indiceInicial;
    }

    public void iniciar() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(porta), 0);
        server.createContext("/api/status", new StatusHandler());
        server.createContext("/api/indexar", new IndexarHandler());
        server.createContext("/api/buscar", new BuscarHandler());
        server.setExecutor(null); // executor padrão (suficiente para o trabalho)
        server.start();
        System.out.println("Servidor HTTP no ar em http://localhost:" + porta);
        System.out.println("  GET /api/status");
        System.out.println("  GET /api/indexar?dir=<caminho>");
        System.out.println("  GET /api/buscar?q=<palavras>");
    }

    // ------------------------------------------------------------------
    // Handlers
    // ------------------------------------------------------------------

    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (preflight(ex)) return;
            String json;
            if (indice == null) {
                json = "{\"indiceCarregado\":false,\"documentos\":0,\"palavras\":0,\"fatorCarga\":0.0}";
            } else {
                json = "{\"indiceCarregado\":true,"
                        + "\"documentos\":" + indice.getTotalDocumentos() + ","
                        + "\"palavras\":" + indice.getTotalPalavras() + ","
                        + "\"fatorCarga\":" + indice.getFatorCarga() + "}";
            }
            enviar(ex, 200, json);
        }
    }

    private class IndexarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (preflight(ex)) return;
            String dir = param(ex, "dir");
            if (dir == null || dir.trim().isEmpty()) {
                enviar(ex, 400, "{\"ok\":false,\"erro\":\"parametro 'dir' obrigatorio\"}");
                return;
            }
            try {
                long t0 = System.currentTimeMillis();
                IndiceInvertido novo = new IndiceInvertido();
                int arquivos = indexador.indexarDiretorio(dir, novo);
                repositorio.salvar(novo, dirDados);
                indice = novo; // troca o índice em memória
                long ms = System.currentTimeMillis() - t0;

                String json = "{\"ok\":true,"
                        + "\"arquivos\":" + arquivos + ","
                        + "\"documentos\":" + novo.getTotalDocumentos() + ","
                        + "\"palavras\":" + novo.getTotalPalavras() + ","
                        + "\"fatorCarga\":" + novo.getFatorCarga() + ","
                        + "\"tempoMs\":" + ms + "}";
                enviar(ex, 200, json);
            } catch (IllegalArgumentException e) {
                enviar(ex, 400, "{\"ok\":false,\"erro\":\"" + JsonUtil.escape(e.getMessage()) + "\"}");
            } catch (Exception e) {
                enviar(ex, 500, "{\"ok\":false,\"erro\":\"" + JsonUtil.escape(String.valueOf(e.getMessage())) + "\"}");
            }
        }
    }

    private class BuscarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (preflight(ex)) return;

            if (indice == null) {
                enviar(ex, 200, "{\"consulta\":[],\"totalDocumentos\":0,\"documentos\":[],"
                        + "\"erro\":\"indice nao carregado\"}");
                return;
            }

            String q = param(ex, "q");
            if (q == null) {
                q = "";
            }

            MotorBusca motor = new MotorBusca(indice);
            ListaEncadeada<String> termos = motor.termosDe(q);
            ListaEncadeada<Documento> docs = motor.pesquisar(q);

            enviar(ex, 200, montarJsonBusca(termos, docs));
        }
    }

    // ------------------------------------------------------------------
    // Montagem de JSON
    // ------------------------------------------------------------------

    private String montarJsonBusca(ListaEncadeada<String> termos, ListaEncadeada<Documento> docs) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"consulta\":[");

        NoLista<String> t = termos.getPrimeiro();
        boolean primeiro = true;
        while (t != null) {
            if (!primeiro) sb.append(",");
            sb.append("\"").append(JsonUtil.escape(t.getInfo())).append("\"");
            primeiro = false;
            t = t.getProximo();
        }

        sb.append("],\"totalDocumentos\":").append(docs.obterComprimento());
        sb.append(",\"documentos\":[");

        NoLista<Documento> d = docs.getPrimeiro();
        primeiro = true;
        while (d != null) {
            if (!primeiro) sb.append(",");
            Documento doc = d.getInfo();
            sb.append("{\"id\":").append(doc.getId())
              .append(",\"caminho\":\"").append(JsonUtil.escape(doc.getCaminho())).append("\"}");
            primeiro = false;
            d = d.getProximo();
        }

        sb.append("]}");
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Infraestrutura HTTP (CORS, query params, resposta)
    // ------------------------------------------------------------------

    private String param(HttpExchange ex, String nome) {
        String q = ex.getRequestURI().getRawQuery();
        if (q == null) {
            return null;
        }
        String[] pares = q.split("&");
        for (String par : pares) {
            int eq = par.indexOf('=');
            String chave = eq >= 0 ? par.substring(0, eq) : par;
            if (chave.equals(nome)) {
                String val = eq >= 0 ? par.substring(eq + 1) : "";
                return URLDecoder.decode(val, StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private void cors(Headers h) {
        h.add("Access-Control-Allow-Origin", "*");
        h.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        h.add("Access-Control-Allow-Headers", "Content-Type");
    }

    /** Responde requisições OPTIONS (preflight CORS). Retorna true se tratou. */
    private boolean preflight(HttpExchange ex) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            cors(ex.getResponseHeaders());
            ex.sendResponseHeaders(204, -1);
            ex.close();
            return true;
        }
        return false;
    }

    private void enviar(HttpExchange ex, int status, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        Headers h = ex.getResponseHeaders();
        h.add("Content-Type", "application/json; charset=utf-8");
        cors(h);
        ex.sendResponseHeaders(status, body.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(body);
        }
    }
}
