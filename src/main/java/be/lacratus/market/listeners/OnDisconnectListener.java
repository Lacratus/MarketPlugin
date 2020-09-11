package be.lacratus.market.listeners;

import be.lacratus.market.Market;
import org.bukkit.event.Listener;

public class OnDisconnectListener implements Listener {

    private Market main;

    public OnDisconnectListener(Market main) {
        this.main = main;
    }
}
