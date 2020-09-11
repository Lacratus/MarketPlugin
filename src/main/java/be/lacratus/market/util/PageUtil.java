package be.lacratus.market.util;

import be.lacratus.market.objects.VeilingItem;

import java.util.ArrayList;
import java.util.List;

public class PageUtil {

    public static List<VeilingItem> getPageItems(List<VeilingItem> items, int page, int spaces) {
        int upperBound = page * spaces;
        int lowerBound = upperBound - spaces;

        List<VeilingItem> newItems = new ArrayList<>();
        for (int i = lowerBound; i < upperBound; i++) {

            try {
                newItems.add(items.get(i));
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        return newItems;
    }

    public static boolean isPageValid(List<VeilingItem> items, int page, int spaces) {
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
