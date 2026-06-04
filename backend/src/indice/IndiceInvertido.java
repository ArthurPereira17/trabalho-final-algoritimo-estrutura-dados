package indice;

import dominio.Documento;
import estruturas.ListaEncadeada;
import estruturas.MapaDispersao;

/**
 * Índice invertido: o coração da aplicação.
 *
 * Mantém duas tabelas hash ({@link MapaDispersao}):
 *
 *  - {@code palavras}   : palavra (String) -> lista de IDs de documentos que a contêm.
 *  - {@code documentos} : ID (Integer)     -> {@link Documento}.
 *
 * Guardar IDs (e não o caminho completo) em cada palavra deixa as listas
 * compactas e a interseção da busca AND comparando inteiros.
 *
 * A busca por palavra é O(1) na média (acesso direto ao bucket pelo hash),
 * independente do tamanho do acervo — é isso que torna a busca "instantânea".
 */
public class IndiceInvertido {

    /** Tamanho (primo) do mapa de palavras. Folgado para manter fator de carga baixo. */
    public static final int TAMANHO_PALAVRAS_PADRAO = 100003;
    /** Tamanho (primo) do mapa de documentos. */
    public static final int TAMANHO_DOCUMENTOS_PADRAO = 1009;

    private MapaDispersao<String, ListaEncadeada<Integer>> palavras;
    private MapaDispersao<Integer, Documento> documentos;
    private ListaEncadeada<Documento> listaDocumentos; // para varrer na persistência
    private int proximoId;
    private int totalPalavras;

    public IndiceInvertido() {
        this(TAMANHO_PALAVRAS_PADRAO, TAMANHO_DOCUMENTOS_PADRAO);
    }

    public IndiceInvertido(int tamanhoMapaPalavras, int tamanhoMapaDocumentos) {
        palavras = new MapaDispersao<>(tamanhoMapaPalavras);
        documentos = new MapaDispersao<>(tamanhoMapaDocumentos);
        listaDocumentos = new ListaEncadeada<>();
        proximoId = 0;
        totalPalavras = 0;
    }

    /**
     * Registra um novo documento e devolve seu ID sequencial.
     */
    public int registrarDocumento(String caminho) {
        int id = proximoId;
        proximoId++;
        Documento doc = new Documento(id, caminho);
        documentos.inserir(id, doc);
        listaDocumentos.inserir(doc);
        return id;
    }

    /**
     * Registra um documento com ID já conhecido (usado ao CARREGAR do disco).
     * Mantém o contador {@code proximoId} consistente.
     */
    public void registrarDocumentoComId(int id, String caminho) {
        Documento doc = new Documento(id, caminho);
        documentos.inserir(id, doc);
        listaDocumentos.inserir(doc);
        if (id >= proximoId) {
            proximoId = id + 1;
        }
    }

    /**
     * Adiciona a ocorrência de uma palavra em um documento.
     *
     * IMPORTANTE: o {@code MapaDispersao.inserir} NÃO verifica duplicata —
     * por isso buscamos a chave ANTES. Se a palavra é nova, criamos a lista
     * e inserimos a chave UMA única vez; caso contrário, mutamos a lista já
     * referenciada pelo mapa (sem reinserir a chave). Evita chaves duplicadas.
     */
    public void adicionarOcorrencia(String palavra, int idDocumento) {
        ListaEncadeada<Integer> lista = palavras.buscar(palavra);
        if (lista == null) {
            lista = new ListaEncadeada<>();
            palavras.inserir(palavra, lista);
            totalPalavras++;
        }
        if (lista.buscar(idDocumento) == null) {
            lista.inserir(idDocumento);
        }
    }

    /** Retorna a lista de IDs de documentos que contêm a palavra, ou null. */
    public ListaEncadeada<Integer> buscarUma(String palavra) {
        return palavras.buscar(palavra);
    }

    /** Resolve um ID para o objeto Documento. */
    public Documento obterDocumento(int id) {
        return documentos.buscar(id);
    }

    public ListaEncadeada<Documento> obterTodosDocumentos() {
        return listaDocumentos;
    }

    /** Exposto para a persistência varrer todas as palavras. */
    public MapaDispersao<String, ListaEncadeada<Integer>> obterMapaPalavras() {
        return palavras;
    }

    public int getTotalDocumentos() {
        return listaDocumentos.obterComprimento();
    }

    public int getTotalPalavras() {
        return totalPalavras;
    }

    public double getFatorCarga() {
        return palavras.calcularFatorCarga();
    }

    public int getTamanhoMapaPalavras() {
        return palavras.obterTamanho();
    }

    public int getTamanhoMapaDocumentos() {
        return documentos.obterTamanho();
    }
}
