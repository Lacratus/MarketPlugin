package be.lacratus.market.util;

import be.lacratus.market.objects.VeilingItem;

import java.util.Comparator;

public class VeilingItemComparator implements Comparator<VeilingItem> {


    @Override
    public int compare(VeilingItem veilingItem, VeilingItem t1) {
        if (veilingItem.getTimeOfDeletion() > t1.getTimeOfDeletion())
            return -1;
        else if (veilingItem.getTimeOfDeletion() < t1.getTimeOfDeletion())
            return 1;
        return 0;
    }
}
