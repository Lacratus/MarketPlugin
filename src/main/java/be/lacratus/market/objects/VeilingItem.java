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
    private long timeOfDeletion;
    private BukkitTask bukkitTask;


    public boolean bumped;

    public VeilingItem(ItemStack itemStack, UUID uuidOwner) {
        this.itemStack = itemStack;
        this.uuidOwner = uuidOwner;
        this.timeOfDeletion = 20L + System.currentTimeMillis() / 1000;
    }

    public VeilingItem(ItemStack itemStack, UUID uuidOwner, long timeLeft) {
        this.itemStack = itemStack;
        this.uuidOwner = uuidOwner;
        this.timeOfDeletion = timeLeft + System.currentTimeMillis() / 1000;
    }

    public VeilingItem(int id, ItemStack itemStack, UUID uuidOwner, long timeLeft) {
        this.id = id;
        this.itemStack = itemStack;
        this.uuidOwner = uuidOwner;
        this.timeOfDeletion = timeLeft + System.currentTimeMillis() / 1000;
    }

    public boolean isBumped() {
        return bumped;
    }

    public void setBumped(boolean bumped) {
        this.bumped = bumped;
        this.timeOfDeletion = 20L + System.currentTimeMillis() / 1000;
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


    public long getTimeOfDeletion() {
        return timeOfDeletion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimeOfDeletion(long timeOfDeletion) {
        this.timeOfDeletion = timeOfDeletion;
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
