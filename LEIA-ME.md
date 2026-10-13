# Projeto de cliente sobre a Athena (modelo)

Ponto de partida de cada solução de cliente. Fica dentro do repositório da Athena só para ser compilado e testado junto
(o caminho continua funcionando a cada mudança da Athena); o projeto do cliente é uma **cópia** desta pasta num
repositório próprio.

## O que tem aqui

- `SolucaoApplication`: a aplicação — a Athena inteira (`br.com.brainstorming.athena`) mais o pacote do cliente.
- `ModeloDaSolucao`: o modelo pronto do cliente (implementa `ModeloDeSolucao`), que aparece em Configuração → "Modelo
  pronto" e monta por configuração os tipos de caso, listas, campos, fases, alertas e roteiro do relato. Aqui há um
  exemplo genérico ("Reclamações de clientes"); no cliente, ele é escrito a partir dos documentos e planilhas dele.
- `db/cliente/master.yaml`: a estrutura do banco — a da Athena primeiro, depois a do cliente (se precisar de tabela
  própria, o que é raro: quase tudo é configuração).
- `application.properties`: porta 8091 e H2 de desenvolvimento; nada de senha (vai em `config/application.properties`,
  fora do git, ou no `application.properties` da instalação).
- `alexandria/`: o conhecimento do cliente (fontes, formulários de validação e uma ficha por regra; ver
  `alexandria/LEIA-ME.md`). O build valida as fichas e põe só as aprovadas dentro do jar; a ajuda dos campos do
  `ModeloDaSolucao` vem delas.
- `SolucaoTest`: confere que a aplicação sobe com a Athena, que o modelo do cliente instala e a rastreabilidade com a
  Alexandria (tudo o que as fichas dizem implementar existe; todo campo, alerta e ponto do roteiro tem ficha de origem).

## Criar o projeto de um cliente

1. Na Athena, publicar a biblioteca no repositório Maven local: `.\mvnw.cmd install -DskipTests` (na raiz da Athena).
2. Criar o repositório do cliente e copiar para ele: esta pasta inteira, a pasta `instalacao` da Athena, o `mvnw`,
   `mvnw.cmd`, a pasta `.mvn` e o `.gitignore`.
3. No `pom.xml`: trocar o `<parent>` pelo `spring-boot-starter-parent` (a mesma versão da Athena) e pôr a versão da Athena
   na dependência `athena-core`; trocar `groupId`/`artifactId`/`name` pelos do cliente.
4. Renomear o pacote `br.com.brainstorming.cliente` para o do cliente (ex.: `br.com.brainstorming.cliente.acme`) e
   ajustar o `scanBasePackages` da `SolucaoApplication`.
5. Escrever as fichas da Alexandria a partir dos documentos e do formulário de validação, e o `ModeloDaSolucao` com o
   desenho validado (cada item citando a ficha de origem); criar o CLAUDE.md do projeto do cliente com as
   regras de negócio dele (o que é da Athena continua no CLAUDE.md da Athena).
6. Em `instalacao/pacote.properties`: `nome` e `codigo` da solução, `jar=target/solucao.jar`, porta e pasta padrão.
   `instalacao\GERAR-PACOTE.bat` gera o zip de instalação.

## Atualizar a Athena num cliente

Publicar a versão nova da Athena (`mvnw install` na Athena), trocar a versão de `athena-core` no `pom.xml` do cliente,
rodar os testes do cliente e gerar o pacote. Mudanças de estrutura do banco da Athena são aplicadas sozinhas na subida.
