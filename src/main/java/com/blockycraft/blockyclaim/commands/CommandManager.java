package com.blockycraft.blockyclaim.commands;

import com.blockycraft.blockyclaim.BlockyClaim;
import com.blockycraft.blockyclaim.config.ConfigManager;
import com.blockycraft.blockyclaim.data.Claim;
import com.blockycraft.blockyclaim.listeners.ClaimToolListener;
import com.blockycraft.blockyclaim.managers.ClaimManager;
import com.blockycraft.blockyclaim.managers.PlayerDataManager;
import com.blockycraft.blockyfactions.api.BlockyFactionsAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class CommandManager implements CommandExecutor {
    private final BlockyClaim plugin;
    private static final int TOTAL_HELP_PAGES = 2;

    public CommandManager(BlockyClaim plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando so pode ser usado por jogadores.");
            return true;
        }
        Player player = (Player) sender;
        String commandName = command.getName().toLowerCase();

        if (commandName.equals("claim")) {
            return handleClaimCommand(player, args);
        } else if (commandName.equals("trust")) {
            return handleTrustCommand(player, args);
        } else if (commandName.equals("untrust")) {
            return handleUntrustCommand(player, args);
        }
        return false;
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void displayHelpPage(Player player, int page) {
        ConfigManager cfg = plugin.getConfigManager();
        String prefix = cfg.getMsg("prefixo", "");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.header", "&6--- Ajuda BlockyClaim ---").replace(prefix, "")));
        switch (page) {
            case 1:
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.saldo", "&b/claim saldo &7- ...")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.comprar", "&b/claim comprar  &7- ...")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.confirm", "&b/claim confirm  &7- ...")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.list", "&b/claim list [jogador] &7- ...")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.cancelar", "&b/claim cancelar &7- Cancela a selecao de claim atual.")));
                break;
            case 2:
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.anunciar", "&b/claim anunciar  &7- ...")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.adquirir", "&b/claim adquirir  &7- ...")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.unanunciar", "&b/claim unanunciar &7- ...")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.ocupar", "&b/claim ocupar &7- ...")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.trust", "&b/trust  &7- ...")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.untrust", "&b/untrust  &7- ...")));
                break;
            default:
                player.sendMessage(cfg.getMsg("erro.pagina-invalida", "&cPagina invalida."));
                return;
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.footer", "&7--- Pagina {current}/{total} ---")
                .replace(prefix, "")
                .replace("{current}", String.valueOf(page))
                .replace("{total}", String.valueOf(TOTAL_HELP_PAGES))));
    }

    private boolean handleClaimCommand(Player player, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (args.length == 0) {
            displayHelpPage(player, 1);
            return true;
        }
        // Novo comando: /claim cancelar
        if (args[0].equalsIgnoreCase("cancelar")) {
            Map<String, Location[]> pending = ClaimToolListener.getPendingConfirmations();
            if (pending.containsKey(player.getName())) {
                pending.remove(player.getName());
                player.sendMessage(cfg.getMsg("selecao-cancelada", "&eSelecao de claim cancelada com sucesso."));
            } else {
                player.sendMessage(cfg.getMsg("nao-ha-selecao-para-cancelar", "&cVoce nao tem nenhuma selecao ativa para cancelar."));
            }
            return true;
        }

        if (isInteger(args[0])) {
            int page = Integer.parseInt(args[0]);
            displayHelpPage(player, page);
            return true;
        }
        String subCommand = args[0].toLowerCase();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();

        if (subCommand.equals("saldo")) {
            int balance = playerDataManager.getClaimBlocks(player.getName());
            player.sendMessage(cfg.getMsg("teu-saldo", "&aVoce tem &6{balance} &ablocos de protecao disponiveis.")
                    .replace("{balance}", String.valueOf(balance)));
            return true;
        }
        if (subCommand.equals("comprar")) {
            if (args.length < 2) {
                player.sendMessage(cfg.getMsg("ajuda.comprar", "&cUse: /claim comprar <qtde>"));
                return true;
            }
            int amountToAdquirir;
            try {
                amountToAdquirir = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(cfg.getMsg("erro.numero-invalido", "&c'{arg}' nao e um numero valido.").replace("{arg}", args[1]));
                return true;
            }
            if (amountToAdquirir <= 0) {
                player.sendMessage(cfg.getMsg("erro.numero-positivo", "&cA quantidade deve ser maior que zero."));
                return true;
            }
            Material itemCompra = cfg.getItemCompra();
            double custoPorBloco = cfg.getCustoPorBloco();
            double custoTotalDouble = amountToAdquirir * custoPorBloco;
            int custoTotalInteiro = (int) Math.ceil(custoTotalDouble);
            int quantidadeMinima = (int) Math.ceil(1.0 / custoPorBloco);
            if (custoPorBloco < 1.0 && amountToAdquirir < quantidadeMinima) {
                player.sendMessage(cfg.getMsg("erro.quantidade-minima", "&cCom o custo atual de &6{custo} &cpor bloco, voce precisa comprar no minimo &6{minimo} &cblocos.")
                        .replace("{custo}", String.format("%.2f", custoPorBloco))
                        .replace("{minimo}", String.valueOf(quantidadeMinima)));
                return true;
            }
            if (!player.getInventory().contains(itemCompra, custoTotalInteiro)) {
                player.sendMessage(cfg.getMsg("saldo-insuficiente-itens", "&cVoce nao tem {item_name} suficientes! Precisa de {cost}.")
                        .replace("{item_name}", itemCompra.name().replace("_", " ").toLowerCase())
                        .replace("{cost}", String.valueOf(custoTotalInteiro)));
                return true;
            }
            player.getInventory().removeItem(new ItemStack(itemCompra, custoTotalInteiro));
            playerDataManager.addClaimBlocks(player.getName(), amountToAdquirir);
            player.sendMessage(cfg.getMsg("blocos-comprados", "&aVoce comprou &6{amount} &ablocos de protecao!")
                    .replace("{amount}", String.valueOf(amountToAdquirir)));
            player.sendMessage(cfg.getMsg("saldo-atual", "&aSeu novo saldo e: &6{balance}")
                    .replace("{balance}", String.valueOf(playerDataManager.getClaimBlocks(player.getName()))));
            return true;
        }
        if (subCommand.equals("confirm")) { return handleConfirmCommand(player, args); }
        if (subCommand.equals("list")) { return handleListCommand(player, args); }
        if (subCommand.equals("ocupar")) { return handleOcuparCommand(player, args); }
        if (subCommand.equals("anunciar")) { return handleAnunciarCommand(player, args); }
        if (subCommand.equals("adquirir")) { return handleAdquirirCommand(player, args); }
        if (subCommand.equals("unanunciar")) { return handleUnanunciarCommand(player, args); }
        player.sendMessage(cfg.getMsg("erro.comando-desconhecido", "&cComando desconhecido. Use /claim para ajuda."));
        return true;
    }

    private boolean handleTrustCommand(Player player, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (args.length < 1) {
            player.sendMessage(cfg.getMsg("ajuda.trust", "&cUse: /trust <jogador>"));
            return true;
        }
        Claim claim = plugin.getClaimManager().getClaimAt(player.getLocation());
        if (claim == null) {
            player.sendMessage(cfg.getMsg("precisa-estar-dentro", "&cVoce precisa estar dentro de um terreno seu para usar este comando."));
            return true;
        }
        if (!claim.getOwnerName().equalsIgnoreCase(player.getName())) {
            player.sendMessage(cfg.getMsg("nao-e-dono", "&cVoce nao e o dono deste terreno."));
            return true;
        }
        String targetName = args[0];
        claim.trustPlayer(targetName);
        player.sendMessage(cfg.getMsg("trust-adicionado", "&a{target} agora tem permissao no seu terreno.")
                .replace("{target}", targetName));
        return true;
    }

    private boolean handleUntrustCommand(Player player, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (args.length < 1) {
            player.sendMessage(cfg.getMsg("ajuda.untrust", "§cUse: /untrust <jogador>"));
            return true;
        }
        Claim claim = plugin.getClaimManager().getClaimAt(player.getLocation());
        if (claim == null) {
            player.sendMessage(cfg.getMsg("precisa-estar-dentro", "§cVoce precisa estar dentro de um terreno seu para usar este comando."));
            return true;
        }
        if (!claim.getOwnerName().equalsIgnoreCase(player.getName())) {
            player.sendMessage(cfg.getMsg("nao-e-dono", "§cVoce nao e o dono deste terreno."));
            return true;
        }
        String targetName = args[0];
        if (BlockyClaim.getInstance().isFactionsHookEnabled() && BlockyFactionsAPI.arePlayersInSameFaction(player.getName(), targetName)) {
            player.sendMessage(cfg.getMsg("erro.nao-pode-untrust-faccao", "§cVoce nao pode remover a permissao de um membro da sua faccao."));
            return true;
        }
        claim.untrustPlayer(targetName);
        player.sendMessage(cfg.getMsg("untrust-removido", "§a{target} §anao tem mais permissao no seu terreno.")
                .replace("{target}", targetName));
        return true;
    }

    // Os métodos abaixo permanecem como no original.
    // Adapte conforme necessidade para integração com o novo /claim cancelar.

    private boolean handleAnunciarCommand(Player player, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        ClaimManager claimManager = plugin.getClaimManager();
        if (args.length < 2) {
            player.sendMessage(cfg.getMsg("ajuda.anunciar", "&cUse: /claim anunciar <preco>"));
            return true;
        }
        Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null || !claim.getOwnerName().equalsIgnoreCase(player.getName())) {
            player.sendMessage(cfg.getMsg("nao-e-dono", "&cVoce precisa estar dentro de um terreno seu para vende-lo."));
            return true;
        }
        int price;
        try {
            price = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(cfg.getMsg("erro.numero-invalido", "&c'{arg}' nao e um numero valido.").replace("{arg}", args[1]));
            return true;
        }
        if (price < 0) {
            player.sendMessage(cfg.getMsg("erro.numero-positivo", "&cO preco deve ser um numero positivo."));
            return true;
        }
        claim.putForSale(price);
        String itemName = cfg.getItemCompra().name().replace("_", " ").toLowerCase();
        player.sendMessage(cfg.getMsg("venda.colocada-a-venda", "Terreno a venda!")
                .replace("{claim_name}", claim.getClaimName())
                .replace("{price}", String.valueOf(price))
                .replace("{item_name}", itemName));
        return true;
    }

    private boolean handleUnanunciarCommand(Player player, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        ClaimManager claimManager = plugin.getClaimManager();
        Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null || !claim.getOwnerName().equalsIgnoreCase(player.getName())) {
            player.sendMessage(cfg.getMsg("nao-e-dono", "&cVoce precisa estar dentro de um terreno seu para gerenciar a venda."));
            return true;
        }
        if (!claim.isForSale()) {
            player.sendMessage(cfg.getMsg("erro.nao-esta-a-venda", "&cEste terreno nao esta a venda."));
            return true;
        }
        claim.removeFromSale();
        player.sendMessage(cfg.getMsg("venda.removida-do-mercado", "Venda cancelada!")
                .replace("{claim_name}", claim.getClaimName()));
        return true;
    }

    private boolean handleAdquirirCommand(Player player, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        ClaimManager claimManager = plugin.getClaimManager();
        if (args.length < 2) {
            player.sendMessage(cfg.getMsg("ajuda.adquirir", "&cUse: /claim adquirir <novo-nome>"));
            return true;
        }
        Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null || !claim.isForSale()) {
            player.sendMessage(cfg.getMsg("erro.nao-esta-a-venda", "&cEste terreno nao esta a venda."));
            return true;
        }
        if (claim.getOwnerName().equalsIgnoreCase(player.getName())) {
            player.sendMessage(cfg.getMsg("erro.comprar-proprio-terreno", "&cVoce nao pode comprar seu proprio terreno."));
            return true;
        }
        int maxClaims = cfg.getMaxClaimsPorJogador();
        if (maxClaims > 0 && claimManager.getClaimsByOwner(player.getName()).size() >= maxClaims) {
            player.sendMessage(cfg.getMsg("limite-claims-atingido", "&cVoce atingiu o seu limite de &6{max_claims} &cclaims.")
                    .replace("{max_claims}", String.valueOf(maxClaims)));
            return true;
        }
        int price = claim.getSalePrice();
        Material itemType = cfg.getItemCompra();
        ItemStack payment = new ItemStack(itemType, price);
        if (!player.getInventory().contains(itemType, price)) {
            player.sendMessage(cfg.getMsg("saldo-insuficiente-itens", "Itens insuficientes!")
                    .replace("{item_name}", itemType.name().replace("_", " ").toLowerCase())
                    .replace("{cost}", String.valueOf(price)));
            return true;
        }
        Player anunciarer = plugin.getServer().getPlayer(claim.getOwnerName());
        if (anunciarer == null || !anunciarer.isOnline()) {
            player.sendMessage(cfg.getMsg("erro.vendedor-offline", "&cO dono deste terreno nao esta online."));
            return true;
        }
        if (anunciarer.getInventory().firstEmpty() == -1) {
            player.sendMessage(cfg.getMsg("erro.inventario-vendedor-cheio", "&cO inventario do vendedor esta cheio!"));
            return true;
        }
        player.getInventory().removeItem(payment);
        anunciarer.getInventory().addItem(payment);
        String oldClaimName = claim.getClaimName();
        String newClaimName = args[1];
        String itemName = itemType.name().replace("_", " ").toLowerCase();
        claim.setOwner(player.getName());
        claim.setClaimName(newClaimName);
        claim.getTrustedPlayers().clear();
        claim.removeFromSale();
        player.sendMessage(cfg.getMsg("venda.compra-sucesso-comprador", "Compra realizada!")
                .replace("{claim_name}", newClaimName));
        anunciarer.sendMessage(cfg.getMsg("venda.compra-sucesso-vendedor", "Terreno vendido!")
                .replace("{claim_name_antigo}", oldClaimName)
                .replace("{comprador}", player.getName())
                .replace("{price}", String.valueOf(price))
                .replace("{item_name}", itemName));
        System.out.println("[BlockyClaim] O jogador " + player.getName() + " comprou a claim '" + oldClaimName + "' de " + anunciarer.getName() + " por " + price + " " + itemName);
        return true;
    }

    private boolean handleListCommand(Player player, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        ClaimManager claimManager = plugin.getClaimManager();
        String targetName;
        boolean isSelf = true;
        if (args.length > 1) {
            targetName = args[1];
            isSelf = false;
        } else {
            targetName = player.getName();
        }
        List<Claim> claims = claimManager.getClaimsByOwner(targetName);
        if (claims.isEmpty()) {
            if (isSelf) {
                player.sendMessage(cfg.getMsg("list.no-claims-self", "&cVoce nao possui nenhuma claim."));
            } else {
                player.sendMessage(cfg.getMsg("list.no-claims-other", "&c{player} &cnao possui nenhuma claim.")
                        .replace("{player}", targetName));
            }
        } else {
            String prefix = cfg.getMsg("prefixo", "");
            if (isSelf) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("list.header-self", "&e--- Suas Claims ---").replace(prefix, "")));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("list.header-other", "&e--- Claims de {player} ---").replace(prefix, "")
                        .replace("{player}", targetName)));
            }
            for (Claim claim : claims) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("list.format", "&7- &f{claim_name}").replace(prefix, "")
                        .replace("{claim_name}", claim.getClaimName())));
            }
        }
        return true;
    }

    private boolean handleOcuparCommand(Player player, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        ClaimManager claimManager = plugin.getClaimManager();
        if (args.length < 2) {
            player.sendMessage(cfg.getMsg("ajuda.ocupar", "&cUse: /claim ocupar <novo-nome>"));
            return true;
        }
        Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null) {
            player.sendMessage(cfg.getMsg("abandono.nao-esta-em-claim", "&cVoce precisa estar dentro de um terreno abandonado para ocupa-lo."));
            return true;
        }
        if (!claimManager.isAbandoned(claim)) {
            player.sendMessage(cfg.getMsg("abandono.nao-esta-abandonado", "&cEste terreno nao esta abandonado."));
            return true;
        }
        int maxClaims = cfg.getMaxClaimsPorJogador();
        if (maxClaims > 0 && claimManager.getClaimsByOwner(player.getName()).size() >= maxClaims) {
            player.sendMessage(cfg.getMsg("limite-claims-atingido", "&cVoce atingiu o seu limite de &6{max_claims} &cclaims.")
                    .replace("{max_claims}", String.valueOf(maxClaims)));
            return true;
        }
        double custoOriginal = claim.getSize() * cfg.getCustoPorBloco();
        double percentualOcupar = cfg.getPercentualPrecoOcupar() / 100.0;
        int custoFinal = (int) Math.ceil(custoOriginal * percentualOcupar);
        Material itemCompra = cfg.getItemCompra();
        if (!player.getInventory().contains(itemCompra, custoFinal)) {
            player.sendMessage(cfg.getMsg("saldo-insuficiente-itens", "&cVoce nao tem {item_name} suficientes! Precisa de {cost}.")
                    .replace("{item_name}", itemCompra.name().replace("_", " ").toLowerCase())
                    .replace("{cost}", String.valueOf(custoFinal)));
            return true;
        }
        player.getInventory().removeItem(new ItemStack(itemCompra, custoFinal));
        String antigoDono = claim.getOwnerName();
        String novoNome = args[1];
        claim.setOwner(player.getName());
        claim.setClaimName(novoNome);
        claim.getTrustedPlayers().clear();
        player.sendMessage(cfg.getMsg("abandono.ocupado-sucesso", "&aVoce ocupou o terreno abandonado! O novo nome e '&6{claim_name}&a'.")
                .replace("{claim_name}", novoNome));
        System.out.println("[BlockyClaim] O jogador " + player.getName() + " ocupou a claim '" + claim.getClaimName() + "' que pertencia a " + antigoDono);
        return true;
    }

    private boolean handleConfirmCommand(Player player, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (args.length < 2) {
            player.sendMessage(cfg.getMsg("ajuda.confirm", "&cUse: /claim confirm <nome>"));
            return true;
        }
        Map<String, Location[]> pending = ClaimToolListener.getPendingConfirmations();
        Location[] corners = pending.get(player.getName());
        if (corners == null || corners[1] == null) {
            player.sendMessage(cfg.getMsg("sem-selecao-pendente", "&cVoce nao tem uma selecao de terreno pendente para confirmar."));
            return true;
        }
        String claimName = args[1];
        ClaimManager claimManager = plugin.getClaimManager();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        int maxClaims = cfg.getMaxClaimsPorJogador();
        if (maxClaims > 0 && claimManager.getClaimsByOwner(player.getName()).size() >= maxClaims) {
            player.sendMessage(cfg.getMsg("limite-claims-atingido", "&cVoce atingiu o seu limite de &6{max_claims} &cclaims.")
                    .replace("{max_claims}", String.valueOf(maxClaims)));
            return true;
        }
        if (claimManager.isAreaClaimed(corners[0], corners[1])) {
            player.sendMessage(cfg.getMsg("selecao-sobrepoe", "&cSua selecao sobrepoe um terreno ja protegido! Selecao cancelada."));
            pending.remove(player.getName());
            return true;
        }
        int claimSize = (Math.abs(corners[0].getBlockX() - corners[1].getBlockX()) + 1) * (Math.abs(corners[0].getBlockZ() - corners[1].getBlockZ()) + 1);
        int playerBlocks = playerDataManager.getClaimBlocks(player.getName());
        if (playerBlocks < claimSize) {
            player.sendMessage(cfg.getMsg("blocos-insuficientes", "&cVoce nao tem blocos de protecao suficientes! (&6{needed}&c/&6{has}&c)")
                    .replace("{needed}", String.valueOf(claimSize))
                    .replace("{has}", String.valueOf(playerBlocks)));
            return true;
        }
        Claim newClaim = new Claim(player.getName(), claimName, corners[0], corners[1]);
        playerDataManager.removeClaimBlocks(player.getName(), claimSize);
        claimManager.addClaim(newClaim);
        pending.remove(player.getName());
        player.sendMessage(cfg.getMsg("claim-criada-sucesso", "&aProtecao '&6{claim_name}&a' criada com sucesso! (&6{size} &ablocos)")
                .replace("{claim_name}", claimName)
                .replace("{size}", String.valueOf(claimSize)));
        player.sendMessage(cfg.getMsg("data-criacao", "&aData de criacao: &7{date}")
                .replace("{date}", newClaim.getFormattedCreationDate()));
        player.sendMessage(cfg.getMsg("saldo-atual", "&aSeu novo saldo e: &6{balance}")
                .replace("{balance}", String.valueOf(playerDataManager.getClaimBlocks(player.getName()))));
        return true;
    }
}
