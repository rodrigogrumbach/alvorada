# Registro das fontes

Cada documento que o cliente enviou, com um código (D-01, D-02...) que as fichas citam na origem ("D-01, item 2").
**Os originais ficam fora do Git e do jar**, no ambiente protegido do cliente, indicado em `local`; aqui fica só o registro, com o
resumo SHA-256 que prova qual versão foi usada. Registre com `.\mvnw.cmd exec:java@fonte` (ver alexandria/PROCESSO.md).
O padrão é documento anonimizado ou fictício; documento com dado sigiloso só com `autorizacao`.

## D-01 · Política de atendimento de reclamações (exemplo fictício)
sha256: d311f22f05b837db237a096a7a4bec2b63707a8bcbcafc0fac609f747f8d9057
versao: 1
recebido_em: 12/10/2026
remetente: Ana Lima (Ouvidoria, fictícia)
local: acervo-exemplo/reclamacoes/2026-10-12/politica-atendimento-v1.md (fictício: documento inventado para o projeto-modelo)
anonimizado: sim
dados_sigilosos: não
observacao: política de 7 itens (registro, canais, motivos, valor, relato, etapas e concentração por motivo)

## D-02 · Procedimento PQ-07 - Tratamento de Não Conformidades de Fornecedores
sha256: 26cd51c4b9eba15f74c00917b8151572704ad38d2a8a42f751dfb78c434668fe
versao: 3
recebido_em: 13/10/2026
remetente: Gerência da Qualidade da Metalúrgica Alvorada (fictícia)
local: alvorada-qualidade/2026-10-13/PQ-07-Tratamento-de-NC-de-fornecedores-v3.pdf
anonimizado: sim
dados_sigilosos: não
observacao: documento fictício criado para teste da Athena; procedimento de 10 itens (objetivo, abertura, tipos, gravidade, etapas, plano de ação, custo, reincidência, encerramento, indicadores)

## D-03 · Planilha de controle de NCs de fornecedores 2026
sha256: 757094739dc106ee25a32090b3f9a3f3f53ee876c366cdec1672a989f430a80b
versao: 2026 (sem número de versão no arquivo)
recebido_em: 13/10/2026
remetente: Gerência da Qualidade da Metalúrgica Alvorada (fictícia)
local: alvorada-qualidade/2026-10-13/Controle-NC-fornecedores-2026.xlsx
anonimizado: sim
dados_sigilosos: não
observacao: planilha fictícia (sem dados reais): abas Controle de NCs 2026 (12 colunas, 12 NCs inventadas), Listas (Tipo, Gravidade, Etapa) e Leia-me
