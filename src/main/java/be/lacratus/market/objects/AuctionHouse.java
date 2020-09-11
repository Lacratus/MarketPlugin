package be.lacratus.market.objects;

import be.lacratus.market.util.PageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AuctionHouse {

    private Inventory auctionHouse;
    private List<VeilingItem> allItems;


    public AuctionHouse(Player player, List<VeilingItem> allItems, int page) {

        this.auctionHouse = Bukkit.createInventory(null, 54, "AuctionHouse - " + page);
        this.allItems = allItems;

        //Get to previous page if possible
        ItemStack previousPage;
        ItemMeta previousMeta;
        if (PageUtil.isPageValid(allItems, page - 1, 45)) {
            previousPage = new ItemStack(Material.STAINED_GLASS, 1, (short) 5);
        } else {
            previousPage = new ItemStack(Material.STAINED_GLASS, 1, (short) 14);
        }
        previousMeta = previousPage.getItemMeta();
        previousMeta.setDisplayName(ChatColor.GREEN + "PREVIOUS PAGE");
        previousMeta.setLocalizedName(page + "");
        previousPage.setItemMeta(previousMeta);

        auctionHouse.setItem(45, previousPage);
        //Get to next page if possible
        ItemStack nextPage;
        ItemMeta nextMeta;
        if (PageUtil.isPageValid(allItems, page + 1, 45)) {
            nextPage = new ItemStack(Material.STAINED_GLASS, 1, (short) 5);
        } else {
            nextPage = new ItemStack(Material.STAINED_GLASS, 1, (short) 14);
        }
        nextMeta = nextPage.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GREEN + "NEXT PAGE");
        nextPage.setItemMeta(nextMeta);

        auctionHouse.setItem(53, nextPage);
        //Fill up AuctionHouse
        for (VeilingItem item : PageUtil.getPageItems(allItems, page, 45)) {
            auctionHouse.setItem(auctionHouse.firstEmpty(), item.getItemStack());
        }

        player.openInventory(auctionHouse);
    }

    public void addItem(VeilingItem veilingItem) {
        allItems.add(veilingItem);
    }
}



