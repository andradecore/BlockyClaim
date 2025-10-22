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

        // Delega para os handlers especificos
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
        if (s == null) return false;
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void displayHelpPage(Player player, int page) {
        ConfigManager cfg = plugin.getConfigManager();
        String itemCompraName = cfg.getItemCompra().name().replace("_", " ").toLowerCase();

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getRawMsg("ajuda.header", "&f--- Comandos de Claim (Pagina {current}/{total}) ---")
            .replace("{current}", String.valueOf(page))
            .replace("{total}", String.valueOf(TOTAL_HELP_PAGES))
        ));

        switch (page) {
            case 1:
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getRawMsg("ajuda.saldo", "&b/claim saldo &7- Mostra seus blocos.")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getRawMsg("ajuda.comprar", "&b/claim comprar <quantidade> &7- Compra blocos com {item_name}.")
                    .replace("{item_name}", itemCompraName)
                ));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getRawMsg("ajuda.confirm", "&b/claim confirm <nome> &7- Confirma a criacao.")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getRawMsg("ajuda.list", "&b/claim list [jogador] &7- Lista claims.")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getRawMsg("ajuda.trust", "&b/trust <jogador> &7- Da permissao.")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getRawMsg("ajuda.untrust", "&b/untrust <jogador> &7- Remove permissao.")));
                break;
            case 2:
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getRawMsg("ajuda.sell", "&b/claim sell <preco> &7- Coloca o terreno atual a venda por itens {item_name}.")
                     .replace("{item_name}", itemCompraName)
                ));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getRawMsg("ajuda.adquirir", "&b/claim adquirir <novo-nome> &7- Compra um terreno que esta a venda.")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getRawMsg("ajuda.unsell", "&b/claim unsell &7- Tira o terreno atual do mercado.")));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getRawMsg("ajuda.ocupar", "&b/claim ocupar <novo-nome> &7- Ocupa um terreno abandonado (custo menor).")));
                break;
            default:
                player.sendMessage(cfg.getMsg("erro.pagina-invalida", "&cPagina invalida."));
                return;
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getRawMsg("ajuda.footer", "&7--- Pagina {current}/{total} --- &fUse &b/claim <pagina>&f para navegar.")
            .replace("{current}", String.valueOf(page))
            .replace("{total}", String.valueOf(TOTAL_HELP_PAGES))));
    }

    private boolean handleClaimCommand(Player player, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();

        if (args.length == 0) {
            displayHelpPage(player, 1);
            return true;
        }

        if (isInteger(args[0])) {
            try {
                int page = Integer.parseInt(args[0]);
                if (page > 0 && page <= TOTAL_HELP_PAGES) {
                    displayHelpPage(player, page);
                } else {
                     player.sendMessage(cfg.getMsg("erro.pagina-invalida", "&cPagina invalida."));
                }
            } catch (NumberFormatException e) {
                 player.sendMessage(cfg.getMsg("erro.numero-invalido", "&c'{arg}' nao e um numero valido.").replace("{arg}", args[0]));
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();

        if (subCommand.equals("saldo")) {
             if (!player.hasPermission("blockyclaim.claim")) {
                player.sendMessage(cfg.getMsg("erro.no-permission", "&cVoce nao tem permissao."));
                return true;
            }
            int balance = playerDataManager.getClaimBlocks(player.getName());
            player.sendMessage(cfg.getMsg("teu-saldo", "&aVoce tem &6{balance} &ablocos de protecao disponiveis.")
                .replace("{balance}", String.valueOf(balance)));
            return true;
        }

        if (subCommand.equals("comprar")) {
             if (!player.hasPermission("blockyclaim.claim")) {
                player.sendMessage(cfg.getMsg("erro.no-permission", "&cVoce nao tem permissao."));
                return true;
            }
            if (args.length < 2) {
                String itemCompraName = cfg.getItemCompra().name().replace("_", " ").toLowerCase();
                player.sendMessage(cfg.getMsg("ajuda.comprar", "&cUse: /claim comprar <quantidade> - Compra blocos com {item_name}.")
                    .replace("{item_name}", itemCompraName));
                return true;
            }
            int amountToBuy;
            try {
                amountToBuy = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(cfg.getMsg("erro.numero-invalido", "&c'{arg}' nao e um numero valido.").replace("{arg}", args[1]));
                return true;
            }
            if (amountToBuy < 2) {
                player.sendMessage(cfg.getMsg("erro.compra-minima", "&cVoce deve comprar no minimo 2 blocos."));
                return true;
            }

            Material itemCompra = cfg.getItemCompra();
            double custoPorBlocoDouble = cfg.getCustoPorBloco();

            double totalCostDouble = amountToBuy * custoPorBlocoDouble;
            int cost = (int) Math.ceil(totalCostDouble);

            if (cost <= 0 && totalCostDouble > 0) {
                cost = 1;
            }

            if (!player.getInventory().contains(itemCompra, cost)) {
                player.sendMessage(cfg.getMsg("saldo-insuficiente-itens", "&cVoce nao tem {item_name} suficientes! Precisa de {cost}.")
                    .replace("{item_name}", itemCompra.name().replace("_", " ").toLowerCase())
                    .replace("{cost}", String.valueOf(cost)));
                return true;
            }

            player.getInventory().removeItem(new ItemStack(itemCompra, cost));
            playerDataManager.addClaimBlocks(player.getName(), amountToBuy);

            player.sendMessage(cfg.getMsg("blocos-comprados", "&aVoce comprou &6{amount} &ablocos de protecao!")
                .replace("{amount}", String.valueOf(amountToBuy)));
            player.sendMessage(cfg.getMsg("saldo-atual", "&aSeu novo saldo e: &6{balance}")
                .replace("{balance}", String.valueOf(playerDataManager.getClaimBlocks(player.getName()))));
            return true;
        }

        if (subCommand.equals("confirm")) { return handleConfirmCommand(player, args); }
        if (subCommand.equals("list")) { return handleListCommand(player, args); }
        if (subCommand.equals("ocupar")) { return handleOcuparCommand(player, args); }
        if (subCommand.equals("sell")) { return handleSellCommand(player, args); }
        if (subCommand.equals("adquirir")) { return handleAdquirirCommand(player, args); }
        if (subCommand.equals("unsell")) { return handleUnsellCommand(player, args); }

        player.sendMessage(cfg.getMsg("erro.comando-desconhecido", "&cComando desconhecido. Use /claim para ajuda."));
        return true;
    }

    private boolean handleTrustCommand(Player player, String[] args) {
         if (!player.hasPermission("blockyclaim.trust")) {
            player.sendMessage(plugin.getConfigManager().getMsg("erro.no-permission", "&cVoce nao tem permissao."));
            return true;
        }
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
        if (targetName.equalsIgnoreCase(player.getName())) {
             player.sendMessage(cfg.getMsg("erro.target-self", "&cVoce nao pode dar trust a si mesmo."));
             return true;
        }

        claim.trustPlayer(targetName);
        plugin.getClaimManager().saveClaims();
        player.sendMessage(cfg.getMsg("trust-adicionado", "&a{target} &aagora tem permissao no seu terreno.")
            .replace("{target}", targetName));
        return true;
    }

    private boolean handleUntrustCommand(Player player, String[] args) {
         if (!player.hasPermission("blockyclaim.untrust")) {
            player.sendMessage(plugin.getConfigManager().getMsg("erro.no-permission", "&cVoce nao tem permissao."));
            return true;
        }
        ConfigManager cfg = plugin.getConfigManager();
        if (args.length < 1) {
            player.sendMessage(cfg.getMsg("ajuda.untrust", "&cUse: /untrust <jogador>"));
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

        if (plugin.isFactionsHookEnabled() && BlockyFactionsAPI.arePlayersInSameFaction(player.getName(), targetName)) {
            player.sendMessage(cfg.getMsg("erro.nao-pode-untrust-faccao", "&cVoce nao pode remover a permissao de um membro da sua faccao."));
            return true;
        }

        if (!claim.getTrustedPlayers().contains(targetName.toLowerCase())) {
             player.sendMessage(cfg.getMsg("erro.jogador-nao-trust", "&c{target} ja nao tinha permissao neste terreno.").replace("{target}", targetName));
             return true;
        }

        claim.untrustPlayer(targetName);
        plugin.getClaimManager().saveClaims();
        player.sendMessage(cfg.getMsg("untrust-removido", "&a{target} &anao tem mais permissao no seu terreno.")
            .replace("{target}", targetName));
        return true;
    }

    private boolean handleSellCommand(Player player, String[] args) {
         if (!player.hasPermission("blockyclaim.claim")) {
            player.sendMessage(plugin.getConfigManager().getMsg("erro.no-permission", "&cVoce nao tem permissao."));
            return true;
        }
        ConfigManager cfg = plugin.getConfigManager();
        ClaimManager claimManager = plugin.getClaimManager();

        if (args.length < 2) {
            // --- CORRECAO AQUI ---
            String itemCompraName = cfg.getItemCompra().name().replace("_", " ").toLowerCase();
            // Usa getMsg (sem Raw) e substitui o placeholder na mensagem de USO
            player.sendMessage(cfg.getMsg("ajuda.sell", "&cUse: /claim sell <preco> - Coloca a venda por itens {item_name}.")
                .replace("{item_name}", itemCompraName));
            // --- FIM DA CORRECAO ---
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
            player.sendMessage(cfg.getMsg("erro.numero-nao-negativo", "&cO preco nao pode ser negativo."));
            return true;
        }

        claim.putForSale(price);
        claimManager.saveClaims();
        String itemName = cfg.getItemCompra().name().replace("_", " ").toLowerCase(); // Reutiliza nome do item
        player.sendMessage(cfg.getMsg("venda.colocada-a-venda", "&aVoce colocou o terreno '&6{claim_name}&a' a venda por &e{price} {item_name}&a.")
            .replace("{claim_name}", claim.getClaimName())
            .replace("{price}", String.valueOf(price))
            .replace("{item_name}", itemName)); // Usa variavel itemName
        return true;
    }

    private boolean handleUnsellCommand(Player player, String[] args) {
         if (!player.hasPermission("blockyclaim.claim")) {
            player.sendMessage(plugin.getConfigManager().getMsg("erro.no-permission", "&cVoce nao tem permissao."));
            return true;
        }
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
        claimManager.saveClaims();
        player.sendMessage(cfg.getMsg("venda.removida-do-mercado", "&aVoce removeu o terreno '&6{claim_name}&a' do mercado.")
            .replace("{claim_name}", claim.getClaimName()));
        return true;
    }

    private boolean handleAdquirirCommand(Player player, String[] args) {
         if (!player.hasPermission("blockyclaim.claim")) {
            player.sendMessage(plugin.getConfigManager().getMsg("erro.no-permission", "&cVoce nao tem permissao."));
            return true;
        }
        ConfigManager cfg = plugin.getConfigManager();
        ClaimManager claimManager = plugin.getClaimManager();

        if (args.length < 2) {
            player.sendMessage(cfg.getMsg("ajuda.adquirir", "&cUse: /claim adquirir <novo-nome>"));
            return true;
        }

        Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null || !claim.isForSale()) {
            player.sendMessage(cfg.getMsg("erro.nao-esta-a-venda", "&cVoce precisa estar dentro de um terreno que esta a venda."));
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

        String newClaimName = args[1];
        if (claimManager.getClaimByName(player.getName(), newClaimName) != null) {
             player.sendMessage(cfg.getMsg("erro.nome-claim-existente", "&cVoce ja possui um claim com o nome '&6{claim_name}&c'.").replace("{claim_name}", newClaimName));
             return true;
        }
        if (newClaimName.isEmpty() || newClaimName.length() > 30 || !newClaimName.matches("^[a-zA-Z0-9_]+$")) {
             player.sendMessage(cfg.getMsg("erro.nome-claim-invalido", "&cNome invalido. Use apenas letras, numeros e _, max 30 caracteres."));
             return true;
        }

        int price = claim.getSalePrice();
        Material itemType = cfg.getItemCompra();

        if (!player.getInventory().contains(itemType, price)) {
            player.sendMessage(cfg.getMsg("saldo-insuficiente-itens", "Itens insuficientes!")
                .replace("{item_name}", itemType.name().replace("_", " ").toLowerCase())
                .replace("{cost}", String.valueOf(price)));
            return true;
        }

        Player seller = plugin.getServer().getPlayerExact(claim.getOwnerName());
        if (seller == null || !seller.isOnline()) {
            player.sendMessage(cfg.getMsg("erro.vendedor-offline", "&cO dono deste terreno nao esta online."));
            return true;
        }

        ItemStack paymentStack = new ItemStack(itemType, price);
        if (price > 0) {
            boolean sellerHasSpace = false;
            if (seller.getInventory().firstEmpty() != -1) {
                sellerHasSpace = true;
            } else {
                for (ItemStack item : seller.getInventory().getContents()) {
                    if (item != null &&
                        item.getTypeId() == itemType.getId() &&
                        (item.getData() == null ? 0 : item.getData().getData()) == (paymentStack.getData() == null ? 0 : paymentStack.getData().getData()) &&
                        item.getAmount() < item.getMaxStackSize()) {
                         if (item.getAmount() + price <= item.getMaxStackSize()) {
                             sellerHasSpace = true;
                             break;
                         }
                    }
                }
            }
             if (!sellerHasSpace) {
                player.sendMessage(cfg.getMsg("erro.inventario-vendedor-cheio", "&cO inventario do vendedor esta cheio!"));
                return true;
            }
            player.getInventory().removeItem(paymentStack);
            seller.getInventory().addItem(paymentStack);
        }

        String oldClaimName = claim.getClaimName();
        String itemName = itemType.name().replace("_", " ").toLowerCase();

        claim.setOwner(player.getName());
        claim.setClaimName(newClaimName);
        claim.getTrustedPlayers().clear();
        claim.removeFromSale();

        claimManager.saveClaims();

        player.sendMessage(cfg.getMsg("venda.compra-sucesso-comprador", "&aCompra realizada! O novo nome do terreno e '&6{claim_name}&a'.")
            .replace("{claim_name}", newClaimName));

        seller.sendMessage(cfg.getMsg("venda.compra-sucesso-vendedor", "&aSeu terreno '&6{claim_name_antigo}&a' foi vendido para &6{comprador}&a por &e{price} {item_name}&a!")
            .replace("{claim_name_antigo}", oldClaimName)
            .replace("{comprador}", player.getName())
            .replace("{price}", String.valueOf(price))
            .replace("{item_name}", itemName));

        System.out.println("[BlockyClaim] O jogador " + player.getName() + " comprou a claim '" + newClaimName + "' (antes '" + oldClaimName + "') de " + seller.getName() + " por " + price + " " + itemName);
        return true;
    }

    private boolean handleListCommand(Player player, String[] args) {
        String targetName;
        boolean isSelf;

        if (args.length > 1) {
            if (!player.hasPermission("blockyclaim.list.other")) {
                player.sendMessage(plugin.getConfigManager().getMsg("erro.no-permission", "&cVoce nao tem permissao."));
                return true;
            }
            targetName = args[1];
            isSelf = false;
        } else {
             if (!player.hasPermission("blockyclaim.list.self")) {
                player.sendMessage(plugin.getConfigManager().getMsg("erro.no-permission", "&cVoce nao tem permissao."));
                return true;
            }
            targetName = player.getName();
            isSelf = true;
        }

        ConfigManager cfg = plugin.getConfigManager();
        ClaimManager claimManager = plugin.getClaimManager();
        List<Claim> claims = claimManager.getClaimsByOwner(targetName);

        if (claims.isEmpty()) {
            if (isSelf) {
                player.sendMessage(cfg.getMsg("list.no-claims-self", "&cVoce nao possui nenhuma claim."));
            } else {
                player.sendMessage(cfg.getMsg("list.no-claims-other", "&c{player} &cnao possui nenhuma claim.")
                    .replace("{player}", targetName));
            }
        } else {
            if (isSelf) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getRawMsg("list.header-self", "&e--- Suas Claims ---")));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getRawMsg("list.header-other", "&e--- Claims de {player} ---")
                    .replace("{player}", targetName)));
            }

            for (Claim claim : claims) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getRawMsg("list.format", "&7- &f{claim_name}")
                    .replace("{claim_name}", claim.getClaimName())));
            }
        }
        return true;
    }

    private boolean handleOcuparCommand(Player player, String[] args) {
         if (!player.hasPermission("blockyclaim.claim")) {
            player.sendMessage(plugin.getConfigManager().getMsg("erro.no-permission", "&cVoce nao tem permissao."));
            return true;
        }
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

        if (claim.getOwnerName().equalsIgnoreCase(player.getName())) {
             player.sendMessage(cfg.getMsg("erro.ocupar-proprio-terreno", "&cVoce nao pode ocupar seu proprio terreno, mesmo abandonado."));
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

        String newClaimName = args[1];
        if (claimManager.getClaimByName(player.getName(), newClaimName) != null) {
             player.sendMessage(cfg.getMsg("erro.nome-claim-existente", "&cVoce ja possui um claim com o nome '&6{claim_name}&c'.").replace("{claim_name}", newClaimName));
             return true;
        }
        if (newClaimName.isEmpty() || newClaimName.length() > 30 || !newClaimName.matches("^[a-zA-Z0-9_]+$")) {
             player.sendMessage(cfg.getMsg("erro.nome-claim-invalido", "&cNome invalido. Use apenas letras, numeros e _, max 30 caracteres."));
             return true;
        }

        double custoPorBlocoDouble = cfg.getCustoPorBloco();
        double custoOriginalDouble = claim.getSize() * custoPorBlocoDouble;
        double percentualOcupar = cfg.getPercentualPrecoOcupar() / 100.0;
        double custoFinalDouble = custoOriginalDouble * percentualOcupar;

        int custoFinal = (int) Math.ceil(custoFinalDouble);
        if (custoFinal <= 0 && custoFinalDouble > 0) {
            custoFinal = 1;
        }

        Material itemCompra = cfg.getItemCompra();

        if (custoFinal > 0 && !player.getInventory().contains(itemCompra, custoFinal)) {
            player.sendMessage(cfg.getMsg("saldo-insuficiente-itens", "&cVoce nao tem {item_name} suficientes! Precisa de {cost}.")
                .replace("{item_name}", itemCompra.name().replace("_", " ").toLowerCase())
                .replace("{cost}", String.valueOf(custoFinal)));
            return true;
        }

        if(custoFinal > 0) {
            player.getInventory().removeItem(new ItemStack(itemCompra, custoFinal));
        }

        String antigoDono = claim.getOwnerName();
        String nomeAntigoClaim = claim.getClaimName();

        claim.setOwner(player.getName());
        claim.setClaimName(newClaimName);
        claim.getTrustedPlayers().clear();
        claim.removeFromSale();

        claimManager.saveClaims();

        player.sendMessage(cfg.getMsg("abandono.ocupado-sucesso", "&aVoce ocupou o terreno abandonado! O novo nome e '&6{claim_name}&a'.")
            .replace("{claim_name}", newClaimName));

        System.out.println("[BlockyClaim] O jogador " + player.getName() + " ocupou a claim '" + newClaimName + "' (antes '" + nomeAntigoClaim + "') que pertencia a " + antigoDono + " por " + custoFinal + " " + itemCompra.name());

        return true;
    }

    private boolean handleConfirmCommand(Player player, String[] args) {
         if (!player.hasPermission("blockyclaim.claim")) {
            player.sendMessage(plugin.getConfigManager().getMsg("erro.no-permission", "&cVoce nao tem permissao."));
            return true;
        }
        ConfigManager cfg = plugin.getConfigManager();
        if (args.length < 2) {
            player.sendMessage(cfg.getMsg("ajuda.confirm", "&cUse: /claim confirm <nome-da-claim>"));
            return true;
        }

        Map<String, Location[]> pending = ClaimToolListener.getPendingConfirmations();
        Location[] corners = pending.get(player.getName());

        if (corners == null || corners[0] == null || corners[1] == null) {
            player.sendMessage(cfg.getMsg("sem-selecao-pendente", "&cVoce nao tem uma selecao de terreno pendente para confirmar. Use a ferramenta primeiro."));
            return true;
        }

        String claimName = args[1];
        if (claimName.isEmpty() || claimName.length() > 30 || !claimName.matches("^[a-zA-Z0-9_]+$")) {
             player.sendMessage(cfg.getMsg("erro.nome-claim-invalido", "&cNome invalido. Use apenas letras, numeros e _, max 30 caracteres."));
             return true;
        }

        ClaimManager claimManager = plugin.getClaimManager();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();

        if (claimManager.getClaimByName(player.getName(), claimName) != null) {
             player.sendMessage(cfg.getMsg("erro.nome-claim-existente", "&cVoce ja possui um claim com o nome '&6{claim_name}&c'.").replace("{claim_name}", claimName));
             return true;
        }

        int maxClaims = cfg.getMaxClaimsPorJogador();
        if (maxClaims > 0 && claimManager.getClaimsByOwner(player.getName()).size() >= maxClaims) {
            player.sendMessage(cfg.getMsg("limite-claims-atingido", "&cVoce atingiu o seu limite de &6{max_claims} &cclaims.")
                .replace("{max_claims}", String.valueOf(maxClaims)));
            pending.remove(player.getName());
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

         int minSize = cfg.getTamanhoMinimoClaim();
         if (minSize > 0 && claimSize < minSize) {
             player.sendMessage(cfg.getMsg("tamanho-minimo-nao-atingido", "&cA sua selecao e muito pequena. O tamanho minimo e de &6{min_size} &cblocos.")
                 .replace("{min_size}", String.valueOf(minSize)));
             pending.remove(player.getName());
             return true;
         }

        Claim newClaim = new Claim(player.getName(), claimName, corners[0], corners[1]);
        playerDataManager.removeClaimBlocks(player.getName(), claimSize);
        claimManager.addClaim(newClaim);
        claimManager.saveClaims();

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