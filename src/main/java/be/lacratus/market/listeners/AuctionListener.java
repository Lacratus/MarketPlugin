package be.lacratus.market.listeners;

import be.lacratus.market.Market;
import be.lacratus.market.objects.AuctionItem;
import be.lacratus.market.objects.DDGPlayer;
import be.lacratus.market.util.AuctionHouse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionListener implements Listener {

    private Market main;

    public AuctionListener(Market market) {
        this.main = market;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();
        DDGPlayer owner = main.getOnlinePlayers().get(player.getUniqueId());
        ItemStack item = event.getCurrentItem();
        Inventory inv = event.getInventory();
        if (item == null || item.getType() == null) {
            return;
        }
        if (inv.getTitle().contains("AuctionHouse")) {
            if (event.getRawSlot() < event.getInventory().getSize()) {
                int page = Integer.parseInt(inv.getItem(45).getItemMeta().getLocalizedName());

                //Next or previous page
                if (event.getRawSlot() == 45 && item.getType().equals(Material.STAINED_GLASS) && item.getDurability() == 5) {
                    AuctionHouse.openAuctionHouse(player, main.getAuctionItems(), page - 1, "AuctionHouse");
                } else if (event.getRawSlot() == 53 && item.getType().equals(Material.STAINED_GLASS) && item.getDurability() == 5) {
                    AuctionHouse.openAuctionHouse(player, main.getAuctionItems(), page + 1, "AuctionHouse");
                } else {
                    //Setting up bidding
                    int slot = event.getSlot();
                    List<AuctionItem> auctionItems = new ArrayList<>(main.getAuctionItems());
                    if (auctionItems.size() > slot) {
                        AuctionItem auctionItem = auctionItems.get(slot);
                        if (!main.getAuctionItems().contains(auctionItem)) {
                            player.sendMessage("This item has been sold or magically doesn't exist");
                        } else if (owner.getPersonalItems().contains(auctionItem)) {
                            player.sendMessage("You can't bid on your own items!");
                        } else {
                            owner.setBidAuctionItem(auctionItem);
                            player.sendMessage("How much do you want to bid?");
                            owner.setBidding(true);
                        }
                        event.getWhoClicked().closeInventory();
                    }
                }
            }
            event.setCancelled(true);
        } else if (inv.getTitle().contains("Personal")) {
            event.setCancelled(true);
            if (player.getInventory().firstEmpty() != -1 && event.getRawSlot() < event.getInventory().getSize()) {
                // Get Item
                int slot = event.getSlot();
                try {
                    List<AuctionItem> personalAuctionItems = new ArrayList<>(owner.getPersonalItems());
                    if (personalAuctionItems.size() > slot) {

                        AuctionItem auctionItem = personalAuctionItems.get(slot);
                        // Delete item from Auctionhouse
                        main.getAuctionItems().remove(auctionItem);
                        owner.getPersonalItems().remove(auctionItem);

                        // Give item back to owner
                        ItemStack returnedItem = auctionItem.getItemStack();
                        ItemMeta itemMeta = returnedItem.getItemMeta();
                        itemMeta.setLore(null);
                        returnedItem.setItemMeta(itemMeta);
                        player.getInventory().setItem(player.getInventory().firstEmpty(), returnedItem);
                        // Stop Task
                        auctionItem.getBukkitTask().cancel();

                        // Delete item at bidder
                        DDGPlayer bidder = main.getPlayersWithBiddings().get(auctionItem.getUuidBidder());
                        bidder.getBiddenItems().remove(auctionItem);
                        if (auctionItem.getUuidBidder() != auctionItem.getUuidOwner()) {
                            main.getEconomyImplementer().depositPlayer(Bukkit.getOfflinePlayer(bidder.getUuid()), auctionItem.getHighestOffer());
                        }
                        // Update inventory
                        AuctionHouse.openAuctionHouse(player, main.getAuctionItems(), 1, "Personal");
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            } else {
                player.sendMessage("Clear your inventory");
            }
        }
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        DDGPlayer ddgPlayer = main.getOnlinePlayers().get(uuid);
        if (ddgPlayer.isBidding()) {
            try {
                int bidding = Integer.parseInt(event.getMessage());
                AuctionItem auctionItem = ddgPlayer.getBidAuctionItem();
                if (bidding >= main.getPlayerBank().get(uuid)) {
                    player.sendMessage("You don't have enough money");
                    return;
                }
                if (bidding < auctionItem.getHighestOffer()) {
                    player.sendMessage("Your offer isn't high enough");
                    return;
                }
                //Delete oldbidder
                DDGPlayer oldBidder = main.getPlayersWithBiddings().get(auctionItem.getUuidBidder());
                oldBidder.getBiddenItems().remove(auctionItem);
                main.updateLists(oldBidder);
                // Return money
                if (auctionItem.getUuidOwner() != auctionItem.getUuidBidder()) {
                    main.getEconomyImplementer().depositPlayer(Bukkit.getOfflinePlayer(oldBidder.getUuid()), auctionItem.getHighestOffer());
                }
                // Create newBidder
                auctionItem.setHighestOffer(bidding);
                auctionItem.setUuidBidder(uuid);
                            /*List<String> lores = veilingItem.getLore();
                            String bidLore = "Bid: " + bidding;
                            lores.set(0, bidLore);*/
                ddgPlayer.getBiddenItems().add(auctionItem);
                main.getPlayersWithBiddings().put(uuid, ddgPlayer);
                main.updateLists(ddgPlayer);
                // Remove & return money
                main.getEconomyImplementer().withdrawPlayer(player, bidding);

                // Run new task
                long timeLeft = auctionItem.getTimeOfDeletion() - (System.currentTimeMillis() / 1000);
                auctionItem.getBukkitTask().cancel();
                main.runTaskGiveItem(auctionItem, ddgPlayer, timeLeft);
                ddgPlayer.setBidAuctionItem(null);
            } catch (Exception ex) {
                player.sendMessage("The bidding can't be text");
                ex.printStackTrace();
            }

            ddgPlayer.setBidding(false);
            event.setCancelled(true);
        }
    }

}
