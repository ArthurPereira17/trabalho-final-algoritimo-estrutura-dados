package indice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import estruturas.ListaEncadeada;
import estruturas.NoLista;

/**
 * Responsável por varrer um diretório (e subdiretórios), encontrar os
 * arquivos {@code .txt} e alimentar o {@link IndiceInvertido} com as
 * palavras extraídas de cada arquivo.
 *
 * A varredura é recursiva e usa apenas {@link java.io.File} e arrays
 * (permitidos — não são "estruturas de dados" da disciplina).
 */
public class Indexador {

    private final ExtratorPalavras extrator = new ExtratorPalavras();

    /**
     * Indexa todos os .txt do diretório informado e de seus subdiretórios,
     * populando o índice recebido.
     *
     * @return a quantidade de arquivos indexados
     */
    public int indexarDiretorio(String caminhoDiretorio, IndiceInvertido indice) {
        File raiz = new File(caminhoDiretorio);
        if (!raiz.exists() || !raiz.isDirectory()) {
            throw new IllegalArgumentException("Diretório inválido: " + caminhoDiretorio);
        }
        return varrer(raiz, indice);
    }

    private int varrer(File diretorio, IndiceInvertido indice) {
        int arquivosIndexados = 0;
        File[] filhos = diretorio.listFiles();
        if (filhos == null) {
            return 0; // sem permissão de leitura, por exemplo
        }
        for (File f : filhos) {
            if (f.isDirectory()) {
                arquivosIndexados += varrer(f, indice);
            } else if (f.isFile() && ehTxt(f)) {
                indexarArquivo(f, indice);
                arquivosIndexados++;
            }
        }
        return arquivosIndexados;
    }

    private boolean ehTxt(File f) {
        return f.getName().toLowerCase().endsWith(".txt");
    }

    private void indexarArquivo(File arquivo, IndiceInvertido indice) {
        int id = indice.registrarDocumento(arquivo.getAbsolutePath());

        try (BufferedReader leitor = new BufferedReader(
                new InputStreamReader(new FileInputStream(arquivo), StandardCharsets.UTF_8))) {

            String linha;
            while ((linha = leitor.readLine()) != null) {
                ListaEncadeada<String> palavras = extrator.extrair(linha);
                NoLista<String> p = palavras.getPrimeiro();
                while (p != null) {
                    indice.adicionarOcorrencia(p.getInfo(), id);
                    p = p.getProximo();
                }
            }
        } catch (IOException e) {
            System.err.println("Falha ao ler '" + arquivo.getAbsolutePath() + "': " + e.getMessage());
        }
    }
}
