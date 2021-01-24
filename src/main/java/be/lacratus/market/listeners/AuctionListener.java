package be.lacratus.market.listeners;

import be.lacratus.market.Market;
import be.lacratus.market.objects.AuctionHouse;
import be.lacratus.market.objects.DDGSpeler;
import be.lacratus.market.objects.VeilingItem;
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
        DDGSpeler eigenaar = main.getOnlinePlayers().get(player.getUniqueId());
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
                    AuctionHouse.openAuctionHouse(player, main.getVeilingItems(), page - 1, "AuctionHouse");
                } else if (event.getRawSlot() == 53 && item.getType().equals(Material.STAINED_GLASS) && item.getDurability() == 5) {
                    AuctionHouse.openAuctionHouse(player, main.getVeilingItems(), page + 1, "AuctionHouse");
                } else {
                    //Setting up bidding
                    int slot = event.getSlot();
                    List<VeilingItem> veilingItems = new ArrayList<>(main.getVeilingItems());
                    if (veilingItems.size() > slot) {
                        VeilingItem veilingItem = veilingItems.get(slot);
                        if (!main.getVeilingItems().contains(veilingItem)) {
                            player.sendMessage("This item has been sold or magically doesn't exist");
                        } else if (eigenaar.getPersoonlijkeItems().contains(veilingItem)) {
                            player.sendMessage("You can't bid on your own items!");
                        } else {
                            eigenaar.setBidVeilingItem(veilingItem);
                            player.sendMessage("How much do you want to bid?");
                            eigenaar.setBidding(true);
                        }
                        event.getWhoClicked().closeInventory();
                    }
                }
            }
            event.setCancelled(true);
        } else if (inv.getTitle().contains("Personal")) {
            event.setCancelled(true);
            if (player.getInventory().firstEmpty() != -1 && event.getRawSlot() < event.getInventory().getSize()) {
                //Verkrijg item
                int slot = event.getSlot();
                try {
                    List<VeilingItem> personalVeilingItems = new ArrayList<>(eigenaar.getPersoonlijkeItems());
                    VeilingItem veilingItem = personalVeilingItems.get(slot);
                    //Verwijder item uit auctionhouse
                    main.getVeilingItems().remove(veilingItem);
                    eigenaar.getPersoonlijkeItems().remove(veilingItem);

                    //Geef item terug aan eigenaar
                    ItemStack returnedItem = veilingItem.getItemStack();
                    ItemMeta itemMeta = returnedItem.getItemMeta();
                    itemMeta.setLore(null);
                    returnedItem.setItemMeta(itemMeta);
                    player.getInventory().setItem(player.getInventory().firstEmpty(), returnedItem);
                    //Stop teruggave na tijdsaangifte
                    veilingItem.getBukkitTask().cancel();

                    //Verwijder item bij bieder
                    DDGSpeler bieder = main.getPlayersWithBiddings().get(veilingItem.getUuidBidder());
                    bieder.getBiddenItems().remove(veilingItem);
                    if (veilingItem.getUuidBidder() != veilingItem.getUuidOwner()) {
                        main.getEconomyImplementer().depositPlayer(Bukkit.getOfflinePlayer(bieder.getUuid()), veilingItem.getHighestOffer());
                    }
                    //update inventory
                    AuctionHouse.openAuctionHouse(player, main.getVeilingItems(), 1, "Personal");
                    System.out.println("works 2");

                } catch (Exception e1) {
                    e1.printStackTrace();
                    System.out.println("works 3");
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
        DDGSpeler ddgSpeler = main.getOnlinePlayers().get(uuid);
        if (ddgSpeler.isBidding()) {
            try {
                int bidding = Integer.parseInt(event.getMessage());
                VeilingItem veilingItem = ddgSpeler.getBidVeilingItem();
                if (bidding >= main.getPlayerBank().get(uuid)) {
                    player.sendMessage("You don't have enough money");
                    return;
                }
                if (bidding < veilingItem.getHighestOffer()) {
                    player.sendMessage("Your offer isn't high enough");
                    return;
                }
                //Delete oldbidder
                System.out.println("" + veilingItem.getUuidBidder() + "    " + main.getPlayersWithBiddings());
                DDGSpeler oldBidder = main.getPlayersWithBiddings().get(veilingItem.getUuidBidder());
                System.out.println(oldBidder.getBiddenItems());
                oldBidder.getBiddenItems().remove(veilingItem);
                System.out.println(main.getPlayersWithBiddings());
                main.updateLists(oldBidder);
                System.out.println(oldBidder.getBiddenItems());
                System.out.println(main.getPlayersWithBiddings());
                //Return money
                if (veilingItem.getUuidOwner() != veilingItem.getUuidBidder()) {
                    main.getEconomyImplementer().depositPlayer(Bukkit.getOfflinePlayer(oldBidder.getUuid()), veilingItem.getHighestOffer());
                }
                //Create newBidder
                veilingItem.setHighestOffer(bidding);
                veilingItem.setUuidBidder(uuid);
                            /*List<String> lores = veilingItem.getLore();
                            String bidLore = "Bid: " + bidding;
                            lores.set(0, bidLore);*/
                ddgSpeler.getBiddenItems().add(veilingItem);
                main.getPlayersWithBiddings().put(uuid, ddgSpeler);
                main.updateLists(ddgSpeler);
                //Remove & return money
                main.getEconomyImplementer().withdrawPlayer(player, bidding);
                //
                long timeLeft = veilingItem.getTimeOfDeletion() - (System.currentTimeMillis() / 1000);
                veilingItem.getBukkitTask().cancel();
                System.out.println("Test 1.1: " + ddgSpeler.getBiddenItems());
                main.runTaskGiveItem(veilingItem, ddgSpeler, timeLeft);
                ddgSpeler.setBidVeilingItem(null);
            } catch (Exception ex) {
                player.sendMessage("The bidding can't be text");
                ex.printStackTrace();
            }

            ddgSpeler.setBidding(false);
            event.setCancelled(true);
        }
    }

}
