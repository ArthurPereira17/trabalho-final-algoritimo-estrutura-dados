package indice;

import java.util.Locale;

import estruturas.ListaEncadeada;

/**
 * Extrai palavras válidas de um trecho de texto, aplicando as regras do
 * enunciado (RF02–RF06):
 *
 *  - Não distinguir maiúsculas/minúsculas  -> converte para minúsculas.
 *  - Desprezar pontuação                   -> qualquer não-letra é separador.
 *  - Ignorar tokens só de dígitos/pontos   -> dígitos também são separadores,
 *                                             então tokens são SÓ de letras.
 *  - Apenas palavras com 3+ letras         -> filtro de comprimento mínimo.
 *
 * Estratégia: varredura por "corridas de letras". Percorre o texto e isola
 * sequências contíguas de {@link Character#isLetter(char)} (que aceita
 * acentuação do português: á, ç, ã, é, ...). Não usa StringBuilder nem
 * coleções nativas — apenas índices e {@code substring}.
 */
public class ExtratorPalavras {

    public static final int TAMANHO_MINIMO = 3;

    /**
     * Extrai todas as palavras válidas de uma linha/texto.
     *
     * @param texto trecho a tokenizar (pode ser uma linha de arquivo ou a
     *              própria consulta do usuário)
     * @return lista encadeada com as palavras já normalizadas (minúsculas)
     */
    public ListaEncadeada<String> extrair(String texto) {
        ListaEncadeada<String> palavras = new ListaEncadeada<>();
        if (texto == null || texto.isEmpty()) {
            return palavras;
        }

        int inicio = -1; // índice de início da corrida de letras atual (-1 = nenhuma)
        int n = texto.length();

        for (int i = 0; i <= n; i++) {
            boolean ehLetra = i < n && Character.isLetter(texto.charAt(i));

            if (ehLetra) {
                if (inicio < 0) {
                    inicio = i;
                }
            } else if (inicio >= 0) {
                // fechou um token (de letras): texto[inicio .. i)
                if (i - inicio >= TAMANHO_MINIMO) {
                    String token = texto.substring(inicio, i).toLowerCase(Locale.ROOT);
                    palavras.inserir(token);
                }
                inicio = -1;
            }
        }

        return palavras;
    }

    /**
     * Normaliza uma única palavra com as mesmas regras de extração.
     * Retorna {@code null} se a palavra não for válida (menos de 3 letras
     * ou contendo caracteres não-letra).
     */
    public String normalizar(String palavra) {
        ListaEncadeada<String> r = extrair(palavra);
        if (r.estaVazia()) {
            return null;
        }
        // uma palavra "limpa" gera exatamente um token
        return r.getPrimeiro().getInfo();
    }
}
