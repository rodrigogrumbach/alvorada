# Alexandria — o conhecimento desta solução

O passo a passo do processo (Athena Genesis: recebimento → registro da fonte → análise → fichas sugeridas →
formulário → respostas → revisão e aprovação → construção) está em **`PROCESSO.md`**. Este arquivo descreve o formato.

Fonte única das regras de negócio do cliente, versionada com o código. **Dados reais do cliente não entram aqui**
(fontes de preferência anonimizadas; nunca planilhas com casos reais).

- `fontes/`: **só o registro** `fontes/INDICE.md` — os originais ficam fora do Git e do jar, no ambiente protegido do cliente.
  Cada documento tem um código (D-01, D-02...), nome, `versao`, `recebido_em`, remetente, `local` (referência estável
  no acervo), se foi anonimizado, `dados_sigilosos` (o padrão é "não"; "sim" só com `autorizacao`: quem autorizou, em
  que ambiente seguro, quando) e o `sha256` do original. Registre com `.\mvnw.cmd exec:java@fonte` (ver PROCESSO.md).
  Nova versão de um documento = registro novo, com outro código.
- `formularios/`: formulários de validação preenchidos pelo cliente (cada item com código: P-01, E-01...).
- `conhecimento/`: uma ficha Markdown por item. O nome do arquivo começa pelo id (`REG-003-canais.md`).

## A ficha

```
---
id: REG-003                      # prefixo de 2 a 6 letras + número; nunca reaproveitar
tipo: regra                      # regra | entendimento | decisao | pergunta | glossario | aprendizado
titulo: Canais de entrada
status: aprovado                 # sugerido | pendente | aprovado | substituido | revogado
origem:
  - D-01, seção 2                 # o documento pelo código do registro (fontes/INDICE.md) + onde nele
validacao: Ana Lima (Ouvidoria) · 12/10/2026 · F-01 item P-02
formulario: F-01 · P-02           # o formulário e o item que validaram (o arquivo F-01... precisa existir em formularios/)
implementado_em: lista:Canais; campo:reclamacao.canal
implementacao: implementada      # pendente | implementada | nao_se_aplica (sem ela: deduzida, ver abaixo)
prevista_para: 1.3.0             # opcional, só em aprovada pendente: a versão em que entra (antes disso, "planejada")
visivel_sophia: sim              # a assistente pode usar para responder (só fichas aprovadas)
substitui: PER-01                # opcional: a ficha que esta substitui (que fica com status substituido)
observacao: ...                  # obrigatória quando pendente (o que falta) ou revogado (por que deixou de valer)
---
Texto oficial, fiel à fonte e ao que o cliente validou (é o que se audita).

## Como explicar
O mesmo, dito como a assistente diria a um colega: natural, sem códigos de regra nem citações.

## Perguntas
- jeitos de perguntar sobre o assunto (ajudam a assistente a encontrar a ficha)
```

`implementado_em`: `tipo:<codigo>`, `campo:<tipo>.<campo>`, `fase:<tipo>/<nome>`, `lista:<nome>`,
`alerta:<tipo>/<nome>`, `roteiro:<tipo>/<ponto>` (conferidos no sistema pelo teste), `codigo:<descrição>` e
`fora:<descrição>` (procedimento fora do sistema).

O build também confere o registro das fontes: código único, resumo com 64 hexadecimais, data, remetente,
`dados_sigilosos: não`, nenhum original na pasta `fontes/` e toda origem "D-xx" registrada. O registro (só os dados, nunca os originais) vai no pacote e
a tela da Base de conhecimento mostra, em cada origem, o documento, quando chegou, de quem e o início do resumo.

`implementacao` separa **validada** (status aprovado) de **construída**: `implementada` exige `implementado_em`;
`pendente` = aprovada e ainda não construída (aprovar vem antes de construir: é pendência, não erro);
`nao_se_aplica` = não há o que construir (glossário, decisão, entendimento). Sem a chave, vale o que se deduz:
com `implementado_em` → implementada; regra sem ele → pendente; os outros tipos → nao_se_aplica. **A assistente só usa
as aprovadas vigentes** (implementadas ou nao_se_aplica) e marcadas `visivel_sophia: sim`.

Histórico: corrigir o texto de uma regra = editar a ficha (o Git guarda a versão anterior); mudar a regra = ficha nova
com `substitui:` e a antiga com status `substituido`; regra que deixa de valer sem substituta = status `revogado` com
a observação. Ids nunca são reaproveitados. Fichas substituídas e revogadas ficam no repositório e não vão para o jar.

Formulário de validação: "Correto" → `aprovado`; "Precisa ajustar" → `pendente` com a observação; "A confirmar" →
`pendente`; o que for só inferido (sem o cliente confirmar) → `sugerido`. Pergunta respondida vira regra ou
entendimento que a substitui.

## No build

O `exec-maven-plugin` (fase `process-classes`, roda também com `-DskipTests`) valida todas as fichas — id repetido,
status, tipo ou implementação inválidos, origem ausente ou arquivo de origem inexistente, formulário inexistente,
aprovada/substituída/revogada sem validação, ficha não aprovada implementada, substituição quebrada — e **reprova o
build** com a lista. Regras aprovadas e ainda não implementadas aparecem como `[Alexandria] pendência:` no build e na
tela da Base de conhecimento; com `-Dalexandria.estrito=true` (ao gerar a versão que vai para o cliente) viram erro. Sem
erros, grava no jar (`alexandria/` no classpath) só as fichas **aprovadas** e o manifesto (versão do projeto, data,
ids e resumo SHA-256). Em produção a aplicação lê esse pacote; o repositório Git não vai para o cliente.

O `SolucaoTest` confere a rastreabilidade com o modelo instalado: tudo o que as fichas dizem implementar existe, e todo
campo, alerta e ponto do roteiro tem uma ficha aprovada de origem.

## Formulário de Esclarecimentos

```
.\mvnw.cmd exec:java@formulario
```

Gera em `formularios/` o **Formulário de Esclarecimentos** `F-xx-esclarecimentos.pdf` (PDF preenchível), o **Anexo de
Entendimentos Preliminares** `F-xx-anexo-entendimentos.pdf` e `F-xx-itens.properties` (item → ficha e o resumo da
ficha no envio). Entram só as fichas que ainda dependem do cliente: `sugerido` e `pendente`. Pergunta vira P-01,
P-02...; o resto vira E-01, E-02..., com "Correto / Precisa ajustar / A confirmar" e observação. O cliente não vê o id
interno das fichas; o formulário lembra de não escrever dados sigilosos. Cada ficha incluída ganha `formulario: F-xx ·
E-yy`. As fichas precisam estar válidas para gerar (o mesmo conferido no build). O número do formulário é o seguinte ao
maior que existe em `formularios/`. O PDFBox entra só nesse comando, nunca no jar da solução.

## Respostas do cliente

```
.\mvnw.cmd exec:java@respostas "-Drespostas=C:\caminho\F-02-respondido.pdf"
```

Lê o PDF devolvido (o código F-xx vem do próprio PDF) e **registra** cada resposta na ficha, sem aprovar nada:
`resposta_cliente: Correto · Ana Lima (Ouvidoria) · 20/10/2026 · F-02 item E-03`, status `pendente` e a observação
do cliente. "Correto" com ressalva ("mas", "exceto", "depende", "?"...) fica marcado como ambíguo; P respondida fica
pendente com a resposta (escreve-se a regra que a substitui). Item sem resposta ou ficha alterada depois do envio não
muda. Nada é gravado se faltar quem respondeu, se o formulário já foi aplicado ou se alguma ficha ficar inválida.
**O PDF respondido não fica no repositório**: a evidência fica em `formularios/F-xx-respostas.md` (resumo SHA-256 do
PDF, quem respondeu, quando foi aplicado e a transcrição de cada resposta).

## Aprovação (depois da revisão)

```
.\mvnw.cmd exec:java@aprovar "-Dfichas=ENT-001,REG-008" "-Drevisor=Nome de quem revisou"
```

Promove a `aprovado` só o que tem base na resposta do cliente — `resposta_cliente: Correto` sem ressalva, ou a ficha
nova que `substitui` uma pergunta respondida (a pergunta passa a `substituido`) —, com a validação "resposta do cliente
· revisado por <revisor> em <data>". Recusa ambígua, "Precisa ajustar", "A confirmar", pergunta e ficha sem resposta.
As mudanças nas fichas aparecem no Git, como qualquer edição: é ali que se revisa e se guarda o histórico.
