package dominio;

/**
 * Documento indexado: um arquivo .txt encontrado na varredura.
 *
 * Cada documento recebe um ID inteiro sequencial. O índice de palavras
 * guarda listas de IDs (compactas) em vez de repetir o caminho completo
 * em cada palavra.
 */
public class Documento {

    private int id;
    private String caminho;

    public Documento(int id, String caminho) {
        this.id = id;
        this.caminho = caminho;
    }

    public int getId() {
        return id;
    }

    public String getCaminho() {
        return caminho;
    }

    /**
     * Igualdade pelo ID — permite buscar um documento numa
     * {@code ListaEncadeada<Documento>} usando um critério apenas com o ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Documento outro = (Documento) o;
        return id == outro.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Documento{id=" + id + ", caminho='" + caminho + "'}";
    }
}
