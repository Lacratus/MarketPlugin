package be.lacratus.market.listeners;

import be.lacratus.market.Market;
import be.lacratus.market.objects.DDGSpeler;
import be.lacratus.market.objects.VeilingItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class OnJoinListener implements Listener {

    private Market main;

    public OnJoinListener(Market main) {
        this.main = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (main.getEconomyImplementer().createPlayerAccount(player)) {
            System.out.println("Account created");
        } else {
            System.out.println("Account already existed");
        }
        System.out.print("TEST?????");

        //Fill onlinePlayers
        DDGSpeler ddgSpeler = new DDGSpeler(player.getUniqueId());
        System.out.println(player.getUniqueId());
        main.getOnlinePlayers().put(player.getUniqueId(), ddgSpeler);

        //Check if player gets item back
        if (main.getPlayersWithBiddings().containsKey(uuid)) {
            for (VeilingItem item : main.getPlayersWithBiddings().get(uuid).getBiddenItems()) {
                long timeLeft = item.getTimeOfDeletion() - (System.currentTimeMillis() / 1000);
                if (timeLeft < 0) {
                    player.getInventory().setItem(player.getInventory().firstEmpty(), item.getItemStack());
                    main.getPlayersWithBiddings().get(uuid).removebiddedItem(item);
                }
            }
        }
    }
}
