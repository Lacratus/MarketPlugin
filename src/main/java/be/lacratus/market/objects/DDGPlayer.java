package be.lacratus.market.objects;


import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;

public class DDGPlayer {

    private UUID uuid;
    private PriorityQueue<AuctionItem> personalItems;
    private boolean isBidding;
    private AuctionItem bidAuctionItem;
    private List<AuctionItem> biddenItems;

    public DDGPlayer(UUID uuid) {
        this.uuid = uuid;
        this.personalItems = new PriorityQueue<>();
        this.biddenItems = new ArrayList<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public PriorityQueue<AuctionItem> getPersonalItems() {
        return personalItems;
    }


    public boolean isBidding() {
        return isBidding;
    }

    public void setBidding(boolean bidding) {
        isBidding = bidding;
    }

    public AuctionItem getBidAuctionItem() {
        return bidAuctionItem;
    }

    public void setBidAuctionItem(AuctionItem bidAuctionItem) {
        this.bidAuctionItem = bidAuctionItem;
    }

    public List<AuctionItem> getBiddenItems() {
        return biddenItems;
    }


}
