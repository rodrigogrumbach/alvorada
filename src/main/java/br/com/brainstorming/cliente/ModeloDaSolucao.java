package br.com.brainstorming.cliente;

import br.com.brainstorming.athena.alexandria.Alexandria;
import br.com.brainstorming.athena.assistente.RoteiroService;
import br.com.brainstorming.athena.casos.AlertasService;
import br.com.brainstorming.athena.casos.Configuracao.Catalogo;
import br.com.brainstorming.athena.casos.Configuracao.Fase;
import br.com.brainstorming.athena.casos.Configuracao.TipoCompleto;
import br.com.brainstorming.athena.casos.ConfiguracaoRepository;
import br.com.brainstorming.athena.casos.ConfiguracaoService;
import br.com.brainstorming.athena.casos.ModeloDeSolucao;
import br.com.brainstorming.athena.seguranca.UsuarioAthena;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * O modelo da solução deste cliente: monta por configuração os tipos de caso que os documentos e planilhas dele
 * descrevem. Aparece em Configuração → "Modelo pronto" e é instalado com um clique. ESTE É UM EXEMPLO GENÉRICO
 * ("Reclamações de clientes"): no projeto de um cliente, trocar pelo desenho validado com ele. Cada campo, lista,
 * fase, alerta e ponto do roteiro nasce de uma ficha aprovada da Alexandria (pasta alexandria/), e a ajuda dos campos
 * vem da própria ficha.
 */
@Service
public class ModeloDaSolucao implements ModeloDeSolucao {

    static final String CODIGO = "reclamacao";
    static final List<String> CANAIS = List.of("Telefone", "E-mail", "Site", "Presencial");
    static final List<String> MOTIVOS = List.of("Cobrança", "Atendimento", "Produto", "Prazo", "Outros");

    private final ConfiguracaoService config;
    private final ConfiguracaoRepository configRepo;
    private final AlertasService alertas;
    private final RoteiroService roteiro;
    private final Alexandria alexandria;

    public ModeloDaSolucao(ConfiguracaoService config, ConfiguracaoRepository configRepo, AlertasService alertas, RoteiroService roteiro,
                           Alexandria alexandria) {
        this.config = config;
        this.configRepo = configRepo;
        this.alertas = alertas;
        this.roteiro = roteiro;
        this.alexandria = alexandria;
    }

    @Override public String codigo() { return "exemplo-reclamacoes"; }
    @Override public String nome() { return "Reclamações de clientes (exemplo)"; }
    @Override public String descricao() { return "Exemplo do modelo de um projeto de cliente: o tipo \"Reclamação\" com canal, motivo, fases e um alerta."; }
    @Override public List<String> itens() {
        return List.of("Listas: Canais e Motivos", "Campos: data de recebimento, canal, motivo, valor envolvido e descrição",
                "Fases: Recebida → Em análise → Respondida → Encerrada", "Alerta: concentração por motivo; roteiro do relato com 2 pontos");
    }
    @Override public boolean instalado() { return configRepo.tipoPorCodigo(CODIGO).isPresent(); }

    @Override
    @Transactional
    public List<String> instalar(boolean comExemplos, UsuarioAthena u) {
        if (instalado()) return List.of("O modelo já está instalado.");
        List<String> erros = new ArrayList<>();
        erros.addAll(config.gravarCatalogo(null, "Canais", String.join("\n", CANAIS)));
        erros.addAll(config.gravarCatalogo(null, "Motivos", String.join("\n", MOTIVOS)));
        ConfiguracaoService.Resultado r = config.criarTipo(CODIGO, "Reclamação", "Reclamações", "Atendimento",
                "Reclamações de clientes: canal, motivo, prazo de resposta e o que foi feito.", "RC", "ouvidoria");
        if (!r.ok()) return r.erros();
        long tipo = r.id();
        for (Fase f : configRepo.fases(tipo)) configRepo.excluirFase(tipo, f.id());
        String[] fases = {"Recebida", "Em análise", "Respondida", "Encerrada"};
        for (int i = 0; i < fases.length; i++) configRepo.inserirFase(tipo, fases[i], (i + 1) * 10, ConfiguracaoService.CORES.get(i), i == fases.length - 1);

        Long canais = lista("Canais"), motivos = lista("Motivos");
        erros.addAll(config.adicionarCampo(tipo, "data_recebimento", "Data de recebimento", "DATA", true, ajuda("REG-002"), null, true));
        erros.addAll(config.adicionarCampo(tipo, "canal", "Canal", "LISTA", true, ajuda("REG-003"), canais, true));
        erros.addAll(config.adicionarCampo(tipo, "motivo", "Motivo", "LISTA", true, ajuda("REG-004"), motivos, true));
        erros.addAll(config.adicionarCampo(tipo, "valor", "Valor envolvido", "MOEDA", false, ajuda("REG-005"), null, true));
        erros.addAll(config.adicionarCampo(tipo, "descricao", "Descrição", "TEXTO_LONGO", true, ajuda("REG-006"), null, false));
        if (!erros.isEmpty()) throw new IllegalStateException("Modelo inválido: " + erros);

        TipoCompleto t = config.completo(tipo).orElseThrow();
        long campoMotivo = id(t, "motivo"), campoData = id(t, "data_recebimento");
        erros.addAll(alertas.criar(t, "Concentração por motivo", "CONCENTRACAO", campoMotivo, null, campoData, 7, 5, BigDecimal.valueOf(2), null));
        erros.addAll(roteiro.adicionar(tipo, "O que o cliente pede", "pede, solicita, quer, deseja", "O que o cliente pede?",
                "o estorno do valor\numa explicação\na troca do produto", "O cliente pede {resposta}."));
        erros.addAll(roteiro.adicionar(tipo, "O que foi respondido", "respondid, informad, orientad", "O que foi respondido?",
                "que o caso está em análise\no prazo de solução\na solução aplicada", "Foi respondido {resposta}."));
        if (!erros.isEmpty()) throw new IllegalStateException("Modelo inválido: " + erros);
        return List.of();
    }

    /** A ajuda do campo vem da ficha aprovada da Alexandria (a explicação em linguagem natural, sem código de regra). */
    private String ajuda(String ficha) {
        return alexandria.explicacao(ficha);
    }

    private Long lista(String nome) {
        return config.catalogos().stream().filter(c -> c.nome().equals(nome)).map(Catalogo::id).findFirst().orElseThrow();
    }

    private static long id(TipoCompleto t, String codigo) {
        return t.campos().stream().filter(c -> c.codigo().equals(codigo)).findFirst().orElseThrow().id();
    }
}
