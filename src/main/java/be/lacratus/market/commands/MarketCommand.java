package be.lacratus.market.commands;

import be.lacratus.market.Market;
import be.lacratus.market.objects.AuctionHouse;
import be.lacratus.market.objects.DDGSpeler;
import be.lacratus.market.objects.VeilingItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MarketCommand implements CommandExecutor {

    private Market main;

    public MarketCommand(Market market) {
        this.main = market;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        if (sender instanceof Player) {
            if (args.length == 0) {
                new AuctionHouse(player, main.getVeilingItems(), 1, "AuctionHouse");
                Bukkit.getServer().broadcastMessage(main.getVeilingItems().size() + "");
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("personal")) {
                    DDGSpeler ddgSpeler = main.getOnlinePlayers().get(uuid);
                    new AuctionHouse(player, ddgSpeler.getPersoonlijkeItems(), 1, "Personal");
                } else if (args[0].equalsIgnoreCase("help")) {
                    //Help
                } else {
                    sender.sendMessage("Unknown command");
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("sell")) {
                    try {
                        int bid = Integer.parseInt(args[1]);
                        if (bid > 0) {
                            if (player.getInventory().getItemInMainHand() == null) {
                                player.sendMessage("You need an item in your hand");
                                return false;
                            }
                            //Creating VeilingItem
                            ItemStack itemToSell = player.getInventory().getItemInMainHand();
                            VeilingItem veilingItem = new VeilingItem(itemToSell, uuid);
                            veilingItem.setHighestOffer(bid);
                            veilingItem.setUuidOwner(uuid);
                            veilingItem.setUuidBidder(uuid);
                            player.getInventory().setItemInMainHand(null);
                            //Adding to Auctionhouse
                            main.getVeilingItems().add(veilingItem);
                            //Adding to Personal Items
                            DDGSpeler ddgSpeler;
                            if (main.getPlayersWithItems().containsKey(uuid)) {
                                ddgSpeler = main.getPlayersWithItems().get(uuid);
                            } else {
                                ddgSpeler = main.getOnlinePlayers().get(uuid);
                                main.getPlayersWithItems().put(uuid, ddgSpeler);
                            }
                            ddgSpeler.getPersoonlijkeItems().add(veilingItem);
                            ddgSpeler.addBiddeditem(veilingItem);
                            main.getPlayersWithBiddings().put(uuid, ddgSpeler);
                            //remove timer
                            long timeLeft = veilingItem.getTimeOfDeletion() - (System.currentTimeMillis() / 1000);
                            System.out.println(timeLeft / 1000);
                            main.runTaskGiveItem(veilingItem, ddgSpeler, timeLeft);


                        } else {
                            sender.sendMessage("Your minimal price has to be positive");
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Please enter a number");
                    }
                } else if (args[0].equalsIgnoreCase("bump")) {
                    try {
                        int numberOfItem = Integer.parseInt(args[1]);
                        DDGSpeler ddgSpeler = main.getPlayersWithItems().get(uuid);
                        List<VeilingItem> veilingItems = Market.priorityQueueToList(ddgSpeler.getPersoonlijkeItems());
                        VeilingItem veilingItem = veilingItems.get(numberOfItem - 1);
                        DDGSpeler highestbidder = main.getPlayersWithBiddings().get(veilingItem.getUuidBidder());
                        if (!veilingItem.isBumped()) {
                            veilingItem.setBumped(true);
                            main.getVeilingItems().remove(veilingItem);
                            main.getVeilingItems().add(veilingItem);
                            veilingItem.getBukkitTask().cancel();

                            long timeLeft = veilingItem.getTimeOfDeletion() - System.currentTimeMillis();
                            main.runTaskGiveItem(veilingItem, highestbidder, timeLeft);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    sendHelpMessage(player);
                }
            } else {
                sendHelpMessage(player);
            }
        } else {
            System.out.println("You have to be a player");
        }
        return false;
    }


    public void sendHelpMessage(Player player) {
        player.sendMessage("/Market - Opens auctionhouse \n" +
                "/Market personal - Watch personal items \n" +
                "/Market sell <Price> - Sell item for certain price \n" +
                "/Market bump <#> - Item to bump(Once per item)");
    }
}
