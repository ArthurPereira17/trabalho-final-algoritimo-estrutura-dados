package busca;

import dominio.Documento;
import estruturas.ListaEncadeada;
import estruturas.NoLista;
import indice.ExtratorPalavras;
import indice.IndiceInvertido;

/**
 * Motor de busca sobre o {@link IndiceInvertido} (RF10/RF11).
 *
 *  - 1 palavra : retorna os documentos que a contêm (acesso O(1) ao bucket).
 *  - N palavras: retorna apenas os documentos que contêm TODAS (interseção AND).
 *
 * A consulta passa pela MESMA normalização da indexação (minúsculas, 3+
 * letras), garantindo que "Casa", "casa" e "CASA." casem com o índice.
 */
public class MotorBusca {

    private final IndiceInvertido indice;
    private final ExtratorPalavras extrator = new ExtratorPalavras();

    public MotorBusca(IndiceInvertido indice) {
        this.indice = indice;
    }

    /** Termos normalizados da consulta (útil para devolver na resposta). */
    public ListaEncadeada<String> termosDe(String consulta) {
        return extrator.extrair(consulta);
    }

    /** Executa a busca e resolve os IDs em objetos {@link Documento}. */
    public ListaEncadeada<Documento> pesquisar(String consulta) {
        ListaEncadeada<String> termos = extrator.extrair(consulta);
        ListaEncadeada<Integer> ids = pesquisarIds(termos);
        return resolverDocumentos(ids);
    }

    /**
     * Núcleo do AND: começa pela lista do primeiro termo e vai intersectando
     * com as listas dos termos seguintes. Qualquer termo inexistente, ou uma
     * interseção que esvazia, encerra com resultado vazio (poda).
     */
    private ListaEncadeada<Integer> pesquisarIds(ListaEncadeada<String> termos) {
        if (termos.estaVazia()) {
            return new ListaEncadeada<>();
        }

        NoLista<String> termo = termos.getPrimeiro();

        ListaEncadeada<Integer> resultado = indice.buscarUma(termo.getInfo());
        if (resultado == null) {
            return new ListaEncadeada<>(); // 1º termo não existe -> AND falha
        }

        termo = termo.getProximo();
        while (termo != null) {
            ListaEncadeada<Integer> listaTermo = indice.buscarUma(termo.getInfo());
            if (listaTermo == null) {
                return new ListaEncadeada<>(); // termo inexistente -> AND falha
            }
            resultado = interseccao(resultado, listaTermo);
            if (resultado.estaVazia()) {
                return resultado; // poda: nada em comum, pode parar
            }
            termo = termo.getProximo();
        }

        return resultado;
    }

    /** Retorna uma nova lista com os IDs presentes em A e em B. */
    private ListaEncadeada<Integer> interseccao(ListaEncadeada<Integer> a, ListaEncadeada<Integer> b) {
        ListaEncadeada<Integer> resultado = new ListaEncadeada<>();
        NoLista<Integer> p = a.getPrimeiro();
        while (p != null) {
            Integer id = p.getInfo();
            if (b.buscar(id) != null) {
                resultado.inserir(id);
            }
            p = p.getProximo();
        }
        return resultado;
    }

    private ListaEncadeada<Documento> resolverDocumentos(ListaEncadeada<Integer> ids) {
        ListaEncadeada<Documento> docs = new ListaEncadeada<>();
        NoLista<Integer> p = ids.getPrimeiro();
        while (p != null) {
            Documento d = indice.obterDocumento(p.getInfo());
            if (d != null) {
                docs.inserir(d);
            }
            p = p.getProximo();
        }
        return docs;
    }
}
