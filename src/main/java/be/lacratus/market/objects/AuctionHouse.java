package be.lacratus.market.objects;

import be.lacratus.market.Market;
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
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AuctionHouse {


    public AuctionHouse(Player player, PriorityQueue<VeilingItem> allItems, int page, String title) {
        Inventory auctionHouse = Bukkit.createInventory(null, 54, title);

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
            //Get Itemstack
            ItemStack itemstack = item.getItemStack();
            ItemMeta itemMeta = itemstack.getItemMeta();
            //Create lore
            List<String> lores = new ArrayList<>();
            lores.add("Bid: " + item.getHighestOffer() + "€");
            long timeLeft = item.getTimeOfDeletion() - System.currentTimeMillis() / 1000;
            long[] timeList = onlineTimeToLong(timeLeft);
            lores.add("Hours: " + timeList[0] + ", Mintutes: " + timeList[1] + ", Seconds: " + timeList[2]);
            itemMeta.setLore(lores);
            itemstack.setItemMeta(itemMeta);
            //Set Item
            auctionHouse.setItem(auctionHouse.firstEmpty(), item.getItemStack());
        }
        player.openInventory(auctionHouse);
    }

    public long[] onlineTimeToLong(long timeInSeconds) {
        long hours = TimeUnit.SECONDS.toHours(timeInSeconds);
        timeInSeconds -= TimeUnit.HOURS.toSeconds(hours);
        long minutes = TimeUnit.SECONDS.toMinutes(timeInSeconds);
        timeInSeconds -= TimeUnit.MINUTES.toSeconds(minutes);
        long seconds = TimeUnit.SECONDS.toSeconds(timeInSeconds);
        return new long[]{hours, minutes, seconds};
    }
}



