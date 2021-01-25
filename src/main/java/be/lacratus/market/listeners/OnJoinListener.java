package be.lacratus.market.listeners;

import be.lacratus.market.Market;
import be.lacratus.market.data.StoredDataHandler;
import be.lacratus.market.objects.DDGPlayer;
import be.lacratus.market.objects.AuctionItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OnJoinListener implements Listener {

    private Market main;
    private StoredDataHandler storedDataHandler;

    public OnJoinListener(Market main) {
        this.main = main;
        this.storedDataHandler = main.getStoredDataHandler();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (main.getEconomyImplementer().createPlayerAccount(player)) {
            System.out.println("Account created");
        } else {
            System.out.println("Account already existed");
        }

        //Fill onlinePlayers

        //Check if player gets item back
        DDGPlayer ddgPlayer;
        List<AuctionItem> auctionItems;
        if (main.getPlayersWithItems().containsKey(uuid)) {
            ddgPlayer = main.getPlayersWithItems().get(uuid);
            auctionItems = new ArrayList<>(main.getPlayersWithItems().get(uuid).getBiddenItems());
            for (AuctionItem item : auctionItems) {
                long timeLeft = item.getTimeOfDeletion() - (System.currentTimeMillis() / 1000);
                if (timeLeft < 0) {
                    player.getInventory().setItem(player.getInventory().firstEmpty(), item.getItemStack());
                    main.getPlayersWithBiddings().get(uuid).getBiddenItems().remove(item);
                    main.getItemsRemoveDatabase().add(item);
                }
            }
            main.getOnlinePlayers().put(uuid, ddgPlayer);
            main.updateLists(ddgPlayer);

        } else if (main.getPlayersWithBiddings().containsKey(uuid)) {
            ddgPlayer = main.getPlayersWithBiddings().get(uuid);
            auctionItems = new ArrayList<>(main.getPlayersWithBiddings().get(uuid).getBiddenItems());
            for (AuctionItem item : auctionItems) {
                long timeLeft = item.getTimeOfDeletion() - (System.currentTimeMillis() / 1000);
                if (timeLeft < 0) {
                    player.sendMessage("You are getting a item from the auction, get some inventory space");
                    main.giveItemWhenInventoryFull(item, ddgPlayer, 30);
                    main.getPlayersWithBiddings().get(uuid).getBiddenItems().remove(item);
                }
            }
            main.getOnlinePlayers().put(uuid, ddgPlayer);
            main.updateLists(ddgPlayer);
        } else {
            storedDataHandler.loadData(uuid).thenAccept(DDGspeler ->
                    main.getOnlinePlayers().put(uuid, DDGspeler)).exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            });
        }
    }
}
