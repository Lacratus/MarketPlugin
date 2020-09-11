package be.lacratus.market.objects;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class VeilingItem {
    private ItemStack itemStack;
    private int highestOffer;
    private UUID uuidBidder;
    private UUID uuidOwner;


    public boolean Bumped;

    public VeilingItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public boolean isBumped() {
        return Bumped;
    }

    public void setBumped(boolean bumped) {
        Bumped = bumped;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getHighestOffer() {
        return highestOffer;
    }

    public UUID getUuidBidder() {
        return uuidBidder;
    }

    public UUID getUuidOwner() {
        return uuidOwner;
    }
}
