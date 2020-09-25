package be.lacratus.market.objects;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.UUID;

public class VeilingItem implements Comparable<VeilingItem> {
    private int id;
    private ItemStack itemStack;
    private int highestOffer;
    private UUID uuidBidder;
    private UUID uuidOwner;
    private Long timeOfDeletion;
    private BukkitTask bukkitTask;


    public boolean Bumped;

    public VeilingItem(ItemStack itemStack, UUID uuidOwner) {
        this.itemStack = itemStack;
        this.uuidOwner = uuidOwner;
        this.timeOfDeletion = (System.currentTimeMillis() / 1000) + 20L;
    }

    public VeilingItem(ItemStack itemStack, UUID uuidOwner, long timeOfDeletion) {
        this.itemStack = itemStack;
        this.uuidOwner = uuidOwner;
        this.timeOfDeletion = timeOfDeletion;
    }

    public VeilingItem(int id, ItemStack itemStack, UUID uuidOwner, long timeOfDeletion) {
        this.id = id;
        this.itemStack = itemStack;
        this.uuidOwner = uuidOwner;
        this.timeOfDeletion = timeOfDeletion;
    }

    public boolean isBumped() {
        return Bumped;
    }

    public void setBumped(boolean bumped) {
        Bumped = bumped;
        this.timeOfDeletion = System.currentTimeMillis() + 20000L;
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

    public BukkitTask getBukkitTask() {
        return bukkitTask;
    }

    public void setBukkitTask(BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    public void setHighestOffer(int highestOffer) {
        this.highestOffer = highestOffer;
    }

    public void setUuidBidder(UUID uuidBidder) {
        this.uuidBidder = uuidBidder;
    }

    public void setUuidOwner(UUID uuidOwner) {
        this.uuidOwner = uuidOwner;
    }

    public Long getTimeOfDeletion() {
        return timeOfDeletion;
    }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(VeilingItem veilingItem) {
        if (timeOfDeletion > veilingItem.timeOfDeletion)
            return -1;
        else if (timeOfDeletion < veilingItem.timeOfDeletion)
            return 1;
        return 0;
    }


}
