package be.lacratus.market.util;

import be.lacratus.market.Market;
import be.lacratus.market.objects.VeilingItem;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class PageUtil {

    public static List<VeilingItem> getPageItems(PriorityQueue<VeilingItem> items, int page, int spaces) {
        int upperBound = page * spaces;
        int lowerBound = upperBound - spaces;

        List<VeilingItem> newItems = new ArrayList<>();
        for (int i = lowerBound; i < upperBound; i++) {

            try {
                List<VeilingItem> itemsList = Market.priorityQueueToList(items);
                newItems.add(itemsList.get(i));
            } catch (IndexOutOfBoundsException ignored) {
            }
        }

        return newItems;
    }

    public static boolean isPageValid(PriorityQueue<VeilingItem> items, int page, int spaces) {
        {
            if (page <= 0) {
                return false;
            }

            int upperBound = page * spaces;
            int lowerBound = upperBound - spaces;

            return items.size() > lowerBound;
        }

    }


}
