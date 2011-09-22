package lodran.creaturebox;

import java.util.HashMap;
import java.util.HashSet;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

class CB_Permissions
{
  private boolean _operatorPermissions = true;
  
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
  
  public boolean has(CommandSender inSender, String inPermission, boolean inReport)
  {
    this.loadPermissionsPlugin();
    
    String theGroup = ((inSender instanceof Player) ? 
                       (inSender.isOp() ? "Operator" : "Player") :
                       "Console");
    
    boolean thePermission = _permissionNodes.get(theGroup).contains(inPermission);
    
    if (inSender instanceof Player)
    {
      Player thePlayer = (Player) inSender;
      
      if (this._permissions != null)
      {
        try
        {
          thePermission = this._permissions.has(thePlayer, inPermission);
          
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
  
  public String getGroup(CommandSender inSender)
  {
    this.loadPermissionsPlugin();
    
    String theGroup = ((inSender instanceof Player) ? 
                       (inSender.isOp() ? "Operator" : "Player") :
                       "Console");
    
    if ((_operatorPermissions == false) && (inSender instanceof Player))
      theGroup = "Operator";
    
    try
    {
      if (this._permissions != null)
      {
        if (inSender instanceof Player)
        {
          String[] allGroups = _permissions.getGroups(((Player)inSender).getWorld().getName(), ((Player)inSender).getName());
          for(String grpname : allGroups)
          {
            theGroup += grpname + ", ";
          }
        }
      }
    }
    catch (Exception inException)
    {
      this.pluginIsUnsafe(inSender);
    }
    
    return theGroup;
  }
  
  private void loadPermissionsPlugin()
  {
    if (this._permissionsIsSafe)
    {
      if (this._permissions == null)
      {
        Plugin thePermissionsPlugin = this._plugin.getServer().getPluginManager().getPlugin("Permissions");
        if (thePermissionsPlugin != null)
        {
          if (this._plugin.getServer().getPluginManager().isPluginEnabled(thePermissionsPlugin))
          {
            this._permissions = ((Permissions) thePermissionsPlugin).getHandler();
            System.out.println( "creaturebox: using permissions plugin." );
          }
        }
      }
    }
  }
  
  private void pluginIsUnsafe(CommandSender inSender)
  {
    this._permissionsIsSafe = false;
    this._permissions = null;
    
    System.out.println("creaturebox: permissions plugin threw an exception, and is not working properly.");
    System.out.println("creaturebox: reverting to using isOp for access settings.");
    
    CreatureboxPlugin.message(inSender, CreatureboxPlugin.messageError, "permissions plugin threw an exception, and is not working properly.");
    DebuggerPlugin.notify(DebuggerPlugin.priorityError, "permissions plugin threw an exception, and is not working properly.");
  }
  
  private final CreatureboxPlugin _plugin;
  private PermissionHandler _permissions = null;
  private boolean _permissionsIsSafe = true;
  private static HashMap<String, HashSet<String>> _permissionNodes = new HashMap<String, HashSet<String>>();
  
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