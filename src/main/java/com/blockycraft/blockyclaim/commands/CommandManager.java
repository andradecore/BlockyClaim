package com.blockycraft.blockyclaim.commands;

import com.blockycraft.blockyclaim.BlockyClaim;
import com.blockycraft.blockyclaim.config.ConfigManager;
import com.blockycraft.blockyclaim.data.Claim;
import com.blockycraft.blockyclaim.lang.LanguageManager;
import com.blockycraft.blockyclaim.listeners.ClaimToolListener;
import com.blockycraft.blockyclaim.managers.ClaimManager;
import com.blockycraft.blockyclaim.managers.PlayerDataManager;
import com.blockycraft.blockyclaim.database.DatabaseManager;
import com.blockycraft.blockyclaim.database.DatabaseManagerClaims;
import com.blockycraft.blockyfactions.api.BlockyFactionsAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager implements CommandExecutor {
    private final BlockyClaim plugin;
    private final LanguageManager langManager;
    private static final int TOTAL_HELP_PAGES = 2;

    public CommandManager(BlockyClaim plugin) {
        this.plugin = plugin;
        this.langManager = plugin.getLanguageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(langManager.get("en", "comando-apenas-jogadores"));
            return true;
        }
        Player player = (Player) sender;
        String lang = plugin.getGeoIPManager().getPlayerLanguage(player);
        String commandName = command.getName().toLowerCase();

        if (commandName.equals("claim")) {
            return handleClaimCommand(player, args, lang);
        } else if (commandName.equals("trust") || commandName.equals("confiar")) {
            return handleTrustCommand(player, args, lang);
        } else if (commandName.equals("untrust") || commandName.equals("desconfiar")) {
            return handleUntrustCommand(player, args, lang);
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

    private void displayHelpPage(Player player, int page, String lang) {
        player.sendMessage(langManager.get(lang, "ajuda.header"));
        switch (page) {
            case 1:
                player.sendMessage(langManager.get(lang, "ajuda.saldo"));
                player.sendMessage(langManager.get(lang, "ajuda.comprar"));
                player.sendMessage(langManager.get(lang, "ajuda.confirm"));
                player.sendMessage(langManager.get(lang, "ajuda.list"));
                player.sendMessage(langManager.get(lang, "ajuda.cancelar"));
                break;
            case 2:
                player.sendMessage(langManager.get(lang, "ajuda.anunciar"));
                player.sendMessage(langManager.get(lang, "ajuda.adquirir"));
                player.sendMessage(langManager.get(lang, "ajuda.unanunciar"));
                player.sendMessage(langManager.get(lang, "ajuda.ocupar"));
                player.sendMessage(langManager.get(lang, "ajuda.trust"));
                player.sendMessage(langManager.get(lang, "ajuda.untrust"));
                break;
            default:
                player.sendMessage(langManager.get(lang, "erro.pagina-invalida"));
                return;
        }
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("current", String.valueOf(page));
        placeholders.put("total", String.valueOf(TOTAL_HELP_PAGES));
        player.sendMessage(langManager.get(lang, "ajuda.footer", placeholders));
    }

    private boolean handleClaimCommand(Player player, String[] args, String lang) {
        if (args.length == 0) {
            displayHelpPage(player, 1, lang);
            return true;
        }

        if (args[0].equalsIgnoreCase("cancel") || args[0].equalsIgnoreCase("cancelar")) {
            Map<String, Location[]> pending = ClaimToolListener.getPendingConfirmations();
            if (pending.containsKey(player.getName())) {
                pending.remove(player.getName());
                player.sendMessage(langManager.get(lang, "selecao-cancelada"));
            } else {
                player.sendMessage(langManager.get(lang, "nao-ha-selecao-para-cancelar"));
            }
            return true;
        }

        if (isInteger(args[0])) {
            int page = Integer.parseInt(args[0]);
            displayHelpPage(player, page, lang);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        DatabaseManager dbManager = plugin.getDatabaseManager();
        DatabaseManagerClaims dbManagerClaims = plugin.getDatabaseManagerClaims();
        ConfigManager cfg = plugin.getConfigManager();

        if (subCommand.equals("saldo") || subCommand.equals("balance")) {
            int balance = playerDataManager.getClaimBlocks(player.getName());
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("balance", String.valueOf(balance));
            player.sendMessage(langManager.get(lang, "teu-saldo", placeholders));
            return true;
        }

        if (subCommand.equals("comprar") || subCommand.equals("buy")) {
            if (args.length < 2) {
                player.sendMessage(langManager.get(lang, "ajuda.comprar"));
                return true;
            }
            int amountToAdquirir;
            try {
                amountToAdquirir = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("arg", args[1]);
                player.sendMessage(langManager.get(lang, "erro.numero-invalido", placeholders));
                return true;
            }
            if (amountToAdquirir <= 0) {
                player.sendMessage(langManager.get(lang, "erro.numero-positivo"));
                return true;
            }
            Material itemCompra = cfg.getItemCompra();
            double custoPorBloco = cfg.getCustoPorBloco();
            double custoTotalDouble = amountToAdquirir * custoPorBloco;
            int custoTotalInteiro = (int) Math.ceil(custoTotalDouble);
            int quantidadeMinima = (int) Math.ceil(1.0 / custoPorBloco);
            if (custoPorBloco < 1.0 && amountToAdquirir < quantidadeMinima) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("custo", String.format("%.2f", custoPorBloco));
                placeholders.put("minimo", String.valueOf(quantidadeMinima));
                player.sendMessage(langManager.get(lang, "erro.quantidade-minima", placeholders));
                return true;
            }
            if (!player.getInventory().contains(itemCompra, custoTotalInteiro)) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("item_name", itemCompra.name().replace("_", " ").toLowerCase());
                placeholders.put("cost", String.valueOf(custoTotalInteiro));
                player.sendMessage(langManager.get(lang, "saldo-insuficiente-itens", placeholders));
                return true;
            }
            player.getInventory().removeItem(new ItemStack(itemCompra, custoTotalInteiro));
            playerDataManager.addClaimBlocks(player.getName(), amountToAdquirir);

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("amount", String.valueOf(amountToAdquirir));
            player.sendMessage(langManager.get(lang, "blocos-comprados", placeholders));

            placeholders.clear();
            placeholders.put("balance", String.valueOf(playerDataManager.getClaimBlocks(player.getName())));
            player.sendMessage(langManager.get(lang, "saldo-atual", placeholders));

            dbManager.logCompra(player.getUniqueId().toString(), player.getName(), amountToAdquirir, custoTotalInteiro, System.currentTimeMillis() / 1000L);
            return true;
        }

        if (subCommand.equals("confirm") || subCommand.equals("confirmar")) {
            return handleConfirmCommand(player, args, dbManagerClaims, lang);
        }
        if (subCommand.equals("list")) {
            return handleListCommand(player, args, lang);
        }
        if (subCommand.equals("ocupar") || subCommand.equals("occupy")) {
            return handleOcuparCommand(player, args, dbManagerClaims, lang);
        }
        if (subCommand.equals("anunciar") || subCommand.equals("sell")) {
            return handleAnunciarCommand(player, args, lang);
        }
        if (subCommand.equals("adquirir") || subCommand.equals("acquire")) {
            return handleAdquirirCommand(player, args, dbManagerClaims, lang);
        }
        if (subCommand.equals("desanunciar") || subCommand.equals("unsell") || subCommand.equals("quitarventa")) {
            return handleUnanunciarCommand(player, args, lang);
        }
        player.sendMessage(langManager.get(lang, "erro.comando-desconhecido"));
        return true;
    }

    private boolean handleConfirmCommand(Player player, String[] args, DatabaseManagerClaims dbManagerClaims, String lang) {
        if (args.length < 2) {
            player.sendMessage(langManager.get(lang, "ajuda.confirm"));
            return true;
        }
        Map<String, Location[]> pending = ClaimToolListener.getPendingConfirmations();
        Location[] corners = pending.get(player.getName());
        if (corners == null || corners[1] == null) {
            player.sendMessage(langManager.get(lang, "sem-selecao-pendente"));
            return true;
        }

        String claimName = args[1];
        ClaimManager claimManager = plugin.getClaimManager();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        String worldName = corners[0].getWorld().getName();
        int minX = Math.min(corners[0].getBlockX(), corners[1].getBlockX());
        int maxX = Math.max(corners[0].getBlockX(), corners[1].getBlockX());
        int minZ = Math.min(corners[0].getBlockZ(), corners[1].getBlockZ());
        int maxZ = Math.max(corners[0].getBlockZ(), corners[1].getBlockZ());

        List<Claim> overlapped = claimManager.getClaimsInArea(worldName, minX, maxX, minZ, maxZ);

        for (Claim c : overlapped) {
            if (!c.getOwnerName().equalsIgnoreCase(player.getName())) {
                player.sendMessage(langManager.get(lang, "selecao-sobrepoe"));
                pending.remove(player.getName());
                return true;
            }
        }

        int blocksToPay = claimManager.getUnclaimedBlockCount(player.getName(), worldName, minX, maxX, minZ, maxZ);
        int claimArea = (maxX - minX + 1) * (maxZ - minZ + 1);
        int playerBlocks = playerDataManager.getClaimBlocks(player.getName());
        int maxClaims = plugin.getConfigManager().getMaxClaimsPorJogador();

        if (maxClaims > 0 && claimManager.getClaimsByOwner(player.getName()).size() >= maxClaims) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("max_claims", String.valueOf(maxClaims));
            player.sendMessage(langManager.get(lang, "limite-claims-atingido", placeholders));
            return true;
        }
        if (playerBlocks < blocksToPay) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("needed", String.valueOf(blocksToPay));
            placeholders.put("has", String.valueOf(playerBlocks));
            player.sendMessage(langManager.get(lang, "blocos-insuficientes", placeholders));
            return true;
        }

        if (blocksToPay > 0) {
            playerDataManager.removeClaimBlocks(player.getName(), blocksToPay);
        }

        Claim newClaim = new Claim(player.getName(), claimName, corners[0], corners[1]);
        claimManager.addClaim(newClaim);

        dbManagerClaims.insertClaim(player.getUniqueId().toString(), player.getName(), System.currentTimeMillis() / 1000L, minX, minZ, maxX, maxZ);

        pending.remove(player.getName());
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("claim_name", claimName);
        placeholders.put("size", String.valueOf(claimArea));
        player.sendMessage(langManager.get(lang, "claim-criada-sucesso", placeholders));

        placeholders.clear();
        placeholders.put("date", newClaim.getFormattedCreationDate());
        player.sendMessage(langManager.get(lang, "data-criacao", placeholders));

        placeholders.clear();
        placeholders.put("balance", String.valueOf(playerDataManager.getClaimBlocks(player.getName())));
        player.sendMessage(langManager.get(lang, "saldo-atual", placeholders));
        return true;
    }

    private boolean handleAdquirirCommand(Player player, String[] args, DatabaseManagerClaims dbManagerClaims, String lang) {
        ConfigManager cfg = plugin.getConfigManager();
        ClaimManager claimManager = plugin.getClaimManager();

        if (args.length < 2) {
            player.sendMessage(langManager.get(lang, "ajuda.adquirir"));
            return true;
        }
        Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null || !claim.isForSale()) {
            player.sendMessage(langManager.get(lang, "erro.nao-esta-a-venda"));
            return true;
        }
        if (claim.getOwnerName().equalsIgnoreCase(player.getName())) {
            player.sendMessage(langManager.get(lang, "erro.comprar-proprio-terreno"));
            return true;
        }
        int maxClaims = cfg.getMaxClaimsPorJogador();
        if (maxClaims > 0 && claimManager.getClaimsByOwner(player.getName()).size() >= maxClaims) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("max_claims", String.valueOf(maxClaims));
            player.sendMessage(langManager.get(lang, "limite-claims-atingido", placeholders));
            return true;
        }
        int price = claim.getSalePrice();
        Material itemType = cfg.getItemCompra();
        ItemStack payment = new ItemStack(itemType, price);
        if (!player.getInventory().contains(itemType, price)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("item_name", itemType.name().replace("_", " ").toLowerCase());
            placeholders.put("cost", String.valueOf(price));
            player.sendMessage(langManager.get(lang, "saldo-insuficiente-itens", placeholders));
            return true;
        }
        Player anunciarer = plugin.getServer().getPlayer(claim.getOwnerName());
        if (anunciarer == null || !anunciarer.isOnline()) {
            player.sendMessage(langManager.get(lang, "erro.vendedor-offline"));
            return true;
        }
        if (anunciarer.getInventory().firstEmpty() == -1) {
            player.sendMessage(langManager.get(lang, "erro.inventario-vendedor-cheio"));
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

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("claim_name", newClaimName);
        player.sendMessage(langManager.get(lang, "venda.compra-sucesso-comprador", placeholders));

        placeholders.clear();
        placeholders.put("claim_name_antigo", oldClaimName);
        placeholders.put("comprador", player.getName());
        placeholders.put("price", String.valueOf(price));
        placeholders.put("item_name", itemName);
        anunciarer.sendMessage(langManager.get(lang, "venda.compra-sucesso-vendedor", placeholders));

        dbManagerClaims.updateClaimOwnerByCoords(claim.getMinX(), claim.getMinZ(), claim.getMaxX(), claim.getMaxZ(), player.getUniqueId().toString(), player.getName(), System.currentTimeMillis() / 1000L);

        return true;
    }

    private boolean handleOcuparCommand(Player player, String[] args, DatabaseManagerClaims dbManagerClaims, String lang) {
        ConfigManager cfg = plugin.getConfigManager();
        ClaimManager claimManager = plugin.getClaimManager();
        if (args.length < 2) {
            player.sendMessage(langManager.get(lang, "ajuda.ocupar"));
            return true;
        }
        Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null) {
            player.sendMessage(langManager.get(lang, "abandono.nao-esta-em-claim"));
            return true;
        }
        if (!claimManager.isAbandoned(claim)) {
            player.sendMessage(langManager.get(lang, "abandono.nao-esta-abandonado"));
            return true;
        }
        int maxClaims = cfg.getMaxClaimsPorJogador();
        if (maxClaims > 0 && claimManager.getClaimsByOwner(player.getName()).size() >= maxClaims) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("max_claims", String.valueOf(maxClaims));
            player.sendMessage(langManager.get(lang, "limite-claims-atingido", placeholders));
            return true;
        }
        double custoOriginal = claim.getSize() * cfg.getCustoPorBloco();
        double percentualOcupar = cfg.getPercentualPrecoOcupar() / 100.0;
        int custoFinal = (int) Math.ceil(custoOriginal * percentualOcupar);
        Material itemCompra = cfg.getItemCompra();
        if (!player.getInventory().contains(itemCompra, custoFinal)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("item_name", itemCompra.name().replace("_", " ").toLowerCase());
            placeholders.put("cost", String.valueOf(custoFinal));
            player.sendMessage(langManager.get(lang, "saldo-insuficiente-itens", placeholders));
            return true;
        }
        player.getInventory().removeItem(new ItemStack(itemCompra, custoFinal));
        String novoNome = args[1];
        claim.setOwner(player.getName());
        claim.setClaimName(novoNome);
        claim.getTrustedPlayers().clear();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("claim_name", novoNome);
        player.sendMessage(langManager.get(lang, "abandono.ocupado-sucesso", placeholders));

        dbManagerClaims.updateClaimOwnerByCoords(claim.getMinX(), claim.getMinZ(), claim.getMaxX(), claim.getMaxZ(), player.getUniqueId().toString(), player.getName(), System.currentTimeMillis() / 1000L);

        return true;
    }

    private boolean handleTrustCommand(Player player, String[] args, String lang) {
        if (args.length < 1) {
            player.sendMessage(langManager.get(lang, "ajuda.trust"));
            return true;
        }
        Claim claim = plugin.getClaimManager().getClaimAt(player.getLocation());
        if (claim == null) {
            player.sendMessage(langManager.get(lang, "precisa-estar-dentro"));
            return true;
        }
        if (!claim.getOwnerName().equalsIgnoreCase(player.getName())) {
            player.sendMessage(langManager.get(lang, "nao-e-dono"));
            return true;
        }
        String targetName = args[0];
        claim.trustPlayer(targetName);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("target", targetName);
        player.sendMessage(langManager.get(lang, "trust-adicionado", placeholders));
        return true;
    }

    private boolean handleUntrustCommand(Player player, String[] args, String lang) {
        if (args.length < 1) {
            player.sendMessage(langManager.get(lang, "ajuda.untrust"));
            return true;
        }
        Claim claim = plugin.getClaimManager().getClaimAt(player.getLocation());
        if (claim == null) {
            player.sendMessage(langManager.get(lang, "precisa-estar-dentro"));
            return true;
        }
        if (!claim.getOwnerName().equalsIgnoreCase(player.getName())) {
            player.sendMessage(langManager.get(lang, "nao-e-dono"));
            return true;
        }
        String targetName = args[0];
        if (BlockyClaim.getInstance().isFactionsHookEnabled() &&
                BlockyFactionsAPI.arePlayersInSameFaction(player.getName(), targetName)) {
            player.sendMessage(langManager.get(lang, "erro.nao-pode-untrust-faccao"));
            return true;
        }
        claim.untrustPlayer(targetName);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("target", targetName);
        player.sendMessage(langManager.get(lang, "untrust-removido", placeholders));
        return true;
    }

    private boolean handleAnunciarCommand(Player player, String[] args, String lang) {
        ConfigManager cfg = plugin.getConfigManager();
        ClaimManager claimManager = plugin.getClaimManager();
        if (args.length < 2) {
            player.sendMessage(langManager.get(lang, "ajuda.anunciar"));
            return true;
        }
        Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null || !claim.getOwnerName().equalsIgnoreCase(player.getName())) {
            player.sendMessage(langManager.get(lang, "nao-e-dono"));
            return true;
        }
        int price;
        try {
            price = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("arg", args[1]);
            player.sendMessage(langManager.get(lang, "erro.numero-invalido", placeholders));
            return true;
        }
        if (price < 0) {
            player.sendMessage(langManager.get(lang, "erro.numero-positivo"));
            return true;
        }
        claim.putForSale(price);
        String itemName = cfg.getItemCompra().name().replace("_", " ").toLowerCase();
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("claim_name", claim.getClaimName());
        placeholders.put("price", String.valueOf(price));
        placeholders.put("item_name", itemName);
        player.sendMessage(langManager.get(lang, "venda.colocada-a-venda", placeholders));
        return true;
    }

    private boolean handleUnanunciarCommand(Player player, String[] args, String lang) {
        ClaimManager claimManager = plugin.getClaimManager();
        Claim claim = claimManager.getClaimAt(player.getLocation());
        if (claim == null || !claim.getOwnerName().equalsIgnoreCase(player.getName())) {
            player.sendMessage(langManager.get(lang, "nao-e-dono"));
            return true;
        }
        if (!claim.isForSale()) {
            player.sendMessage(langManager.get(lang, "erro.nao-esta-a-venda"));
            return true;
        }
        claim.removeFromSale();
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("claim_name", claim.getClaimName());
        player.sendMessage(langManager.get(lang, "venda.removida-do-mercado", placeholders));
        return true;
    }

    private boolean handleListCommand(Player player, String[] args, String lang) {
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
                player.sendMessage(langManager.get(lang, "list.no-claims-self"));
            } else {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", targetName);
                player.sendMessage(langManager.get(lang, "list.no-claims-other", placeholders));
            }
        } else {
            if (isSelf) {
                player.sendMessage(langManager.get(lang, "list.header-self"));
            } else {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", targetName);
                player.sendMessage(langManager.get(lang, "list.header-other", placeholders));
            }
            for (Claim claim : claims) {
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("claim_name", claim.getClaimName());
                player.sendMessage(langManager.get(lang, "list.format", placeholders));
            }
        }
        return true;
    }
}
