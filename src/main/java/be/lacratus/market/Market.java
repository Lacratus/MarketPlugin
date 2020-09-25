package be.lacratus.market;

import be.lacratus.market.API.EconomyImplementer;
import be.lacratus.market.commands.MarketCommand;
import be.lacratus.market.commands.MoneyCommand;
import be.lacratus.market.data.StoredDataHandler;
import be.lacratus.market.listeners.AuctionListener;
import be.lacratus.market.listeners.OnDisconnectListener;
import be.lacratus.market.listeners.OnJoinListener;
import be.lacratus.market.objects.DDGSpeler;
import be.lacratus.market.objects.VeilingItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class Market extends JavaPlugin {

    //bank
    private final HashMap<UUID, Double> playerBank = new HashMap<>();

    //Database
    private static Connection connection;
    private String host;
    private String database;
    private String username;
    private String password;
    private int port;

    //Handlers
    private StoredDataHandler storedDataHandler;

    private Economy econ;
    private EconomyImplementer economyImplementer;

    //
    private PriorityQueue<VeilingItem> veilingItems;
    private HashMap<UUID, DDGSpeler> onlinePlayers;
    private HashMap<UUID, DDGSpeler> playersWithItems;
    private HashMap<UUID, DDGSpeler> playersWithBiddings;


    @Override
    public void onEnable() {
        //Commands
        getCommand("market").setExecutor(new MarketCommand(this));
        getCommand("money").setExecutor(new MoneyCommand(this));

        //Listeners
        Bukkit.getPluginManager().registerEvents(new OnJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new OnDisconnectListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AuctionListener(this), this);


        //Databank creation

        this.host = "localhost";
        this.port = 3306;
        this.database = "Market";
        this.username = "root";
        this.password = "";
        this.storedDataHandler = new StoredDataHandler(this);
        //Databank fill Auctionhouse and Hashmaps
        CompletableFuture.runAsync(storedDataHandler.fillAuctionHouseAndBank());

        //
        veilingItems = new PriorityQueue<>();
        veilingItems.comparator();
        onlinePlayers = new HashMap<>();
        playersWithItems = new HashMap<>();
        playersWithBiddings = new HashMap<>();


        //Register VaultEconomy
        economyImplementer = new EconomyImplementer(this);
        Bukkit.getServicesManager().register(Economy.class, economyImplementer, this, ServicePriority.Normal);


        if (!setupEconomy()) {
            System.out.println("No Vault Dependency Found");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        //Unregister VaultEconomy
        Bukkit.getServicesManager().unregister(Economy.class, economyImplementer);
    }

    public Connection openConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
            return connection;
        }
        System.out.println("Something went wrong when opening the connection");
        return null;
    }

    public PreparedStatement prepareStatement(String query) {

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            return ps;
        } catch (SQLException var3) {
            var3.printStackTrace();
        }

        return null;
    }


    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            System.out.println("1");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            System.out.println("2");
            return false;
        }
        Economy econ = rsp.getProvider();
        return econ != null;
    }

    public static Connection getConnection() {
        return connection;
    }

    public StoredDataHandler getStoredDataHandler() {
        return storedDataHandler;
    }

    public HashMap<UUID, Double> getPlayerBank() {
        return playerBank;
    }

    public PriorityQueue<VeilingItem> getVeilingItems() {
        return veilingItems;
    }

    public Economy getEcon() {
        return econ;
    }

    public EconomyImplementer getEconomyImplementer() {
        return economyImplementer;
    }

    public HashMap<UUID, DDGSpeler> getOnlinePlayers() {
        return onlinePlayers;
    }

    public HashMap<UUID, DDGSpeler> getPlayersWithItems() {
        return playersWithItems;
    }

    public static List<VeilingItem> priorityQueueToList(PriorityQueue<VeilingItem> priorityQueue) {
        return new ArrayList<>(priorityQueue);
    }

    public HashMap<UUID, DDGSpeler> getPlayersWithBiddings() {
        return playersWithBiddings;
    }

    public void runTaskGiveItem(VeilingItem veilingItem, DDGSpeler ddgSpeler, long timeLeft) {
        UUID uuid = ddgSpeler.getUuid();
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(this, () -> {
            if (this.getVeilingItems().contains(veilingItem)) {
                this.getVeilingItems().remove(veilingItem);
                ddgSpeler.getPersoonlijkeItems().remove(veilingItem);
                ItemMeta itemMeta = veilingItem.getItemStack().getItemMeta();
                itemMeta.setLore(null);
                veilingItem.getItemStack().setItemMeta(itemMeta);
                if (Bukkit.getPlayer(uuid) != null) {
                    Player ownerItem = Bukkit.getPlayer(uuid);
                    ownerItem.getInventory().setItem(ownerItem.getInventory().firstEmpty(), veilingItem.getItemStack());
                } else {
                    if (this.getPlayersWithBiddings().containsKey(uuid)) {
                        ddgSpeler.getBiddenItems().add(veilingItem);
                        this.getPlayersWithBiddings().put(uuid, ddgSpeler);
                    } else {
                        ddgSpeler.getBiddenItems().add(veilingItem);
                        this.getPlayersWithBiddings().put(uuid, ddgSpeler);
                    }
                }
                ddgSpeler.removebiddedItem(veilingItem);
                ddgSpeler.getRemoveItemsDatabase().add(veilingItem);
            }
        }, 20L * (timeLeft / 1000));
        veilingItem.setBukkitTask(bukkitTask);
    }
}
