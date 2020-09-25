package be.lacratus.market.listeners;

import be.lacratus.market.Market;
import be.lacratus.market.objects.AuctionHouse;
import be.lacratus.market.objects.DDGSpeler;
import be.lacratus.market.objects.VeilingItem;
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
    public void onClick(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();
        DDGSpeler ddgSpeler = main.getOnlinePlayers().get(player.getUniqueId());
        ItemStack item = e.getCurrentItem();
        Inventory inv = e.getInventory();

        if (item != null && item.getType() != null) {
            if (inv.getTitle().contains("AuctionHouse")) {
                if (e.getRawSlot() < e.getInventory().getSize()) {
                    int page = Integer.parseInt(inv.getItem(45).getItemMeta().getLocalizedName());

                    //Next or previous page
                    if (e.getRawSlot() == 45 && item.getType().equals(Material.STAINED_GLASS) && item.getDurability() == 5) {
                        new AuctionHouse(player, main.getVeilingItems(), page - 1, "AuctionHouse");
                    } else if (e.getRawSlot() == 53 && item.getType().equals(Material.STAINED_GLASS) && item.getDurability() == 5) {
                        new AuctionHouse(player, main.getVeilingItems(), page + 1, "AuctionHouse");
                    } else {
                        //Setting up bidding
                        int slot = e.getSlot();
                        e.getWhoClicked().closeInventory();
                        List<VeilingItem> veilingItems = new ArrayList<>(main.getVeilingItems());
                        VeilingItem veilingItem = veilingItems.get(slot);
                        if (ddgSpeler.getPersoonlijkeItems().contains(veilingItem)) {
                            player.sendMessage("You can't bid on your own items!");
                        } else {
                            ddgSpeler.setBidVeilingItem(veilingItem);
                            player.sendMessage("How much do you want to bid?");
                            ddgSpeler.setBidding(true);
                        }
                    }
                    System.out.println("Works 1");
                }
                e.setCancelled(true);
            } else if (inv.getTitle().contains("Personal")) {
                if (player.getInventory().firstEmpty() != -1 && e.getRawSlot() < e.getInventory().getSize()) {
                    //Verkrijg item
                    int slot = e.getSlot();
                    try {
                        List<VeilingItem> personalVeilingItems = new ArrayList<>(ddgSpeler.getPersoonlijkeItems());
                        VeilingItem veilingItem = personalVeilingItems.get(slot);
                        //Verwijder item uit auctionhouse
                        main.getVeilingItems().remove(veilingItem);
                        ddgSpeler.getPersoonlijkeItems().remove(veilingItem);

                        //Geef item terug aan eigenaar
                        ItemStack returnedItem = veilingItem.getItemStack();
                        ItemMeta itemMeta = returnedItem.getItemMeta();
                        itemMeta.setLore(null);
                        returnedItem.setItemMeta(itemMeta);
                        player.getInventory().setItem(player.getInventory().firstEmpty(), returnedItem);
                        //Stop teruggave na tijdsaangifte
                        veilingItem.getBukkitTask().cancel();
                        //update inventory
                        new AuctionHouse(player, main.getVeilingItems(), 1, "Personal");
                        System.out.println("works 2");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        System.out.println("works 3");
                    }
                } else {
                    player.sendMessage("Clear your inventory");
                }
                e.setCancelled(true);
            }

        }

    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        DDGSpeler ddgSpeler = main.getOnlinePlayers().get(uuid);

        if (ddgSpeler.isBidding()) {
            try {
                int bidding = Integer.parseInt(e.getMessage());
                VeilingItem veilingItem = ddgSpeler.getBidVeilingItem();
                if (bidding <= main.getPlayerBank().get(uuid)) {
                    if (bidding > veilingItem.getHighestOffer()) {
                        //Delete oldbidder
                        System.out.println(veilingItem.getUuidBidder());
                        System.out.println(main.getPlayersWithBiddings());
                        DDGSpeler oldBidder = main.getPlayersWithBiddings().get(veilingItem.getUuidBidder());
                        oldBidder.getBiddenItems().remove(veilingItem);

                        //Create newBidder
                        veilingItem.setHighestOffer(bidding);
                        veilingItem.setUuidBidder(uuid);
                            /*List<String> lores = veilingItem.getLore();
                            String bidLore = "Bid: " + bidding;
                            lores.set(0, bidLore);*/
                        ddgSpeler.addBiddeditem(veilingItem);
                        ddgSpeler.getBiddenItems().add(veilingItem);
                        main.getPlayersWithBiddings().put(player.getUniqueId(), ddgSpeler);
                        //Remove money
                        main.getPlayerBank().put(uuid, main.getPlayerBank().get(uuid) - bidding);
                        //
                        long timeLeft = veilingItem.getTimeOfDeletion() - (System.currentTimeMillis() / 1000);
                        veilingItem.getBukkitTask().cancel();
                        main.runTaskGiveItem(veilingItem, ddgSpeler, timeLeft);
                        ddgSpeler.setBidVeilingItem(null);
                    } else {
                        player.sendMessage("Your offer isn't high enough");
                    }
                } else {
                    player.sendMessage("You don't have enough money");
                }
            } catch (Exception ex) {
                player.sendMessage("The bidding can't be text");
                ex.printStackTrace();
            }

            ddgSpeler.setBidding(false);
            e.setCancelled(true);
        }
    }

}
