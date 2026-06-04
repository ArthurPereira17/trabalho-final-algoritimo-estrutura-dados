package indice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import dominio.Documento;
import estruturas.ListaEncadeada;
import estruturas.NoLista;
import estruturas.NoMapa;

/**
 * Persistência do índice em disco (RF08/RF09).
 *
 * Formato: texto UTF-8 em dois arquivos dentro do diretório de dados:
 *
 *  documentos.idx
 *  ----------------------------------------------
 *  # tamanhoMapaPalavras=100003
 *  # tamanhoMapaDocumentos=1009
 *  0\tC:\textos\a.txt
 *  1\tC:\textos\sub\b.txt
 *
 *  indice.idx
 *  ----------------------------------------------
 *  casa\t0,1,5
 *  janela\t1
 *
 * Ao carregar, o índice é reconstruído em memória SEM reabrir os .txt
 * originais (não há reindexação).
 */
public class RepositorioIndice {

    public static final String ARQ_DOCUMENTOS = "documentos.idx";
    public static final String ARQ_INDICE = "indice.idx";

    private static final String CAB_PALAVRAS = "# tamanhoMapaPalavras=";
    private static final String CAB_DOCUMENTOS = "# tamanhoMapaDocumentos=";

    /** Indica se existe um índice salvo no diretório informado. */
    public boolean existeIndice(String dirDados) {
        return new File(dirDados, ARQ_DOCUMENTOS).exists()
                && new File(dirDados, ARQ_INDICE).exists();
    }

    // ------------------------------------------------------------------
    // SALVAR
    // ------------------------------------------------------------------

    public void salvar(IndiceInvertido indice, String dirDados) throws IOException {
        File dir = new File(dirDados);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        salvarDocumentos(indice, new File(dir, ARQ_DOCUMENTOS));
        salvarPalavras(indice, new File(dir, ARQ_INDICE));
    }

    private void salvarDocumentos(IndiceInvertido indice, File arquivo) throws IOException {
        try (BufferedWriter w = abrirEscrita(arquivo)) {
            w.write(CAB_PALAVRAS + indice.getTamanhoMapaPalavras());
            w.newLine();
            w.write(CAB_DOCUMENTOS + indice.getTamanhoMapaDocumentos());
            w.newLine();

            NoLista<Documento> p = indice.obterTodosDocumentos().getPrimeiro();
            while (p != null) {
                Documento d = p.getInfo();
                w.write(d.getId() + "\t" + d.getCaminho());
                w.newLine();
                p = p.getProximo();
            }
        }
    }

    private void salvarPalavras(IndiceInvertido indice, File arquivo) throws IOException {
        try (BufferedWriter w = abrirEscrita(arquivo)) {
            ListaEncadeada<NoMapa<String, ListaEncadeada<Integer>>> todos =
                    indice.obterMapaPalavras().obterTodosNos();

            NoLista<NoMapa<String, ListaEncadeada<Integer>>> p = todos.getPrimeiro();
            while (p != null) {
                NoMapa<String, ListaEncadeada<Integer>> no = p.getInfo();
                // ListaEncadeada.toString() já gera os IDs separados por vírgula.
                w.write(no.getChave() + "\t" + no.getValor().toString());
                w.newLine();
                p = p.getProximo();
            }
        }
    }

    // ------------------------------------------------------------------
    // CARREGAR
    // ------------------------------------------------------------------

    public IndiceInvertido carregar(String dirDados) throws IOException {
        File arqDocs = new File(dirDados, ARQ_DOCUMENTOS);
        File arqIdx = new File(dirDados, ARQ_INDICE);
        if (!arqDocs.exists() || !arqIdx.exists()) {
            return null;
        }

        int[] tamanhos = lerCabecalho(arqDocs);
        IndiceInvertido indice = new IndiceInvertido(tamanhos[0], tamanhos[1]);

        carregarDocumentos(arqDocs, indice);
        carregarPalavras(arqIdx, indice);

        return indice;
    }

    private int[] lerCabecalho(File arqDocs) throws IOException {
        int tamPalavras = IndiceInvertido.TAMANHO_PALAVRAS_PADRAO;
        int tamDocs = IndiceInvertido.TAMANHO_DOCUMENTOS_PADRAO;

        try (BufferedReader r = abrirLeitura(arqDocs)) {
            String linha;
            while ((linha = r.readLine()) != null) {
                if (linha.startsWith(CAB_PALAVRAS)) {
                    tamPalavras = Integer.parseInt(linha.substring(CAB_PALAVRAS.length()).trim());
                } else if (linha.startsWith(CAB_DOCUMENTOS)) {
                    tamDocs = Integer.parseInt(linha.substring(CAB_DOCUMENTOS.length()).trim());
                } else if (!linha.startsWith("#")) {
                    break; // acabaram os cabeçalhos
                }
            }
        }
        return new int[] { tamPalavras, tamDocs };
    }

    private void carregarDocumentos(File arqDocs, IndiceInvertido indice) throws IOException {
        try (BufferedReader r = abrirLeitura(arqDocs)) {
            String linha;
            while ((linha = r.readLine()) != null) {
                if (linha.isEmpty() || linha.startsWith("#")) {
                    continue;
                }
                int tab = linha.indexOf('\t');
                if (tab < 0) {
                    continue;
                }
                int id = Integer.parseInt(linha.substring(0, tab).trim());
                String caminho = linha.substring(tab + 1);
                indice.registrarDocumentoComId(id, caminho);
            }
        }
    }

    private void carregarPalavras(File arqIdx, IndiceInvertido indice) throws IOException {
        try (BufferedReader r = abrirLeitura(arqIdx)) {
            String linha;
            while ((linha = r.readLine()) != null) {
                if (linha.isEmpty()) {
                    continue;
                }
                int tab = linha.indexOf('\t');
                if (tab < 0) {
                    continue;
                }
                String palavra = linha.substring(0, tab);
                String csv = linha.substring(tab + 1);

                String[] ids = csv.split(",");
                for (String idTexto : ids) {
                    idTexto = idTexto.trim();
                    if (!idTexto.isEmpty()) {
                        indice.adicionarOcorrencia(palavra, Integer.parseInt(idTexto));
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // util
    // ------------------------------------------------------------------

    private BufferedWriter abrirEscrita(File f) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(f), StandardCharsets.UTF_8));
    }

    private BufferedReader abrirLeitura(File f) throws IOException {
        return new BufferedReader(new InputStreamReader(
                new FileInputStream(f), StandardCharsets.UTF_8));
    }
}
