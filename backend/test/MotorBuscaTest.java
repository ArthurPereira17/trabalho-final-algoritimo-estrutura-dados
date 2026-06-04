package testes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import busca.MotorBusca;
import dominio.Documento;
import estruturas.ListaEncadeada;
import estruturas.NoLista;
import indice.IndiceInvertido;

/**
 * Testes da busca de 1 palavra (RF10) e da busca AND de N palavras (RF11).
 */
public class MotorBuscaTest {

    private IndiceInvertido criarIndice() {
        IndiceInvertido idx = new IndiceInvertido(101, 17);
        int d0 = idx.registrarDocumento("a.txt"); // casa, tempo
        int d1 = idx.registrarDocumento("b.txt"); // casa, rua
        int d2 = idx.registrarDocumento("c.txt"); // tempo, rua

        idx.adicionarOcorrencia("casa", d0);
        idx.adicionarOcorrencia("tempo", d0);
        idx.adicionarOcorrencia("casa", d1);
        idx.adicionarOcorrencia("rua", d1);
        idx.adicionarOcorrencia("tempo", d2);
        idx.adicionarOcorrencia("rua", d2);
        return idx;
    }

    private boolean contemCaminho(ListaEncadeada<Documento> l, String c) {
        NoLista<Documento> p = l.getPrimeiro();
        while (p != null) {
            if (p.getInfo().getCaminho().equals(c)) {
                return true;
            }
            p = p.getProximo();
        }
        return false;
    }

    @Test
    void buscaUmaPalavra() {
        MotorBusca m = new MotorBusca(criarIndice());
        ListaEncadeada<Documento> r = m.pesquisar("casa");
        assertEquals(2, r.obterComprimento());
        assertTrue(contemCaminho(r, "a.txt"));
        assertTrue(contemCaminho(r, "b.txt"));
    }

    @Test
    void buscaAndComInterseccao() {
        MotorBusca m = new MotorBusca(criarIndice());
        ListaEncadeada<Documento> r = m.pesquisar("casa tempo");
        assertEquals(1, r.obterComprimento()); // apenas a.txt tem ambas
        assertTrue(contemCaminho(r, "a.txt"));
    }

    @Test
    void buscaAndComTermoInexistenteRetornaVazio() {
        MotorBusca m = new MotorBusca(criarIndice());
        ListaEncadeada<Documento> r = m.pesquisar("casa naoexiste");
        assertEquals(0, r.obterComprimento());
    }

    @Test
    void buscaIgnoraMaiusculasEPontuacao() {
        MotorBusca m = new MotorBusca(criarIndice());
        assertEquals(2, m.pesquisar("CASA").obterComprimento());
        assertEquals(2, m.pesquisar("rua.").obterComprimento());
        assertEquals(1, m.pesquisar("Casa, Tempo!").obterComprimento());
    }
}
