package br.com.brainstorming.cliente;

import br.com.brainstorming.athena.alexandria.Alexandria;
import br.com.brainstorming.athena.alexandria.RastreabilidadeAlexandria;
import br.com.brainstorming.athena.casos.ConfiguracaoService;
import br.com.brainstorming.athena.casos.ModeloDeSolucao;
import br.com.brainstorming.athena.seguranca.UsuarioAthena;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * O caminho de um projeto de cliente funciona: a aplicação sobe com a Athena inteira (estrutura do banco da Athena pelo
 * master do cliente), o modelo do cliente aparece ao lado do da Athena e instala o tipo de caso dele.
 */
@SpringBootTest(classes = SolucaoApplication.class, properties = "spring.datasource.url=jdbc:h2:mem:solucao-teste;DB_CLOSE_DELAY=-1")
class SolucaoTest {

    @Autowired List<ModeloDeSolucao> modelos;
    @Autowired ConfiguracaoService config;
    @Autowired JdbcTemplate jdbc;
    @Autowired Alexandria alexandria;
    @Autowired RastreabilidadeAlexandria rastreabilidade;

    @Test
    void solucaoSobeComAAthenaEOModeloDoCliente() {
        assertTrue(modelos.stream().anyMatch(m -> m.codigo().equals("exemplo-reclamacoes")), "modelo do cliente registrado");
        assertTrue(modelos.stream().anyMatch(m -> m.codigo().equals("risco-operacional")), "modelo da Athena continua disponível");
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM ath_usuario WHERE login = 'admin'", Integer.class), "estrutura da Athena criada");

        UsuarioAthena adm = new UsuarioAthena(1, "admin", "Administrador", "x", true, true, false, Map.of());
        ModeloDeSolucao meu = modelos.stream().filter(m -> m.codigo().equals("exemplo-reclamacoes")).findFirst().orElseThrow();
        assertEquals(List.of(), meu.instalar(false, adm));
        var t = config.completo("reclamacao").orElseThrow();
        assertEquals(List.of("Recebida", "Em análise", "Respondida", "Encerrada"), t.fases().stream().map(f -> f.nome()).toList());
        assertEquals(5, t.camposAtivos().size());
        assertTrue(meu.instalado());

        // Alexandria: o pacote aprovado foi para dentro da aplicação (o build gravou só as aprovadas) e tudo é rastreável
        assertTrue(alexandria.disponivel(), "pacote de conhecimento no classpath");
        assertTrue(alexandria.ficha("PER-01").isEmpty(), "pendente não vai para o pacote");
        assertTrue(alexandria.ficha("ENT-001").isEmpty(), "sugerida não vai para o pacote");
        assertEquals(alexandria.explicacao("REG-003"), t.camposAtivos().stream().filter(c -> c.codigo().equals("canal")).findFirst().orElseThrow().ajuda(),
                "a ajuda do campo vem da ficha");
        assertEquals(List.of(), rastreabilidade.conferir(List.of(ModeloDaSolucao.CODIGO)));
    }
}
