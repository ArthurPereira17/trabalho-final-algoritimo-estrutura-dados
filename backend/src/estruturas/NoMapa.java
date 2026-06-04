package estruturas;

/**
 * Nó do mapa de dispersão: par chave -> valor com chave genérica.
 *
 * Reutilizado da disciplina (UNI3/aula2). Apenas o pacote foi alterado
 * para {@code estruturas}.
 *
 * O {@code equals} compara APENAS a chave (o valor é ignorado). Isso permite
 * usar um {@code NoMapa(chave, null)} como critério de busca dentro da
 * {@link ListaEncadeada} do bucket.
 *
 * @param <K> tipo da chave (ex: String para palavras, Integer para IDs)
 * @param <T> tipo do valor armazenado
 */
public class NoMapa<K, T> {

    private K chave;
    private T valor;

    public NoMapa(K chave, T valor) {
        this.chave = chave;
        this.valor = valor;
    }

    public K getChave() {
        return chave;
    }

    public void setChave(K chave) {
        this.chave = chave;
    }

    public T getValor() {
        return valor;
    }

    public void setValor(T valor) {
        this.valor = valor;
    }

    /**
     * Compara dois NoMapa apenas pela CHAVE (ignora o valor).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoMapa<?, ?> noMapa = (NoMapa<?, ?>) o;
        return chave.equals(noMapa.chave);
    }

    /**
     * Delega o hashCode para a chave, mantendo o contrato equals/hashCode.
     */
    @Override
    public int hashCode() {
        return chave.hashCode();
    }
}
