package be.lacratus.market;

import be.lacratus.market.API.EconomyImplementer;
import be.lacratus.market.commands.MarketCommand;
import be.lacratus.market.commands.MoneyCommand;
import be.lacratus.market.data.StoredDataHandler;
import be.lacratus.market.listeners.AuctionListener;
import be.lacratus.market.listeners.OnDisconnectListener;
import be.lacratus.market.listeners.OnJoinListener;
import be.lacratus.market.objects.AuctionItem;
import be.lacratus.market.objects.DDGPlayer;
import be.lacratus.market.util.SortByIndexAscending;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.DriverManager;
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

    private int maxIndex;
    //Handlers
    private StoredDataHandler storedDataHandler;

    private EconomyImplementer economyImplementer;

    //
    private PriorityQueue<AuctionItem> auctionItems;
    private Map<UUID, DDGPlayer> onlinePlayers;
    private Map<UUID, DDGPlayer> playersWithItems;
    private Map<UUID, DDGPlayer> playersWithBiddings;
    private List<AuctionItem> itemsRemoveDatabase;

    @Override
    public void onEnable() {
        //Config - Databank creation
        this.getConfig().options().copyDefaults();
        saveDefaultConfig();
        this.host = this.getConfig().getString("Host");
        this.port = this.getConfig().getInt("Port");
        this.database = this.getConfig().getString("Database");
        this.username = this.getConfig().getString("Username");
        this.password = this.getConfig().getString("Password");

        this.storedDataHandler = new StoredDataHandler(this);

        //Intialize lists/maps
        //utils
        this.auctionItems = new PriorityQueue<>(1, new SortByIndexAscending());
        this.auctionItems.comparator();
        this.onlinePlayers = new HashMap<>();
        this.playersWithItems = new HashMap<>();
        this.playersWithBiddings = new HashMap<>();
        this.itemsRemoveDatabase = new ArrayList<>();

        //Commands
        getCommand("market").setExecutor(new MarketCommand(this));
        getCommand("money").setExecutor(new MoneyCommand(this));

        //Listeners
        Bukkit.getPluginManager().registerEvents(new OnJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new OnDisconnectListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AuctionListener(this), this);


        //get highest index


        //Databank fill Auctionhouse and Hashmaps
        CompletableFuture.runAsync(storedDataHandler.fillAuctionHouseAndBank());


        //Save auctionhouse to databank every ... minutes
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, storedDataHandler.saveAuctionHouseAndBalance(), 20L * 15, 20L * 15);


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
        storedDataHandler.saveAuctionHouseAndBalance();
    }

    public Connection openConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
            return connection;
        }
        System.out.println("Something went wrong when opening the connection");
        return null;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        Economy econ = rsp.getProvider();
        return econ != null;
    }

    public StoredDataHandler getStoredDataHandler() {
        return storedDataHandler;
    }

    public HashMap<UUID, Double> getPlayerBank() {
        return playerBank;
    }

    public PriorityQueue<AuctionItem> getAuctionItems() {
        return auctionItems;
    }


    public EconomyImplementer getEconomyImplementer() {
        return economyImplementer;
    }

    public Map<UUID, DDGPlayer> getOnlinePlayers() {
        return onlinePlayers;
    }

    public Map<UUID, DDGPlayer> getPlayersWithItems() {
        return playersWithItems;
    }

    public static List<AuctionItem> priorityQueueToList(PriorityQueue<AuctionItem> priorityQueue) {
        return new ArrayList<>(priorityQueue);
    }

    public List<AuctionItem> getItemsRemoveDatabase() {
        return itemsRemoveDatabase;
    }

    public Map<UUID, DDGPlayer> getPlayersWithBiddings() {
        return playersWithBiddings;
    }

    public int getMaxIndex() {
        return maxIndex;
    }

    public void setMaxindex(int maxIndex) {
        this.maxIndex = maxIndex;
    }

    //Used for bringing/returning items
    public void runTaskGiveItem(AuctionItem auctionItem, DDGPlayer ddgPlayer, long timeLeft) {
        UUID uuid = ddgPlayer.getUuid();
        Player ownerItem = Bukkit.getPlayer(uuid);
        DDGPlayer oldOwner = getPlayersWithItems().get(auctionItem.getUuidOwner());
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLater(this, () -> {

            if (getAuctionItems().contains(auctionItem)) {
                getAuctionItems().remove(auctionItem);
                oldOwner.getPersonalItems().remove(auctionItem);
            }

            if (Bukkit.getPlayer(uuid) != null) {
                if (ownerItem.getInventory().firstEmpty() != -1) {
                    ownerItem.getInventory().setItem(ownerItem.getInventory().firstEmpty(), auctionItem.getItemStack());
                    getItemsRemoveDatabase().add(auctionItem);
                } else {
                    runTaskGiveItem(auctionItem, ddgPlayer, 30);
                    Bukkit.getPlayer(uuid).sendMessage("Your inventor is full, We are trying again in 30 seconds");
                }
            } else {
                ddgPlayer.getBiddenItems().add(auctionItem);
            }
            ddgPlayer.getBiddenItems().remove(auctionItem);

            //update of lists
            updateLists(ddgPlayer);
            if (oldOwner != null && ddgPlayer.getUuid() != oldOwner.getUuid()) {
                updateLists(oldOwner);
                //Money goes to old owner
                getEconomyImplementer().depositPlayer(Bukkit.getOfflinePlayer(oldOwner.getUuid()), auctionItem.getHighestOffer());
            }

        }, 20L * (timeLeft));
        auctionItem.setBukkitTask(bukkitTask);
    }

    public void updateLists(DDGPlayer ddgPlayer) {
        UUID uuid = ddgPlayer.getUuid();
        if (getOnlinePlayers().containsKey(ddgPlayer.getUuid())) {
            getOnlinePlayers().put(uuid, ddgPlayer);
        }
        if (ddgPlayer.getBiddenItems().size() != 0) {
            getPlayersWithBiddings().put(uuid, ddgPlayer);
        } else getPlayersWithBiddings().remove(uuid);

        if (ddgPlayer.getPersonalItems().size() != 0) {
            getPlayersWithItems().put(uuid, ddgPlayer);
        } else getPlayersWithItems().remove(uuid);


    }


}
