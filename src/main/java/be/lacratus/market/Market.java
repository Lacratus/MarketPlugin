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
import be.lacratus.market.util.VeilingItemComparator;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
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

    private Economy econ;
    private EconomyImplementer economyImplementer;

    //
    private PriorityQueue<VeilingItem> veilingItems;
    private Map<UUID, DDGSpeler> onlinePlayers;
    private Map<UUID, DDGSpeler> playersWithItems;
    private Map<UUID, DDGSpeler> playersWithBiddings;
    private List<VeilingItem> itemsRemoveDatabase;

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
        this.veilingItems = new PriorityQueue<>(1, new VeilingItemComparator());
        this.veilingItems.comparator();
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

        //reset Itemindex in database
        storedDataHandler.resetItemIndex();

        //Databank fill Auctionhouse and Hashmaps
        CompletableFuture.runAsync(storedDataHandler.fillAuctionHouseAndBank());

        //get highest index
        try {
            storedDataHandler.getMaxIndex().thenAccept(this::setMaxindex);
        } catch (SQLException e) {
            e.printStackTrace();
        }

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

    public StoredDataHandler getStoredDataHandler() {
        return storedDataHandler;
    }

    public HashMap<UUID, Double> getPlayerBank() {
        return playerBank;
    }

    public PriorityQueue<VeilingItem> getVeilingItems() {
        return veilingItems;
    }




    public EconomyImplementer getEconomyImplementer() {
        return economyImplementer;
    }

    public Map<UUID, DDGSpeler> getOnlinePlayers() {
        return onlinePlayers;
    }

    public Map<UUID, DDGSpeler> getPlayersWithItems() {
        return playersWithItems;
    }

    public static List<VeilingItem> priorityQueueToList(PriorityQueue<VeilingItem> priorityQueue) {
        return new ArrayList<>(priorityQueue);
    }

    public List<VeilingItem> getItemsRemoveDatabase() {
        return itemsRemoveDatabase;
    }

    public Map<UUID, DDGSpeler> getPlayersWithBiddings() {
        return playersWithBiddings;
    }

    public int getMaxIndex() {
        return maxIndex;
    }

    public void setMaxindex(int maxIndex) {
        this.maxIndex = maxIndex;
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
                    this.getItemsRemoveDatabase().add(veilingItem);
                    System.out.println(itemsRemoveDatabase);
                } else {
                    ddgSpeler.getBiddenItems().add(veilingItem);
                }
                ddgSpeler.removebiddedItem(veilingItem);
                if (getOnlinePlayers().containsValue(ddgSpeler)) {
                    updateLists(ddgSpeler);
                }
            }
        }, 20L * (timeLeft));
        veilingItem.setBukkitTask(bukkitTask);
    }

    public void updateLists(DDGSpeler ddgSpeler) {
        UUID uuid = ddgSpeler.getUuid();
        this.getOnlinePlayers().put(uuid, ddgSpeler);
        if (this.getPlayersWithBiddings().containsKey(uuid)) {
            if (ddgSpeler.getBiddenItems().size() != 0) {
                this.getPlayersWithBiddings().put(uuid, ddgSpeler);
            } else {
                this.getPlayersWithBiddings().remove(uuid);
            }
        }

        if (this.getPlayersWithItems().containsKey(uuid)) {
            if (ddgSpeler.getPersoonlijkeItems().size() != 0) {
                this.getPlayersWithItems().put(uuid, ddgSpeler);
            } else {
                this.getPlayersWithItems().remove(uuid);
            }
        }
    }


}
