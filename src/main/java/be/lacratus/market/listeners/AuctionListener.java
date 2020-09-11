package be.lacratus.market.listeners;

import be.lacratus.market.Market;
import be.lacratus.market.objects.AuctionHouse;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AuctionListener implements Listener {

    private Market main;

    public AuctionListener(Market market) {
        this.main = market;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        Inventory inv = e.getInventory();

        if (item != null && item.getType() != null && inv.getTitle().contains("AuctionHouse")) {
            int page = Integer.parseInt(inv.getItem(45).getItemMeta().getLocalizedName());

            if (e.getRawSlot() == 45 && item.getType().equals(Material.STAINED_GLASS) && item.getDurability() == 5) {
                new AuctionHouse(player, main.getVeilingItems(), page - 1);
            } else if (e.getRawSlot() == 53 && item.getType().equals(Material.STAINED_GLASS) && item.getDurability() == 5) {
                new AuctionHouse(player, main.getVeilingItems(), page + 1);
            }

            e.setCancelled(true);
        }

    }

}
