package be.lacratus.market.util;

import be.lacratus.market.objects.AuctionItem;

import java.util.Comparator;

public class SortByIndexDescending implements Comparator<AuctionItem> {


    @Override
    public int compare(AuctionItem auctionItem, AuctionItem t1) {
        if (auctionItem.getId() > t1.getId())
            return 1;
        else if (auctionItem.getId() < t1.getId())
            return -1;
        return 0;
    }
}
