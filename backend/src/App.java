import api.ServidorHttp;
import indice.IndiceInvertido;
import indice.Indexador;
import indice.RepositorioIndice;

/**
 * Ponto de entrada do Buscador de Arquivos.
 *
 * Fluxo:
 *   1. Se já existe índice salvo em ./dados, carrega-o para a memória
 *      (RF09 — sem reindexar).
 *   2. (Opcional) se chamado com --indexar &lt;dir&gt;, indexa e salva antes de subir.
 *   3. Sobe o servidor HTTP na porta 8080.
 *
 * A indexação também pode ser disparada em runtime pelo endpoint
 * GET /api/indexar?dir=&lt;caminho&gt;.
 */
public class App {

    private static final int PORTA = 8080;
    private static final String DIR_DADOS = "dados";

    public static void main(String[] args) throws Exception {
        RepositorioIndice repositorio = new RepositorioIndice();
        IndiceInvertido indice = null;

        // 1. Carregar índice persistido, se houver
        if (repositorio.existeIndice(DIR_DADOS)) {
            System.out.println("Carregando indice salvo de './" + DIR_DADOS + "'...");
            indice = repositorio.carregar(DIR_DADOS);
            System.out.println("Indice carregado: "
                    + indice.getTotalDocumentos() + " documentos, "
                    + indice.getTotalPalavras() + " palavras.");
        } else {
            System.out.println("Nenhum indice salvo encontrado. "
                    + "Indexe com /api/indexar?dir=<caminho> ou --indexar <caminho>.");
        }

        // 2. Indexação opcional via linha de comando
        if (args.length >= 2 && args[0].equals("--indexar")) {
            String dir = args[1];
            System.out.println("Indexando '" + dir + "'...");
            Indexador indexador = new Indexador();
            indice = new IndiceInvertido();
            int arquivos = indexador.indexarDiretorio(dir, indice);
            repositorio.salvar(indice, DIR_DADOS);
            System.out.println("Indexacao concluida: " + arquivos + " arquivos, "
                    + indice.getTotalPalavras() + " palavras. Indice salvo em './" + DIR_DADOS + "'.");
        }

        // 3. Subir o servidor HTTP
        ServidorHttp servidor = new ServidorHttp(PORTA, DIR_DADOS, indice);
        servidor.iniciar();
    }
}
