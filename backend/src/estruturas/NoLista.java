package estruturas;

/**
 * Nó de uma lista encadeada simples.
 *
 * Reutilizado da disciplina (UNI1/aula3). Apenas o pacote foi alterado
 * para {@code estruturas} ao integrar no projeto do Buscador de Arquivos.
 *
 * @param <T> tipo do valor armazenado no nó
 */
public class NoLista<T> {
    private T info;
    private NoLista<T> proximo;

    public T getInfo() {
        return info;
    }

    public void setInfo(T info) {
        this.info = info;
    }

    public NoLista<T> getProximo() {
        return proximo;
    }

    public void setProximo(NoLista<T> proximo) {
        this.proximo = proximo;
    }
}
