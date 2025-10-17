package com.blockycraft.ironclaim.commands;

import com.blockycraft.ironclaim.IronClaim;
import com.blockycraft.ironclaim.config.ConfigManager;
import com.blockycraft.ironclaim.data.Claim;
import com.blockycraft.ironclaim.listeners.ClaimToolListener;
import com.blockycraft.ironclaim.managers.ClaimManager;
import com.blockycraft.ironclaim.managers.PlayerDataManager;
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

    private final IronClaim plugin;

    public CommandManager(IronClaim plugin) {
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

    private boolean handleClaimCommand(Player player, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (args.length == 0) {
            // Remove o prefixo do plugin para formatar a ajuda
            String prefix = cfg.getMsg("prefixo", "");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.header", "&6--- Ajuda IronClaim ---").replace(prefix, "")));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.saldo", "&b/claim saldo &7- Mostra seus blocos.")));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.comprar", "&b/claim comprar <qtde> &7- Compra blocos.")));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.confirm", "&b/claim confirm <nome> &7- Confirma um terreno.")));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.list", "&b/claim list [jogador] &7- Lista suas claims ou de outro jogador.")));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.trust", "&b/trust <jogador> &7- Da permissao a um amigo.")));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', cfg.getMsg("ajuda.untrust", "&b/untrust <jogador> &7- Remove a permissao.")));
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
                player.sendMessage(cfg.getMsg("ajuda.comprar", "&cUse: /claim comprar <quantidade>"));
                return true;
            }
            int amountToBuy;
            try { amountToBuy = Integer.parseInt(args[1]); } 
            catch (NumberFormatException e) {
                player.sendMessage(cfg.getMsg("erro.numero-invalido", "&c'{arg}' nao e um numero valido.").replace("{arg}", args[1]));
                return true;
            }
            if (amountToBuy <= 0) {
                player.sendMessage(cfg.getMsg("erro.numero-positivo", "&cA quantidade deve ser maior que zero."));
                return true;
            }

            Material itemCompra = cfg.getItemCompra();
            int custoPorBloco = cfg.getCustoPorBloco();
            int cost = amountToBuy * custoPorBloco;

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

        if (subCommand.equals("confirm")) {
            return handleConfirmCommand(player, args);
        }
        
        if (subCommand.equals("list")) {
            return handleListCommand(player, args);
        }
        
        player.sendMessage(cfg.getMsg("erro.comando-desconhecido", "&cComando desconhecido. Use /claim para ajuda."));
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

    private boolean handleConfirmCommand(Player player, String[] args) {
        ConfigManager cfg = plugin.getConfigManager();
        if (args.length < 2) {
            player.sendMessage(cfg.getMsg("ajuda.confirm", "&cUse: /claim confirm <nome-da-claim>"));
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
        player.sendMessage(cfg.getMsg("trust-adicionado", "&a{target} &aagora tem permissao no seu terreno.")
            .replace("{target}", targetName));
        return true;
    }

    private boolean handleUntrustCommand(Player player, String[] args) {
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
        claim.untrustPlayer(targetName);
        player.sendMessage(cfg.getMsg("untrust-removido", "&a{target} &anao tem mais permissao no seu terreno.")
            .replace("{target}", targetName));
        return true;
    }
}