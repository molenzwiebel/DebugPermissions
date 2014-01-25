package nl.thijsmolendijk.debugpermissions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import nl.thijsmolendijk.debugpermissions.reflect.ReflectionExecutor.ReflectionObject;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for DebugPermissions
 * @author molenzwiebel
 */
public class DebugPermissions extends JavaPlugin implements Listener {
    /** The config */
    public static FileConfiguration config;
    
    /** The logging type */
    public static LogType logType;

    /** The log file, or null when logging to console */
    public static PrintWriter logFile;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        config = this.getConfig();

        logType = LogType.match(config.getString("logType"));

        if (logType == LogType.FILE) {
            try {
                logFile = new PrintWriter(new BufferedWriter(new FileWriter(new File(getDataFolder(), "calls.log"), true)));
            } catch (Exception e) {
                this.getLogger().severe("Could not open file stream!");
                this.setEnabled(false);
            }
        }
        
        if (this.isEnabled()) Bukkit.getPluginManager().registerEvents(this, this);
        
        this.getLogger().info("DebugPermissions has been enabled with log type "+config.getString("logType"));
        this.getLogger().info("Ignored permissions: ");
        for (String perm : this.getConfig().getStringList("ignoredPermissions"))
            this.getLogger().info(perm);
        
        for (Player pl : Bukkit.getOnlinePlayers())
            injectUser(pl);
    }

    @Override
    public void onDisable() {
        try {
            if (logFile != null) logFile.close();
        } catch (Exception e) {
            this.getLogger().severe("Could not close file stream!");
        }
    }
    
    /**
     * Called when a user joins the server, used for setting their PermissibleBase
     * @param event The PlayerJoinEvent
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        injectUser(event.getPlayer());
    }
    
    /**
     * Sets the users permission handler to a custom one, used for logging
     * @param p The player
     */
    private void injectUser(Player p) {
        ReflectionObject obj = new ReflectionObject(p);
        obj.set("perm", new DebugPermissibleBase(obj.get("perm"), p));
    }
}
