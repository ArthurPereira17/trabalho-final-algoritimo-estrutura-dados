package estruturas;

/**
 * Mapa de dispersão (tabela hash) com chaves genéricas (K, T) e
 * tratamento de colisões por ENCADEAMENTO SEPARADO (cada bucket é uma
 * {@link ListaEncadeada}).
 *
 * Reutilizado da disciplina (UNI3/aula2). Mudanças em relação ao original,
 * todas permitidas pelo enunciado ("é permitido adicionar métodos e
 * atributos complementares"):
 *
 *  1. Pacote alterado para {@code estruturas}.
 *  2. {@link #calcularHash(Object)} blindado contra índice negativo
 *     (ver comentário no método).
 *  3. Métodos complementares {@link #obterTodosNos()} e
 *     {@link #obterTamanho()} para permitir a PERSISTÊNCIA do índice
 *     (varrer todas as entradas do mapa).
 *
 * @param <K> tipo da chave (ex: String para palavras)
 * @param <T> tipo do valor armazenado (ex: ListaEncadeada de IDs)
 */
public class MapaDispersao<K, T> {

    private ListaEncadeada<NoMapa<K, T>>[] info;

    @SuppressWarnings("unchecked")
    public MapaDispersao(int tamanho) {
        info = new ListaEncadeada[tamanho];
        for (int i = 0; i < tamanho; i++) {
            info[i] = new ListaEncadeada<>();
        }
    }

    /**
     * Calcula o índice do bucket a partir da chave.
     *
     * Usa {@code hashCode() & 0x7fffffff} (zera o bit de sinal) em vez de
     * {@code Math.abs(hashCode())}. Motivo: {@code Math.abs(Integer.MIN_VALUE)}
     * retorna um valor NEGATIVO (overflow), o que geraria índice negativo e
     * {@code ArrayIndexOutOfBoundsException}. A máscara nunca falha.
     */
    public int calcularHash(K chave) {
        return (chave.hashCode() & 0x7fffffff) % info.length;
    }

    public void inserir(K chave, T valor) {
        int indice = calcularHash(chave);
        NoMapa<K, T> novoNo = new NoMapa<>(chave, valor);
        info[indice].inserir(novoNo);
    }

    public void remover(K chave) {
        int indice = calcularHash(chave);
        NoMapa<K, T> chaveBusca = new NoMapa<>(chave, null);
        info[indice].retirar(chaveBusca);
    }

    public T buscar(K chave) {
        int indice = calcularHash(chave);
        NoMapa<K, T> chaveBusca = new NoMapa<>(chave, null);

        NoLista<NoMapa<K, T>> resultado = info[indice].buscar(chaveBusca);

        if (resultado != null) {
            return resultado.getInfo().getValor();
        }

        return null;
    }

    public double calcularFatorCarga() {
        int totalElementos = 0;
        for (int i = 0; i < info.length; i++) {
            totalElementos += info[i].obterComprimento();
        }
        return (double) totalElementos / info.length;
    }

    // ------------------------------------------------------------------
    // Métodos complementares (adicionados para o projeto)
    // ------------------------------------------------------------------

    /**
     * Retorna o número de buckets (tamanho do vetor interno).
     * Usado para persistir o tamanho do mapa e recriá-lo fielmente.
     */
    public int obterTamanho() {
        return info.length;
    }

    /**
     * Percorre TODOS os buckets e devolve uma única {@link ListaEncadeada}
     * com todos os nós (pares chave/valor) do mapa.
     *
     * Necessário para a persistência: o índice precisa iterar sobre todas as
     * palavras para gravá-las em disco, mas os buckets são privados.
     *
     * @return lista com todos os {@link NoMapa} presentes no mapa
     */
    public ListaEncadeada<NoMapa<K, T>> obterTodosNos() {
        ListaEncadeada<NoMapa<K, T>> todos = new ListaEncadeada<>();
        for (int i = 0; i < info.length; i++) {
            NoLista<NoMapa<K, T>> atual = info[i].getPrimeiro();
            while (atual != null) {
                todos.inserir(atual.getInfo());
                atual = atual.getProximo();
            }
        }
        return todos;
    }
}
