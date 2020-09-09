package be.lacratus.market.commands;

import be.lacratus.market.Market;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MoneyCommand implements CommandExecutor {

    Market main;

    public MoneyCommand(Market market) {
        this.main = market;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                OfflinePlayer player = (OfflinePlayer) sender;
                main.getEconomyImplementer().getBalance(player);
            } else {
                System.out.println("Console is not a player");
            }
            return true;
        }
        if (args.length == 1) {
            try {
                Player player = Bukkit.getPlayer(args[0]);
                double balance = main.getEconomyImplementer().getBalance(player);
                sender.sendMessage(String.valueOf(balance));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
