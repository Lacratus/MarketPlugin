package be.lacratus.market.commands;

import be.lacratus.market.Market;
import be.lacratus.market.objects.AuctionHouse;
import be.lacratus.market.objects.VeilingItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class MarketCommand implements CommandExecutor {

    private Market main;

    public MarketCommand(Market market) {
        this.main = market;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 0) {
                new AuctionHouse((Player) sender, main.getVeilingItems(), 1);
                Bukkit.getServer().broadcastMessage(main.getVeilingItems().size() + "");
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("personal")) {

                } else if (args[0].equalsIgnoreCase("help")) {
                    //Help
                } else {
                    sender.sendMessage("Unknown command");
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("sell")) {
                    Player player = (Player) sender;
                    try {
                        int price = Integer.parseInt(args[1]);
                        if (price > 0) {
                            ItemStack itemToSell = player.getInventory().getItemInMainHand();
                            player.getInventory().remove(itemToSell);
                            VeilingItem veilingItem = new VeilingItem(itemToSell);
                            main.getVeilingItems().add(veilingItem);
                        } else {
                            sender.sendMessage("Your price has to be positive");
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        sender.sendMessage("Please enter a number");
                    }
                } else {
                    sender.sendMessage("1");
                }
            } else {
                sender.sendMessage("2");
            }
        }
        return false;
    }
}
