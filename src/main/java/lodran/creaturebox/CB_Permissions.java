package lodran.creaturebox;

import java.util.HashMap;
import java.util.HashSet;

import ru.tehkode.permissions.bukkit.*;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.anjocaido.groupmanager.GroupManager;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

class CB_Permissions
{
  private boolean _operatorPermissions = true;
  private final CreatureboxPlugin _plugin;
  private Plugin _permissions = null;
  private boolean _permissionsIsSafe = true;
  private static HashMap<String, HashSet<String>> _permissionNodes = new HashMap<String, HashSet<String>>();
  private enum PermissionsHandler {
		PERMISSIONSEX, PERMISSIONS3, PERMISSIONS, GROUPMANAGER, BUKKIT
  }
  private static PermissionsHandler handler;
  
  public CB_Permissions(CreatureboxPlugin inPlugin)
  {
    this._plugin = inPlugin;
    this._permissionNodes = CB_Permissions.defaultPermissions();
  }
  
  public boolean getOperatorPermissions()
  {
    return _operatorPermissions;
  }
  
  public void setOperatorPermissions(boolean inValue)
  {
    _operatorPermissions = inValue;
  }
  
  public boolean has(CommandSender inSender, String inCPermission, boolean inReport)
  {
	String inPermission = inCPermission.toLowerCase();
    String theGroup = ((inSender instanceof Player) ? 
                       (inSender.isOp() ? "Operator" : "Player") :
                       "Console");
    
    
    boolean thePermission = _permissionNodes.get(theGroup).contains(inPermission);
    
    if (inSender instanceof Player)
    {
      Player thePlayer = (Player) inSender;
      
      if (handler != null)
      {
        try
        {
    		switch (handler) {
    			case PERMISSIONSEX:
    				thePermission = ((PermissionsEx) this._permissions).getPermissionManager().has(thePlayer, inPermission);
    				break;
    			case PERMISSIONS3:
    				thePermission = ((Permissions) this._permissions).getHandler().has(thePlayer, inPermission);
    				break;
    			case PERMISSIONS:
    				thePermission = ((Permissions) this._permissions).getHandler().has(thePlayer, inPermission);
    				break;
    			case GROUPMANAGER:
    				thePermission = ((GroupManager) this._permissions).getWorldsHolder().getWorldPermissions(thePlayer).has(thePlayer, inPermission);
    				break;
    			case BUKKIT:
    				thePermission = thePlayer.hasPermission(inPermission);
    				break;
    			default:
    				
    		}
          
          if ((thePermission == false) && (inReport))
          {
            thePlayer.sendMessage(ChatColor.RED + "creaturebox: You don't have permission " + inPermission);
          }
          
          return thePermission;
        }
        catch (Exception inException)
        {
          this.pluginIsUnsafe(inSender);
        }
      }
    }
    
    if ((thePermission == false) && (inReport))
    {
      inSender.sendMessage(ChatColor.RED + "creaturebox: You are not an operator");
    }
    
    return thePermission;
  }

  public void loadPermissionsPlugin()
  {
	  Plugin permissionsEx = this._plugin.getServer().getPluginManager().getPlugin("PermissionsEx");
	  Plugin groupManager = this._plugin.getServer().getPluginManager().getPlugin("GroupManager");
	  Plugin permissions = this._plugin.getServer().getPluginManager().getPlugin("Permissions");

	  if (permissionsEx != null) {
		  this._permissions = permissionsEx;
		  handler = PermissionsHandler.PERMISSIONSEX;
		  String version = permissionsEx.getDescription().getVersion();
		  System.out.println("creaturebox: Permissions enabled using: PermissionsEx v" + version);
	  } else if (groupManager != null) {
		  this._permissions = groupManager;
		  handler = PermissionsHandler.GROUPMANAGER;
		  String version = groupManager.getDescription().getVersion();
		  System.out.println("creaturebox: Permissions enabled using: GroupManager v" + version);
	  } else if (permissions != null) {
		  this._permissions = permissions;
		  String version = permissions.getDescription().getVersion();
		  if(version.contains("3.")) {
			  // This shouldn't make any difference according to the Permissions API
			  handler = PermissionsHandler.PERMISSIONS3;
		  } else {
			  handler = PermissionsHandler.PERMISSIONS;
		  }
		  System.out.println("creaturebox: Permissions enabled using: Permissions v" + version);
	  } else {
		  handler = PermissionsHandler.BUKKIT;
		  System.out.println("creaturebox: Permissions enabled using: Bukkit Permissions");
		  this._permissions = null;
	  }
  }

  
  private void pluginIsUnsafe(CommandSender inSender)
  {
    //this._permissionsIsSafe = false;
    //this._permissions = null;
    
    System.out.println("creaturebox: permissions plugin threw an exception, and is not working properly.");
    //System.out.println("creaturebox: reverting to using isOp for access settings.");
    
    CreatureboxPlugin.message(inSender, CreatureboxPlugin.messageError, "permissions plugin threw an exception, and is not working properly.");
    DebuggerPlugin.notify(DebuggerPlugin.priorityError, "permissions plugin threw an exception, and is not working properly.");
  }
  
  private static HashMap<String, HashSet<String>> defaultPermissions()
  {
    HashMap<String, HashSet<String>> theResult = new HashMap<String, HashSet<String>>();
    
    String[] theCreaturePermissions = {
      "creaturebox.creature",
      "creaturebox.creature.pig",
      "creaturebox.creature.chicken",
      "creaturebox.creature.cow",
      "creaturebox.creature.sheep",
      "creaturebox.creature.squid",
      "creaturebox.creature.creeper",
      "creaturebox.creature.ghast",
      "creaturebox.creature.pig_zombie",
      "creaturebox.creature.skeleton",
      "creaturebox.creature.spider",
      "creaturebox.creature.zombie",
      "creaturebox.creature.slime",
      "creaturebox.creature.giant",
      "creaturebox.creature.wolf",
      "creaturebox.creature.cave_spider",
      "creaturebox.creature.enderman",
      "creaturebox.creature.silverfish"
    };
    
    String[] theConsolePermissions = {
      "creaturebox.give"
    };
    
    String[] theOperatorPermissions = {
      "creaturebox.give",
      "creaturebox.set",
      "creaturebox.period",
      "creaturebox.count",
      "creaturebox.limit",
      "creaturebox.requires",
      "creaturebox.dropspawner",
      "creaturebox.placespawner"
    };
    
    String[] thePlayerPermissions = {
      "creaturebox.placespawner"
    };
    
    HashSet<String> thePermissions;
    
    thePermissions = new HashSet<String>();
    for (String thePermission : theConsolePermissions)
    {
      thePermissions.add(thePermission);
    }
    for (String thePermission : theCreaturePermissions)
    {
      thePermissions.add(thePermission);
    }
    
    theResult.put("Console", thePermissions);
    
    thePermissions = new HashSet<String>();
    for (String thePermission : theOperatorPermissions)
    {
      thePermissions.add(thePermission);
    }
    for (String thePermission : theCreaturePermissions)
    {
      thePermissions.add(thePermission);
    }
    
    theResult.put("Operator", thePermissions);
    
    thePermissions = new HashSet<String>();
    for (String thePermission : thePlayerPermissions)
    {
      thePermissions.add(thePermission);
    }
    for (String thePermission : theCreaturePermissions)
    {
      thePermissions.add(thePermission);
    }
    
    theResult.put("Player", thePermissions);
    
    return theResult;
  }
}