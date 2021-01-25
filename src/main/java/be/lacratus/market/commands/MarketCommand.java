package be.lacratus.market.commands;

import be.lacratus.market.Market;
import be.lacratus.market.util.AuctionHouse;
import be.lacratus.market.objects.DDGPlayer;
import be.lacratus.market.objects.AuctionItem;
import be.lacratus.market.util.ItemStackSerializer;
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
            sender.sendMessage("You have to be a player");
            return false;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        // Opens auctionhouse
        if (args.length == 0) {
            AuctionHouse.openAuctionHouse(player, main.getAuctionItems(), 1, "AuctionHouse");
            // Opens Personal Items in Auctionhouse
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("personal")) {
                DDGPlayer ddgPlayer = main.getOnlinePlayers().get(uuid);
                AuctionHouse.openAuctionHouse(player, ddgPlayer.getPersonalItems(), 1, "Personal");
                // Show lists of Main, check on memory leaks
            } else if (args[0].equalsIgnoreCase("debug")) {
                sender.sendMessage("AuctionItems: " + main.getAuctionItems() + ";");
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
            // Bring item up front in AuctionHouse
            if (args[0].equalsIgnoreCase("bump")) {
                try {
                    int numberOfItem = Integer.parseInt(args[1]);
                    DDGPlayer ddgPlayer = main.getOnlinePlayers().get(uuid);
                    List<AuctionItem> auctionItems = new ArrayList<>(ddgPlayer.getPersonalItems());
                    AuctionItem auctionItem = auctionItems.get(numberOfItem - 1);
                    DDGPlayer highestbidder = main.getPlayersWithBiddings().get(auctionItem.getUuidBidder());
                    long timeLeft = auctionItem.getTimeOfDeletion() - System.currentTimeMillis() / 1000;


                    if (timeLeft > 60) {
                        sender.sendMessage("You can't Bump until 60 seconds are remaining");
                        return false;
                    }
                    if (timeLeft < 30) {
                        sender.sendMessage("You can't bump anymore, U can only bump with more than 30 seconds remaining");
                        return false;
                    }
                    // Check if item hasn't been bumped already
                    if (!auctionItem.isBumped()) {
                        // Deletion of old AuctionItem
                        main.getAuctionItems().remove(auctionItem);
                        main.getItemsRemoveDatabase().add(auctionItem);
                        auctionItem.getBukkitTask().cancel();
                        // Creating new AuctionItem
                        AuctionItem newAuctionItem = new AuctionItem(main.getMaxIndex() + 1, auctionItem.getItemStack(), auctionItem.getUuidOwner(), auctionItem.getTimeOfDeletion() - System.currentTimeMillis() / 1000);
                        main.setMaxindex(main.getMaxIndex() + 1);
                        newAuctionItem.setHighestOffer(auctionItem.getHighestOffer());
                        newAuctionItem.setUuidOwner(uuid);
                        newAuctionItem.setUuidBidder(auctionItem.getUuidBidder());
                        newAuctionItem.setBumped(true);
                        main.getAuctionItems().add(newAuctionItem);
                        // Add and delete in lists of players
                        ddgPlayer.getPersonalItems().remove(auctionItem);
                        ddgPlayer.getPersonalItems().add(newAuctionItem);

                        highestbidder.getBiddenItems().remove(auctionItem);
                        highestbidder.getBiddenItems().add(newAuctionItem);

                        // Update lists in main
                        main.updateLists(ddgPlayer);
                        main.updateLists(highestbidder);

                        timeLeft = auctionItem.getTimeOfDeletion() - System.currentTimeMillis() / 1000;
                        main.runTaskGiveItem(newAuctionItem, highestbidder, timeLeft);
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
            // Selling of an item
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

                        // Creating AuctionItem
                        int newId = main.getMaxIndex() + 1;
                        AuctionItem auctionItem = new AuctionItem(newId, itemToSell, uuid, timeleft);
                        main.setMaxindex(newId);
                        auctionItem.setHighestOffer(bid);
                        auctionItem.setUuidOwner(uuid);
                        auctionItem.setUuidBidder(uuid);
                        player.getInventory().setItemInMainHand(null);

                        //Adding to Auctionhouse
                        main.getAuctionItems().add(auctionItem);
                        // Adding to Personal Items
                        DDGPlayer ddgPlayer = main.getOnlinePlayers().get(uuid);

                        ddgPlayer.getPersonalItems().add(auctionItem);
                        ddgPlayer.getBiddenItems().add(auctionItem);
                        // Update lists
                        main.updateLists(ddgPlayer);
                        // Start task
                        main.runTaskGiveItem(auctionItem, ddgPlayer, timeleft);


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
                "/Market sell <Price> <Time> - Sell item for certain price, minimum of 90 seconds on auctionhouse \n" +
                "/Market bump <#> - Item to bump(Once per item)");
    }
}
