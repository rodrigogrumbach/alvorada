# Athena Genesis — da documentação do cliente ao ModeloDeSolucao

Procedimento para construir e evoluir a solução de um cliente a partir dos documentos dele. Vale para qualquer pessoa
do projeto e para as IAs (Claude, ChatGPT): **siga este roteiro em todo documento novo**. O formato das fichas e o que o
build confere estão em `LEIA-ME.md`.

A Alexandria é a memória permanente do projeto: cada regra da solução sabe de qual documento veio, quem a validou, em
que formulário e onde está no sistema — e o Git guarda cada mudança.

## Visão geral

```
1. Recebimento ──► 2. Registro da fonte ──► 3. Análise ──► 4. Fichas sugeridas
                                                                   │
8. Construção do ModeloDeSolucao ◄── 7. Revisão e aprovação ◄── 6. Respostas ◄── 5. Formulário + Anexo
                                                                   │
9. Versão para o cliente (build estrito) ──► a assistente usa só o que foi aprovado e construído
```

| Etapa | Quem | Ferramenta | Resultado |
|---|---|---|---|
| 1. Recebimento | equipe | — | documento no ambiente protegido do cliente |
| 2. Registro da fonte | equipe | `exec:java@fonte` | bloco D-xx em `fontes/INDICE.md` |
| 3. Análise | IA + equipe | conversa | conceitos, regras aparentes e dúvidas |
| 4. Fichas sugeridas | IA + equipe | arquivos em `conhecimento/` | fichas `sugerido` e `pergunta` |
| 5. Formulário e Anexo | equipe | `exec:java@formulario` | `F-xx-esclarecimentos.pdf` e `F-xx-anexo-entendimentos.pdf` |
| 6. Respostas | cliente → equipe | `exec:java@respostas` | `resposta_cliente` em cada ficha + `F-xx-respostas.md` |
| 7. Revisão e aprovação | equipe | `exec:java@aprovar` | fichas `aprovado`, com a validação e o revisor |
| 8. Construção | equipe + IA | `ModeloDaSolucao`, testes | `implementado_em`; rastreabilidade conferida |
| 9. Versão | equipe | `instalacao\GERAR-PACOTE.bat` | jar com o pacote aprovado (build estrito) |

## Cuidados com os dados (valem em todas as etapas)

- **Padrão: documento anonimizado ou fictício.** Planilha: só o cabeçalho e linhas inventadas. Peça ao cliente para
  tirar nomes, CPF/CNPJ, contas, contratos e valores reais antes de enviar.
- **Os originais permanecem no ambiente protegido do cliente.** A Brainstorming não recebe dado sigiloso (cláusula de
  contrato). Acesso excepcional a dado real — só quando **estritamente necessário** — precisa ser **expressamente
  autorizado e rastreável**: feito no ambiente indicado pelo cliente e registrado na fonte
  (`-Dsigiloso=sim -Dautorizacao="quem autorizou, em que ambiente, quando"`); o Git guarda quem registrou e quando.
- **Nada sensível sai do documento para a Alexandria**: fichas, exemplos, perguntas, observações e textos da
  assistente descrevem a **regra**, nunca o caso real. Exemplo: "reclamações com valor acima de R$ 10 mil vão para o
  Jurídico" — e não "a reclamação do cliente Fulano, de R$ 12.345,67...". Exemplo necessário = inventado.
- **O original nunca entra no Git nem no jar.** Fica no ambiente protegido do cliente; a Alexandria guarda o registro com o resumo
  SHA-256 e a referência estável ao local.
- Formulário e respostas também não carregam dado sigiloso (o formulário avisa o cliente).

## 1. Recebimento

1. O documento fica no **ambiente protegido do cliente**, num caminho estável, com a data e a versão no nome
   (ex.: `<repositório do cliente>/projeto-solucao/<aaaa-mm-dd>/<nome>-v<versão>.<ext>`). Esse caminho é o `local`
   do registro. Material anonimizado ou fictício enviado à Brainstorming segue a mesma regra de nome.
2. Confira se há dado sigiloso. Se houver e a análise não for estritamente necessária com ele, **devolva** e peça a
   versão anonimizada.

## 2. Registro da fonte

```
.\mvnw.cmd exec:java@fonte "-Dfonte=C:\acervo\acme\2026-10-14\politica-v3.pdf" "-Dtitulo=Política de atendimento" ^
    "-Dversao=3" "-Dremetente=Ana Lima (Ouvidoria)" "-Dlocal=acervo-clientes/acme/2026-10-14/politica-v3.pdf" ^
    "-Danonimizado=sim" "-Dsigiloso=não"
```

O comando calcula o SHA-256, dá o próximo código (D-01, D-02...) e escreve o bloco em `fontes/INDICE.md`.
Opcionais: `-Drecebido=dd/mm/aaaa` (padrão: hoje), `-Dautorizacao=...` (obrigatória com `-Dsigiloso=sim`),
`-Dobservacao=...`. Nova versão do mesmo documento = registro novo, com outro código. O mesmo arquivo registrado duas
vezes é recusado.

## 3. Análise (IA + equipe)

Leia o documento e anote, sem escrever ficha ainda:

- **conceitos**: o que é registrado (tipos de caso), seus dados (campos e tipos), listas de opções, etapas (fases);
- **relações**: o que se liga a quê (pessoa, documento, agência, caso);
- **regras aparentes**: prazos, limites, obrigatoriedades, quem decide, o que dispara alerta;
- **lacunas e contradições**: o que o documento não diz ou diz de dois jeitos — viram perguntas.

Regra de ouro: **regra de negócio não se decide por suposição**. O que a IA infere é sempre `sugerido`.

## 4. Fichas sugeridas

Uma ficha por conceito ou regra, em `conhecimento/<ID>-<nome>.md` (formato em `LEIA-ME.md`):

- **status**: `sugerido` para o que foi entendido; tipo `pergunta` (status `pendente`, com a `observacao` dizendo o que
  falta) para o que precisa ser perguntado;
- **origem exata**: o código da fonte e onde nela — `D-02, aba Reclamações, coluna F` · `D-01, p. 4, item 3.2` ·
  `reunião de 15/10/2026 com a Ouvidoria`;
- **texto**: a regra como entendemos, em frases que o cliente consiga dizer "está certo" ou "não está";
- **"## Como explicar"**: a mesma coisa em linguagem natural (é o que a assistente vai dizer, depois de aprovada);
- nunca reaproveite um id; nunca copie dado real para a ficha.

O build confere o formato a cada compilação (`.\mvnw.cmd test` ou `package`).

## 5. Formulário de Esclarecimentos e Anexo

```
.\mvnw.cmd exec:java@formulario
```

Gera, das fichas `sugerido` e `pendente`, o formulário PDF preenchível (P-xx para perguntas; E-xx para entendimentos,
com Correto / Precisa ajustar / A confirmar e observação) e o Anexo com o texto completo e a origem de cada item. Cada
ficha ganha `formulario: F-xx · E-yy`. Revise o PDF antes de enviar: **só as perguntas necessárias**; se sobrou ficha
que não precisa ir ao cliente agora, ajuste o status dela e gere de novo (apague o F-xx gerado antes).

## 6. Respostas do cliente

```
.\mvnw.cmd exec:java@respostas "-Drespostas=C:\acervo\acme\2026-10-20\F-02-respondido.pdf"
```

Guarde o PDF respondido no ambiente protegido do cliente (ele **não** entra no repositório). O comando registra cada resposta na
ficha (`resposta_cliente: Correto · Ana Lima (Ouvidoria) · 20/10/2026 · F-02 item E-03`), deixa a ficha `pendente`
e grava a evidência em `formularios/F-xx-respostas.md` (resumo SHA-256 do PDF, quem respondeu, transcrição).
**Nada é aprovado nesta etapa.** O comando lista o que olhar:

- **"Correto" com ressalva** ("mas", "exceto", "depende", "?"...) → ambígua: esclareça com o cliente;
- **"Precisa ajustar"** → reescreva o entendimento com a observação do cliente (sem explicação: pergunte);
- **"A confirmar"** → continua pendente para o próximo formulário;
- **pergunta respondida** → escreva a regra ou o entendimento que a **substitui** (ficha nova com `substitui: PER-xx`,
  origem `formularios/F-xx-respostas.md, P-yy`); resposta ambígua: esclareça antes;
- **ficha alterada depois do envio** → a resposta não foi aplicada: compare e aplique à mão.

## 7. Revisão e aprovação

Antes de aprovar, **revise a interpretação**: a resposta confirma mesmo o texto da ficha? O "Correto" vale para o caso
geral, sem exceção? Qualquer dúvida → a ficha continua pendente e a pergunta volta no próximo formulário.

```
.\mvnw.cmd exec:java@aprovar "-Dfichas=ENT-001,REG-008" "-Drevisor=Rodrigo Grumbach"
```

Aprova só o que tem base em resposta do cliente: `resposta_cliente: Correto` sem ressalva, ou a ficha nova que
substitui uma pergunta respondida (a pergunta passa a `substituido`). A validação fica assim:
`Ana Lima (Ouvidoria) · 20/10/2026 · F-02 item E-03 · revisado por Rodrigo Grumbach em 22/10/2026` (a revisão
nunca é anterior à resposta, e a resposta nunca está no futuro: as ferramentas recusam). Ambígua, "Precisa
ajustar", "A confirmar", pergunta e ficha sem resposta são recusadas. Decisão tomada em reunião (sem formulário):
edite a ficha à mão com a `validacao` (quem, data, reunião) — o Git mostra quem fez.

## 8. Construção do ModeloDeSolucao

1. Implemente no `ModeloDaSolucao` o que as fichas aprovadas descrevem e marque em cada ficha o `implementado_em`
   (`campo:<tipo>.<campo>`, `lista:`, `fase:`, `alerta:`, `roteiro:`...). A ajuda dos campos vem da ficha
   (`alexandria.explicacao("REG-008")`).
2. O `SolucaoTest` confere a rastreabilidade nos dois sentidos: tudo o que as fichas citam existe, e todo campo,
   alerta e ponto do roteiro tem ficha aprovada de origem.
3. Ficha aprovada e ainda não construída é **pendência da versão** (aviso `[Alexandria] pendência desta versão:`), não
   erro. Se ela entra só numa versão posterior, marque `prevista_para: 1.3.0` (a versão da solução): vira **planejada**,
   aparece no build e na tela, e não barra a versão atual. Ao chegar a versão prevista, volta a ser pendência obrigatória.

## 9. Versão para o cliente

`instalacao\GERAR-PACOTE.bat` compila em **modo estrito**: não sai versão com pendência desta versão (regra aprovada, não construída e sem
`prevista_para` posterior). O jar
leva só as fichas aprovadas e o registro das fontes (sem os originais), com resumo SHA-256. A assistente usa, nas
respostas e ajudas, só as aprovadas que já estão no sistema, em linguagem natural, sem códigos nem citações.

## Histórico e mudanças

- **Corrigir o texto** de uma ficha (sem mudar a regra): edite; o Git guarda a anterior.
- **Mudar a regra**: ficha nova com `substitui:`; a antiga passa a `substituido` (o `@aprovar` faz isso para perguntas).
- **Regra que deixa de valer** sem substituta: status `revogado`, com `validacao` (quem decidiu) e `observacao` (por quê).
- Dúvidas registradas pelos usuários na aplicação ("Pergunte", sem resposta) voltam como fichas `sugerido` ou
  `pergunta` no próximo ciclo.
