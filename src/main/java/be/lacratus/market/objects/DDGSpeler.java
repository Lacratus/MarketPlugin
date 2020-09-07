package be.lacratus.market.objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.PriorityQueue;
import java.util.UUID;

public class DDGSpeler {

    private Player player;
    private String uuid;
    private PriorityQueue<VeilingItem> persoonlijkeItems;

    public DDGSpeler(String uuid) {
        this.player = Bukkit.getPlayer(UUID.fromString(uuid));
        this.uuid = uuid;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getUuid() {
        return uuid;
    }

    public PriorityQueue<VeilingItem> getPersoonlijkeItems() {
        return persoonlijkeItems;
    }

    public void voegPersoonlijkItemToe(VeilingItem veilingItem){
        this.persoonlijkeItems.add(veilingItem);
    }

    public void bumpItem(VeilingItem veilingItem){
        if(!veilingItem.isBumped()) {
            this.persoonlijkeItems.remove(veilingItem);
            this.persoonlijkeItems.add(veilingItem);
            veilingItem.setBumped(true);
        }
        player.sendMessage("You have already bumped this item once");
    }
}
