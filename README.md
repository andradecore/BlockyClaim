# BlockyClaim
BlockyClaim é um plugin de proteção de terras para servidores Minecraft baseados no BlockyCRAFT. Permite que jogadores reivindiquem áreas do mundo, protegendo construções contra outros jogadores. O sistema utiliza ferro como moeda para aquisição e manutenção dos claims.

## Instalação
1. Faça o download ou clone este repositório.
2. Compile o projeto utilizando Maven.
3. Mova o arquivo `.jar` gerado para a pasta `plugins` do seu servidor Bukkit/Spigot.
4. Reinicie o servidor.

## Permissões
- `blockyclaim.*` — Todas as permissões do plugin.
- `blockyclaim.claim` — Permite usar `/claim` e subcomandos.
- `blockyclaim.trust` — Permite usar `/trust`.
- `blockyclaim.untrust` — Permite usar `/untrust`.

## Funcionamento do Sistema
- Blocos de proteção podem ser adquiridos utilizando ferro como moeda.
- Áreas podem ser vendidas e compradas entre jogadores.
- Visualização dinâmica das áreas protegidas, exibindo bordas para fácil identificação.
- Permite conceder e remover direitos de construção para outros jogadores.
- Armazena saldo de claims e logins em arquivos próprios no servidor.

## Eventos Protegidos

Listeners interceptam eventos de construção, destruição, explosão (incluindo Creepers) e movimentação, barrando ações de terceiros em claims protegidos.

## Observações

- Recomenda-se consultar o `plugin.yml` para adaptações.
- Em desenvolvimento; sujeito a alterações.

## Tutorial de Comandos

### Claim
- `/claim comprar`
  Compra o bloco de proteção onde você está, consumindo ferro do seu saldo.

- `/claim saldo`
  Mostra quantos blocos de proteção você possui disponíveis.

- `/claim lista`
  Lista todas as áreas protegidas (claims) registradas para o seu jogador.

- `/claim vender`
  Coloca uma de suas áreas de proteção à venda.

- `/claim comprar <jogador>`
  Permite comprar uma área de proteção à venda de outro jogador.

- `/claim confirmar`
  Confirma uma ação de compra ou venda pendente.

- `/claim ocupar`
  Permite ocupar uma área que está abandonada (sem dono ativo).

- `/claim`
  Exibe informações e instruções gerais de uso.

### Trust
- `/trust <jogador>`
  Concede permissão para que outro jogador possa construir ou editar dentro das suas áreas protegidas.

### Untrust
- `/untrust <jogador>`
  Remove a permissão concedida, impedindo o jogador de construir ou modificar blocos dentro dos seus claims.

## Reportar bugs ou requisitar features
Reporte bugs em [issues](https://github.com/andradecore/BlockyClaim/issues).

## Contato:
- Discord: https://discord.gg/tthPMHrP