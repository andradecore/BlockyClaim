# BlockyClaim

BlockyClaim é um plugin de proteção de terras para o servidor BlockyCRAFT. Permite que jogadores reivindiquem áreas do mundo, protegendo construções contra outros jogadores. O sistema utiliza ferro como moeda para aquisição e manutenção dos claims.

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

## Integração com BlockyFactions

Quando o plugin [BlockyFactions](https://github.com/andradecore/BlockyFactions) está ativo no servidor, uma nova mecânica é habilitada:

- **Trust Automático**: Jogadores que fazem parte da mesma facção recebem permissão de construção e interação nos terrenos uns dos outros automaticamente. Isso elimina a necessidade de usar o comando `/trust` para cada membro da sua facção.
- **Proteção de `/untrust`**: Não é possível usar o comando `/untrust` em um jogador que seja membro da sua própria facção. A permissão é gerenciada centralmente pela facção.

## Eventos Protegidos

Listeners interceptam eventos de construção, destruição, explosão (incluindo Creepers) e movimentação, barrando ações de terceiros em claims protegidos.

## Tutorial de Comandos

### Claim

- `/claim comprar <qtde>`
  Compra a quantidade especificada de blocos de proteção, consumindo ferro do seu inventário.

- `/claim saldo`
  Mostra quantos blocos de proteção você possui disponíveis.

- `/claim list [jogador]`
  Lista todas as áreas protegidas (claims) registradas para o seu jogador ou para outro.

- `/claim anunciar <preço>`
  Coloca a área de proteção onde você está à venda pelo preço definido.

- `/claim adquirir <novo-nome>`
  Permite comprar uma área de proteção que está à venda, definindo um novo nome para ela.

- `/claim unanunciar`
  Retira do mercado um terreno seu que estava à venda.

- `/claim confirm <nome>`
  Confirma a criação de um terreno após selecionar os dois cantos.

- `/claim ocupar <novo-nome>`
  Permite ocupar uma área que está abandonada (sem dono ativo) por um custo reduzido.

### Trust

- `/trust <jogador>`
  Concede permissão para que outro jogador (que não é da sua facção) possa construir ou editar dentro das suas áreas protegidas.

### Untrust

- `/untrust <jogador>`
  Remove a permissão concedida a outro jogador (que não é da sua facção).

## Reportar bugs ou requisitar features

Reporte bugs em [issues](https://github.com/andradecore/BlockyClaim/issues).

## Contato:

- Discord: https://discord.gg/tthPMHrP
