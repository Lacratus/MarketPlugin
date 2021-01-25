package be.lacratus.market.util;

import be.lacratus.market.objects.AuctionItem;
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

import java.util.concurrent.TimeUnit;

public class AuctionHouse {


    public AuctionHouse() {

    }

    public static void openAuctionHouse(Player player, PriorityQueue<AuctionItem> allItems, int page, String title) {
        ArrayList<AuctionItem> items = new ArrayList<>(allItems);
        if (title.equals("AuctionHouse")) {
            items.sort(new SortByIndexAscending());
        } else if (title.equals("Personal")) {
            items.sort(new SortByIndexDescending());
        }
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
        //Fill up AuctionHouse and add lores
        for (AuctionItem item : PageUtil.getPageItems(items, page, 45)) {
            //Get Itemstack
            ItemStack itemstack = item.getItemStack();
            ItemMeta itemMeta = itemstack.getItemMeta();
            //Create lore
            List<String> lores = itemMeta.getLore();
            lores.add("Bid: " + item.getHighestOffer() + "€");
            long timeLeft = item.getTimeOfDeletion() - System.currentTimeMillis() / 1000;
            long[] timeList = onlineTimeToLong(timeLeft);
            lores.add("Hours: " + timeList[0] + ", Minutes: " + timeList[1] + ", Seconds: " + timeList[2]);
            itemMeta.setLore(lores);
            itemstack.setItemMeta(itemMeta);
            //Set Item
            auctionHouse.setItem(auctionHouse.firstEmpty(), item.getItemStack());
        }
        player.openInventory(auctionHouse);

        //remove lores
        for (AuctionItem item : PageUtil.getPageItems(items, page, 45)) {
            //Get Itemstack
            ItemStack itemstack = item.getItemStack();
            ItemMeta itemMeta = itemstack.getItemMeta();
            //remove Auction lore
            List<String> lores = itemMeta.getLore();
            lores.remove(lores.size() - 1);
            lores.remove(lores.size() - 1);
            itemMeta.setLore(lores);
            itemstack.setItemMeta(itemMeta);
        }
    }

    public static long[] onlineTimeToLong(long timeInSeconds) {
        long hours = TimeUnit.SECONDS.toHours(timeInSeconds);
        timeInSeconds -= TimeUnit.HOURS.toSeconds(hours);
        long minutes = TimeUnit.SECONDS.toMinutes(timeInSeconds);
        timeInSeconds -= TimeUnit.MINUTES.toSeconds(minutes);
        long seconds = TimeUnit.SECONDS.toSeconds(timeInSeconds);
        return new long[]{hours, minutes, seconds};
    }
}



