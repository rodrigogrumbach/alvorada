package br.com.brainstorming.cliente;

import br.com.brainstorming.athena.casos.ModeloDeSolucao;
import br.com.brainstorming.athena.seguranca.UsuarioAthena;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * As telas da Alexandria numa solução com pacote de conhecimento, renderizadas de verdade: a base de conhecimento (só
 * com a permissão própria), a ficha com o rastreio, e a ajuda contextual (ⓘ) nos campos, nas fases e nas regras de
 * alerta — com a explicação natural e sem código de regra, origem ou validação.
 */
@SpringBootTest(classes = SolucaoApplication.class, properties = "spring.datasource.url=jdbc:h2:mem:solucao-alexandria;DB_CLOSE_DELAY=-1")
class AlexandriaTelasTest {

    @Autowired WebApplicationContext ctx;
    @Autowired @Qualifier("springSecurityFilterChain") Filter seguranca;
    @Autowired List<ModeloDeSolucao> modelos;

    MockMvc mvc;
    final UsuarioAthena adm = new UsuarioAthena(1, "admin", "Administrador", "x", true, true, false, Map.of());

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(ctx).addFilters(seguranca).build();
        ModeloDeSolucao meu = modelos.stream().filter(m -> m.codigo().equals("exemplo-reclamacoes")).findFirst().orElseThrow();
        if (!meu.instalado()) assertEquals(List.of(), meu.instalar(false, adm));
    }

    @Test
    void baseDeConhecimentoEFicha() throws Exception {
        String inicio = pagina(sessao(adm), "/");
        assertTrue(inicio.contains("Base de conhecimento"), "card na tela inicial");

        String lista = pagina(sessao(adm), "/alexandria");
        assertTrue(lista.contains("REG-007") && lista.contains("Concentração de reclamações por motivo") && lista.contains("GLO-001"), lista);
        assertFalse(lista.contains("PER-01") || lista.contains("ENT-001"), "pendente e sugerida não estão no pacote");
        assertFalse(lista.contains("No sistema, sem ficha de origem"), "o modelo de exemplo está todo rastreado");
        String busca = pagina(sessao(adm), "/alexandria?q=telefone");
        assertTrue(busca.contains("REG-003") && !busca.contains("REG-007"), "busca por palavra, sem acento");
        assertTrue(pagina(sessao(adm), "/alexandria?tipo=glossario").contains("GLO-001"));

        String ficha = pagina(sessao(adm), "/alexandria/REG-003");
        assertTrue(ficha.contains("Texto oficial") && ficha.contains("D-01, item 2"), "origem no rastreio");
        assertTrue(ficha.contains("Política de atendimento de reclamações (exemplo fictício) · recebido em 12/10/2026 de Ana Lima"), "dados do documento registrado");
        assertTrue(ficha.contains("Escolha por onde o cliente fez a reclamação"), "como a assistente explica");
        assertTrue(ficha.contains("reclamacao.canal") && ficha.contains("/config/listas/"), "implementação conferida, com link para o administrador");
        assertEquals(404, mvc.perform(get("/alexandria/PER-01").session(sessao(adm))).andReturn().getResponse().getStatus());

        // permissão própria: quem só vê as reclamações não entra na base de conhecimento
        UsuarioAthena atendente = new UsuarioAthena(2, "atendente", "Atendente", "x", true, false, false, Map.of("caso:reclamacao", 'T'));
        assertEquals(403, mvc.perform(get("/alexandria").session(sessao(atendente))).andReturn().getResponse().getStatus());
        UsuarioAthena auditor = new UsuarioAthena(3, "auditor", "Auditor", "x", true, false, false, Map.of("alexandria", 'V'));
        String vista = pagina(sessao(auditor), "/alexandria/REG-003");
        assertTrue(vista.contains("reclamacao.canal") && !vista.contains("href=\"/config/"), "sem link para a configuração");
    }

    @Test
    void ajudaContextualNasTelasDoTipo() throws Exception {
        UsuarioAthena atendente = new UsuarioAthena(2, "atendente", "Atendente", "x", true, false, false, Map.of("caso:reclamacao", 'T'));
        String novo = pagina(sessao(atendente), "/casos/reclamacao/novo");
        assertTrue(novo.contains("ath-ajuda-botao") && novo.contains("Canais de entrada")
                && novo.contains("Escolha por onde o cliente fez a reclamação"), "ⓘ do campo com a explicação natural");
        assertFalse(novo.contains("REG-0") || novo.contains("fontes/") || novo.contains("F-01"), "nada de código, origem ou validação na tela");
        assertEquals(1, ocorrencias(novo, "Escolha por onde o cliente fez a reclamação"), "a ajuda copiada no campo não se repete embaixo");

        String alertas = pagina(sessao(atendente), "/casos/reclamacao/alertas");
        assertTrue(alertas.contains("ajuda-regra-") && alertas.contains("eu aviso: pode haver um problema comum"), "ⓘ na regra de alerta");
        assertFalse(alertas.contains("REG-0"));
    }

    private MockHttpSession sessao(UsuarioAthena u) {
        MockHttpSession s = new MockHttpSession();
        s.setAttribute("SPRING_SECURITY_CONTEXT", new SecurityContextImpl(new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities())));
        return s;
    }

    private String pagina(MockHttpSession s, String url) throws Exception {
        MvcResult r = mvc.perform(get(url).session(s)).andReturn();
        assertEquals(200, r.getResponse().getStatus(), url + " → " + r.getResponse().getStatus() + " " + r.getResponse().getForwardedUrl());
        return r.getResponse().getContentAsString();
    }

    private static int ocorrencias(String texto, String trecho) {
        int n = 0, i = 0;
        while ((i = texto.indexOf(trecho, i)) >= 0) { n++; i += trecho.length(); }
        return n;
    }
}
