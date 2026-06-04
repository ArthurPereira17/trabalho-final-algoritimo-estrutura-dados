package testes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import estruturas.ListaEncadeada;
import indice.ExtratorPalavras;

/**
 * Testes das regras de extração de palavras (RF03–RF06).
 */
public class ExtratorPalavrasTest {

    private final ExtratorPalavras extrator = new ExtratorPalavras();

    private boolean contem(ListaEncadeada<String> lista, String alvo) {
        return lista.buscar(alvo) != null;
    }

    @Test
    void converteParaMinusculas() {
        ListaEncadeada<String> r = extrator.extrair("Casa CASA cAsA");
        assertEquals(3, r.obterComprimento());
        assertTrue(contem(r, "casa"));
    }

    @Test
    void descartaPalavrasComMenosDe3Letras() {
        ListaEncadeada<String> r = extrator.extrair("eu vou no rio ja");
        assertTrue(contem(r, "vou"));
        assertTrue(contem(r, "rio"));
        assertFalse(contem(r, "eu"));
        assertFalse(contem(r, "no"));
        assertFalse(contem(r, "ja"));
    }

    @Test
    void descartaDigitosEPontuacao() {
        ListaEncadeada<String> r = extrator.extrair("ano 2024, total: 1.500 reais!!!");
        assertTrue(contem(r, "ano"));
        assertTrue(contem(r, "total"));
        assertTrue(contem(r, "reais"));
        assertFalse(contem(r, "2024"));
        assertFalse(contem(r, "1500"));
        assertFalse(contem(r, "1.500"));
    }

    @Test
    void mantemAcentosDoPortugues() {
        ListaEncadeada<String> r = extrator.extrair("Água ÁGUA órgão coração");
        assertTrue(contem(r, "água"));
        assertTrue(contem(r, "órgão"));
        assertTrue(contem(r, "coração"));
    }

    @Test
    void digitoNoMeioSeparaToken() {
        ListaEncadeada<String> r = extrator.extrair("casa2quarto");
        assertTrue(contem(r, "casa"));
        assertTrue(contem(r, "quarto"));
        assertFalse(contem(r, "casa2quarto"));
    }

    @Test
    void textoVazioRetornaListaVazia() {
        assertTrue(extrator.extrair("").estaVazia());
        assertTrue(extrator.extrair("12 . , ! 7").estaVazia());
    }
}
