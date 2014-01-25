package nl.thijsmolendijk.debugpermissions;

import java.util.Date;

import org.bukkit.Bukkit;

public enum LogType {
    CONSOLE {
        @Override
        public void log(String permission, boolean result, String player) {
            Bukkit.getLogger().info(
                    String.format("Call to hasPermission, %s, returned %s for player %s", permission, String.valueOf(result), player));
        }
    }, FILE {
        @Override
        public void log(String permission, boolean result, String player) {
            DebugPermissions.logFile.println(
                    String.format("%s: Call to hasPermission, %s, returned %s for player %s", new Date().toString(), permission, String.valueOf(result), player));
        }
    };
    
    /**
     * Logs the specified lookup to the type
     * @param permission The permission that was looked up
     * @param result The result of the permission
     * @param player The player 
     */
    public abstract void log(String permission, boolean result, String player);
    
    public static LogType match(String value) {
        for (LogType v : values())
            if (v.name().equalsIgnoreCase(value)) return v;
        throw new IllegalArgumentException("Log type "+value+" not found!");
    }
}
