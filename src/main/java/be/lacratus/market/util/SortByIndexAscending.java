package be.lacratus.market.util;

import be.lacratus.market.objects.VeilingItem;

import java.util.Comparator;

public class SortByIndexAscending implements Comparator<VeilingItem> {


    @Override
    public int compare(VeilingItem veilingItem, VeilingItem t1) {
        if (veilingItem.getId() > t1.getId())
            return -1;
        else if (veilingItem.getId() < t1.getId())
            return 1;
        return 0;
    }
}
