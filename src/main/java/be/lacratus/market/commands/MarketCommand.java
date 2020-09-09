package be.lacratus.market.commands;

import be.lacratus.market.Market;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class MarketCommand implements CommandExecutor {

    private Market main;

    public MarketCommand(Market market) {
        this.main = market;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 0) {
                //Open auctionhouse
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("personal")) {

                } else if (args[0].equalsIgnoreCase("help")) {
                    //Help
                } else {
                    sender.sendMessage("Unknown command");
                }
            } else {
                if (args.length == 2) {
                    if (args[1].equalsIgnoreCase("sell")) {
                        try {
                            int price = Integer.parseInt(args[1]);
                            if (price > 0) {
                                // YEah What then?
                            } else {
                                sender.sendMessage("Your price has to be positive");
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            sender.sendMessage("Please enter a number");
                        }
                    }
                }
            }
        }
        return false;
    }
}
