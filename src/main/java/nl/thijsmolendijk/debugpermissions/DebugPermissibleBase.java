package nl.thijsmolendijk.debugpermissions;

import nl.thijsmolendijk.debugpermissions.reflect.ReflectionExecutor.ReflectionObject;

import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.ServerOperator;

/**
 * Custom PermissibleBase that notifies the administrator when a call to hasPermission has been made.
 * Useful for debugging plugins that don't state their permissions, but do check for them.
 * @author molenzwiebel
 */
public class DebugPermissibleBase extends PermissibleBase {

    /**
     * The default constructor
     * @param old The old PermissibleBase, used for copying over old permissions to the new instance
     * @param opable The ServerOperator that the default constructor takes in
     */
    public DebugPermissibleBase(Object old, ServerOperator opable) {
        super(opable);
        
        //ReflectionObject for this instance
        ReflectionObject thiz = new ReflectionObject(this);
        //ReflectionObject for the old instance
        ReflectionObject that = new ReflectionObject(old);
        
        //Copy over required fields
        thiz.set("attachments", that.get("attachments"));
        thiz.set("permissions", that.get("permissions"));
    }

    /**
     * Checks whether the user has the required permission, logging the permission call when needed.
     * @param permission The permission to check for
     */
    @Override
    public boolean hasPermission(String permission) {
        if (!super.hasPermission(DebugPermissions.config.getString("permission"))) return super.hasPermission(permission);
        if (matchesIgnoreList(permission)) return super.hasPermission(permission);
        
        boolean result = super.hasPermission(permission);
        if (DebugPermissions.config.getBoolean("logWhenTrue") == false && result == true) return result;
        DebugPermissions.logType.log(permission, result, new ReflectionObject(this).getAsRO("opable").invoke("getName").fetchAs(String.class));
        
        return result;
    }
    
    /**
     * Delegates the call from {@link #hasPermission(Permission)} over to {@link #hasPermission(String)}
     */
    @Override
    public boolean hasPermission(Permission permission) {
        return hasPermission(permission.getName());
    }
    
    /**
     * Checks if the specified permission is on the ignore list, accounting for *
     * @param permission The permission
     * @return If the permission was on the ignore list
     * @author <a href="https://github.com/CoreNetwork/PumpkinChallenge/blob/master/src/java/us/corenetwork/pumpkinchallenge/Util.java#35">matejdro</a>
     */
    private boolean matchesIgnoreList(String permission) {
        while (true) {
            if (DebugPermissions.config.getStringList("ignoredPermissions").contains(permission))
                return true;

            if (permission.length() < 2)
                return false;

            if (permission.endsWith("*"))
                permission = permission.substring(0, permission.length() - 2);

            int lastIndex = permission.lastIndexOf(".");
            if (lastIndex < 0)
                return false;

            permission = permission.substring(0, lastIndex).concat(".*");  
        }
    }
}