package be.lacratus.market.util;

import be.lacratus.market.objects.AuctionItem;

import java.util.Comparator;

public class SortByTimeOfDeletion implements Comparator<AuctionItem> {


    @Override
    public int compare(AuctionItem auctionItem, AuctionItem t1) {
        if (auctionItem.getTimeOfDeletion() > t1.getTimeOfDeletion())
            return -1;
        else if (auctionItem.getTimeOfDeletion() < t1.getTimeOfDeletion())
            return 1;
        return 0;
    }
}
