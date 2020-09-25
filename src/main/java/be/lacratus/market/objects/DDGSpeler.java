package be.lacratus.market.objects;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;

public class DDGSpeler {

    private Player player;
    private UUID uuid;
    private PriorityQueue<VeilingItem> persoonlijkeItems;
    private boolean isBidding;
    private VeilingItem bidVeilingItem;
    private List<VeilingItem> biddenItems;
    private List<VeilingItem> removeItemsDatabase;

    public DDGSpeler(UUID uuid) {
        this.uuid = uuid;
        this.persoonlijkeItems = new PriorityQueue<>();
        this.biddenItems = new ArrayList<>();
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public UUID getUuid() {
        return uuid;
    }

    public PriorityQueue<VeilingItem> getPersoonlijkeItems() {
        return persoonlijkeItems;
    }

    public boolean isBidding() {
        return isBidding;
    }

    public void setBidding(boolean bidding) {
        isBidding = bidding;
    }

    public VeilingItem getBidVeilingItem() {
        return bidVeilingItem;
    }

    public void setBidVeilingItem(VeilingItem bidVeilingItem) {
        this.bidVeilingItem = bidVeilingItem;
    }

    public List<VeilingItem> getBiddenItems() {
        return biddenItems;
    }

    public void addBiddeditem(VeilingItem veilingItem) {
        this.biddenItems.add(veilingItem);
    }

    public void removebiddedItem(VeilingItem veilingItem) {
        this.biddenItems.remove(veilingItem);
    }

    public List<VeilingItem> getRemoveItemsDatabase() {
        return removeItemsDatabase;
    }
}
