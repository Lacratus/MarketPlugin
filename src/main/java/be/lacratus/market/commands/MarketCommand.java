package be.lacratus.market.commands;

import be.lacratus.market.Market;
import be.lacratus.market.objects.AuctionHouse;
import be.lacratus.market.objects.DDGSpeler;
import be.lacratus.market.objects.VeilingItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

        if (!(sender instanceof Player)) {
            System.out.println("You have to be a player");
            return false;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        // Opens auctionhouse
        if (args.length == 0) {
            AuctionHouse.openAuctionHouse(player, main.getVeilingItems(), 1, "AuctionHouse");
            // Opens Personal Items on Auctionhouse
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("personal")) {
                DDGSpeler ddgSpeler = main.getOnlinePlayers().get(uuid);
                AuctionHouse.openAuctionHouse(player, ddgSpeler.getPersoonlijkeItems(), 1, "Personal");
                // Show Debug info
            } else if (args[0].equalsIgnoreCase("debug")) {
                sender.sendMessage("Veilingitems: " + main.getVeilingItems() + ";");
                sender.sendMessage("Owners: " + main.getPlayersWithItems() + ";");
                sender.sendMessage("Bidders: " + main.getPlayersWithBiddings() + ";");
                sender.sendMessage("Online: " + main.getOnlinePlayers() + ";");
                //Shows commands
            } else if (args[0].equalsIgnoreCase("help")) {
                sendHelpMessage((Player) sender);
            } else {
                sender.sendMessage("Unknown command");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("bump")) {
                try {
                    int numberOfItem = Integer.parseInt(args[1]);
                    DDGSpeler ddgSpeler = main.getOnlinePlayers().get(uuid);
                    List<VeilingItem> veilingItems = new ArrayList<>(ddgSpeler.getPersoonlijkeItems());
                    VeilingItem veilingItem = veilingItems.get(numberOfItem - 1);
                    DDGSpeler highestbidder = main.getPlayersWithBiddings().get(veilingItem.getUuidBidder());
                    long timeLeft = veilingItem.getTimeOfDeletion() - System.currentTimeMillis() / 1000;

                    if (timeLeft > 60) {
                        sender.sendMessage("You can't Bump until 60 seconds are remaining");
                        return false;
                    }
                    if (timeLeft < 30) {
                        sender.sendMessage("You can't bump anymore, U can only bump with more than 30 seconds remaining");
                        return false;
                    }
                    if (!veilingItem.isBumped()) {
                        //verwijderen van oude veilingitem
                        main.getVeilingItems().remove(veilingItem);
                        main.getItemsRemoveDatabase().add(veilingItem);
                        veilingItem.getBukkitTask().cancel();
                        //aanmaken nieuw veilingitem
                        VeilingItem newVeilingItem = new VeilingItem(main.getMaxIndex() + 1, veilingItem.getItemStack(), veilingItem.getUuidOwner(), veilingItem.getTimeOfDeletion() - System.currentTimeMillis() / 1000);
                        main.setMaxindex(main.getMaxIndex() + 1);
                        newVeilingItem.setHighestOffer(veilingItem.getHighestOffer());
                        newVeilingItem.setUuidOwner(uuid);
                        newVeilingItem.setUuidBidder(veilingItem.getUuidBidder());
                        newVeilingItem.setBumped(true);
                        main.getVeilingItems().add(newVeilingItem);
                        //Add and delete in lists of players
                        ddgSpeler.getPersoonlijkeItems().remove(veilingItem);
                        ddgSpeler.getPersoonlijkeItems().add(newVeilingItem);

                        highestbidder.getBiddenItems().remove(veilingItem);
                        highestbidder.getBiddenItems().add(newVeilingItem);

                        //update lists in main
                        main.updateLists(ddgSpeler);
                        main.updateLists(highestbidder);

                        timeLeft = veilingItem.getTimeOfDeletion() - System.currentTimeMillis() / 1000;
                        main.runTaskGiveItem(newVeilingItem, highestbidder, timeLeft);
                    } else {
                        player.sendMessage("U have already bumped this item once");
                    }
                } catch (IndexOutOfBoundsException ex) {
                    player.sendMessage("No item on this spot");
                } catch (NumberFormatException ex) {
                    player.sendMessage("Only use numbers");
                }
            } else {
                sendHelpMessage(player);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("sell")) {
                try {
                    int bid = Integer.parseInt(args[1]);
                    int timeleft = Integer.parseInt(args[2]);
                    if (bid > 0 && timeleft >= 90) {
                        ItemStack itemToSell = player.getInventory().getItemInMainHand();
                        if (itemToSell.getType() == Material.AIR) {
                            player.sendMessage("You need an item in your hand");
                            return false;
                        }
                        if (timeleft > 120) {
                            player.sendMessage("Time of item can't be more than 2 minutes");
                            return false;
                        }
                        //Creating VeilingItem
                        int newId = main.getMaxIndex() + 1;
                        VeilingItem veilingItem = new VeilingItem(newId, itemToSell, uuid, timeleft);
                        main.setMaxindex(newId);
                        veilingItem.setHighestOffer(bid);
                        veilingItem.setUuidOwner(uuid);
                        veilingItem.setUuidBidder(uuid);
                        player.getInventory().setItemInMainHand(null);
                        //Adding to Auctionhouse
                        main.getVeilingItems().add(veilingItem);
                        //Adding to Personal Items
                        DDGSpeler ddgSpeler = main.getOnlinePlayers().get(uuid);

                        ddgSpeler.getPersoonlijkeItems().add(veilingItem);
                        ddgSpeler.getBiddenItems().add(veilingItem);
                        //Update lists
                        main.updateLists(ddgSpeler);
                        //Start task
                        main.runTaskGiveItem(veilingItem, ddgSpeler, timeleft);


                    } else {
                        sender.sendMessage("Your minimal price has to be positive and time has to be more than 1 minute");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("Please enter a number");
                }
            }
        } else {
            sendHelpMessage(player);
        }
        return false;
    }


    public void sendHelpMessage(Player player) {
        player.sendMessage("/Market - Opens auctionhouse \n" +
                "/Market personal - Watch personal items \n" +
                "/Market sell <Price> <Time> - Sell item for certain price, minimum of 30 seconds on auctionhouse \n" +
                "/Market bump <#> - Item to bump(Once per item)");
    }
}
