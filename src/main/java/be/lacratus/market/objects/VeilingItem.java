package be.lacratus.market.objects;

import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

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

    public VeilingItem(int id, ItemStack itemStack, UUID uuidOwner, long timeLeft) {
        this.id = id;
        this.itemStack = itemStack;
        this.uuidOwner = uuidOwner;
        this.timeOfDeletion = timeLeft + System.currentTimeMillis() / 1000;
        this.bumped = false;
    }

    public boolean isBumped() {
        return bumped;
    }

    public void setBumped(boolean bumped) {
        this.bumped = bumped;
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


    @Override
    public int compareTo(VeilingItem veilingItem) {
        if (id > veilingItem.id)
            return 1;
        else if (id < veilingItem.id)
            return -1;
        return 0;
    }


}
