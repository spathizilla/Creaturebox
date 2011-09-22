package lodran.creaturebox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.lang.reflect.Field;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;
import net.minecraft.server.*;

public class CreatureboxPlugin extends DebuggerPlugin implements Runnable
{  
  public final static int messageAlways = 0;
  public final static int messageError = 1;
  public final static int messageInfo = 2;
  public final static int messageNoise = 3;

  private static int _messagePriority = messageNoise;
  
  private boolean _enableRedstone = true;
  private static boolean _spawnControl = true;

  private final CB_Permissions _permissions = new CB_Permissions(this);
  private final CB_BlockListener _blockListener = new CB_BlockListener(this);
  private final CB_PlayerListener _playerListener = new CB_PlayerListener(this);
  private final CB_EntityListener _entityListener = new CB_EntityListener(this);

  private HashMap<CB_Location, CB_Spawner> _spawnersByLocation = new HashMap<CB_Location, CB_Spawner>();
  private boolean _hasLoadedSpawners = false;
  private boolean _hasDirtySpawner = false;
  
  // private HashMap<CB_Creature, CB_Spawner> _spawnersByCreature = new HashMap<CB_Creature, CB_Spawner>();
  
  private short _lastDurability;
  
  private static Random __random = new Random();
  
  /******************************************************************************/
  
  @Override public void onEnable()
  {    
    super.onEnable();
    
    _blockListener.onEnable();
    _playerListener.onEnable();
    _entityListener.onEnable();
    
   
    this.getServer().getScheduler().scheduleSyncRepeatingTask(this,
                                                              this,
                                                              20,
                                                              20);
    
    this.makeSpawnersFile();
    this.blockStacking();
    
    PluginDescriptionFile theDescription = this.getDescription();
    DebuggerPlugin.pluginName = theDescription.getName() + " " + theDescription.getVersion();
    
    System.out.println( theDescription.getName() + " version " + theDescription.getVersion() + " is enabled!" );
  }
  
  /******************************************************************************/
  
  @Override public void onDisable()
  {
    super.onDisable();
    
    this.getServer().getScheduler().cancelTasks(this);
        
    this.saveSpawners();
        
    PluginDescriptionFile theDescription = this.getDescription();
    
    System.out.println( theDescription.getName() + " version " + theDescription.getVersion() + " is disabled." );
  }
  
  /******************************************************************************/
  
  @Override public boolean onCommand(CommandSender inSender,
                           Command inCommand,
                           String inCommandLabel,
                           String[] inArguments)
  {
    if (super.onCommand(inSender,
                        inCommand,
                        inCommandLabel,
                        inArguments) == true)
    {
      return true;
    }

    if (inArguments.length < 1)
    {
      this.showUsage(inSender);
      return false;
    }
    
    if (inArguments[0].equalsIgnoreCase("set"))
      return this.onSetCommand(inSender,
                               inCommand,
                               inCommandLabel,
                               inArguments);
    
    if (inArguments[0].equalsIgnoreCase("period"))
      return this.onPeriodCommand(inSender,
                                  inCommand,
                                  inCommandLabel,
                                  inArguments);

    if (inArguments[0].equalsIgnoreCase("count"))
      return this.onCountCommand(inSender,
                                 inCommand,
                                 inCommandLabel,
                                 inArguments);

    if (inArguments[0].equalsIgnoreCase("limit"))
      return this.onLimitCommand(inSender,
                                 inCommand,
                                 inCommandLabel,
                                 inArguments);

    if (inArguments[0].equalsIgnoreCase("requires"))
      return this.onRequiresCommand(inSender,
                                    inCommand,
                                    inCommandLabel,
                                    inArguments);
    
    if (inArguments[0].equalsIgnoreCase("give"))
      return this.onGiveCommand(inSender,
                                inCommand,
                                inCommandLabel,
                                inArguments);
    
    if (inArguments[0].equalsIgnoreCase("access"))
      return this.onAccessCommand(inSender,
                                  inCommand,
                                  inCommandLabel,
                                  inArguments);
    
    if (inArguments[0].equalsIgnoreCase("info"))
      return this.onInfoCommand(inSender,
                                inCommand,
                                inCommandLabel,
                                inArguments);
    
    /*
    if (inArguments[0].equalsIgnoreCase("spawn"))
      return this.onSpawnCommand(inSender,
                                 inCommand,
                                 inCommandLabel,
                                 inArguments);
    */
    
    this.showUsage(inSender);
    return false;
  }
  
  /******************************************************************************/

  public void run()
  {
    if (this._hasLoadedSpawners == false)
    {
      this.loadSpawners();
      this._hasLoadedSpawners = true;
    }
    
    HashMap<World, HashSet<Integer>> theLivingIDsByWorld = new HashMap<World, HashSet<Integer>>();
        
    Iterator theIterator = this._spawnersByLocation.values().iterator();
    
    while(theIterator.hasNext())
    {
      CB_Spawner theSpawner = (CB_Spawner) theIterator.next();
      
      if (theSpawner.isChunkLoaded())
      {
        if (theSpawner.isValid())
        {
          World theWorld = theSpawner.getLocation().getWorld();
          HashSet<Integer> theLivingIDs = theLivingIDsByWorld.get(theWorld);
          if (theLivingIDs == null)
          {
            List<LivingEntity> theLivingEntities = theWorld.getLivingEntities();
            theLivingIDs = new HashSet<Integer>();
            for (LivingEntity theEntity : theLivingEntities)
            {
              theLivingIDs.add(theEntity.getEntityId());
            }
            theLivingIDsByWorld.put(theWorld, theLivingIDs);
          }
          
          theSpawner.run(theLivingIDs);
        }
        else
        {
          theIterator.remove();
          this.hasDirtySpawner();
        }
      }
    }
    
    if (_hasDirtySpawner)
    {
      this.saveSpawners();
      _hasDirtySpawner = false;
    }
  }
  
  /******************************************************************************/
  
  public boolean onSetCommand(CommandSender inSender,
                              Command inCommand,
                              String inCommandLabel,
                              String[] inArguments)
  {
    if (!(inSender instanceof Player))
    {
      CreatureboxPlugin.message(inSender, messageError, "set command requires a player.");
      return false;
    }
    
    Player thePlayer = (Player) inSender;
    
    if (this.permission(thePlayer, "creaturebox.set", true) == false)
      return false;
    
    if (inArguments.length < 2)
    {
      this.showSetUsage(thePlayer);
      return false;
    }
            
    CB_Spawnable theSpawnable = null;
    CreatureType theCreatureType = null;
    ArrayList<CB_Spawnable> theSpawns = new ArrayList<CB_Spawnable>();
    
    for (int theIndex = 1; theIndex < inArguments.length; theIndex++)
    {
      theSpawnable = CB_Spawnable.getSpawnableOf(inArguments[theIndex]);
      if (theSpawnable == null)
      {
        this.showSetUsage(thePlayer);
        return false;
      }
      
      String thePermission = "creaturebox.creature." + theSpawnable.getCreatureName();
      if (this.permission(thePlayer, thePermission, true) == false)
        return false;
      
      theSpawns.add(theSpawnable);
      
      if (theCreatureType == null)
      {
        theCreatureType = theSpawnable.getCreatureType();
      }
    }
    
    assert(theSpawns.size() > 0);
    assert(theSpawnable != null);
                
    Block theBlock = thePlayer.getTargetBlock(__transparentBlocks, 20);
        
    CB_Spawner theSpawner = this.getSpawner(new CB_Location(theBlock.getLocation()));
    
    if (theSpawner == null)
    {
      CreatureboxPlugin.message(thePlayer, messageError, "You are not looking at a mob spawner.");
      return false;
    }
    
    theSpawner.setSpawns(theSpawns);
    
    this.putSpawner(theSpawner);
    
    theSpawner.showSettings(inSender);
    
    return true;
  }
  
  public boolean onPeriodCommand(CommandSender inSender,
                                 Command inCommand,
                                 String inCommandLabel,
                                 String[] inArguments)
  {
    if (!(inSender instanceof Player))
    {
      CreatureboxPlugin.message(inSender, messageError, "period command requires a player.");
      return false;
    }
    
    Player thePlayer = (Player) inSender;
    
    if (this.permission(thePlayer, "creaturebox.period", true) == false)
      return false;
    
    if (inArguments.length != 2)
    {
      this.showPeriodUsage(thePlayer);
      return false;
    }
    
    int thePeriod;
    
    try
    {
      thePeriod = Integer.parseInt(inArguments[1]);
      
      if (thePeriod < CB_Spawner.defaultPeriod)
        throw new NumberFormatException();
    }
    catch (NumberFormatException inException)
    {
      this.showPeriodUsage(thePlayer);
      return false;
    }
    
    Block theBlock = thePlayer.getTargetBlock(__transparentBlocks, 20);
    
    CB_Spawner theSpawner = this.getSpawner(new CB_Location(theBlock.getLocation()));
    
    if (theSpawner == null)
    {
      CreatureboxPlugin.message(thePlayer, messageError, "You are not looking at a mob spawner.");
      return false;
    }
    
    theSpawner.setPeriod(thePeriod);
    
    this.putSpawner(theSpawner);
    
    theSpawner.showSettings(inSender);

    return true;
  }
  
  public boolean onCountCommand(CommandSender inSender,
                                 Command inCommand,
                                 String inCommandLabel,
                                 String[] inArguments)
  {
    if (!(inSender instanceof Player))
    {
      CreatureboxPlugin.message(inSender, messageError, "count command requires a player.");
      return false;
    }
    
    Player thePlayer = (Player) inSender;
    
    if (this.permission(thePlayer, "creaturebox.count", true) == false)
      return false;
    
    if (inArguments.length != 2)
    {
      this.showCountUsage(thePlayer);
      return false;
    }
    
    int theCount;
    
    try
    {
      theCount = Integer.parseInt(inArguments[1]);
      
      if (theCount < CB_Spawner.defaultCount)
        throw new NumberFormatException();
    }
    catch (NumberFormatException inException)
    {
      this.showCountUsage(thePlayer);
      return false;
    }
    
    Block theBlock = thePlayer.getTargetBlock(__transparentBlocks, 20);
    
    CB_Spawner theSpawner = this.getSpawner(new CB_Location(theBlock.getLocation()));
    
    if (theSpawner == null)
    {
      CreatureboxPlugin.message(thePlayer, messageError, "You are not looking at a mob spawner.");
      return false;
    }
    
    theSpawner.setCount(theCount);
    
    this.putSpawner(theSpawner);
    
    theSpawner.showSettings(inSender);

    return true;
  }
  
  public boolean onLimitCommand(CommandSender inSender,
                                Command inCommand,
                                String inCommandLabel,
                                String[] inArguments)
  {
    if (!(inSender instanceof Player))
    {
      CreatureboxPlugin.message(inSender, messageError, "limit command requires a player.");
      return false;
    }
    
    Player thePlayer = (Player) inSender;
    
    if (this.permission(thePlayer, "creaturebox.limit", true) == false)
      return false;
    
    if (inArguments.length != 2)
    {
      this.showLimitUsage(thePlayer);
      return false;
    }
    
    int theLimit;
    
    try
    {
      theLimit = Integer.parseInt(inArguments[1]);
      
      if (theLimit < CB_Spawner.defaultLimit)
        throw new NumberFormatException();
    }
    catch (NumberFormatException inException)
    {
      this.showLimitUsage(thePlayer);
      return false;
    }
    
    Block theBlock = thePlayer.getTargetBlock(__transparentBlocks, 20);
    
    CB_Spawner theSpawner = this.getSpawner(new CB_Location(theBlock.getLocation()));
    
    if (theSpawner == null)
    {
      CreatureboxPlugin.message(thePlayer, messageError, "You are not looking at a mob spawner.");
      return false;
    }
    
    theSpawner.setLimit(theLimit);
    
    this.putSpawner(theSpawner);
    
    theSpawner.showSettings(inSender);

    return true;
  }  
  
  public boolean onRequiresCommand(CommandSender inSender,
                                Command inCommand,
                                String inCommandLabel,
                                String[] inArguments)
  {
    if (!(inSender instanceof Player))
    {
      CreatureboxPlugin.message(inSender, messageError, "requires command requires a player.");
      return false;
    }
    
    Player thePlayer = (Player) inSender;
    
    if (this.permission(thePlayer, "creaturebox.requires", true) == false)
      return false;
    
    ArrayList<String> theRequirements = new ArrayList<String>();
    theRequirements.add("space");
    
    for (int theIndex = 1; theIndex < inArguments.length; theIndex++)
    {
      String theRequirement = inArguments[theIndex];
      if (CB_Spawner.knowsRequirement(theRequirement) == false)
      {
        this.showRequiresUsage(thePlayer);
        return false;
      }
      if (theRequirements.contains(theRequirement) == false)
      {
        theRequirements.add(theRequirement);
      }
    }
    
    Block theBlock = thePlayer.getTargetBlock(__transparentBlocks, 20);
    
    CB_Spawner theSpawner = this.getSpawner(new CB_Location(theBlock.getLocation()));
    
    if (theSpawner == null)
    {
      CreatureboxPlugin.message(thePlayer, messageError, "You are not looking at a mob spawner.");
      return false;
    }
    
    theSpawner.setRequirements(theRequirements);
    
    this.putSpawner(theSpawner);
    
    theSpawner.showSettings(inSender);

    return true;
  }  
  
  /******************************************************************************/
  
  public boolean onInfoCommand(CommandSender inSender,
                               Command inCommand,
                               String inCommandLabel,
                               String[] inArguments)
  {
    if (!(inSender instanceof Player))
    {
      CreatureboxPlugin.message(inSender, messageError, "info command requires a player.");
      return false;
    }

    if (inArguments.length > 1)
    {
      this.showInfoUsage(inSender);
      return false;
    }
    
    Player thePlayer = (Player) inSender;
    Block theBlock = thePlayer.getTargetBlock(__transparentBlocks, 20);
    
    CB_Spawner theSpawner = this.getSpawner(new CB_Location(theBlock.getLocation()));
    
    if (theSpawner == null)
    {
      CreatureboxPlugin.message(thePlayer, messageError, "You are not looking at a mob spawner.");
      return false;
    }
    
    theSpawner.showSettings(inSender);
        
    return true;
  }
  
  /******************************************************************************/
  
  public boolean onGiveCommand(CommandSender inSender,
                               Command inCommand,
                               String inCommandLabel,
                               String[] inArguments)
  {
    if ((inArguments.length < 3) ||
        (inArguments.length > 4))
    {
      this.showGiveUsage(inSender);
      return false;
    }
    
    Player theTarget = null;
    CB_Spawnable theSpawnable = null;
    int theCount = 1;
    
    theTarget = inSender.getServer().getPlayer(inArguments[1]);
    theSpawnable = CB_Spawnable.getSpawnableOf(inArguments[2]);
    if (inArguments.length == 4)
      theCount = Integer.valueOf(inArguments[3]);
    
    if ((theTarget == null) || (theSpawnable == null) || (theCount == 0))
    {
      this.showGiveUsage(inSender);
      return false;
    }
    
    if (inSender instanceof Player)
    {
      // Insure that the player has permission to give spawners.
      
      Player thePlayer = (Player) inSender;
      
      String thePermission = "creaturebox.creature." + theSpawnable.getCreatureName();

      if ((this.permission(thePlayer, "creaturebox.give", true) == false) ||
          (this.permission(thePlayer, thePermission, true) == false))
        return false;
    }
    else
    {
      // For now, we assume that if the sender isn't a player, it's the console, which always has give permission.
    }
    
    int theCreatureIndex = theSpawnable.getCreatureIndex();    
    World theWorld = theTarget.getWorld();
    Location theLocation = theTarget.getLocation();
    ItemStack theItem = new ItemStack(Material.MOB_SPAWNER, theCount, (short) theCreatureIndex);
    
    if (inSender != theTarget)
      CreatureboxPlugin.message(inSender, messageAlways,
              "giving " + theSpawnable.getCreatureName() + " spawner to " + theTarget.getName() + ".");
    CreatureboxPlugin.message(theTarget, messageAlways, "enjoy your " + theSpawnable.getCreatureName() + " spawner.");
    
    theWorld.dropItem(theLocation, theItem);
    
    return true;
  }
  
  /******************************************************************************/
  
  public boolean onAccessCommand(CommandSender inSender,
                                Command inCommand,
                                String inCommandLabel,
                                String[] inArguments)
  {
    if (inArguments.length > 1)
    {
      this.showAccessUsage(inSender);
      return false;
    }
    
    String thePlayerType = this._permissions.getGroup(inSender);
    
    boolean creaturebox_set = this.permission(inSender, "creaturebox.set", false);
    boolean creaturebox_give = this.permission(inSender, "creaturebox.give", false);
    boolean creaturebox_dropspawner = this.permission(inSender, "creaturebox.dropspawner", false);
    boolean creaturebox_placespawner = this.permission(inSender, "creaturebox.placespawner", false);
    
    PluginDescriptionFile theDescription = this.getDescription();
    
    usage(inSender, "creaturebox " + theDescription.getVersion() + " access: " + thePlayerType);
    usage(inSender, "  creaturebox.set: " + creaturebox_set);
    usage(inSender, "  creaturebox.give: " + creaturebox_give);
    usage(inSender, "  creaturebox.dropspawner: " + creaturebox_dropspawner);
    usage(inSender, "  creaturebox.placespawner: " + creaturebox_placespawner);      

    return true;
  }
  
  public boolean onSpawnCommand(CommandSender inSender,
                                Command inCommand,
                                String inCommandLabel,
                                String[] inArguments)
  {
    if (!(inSender instanceof Player))
    {
      CreatureboxPlugin.message(inSender, messageError, "set command requires a player.");
      return false;
    }
    
    Player thePlayer = (Player) inSender;
    
    if (this.permission(thePlayer, "creaturebox.spawn", true) == false)
      return false;
    
    if (inArguments.length != 2)
    {
      return false;
    }
    
    CB_Spawnable theSpawnable = CB_Spawnable.getSpawnableOf(inArguments[1]);
    CreatureType theCreatureType = theSpawnable.getCreatureType();
    Location theLocation = thePlayer.getLocation();
    World theWorld = theLocation.getWorld();
    
    LivingEntity theSpawn = theWorld.spawnCreature(theLocation, theCreatureType);
    
    // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "Spawned: " + theSpawn.toString());
    
    theSpawn = theSpawnable.spawnCreatureAt(theLocation, null);
    
    // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "Spawned: " + theSpawn.toString());
    
    return true;
  }
  
  /******************************************************************************/

  public void showUsage(CommandSender inSender)
  {
    usage(inSender, "creaturebox usage:");
    usage(inSender, "/creaturebox set {creaturename}");
    usage(inSender, "/creaturebox period {period}");
    usage(inSender, "/creaturebox count {count}");
    usage(inSender, "/creaturebox limit {limit}");
    usage(inSender, "/creaturebox requires {requirements}");
    usage(inSender, "/creaturebox give {player} {creaturename} {count}");
    usage(inSender, "/creaturebox access");
    usage(inSender, "/creaturebox debug");
  }
  
  /******************************************************************************/

  public void showSetUsage(CommandSender inSender)
  {
    usage(inSender, "creaturebox set usage: look at a mob spawner, then type:");
    usage(inSender, "/creaturebox set {creaturename}");
    
    String theCreatureNames = "  valid creature names: ";
    int theIndex = 0;
    
    for (String theCreatureName : CB_Spawnable.getCreatureNames())
    {      
      if (theIndex > 0)
        theCreatureNames = theCreatureNames.concat(", ");
  
      if (theCreatureNames.length() + theCreatureName.length() >= 62)
      {
        usage(inSender, theCreatureNames);
        theCreatureNames = "  ";
      }
      
      theCreatureNames = theCreatureNames.concat(theCreatureName);
      
      theIndex++;
    }
    
    usage(inSender, theCreatureNames);
  }
  
  /******************************************************************************/
  
  public void showPeriodUsage(CommandSender inSender)
  {
    usage(inSender, "creaturebox period usage: look at a mob spawner, then type:");
    usage(inSender, "/creaturebox period [period]");
    usage(inSender, "where period is a value in seconds.");
    usage(inSender, "A period of -1 is default (random between 10 and 20 seconds).");
    usage(inSender, "A period of 0 tells the spawner to fire once each time its");
    usage(inSender, "redstone state switches from on to off.");
  }
  
  /******************************************************************************/
  
  public void showCountUsage(CommandSender inSender)
  {
    usage(inSender, "creaturebox count usage: look at a mob spawner, then type:");
    usage(inSender, "/creaturebox count [count]");
    usage(inSender, "where count is the number of mobs to spawn each time the spawner fires.");
    usage(inSender, "A count of -1 is default (random between 1 and 4).");
    usage(inSender, "A count of 0 disables the spawner");
  }
  
  /******************************************************************************/
  
  public void showLimitUsage(CommandSender inSender)
  {
    usage(inSender, "creaturebox limit usage: look at a mob spawner, then type:");
    usage(inSender, "/creaturebox limit [limit]");
    usage(inSender, "where limit is the maximum number of creatures that this");
    usage(inSender, "spawner will create at a time.");
    usage(inSender, "A limit of -1 is default.");
    usage(inSender, "A limit of 0 disables the spawner");
  }
  
  /******************************************************************************/
  
  public void showRequiresUsage(CommandSender inSender)
  {
    usage(inSender, "creaturebox requires usage: look at a mob spawner, then type:");
    usage(inSender, "/creaturebox requires <requirements>");
    usage(inSender, "where requirements is a list of conditions that have to be met");
    usage(inSender, "before a creature can be spawned.  Possible requirements are:");
    usage(inSender, "player  - Require a player to be near the spawner.");
    usage(inSender, "surface - Require grass for critters.");
    usage(inSender, "light   - Require light for critters, dark for monsters.");    
  }
  
  /******************************************************************************/

  public void showGiveUsage(CommandSender inSender)
  {
    usage(inSender, "creaturebox give usage:");
    usage(inSender, "/creaturebox give {player} {creaturename} {count}");
  }
  
  /******************************************************************************/
  
  public void showAccessUsage(CommandSender inSender)
  {
    usage(inSender, "creaturebox access usage:");
    usage(inSender, "/creaturebox access");
    usage(inSender, "  Show the player's current access to creaturebox commands.");
  }
  
  /******************************************************************************/
  
  public void showInfoUsage(CommandSender inSender)
  {
    usage(inSender, "creaturebox info usage: look at a mob spawner, then type:");
    usage(inSender, "/creaturebox info");
    usage(inSender, "  Show information about the targeted mob spawner.");
  }
  
  /******************************************************************************/

  public boolean permission(CommandSender inSender, String inPermission, boolean inReport)
  {
    return _permissions.has(inSender, inPermission, inReport);
  }
  
  /******************************************************************************/

  public boolean blockSpawnsAt(CB_Location inLocation)
  {
    Block theBlock = inLocation.getBlock();
            
    return ((_spawnControl == true) &&
            ((this._enableRedstone && theBlock.isBlockPowered()) ||
            (this.artificialSpawnerAt(inLocation) != null)));
  }
  
  /******************************************************************************/

  public CB_Spawner artificialSpawnerAt(CB_Location inLocation)
  {
    return _spawnersByLocation.get(inLocation);
  }
  
  public CB_Spawner getSpawner(CB_Location inLocation)
  {
    if (inLocation.getBlock().getType() != Material.MOB_SPAWNER)
      return null;
    
    CB_Spawner theSpawner = artificialSpawnerAt(inLocation);
    
    if (theSpawner == null)
      theSpawner = new CB_Spawner(this, inLocation);
    
    return theSpawner;
  }
  
  public void putSpawner(CB_Spawner inSpawner)
  {
    if (inSpawner.getNatural())
    {
      Block theBlock = inSpawner.getLocation().getBlock();
      if (theBlock.getType() == Material.MOB_SPAWNER)
      {
        CreatureSpawner theNaturalSpawner = (CreatureSpawner) theBlock.getState();
        theNaturalSpawner.setCreatureType(inSpawner.getCreatureType());
      }
      _spawnersByLocation.remove(inSpawner.getLocation());
    }
    else
    {
      _spawnersByLocation.put(inSpawner.getLocation(), inSpawner);
    }
    
    this.hasDirtySpawner();
  }
  
  public void hasDirtySpawner()
  {
    _hasDirtySpawner = true;
  }
  
  public static boolean getSpawnControl()
  {
    return _spawnControl;
  }
  
  public static void setSpawnControl(boolean inValue)
  {
    _spawnControl = inValue;
  }
  
  public short getLastDurability()
  {
    return _lastDurability;
  }
  
  public void setLastDurability(short inValue)
  {
    _lastDurability = inValue;
  }
  
  public void setSpawner(Player inPlayer,
                         CB_Location inLocation,
                         CB_Spawnable inSpawnable)
  {
    ArrayList<CB_Spawnable> theSpawns = new ArrayList<CB_Spawnable>();
    theSpawns.add(inSpawnable);
    
    this.setSpawner(inPlayer, inLocation, theSpawns);
  }
  
  public void setSpawner(Player inPlayer,
                         CB_Location inLocation,
                         ArrayList<CB_Spawnable> inSpawns)
  {
    if ((inSpawns.size() == 1) && (inSpawns.get(0).getNatural() == true))
    {
      // Make an unmanaged spawner.

      if (_spawnersByLocation != null)
        _spawnersByLocation.remove(inLocation);
    }
    else
    {
      CB_Spawner theCBSpawner = new CB_Spawner(this, inLocation, inSpawns);
      
      assert(_spawnersByLocation != null);
      
      _spawnersByLocation.put(inLocation, theCBSpawner);
    }
    
    CreatureSpawner theSpawner = (CreatureSpawner) inLocation.getBlock().getState();
    CreatureType theCreatureType = inSpawns.get(0).getCreatureType();
    if (theCreatureType == null)
      theCreatureType = CreatureType.PIG;
    
    theSpawner.setCreatureType(inSpawns.get(0).getCreatureType());
    
    String theTypes = "spawner now creates ";
    
    for (int theIndex = 0; theIndex < inSpawns.size(); theIndex++)
    {
      if (theIndex > 0)
        theTypes += ((theIndex < inSpawns.size() - 1) ? ", " : " and ");
      
      theTypes += inSpawns.get(theIndex).getCreatureName();
    }
    
    theTypes += ".";
    
    CreatureboxPlugin.message(inPlayer, messageAlways, theTypes);
  }
    
  /******************************************************************************/

  public static void message(CommandSender inSender,
                             int inPriority,
                             String inMessage)
  {    
    if (inPriority <= _messagePriority)
    {
      inSender.sendMessage(__messageColorByIndex[inPriority] + pluginName + ": " + inMessage);
    }
  }
  
  /******************************************************************************/

  public static void usage(CommandSender inSender,
                           String inUsage)
  {
    inSender.sendMessage(ChatColor.GOLD + inUsage);
  }  
  
  /******************************************************************************/
  
  public static Random getRandom()
  {
    return __random;
  }
  
  /******************************************************************************/
  
  protected void setDefaultConfiguration()
  {
    this.getConfiguration().setProperty("messagePriority", CreatureboxPlugin._messagePriority);
    this.getConfiguration().setProperty("enableRedstone", this._enableRedstone);
    this.getConfiguration().setProperty("operatorPermissions", this._permissions.getOperatorPermissions());
    
    super.setDefaultConfiguration();
  }
  
  /******************************************************************************/
  
  protected void loadConfiguration()
  {
    CreatureboxPlugin._messagePriority = this.getConfiguration().getInt("messagePriority", CreatureboxPlugin._messagePriority);
    this._enableRedstone = this.getConfiguration().getBoolean("enableRedstone", this._enableRedstone);
    this._permissions.setOperatorPermissions(this.getConfiguration().getBoolean("operatorPermissions", this._permissions.getOperatorPermissions()));
    // System.out.print("creaturebox: messagePriority = " + CreatureboxPlugin._messagePriority);
    // System.out.print("creaturebox: enableRedstone = " + this._enableRedstone);

    super.loadConfiguration();
  }
  
  /******************************************************************************/

  protected void saveConfiguration()
  {
    this.getConfiguration().setProperty("messagePriority", CreatureboxPlugin._messagePriority);
    this.getConfiguration().setProperty("enableRedstone", this._enableRedstone);
    this.getConfiguration().setProperty("operatorPermissions", this._permissions.getOperatorPermissions());

    super.saveConfiguration();
  }
    
  /******************************************************************************/
    
  private void makeSpawnersFile()
  {
    if  (_spawnersByLocation != null)
    {
      File theLabelsFile = new File(this.getDataFolder(), "spawners.yml");
      
      if (theLabelsFile.exists() == false)
      {
        try
        {
          theLabelsFile.createNewFile();
        }
        catch (IOException theException)
        {
        }
        this.saveSpawners();
      }
    }
  }
  
  /******************************************************************************/
  
  private void blockStacking()
  {
	    // Preventing spawners from stacking with different types
	    // Credit to Nisovin for how this is done. 
	    try {
	            boolean ok = false;
	            try {
	                    Field field1 = net.minecraft.server.Item.class.getDeclaredField("bs");
	                    if (field1.getType() == boolean.class) {
	                            field1.setAccessible(true);
	                            field1.setBoolean(net.minecraft.server.Item.byId[52], true);
	                            ok = true;
	                    } 
	            } catch (Exception e) {
	            }
	            if (!ok) {
	                    // otherwise limit stack size to 1
	                    Field field2 = net.minecraft.server.Item.class.getDeclaredField("maxStackSize");
	                    field2.setAccessible(true);
	                    field2.setInt(net.minecraft.server.Item.byId[52], 1);
	            }
	    } catch (Exception e) {
	            e.printStackTrace();
	    }
  }
  
  
  
  /******************************************************************************/

  @SuppressWarnings(value="unchecked")

  private void loadSpawners()
  {
    if (_spawnersByLocation != null)
    {
      File theSpawnersFile = new File(this.getDataFolder(), "spawners.yml");
      try
      {
        InputStream theSpawnersStream = new FileInputStream(theSpawnersFile);
        
        Yaml theParser = new Yaml();
        
        ArrayList<HashMap<String, Object>> theSpawners = (ArrayList<HashMap<String, Object>>) theParser.load(theSpawnersStream);
        
        for (HashMap<String, Object> theDescription : theSpawners)
        {
          CB_Spawner theSpawner = new CB_Spawner(this);
          
          if (theSpawner.putSettings(theDescription))
          {
            _spawnersByLocation.put(theSpawner.getLocation(),
                                    theSpawner);
          }
          else
          {
            System.out.println( "creaturebox: malformed data found in spawners.yml" );
          }
       }
      }
      catch (Exception inException)
      {
        DebuggerPlugin.notify(DebuggerPlugin.priorityError, inException.toString());
      }
    }
  }
  
  /******************************************************************************/

  private void saveSpawners()
  {
    DebuggerPlugin.notify(DebuggerPlugin.priorityInfo, "saving spawners");
    
    if (_spawnersByLocation != null)
    {
      try
      {
        File theSpawnersFile = new File(this.getDataFolder(), "spawners.yml");
        OutputStream theSpawnersStream = new FileOutputStream(theSpawnersFile);
        OutputStreamWriter theSpawnersWriter = new OutputStreamWriter(theSpawnersStream);
        
        Yaml theParser = new Yaml();
        
        ArrayList<HashMap<String, Object>> theSpawners = new ArrayList<HashMap<String, Object>>();
        
        for (CB_Spawner theSpawner : _spawnersByLocation.values())
        {
          if (theSpawner.isValid())
            theSpawners.add(theSpawner.getSettings());
        }
        
        theParser.dump(theSpawners, theSpawnersWriter);
      }
      catch (FileNotFoundException theException)
      {
        DebuggerPlugin.notify(DebuggerPlugin.priorityError, theException.toString());
      }
    }
  }
  
  /******************************************************************************/

  private final static ChatColor __messageColorByIndex[] = { ChatColor.GREEN, ChatColor.RED, ChatColor.YELLOW, ChatColor.GREEN };
  
  private final static HashSet<Byte> __transparentBlocks = new HashSet<Byte>();
  
  static
  {
    Material theTransparentBlocks[] = {
    Material.AIR,
    Material.SAPLING,
    Material.WATER,
    Material.STATIONARY_WATER,
    Material.GLASS,
    Material.BED_BLOCK,
    Material.YELLOW_FLOWER,
    Material.RED_ROSE,
    Material.BROWN_MUSHROOM,
    Material.RED_MUSHROOM,
    Material.STEP,
    Material.TORCH,
    Material.FIRE,
    Material.REDSTONE_WIRE,
    Material.CROPS,
    Material.SIGN_POST,
    Material.LADDER,
    Material.RAILS,
    Material.WALL_SIGN,
    Material.LEVER,
    Material.STONE_PLATE,
    Material.WOOD_PLATE,
    Material.STONE_BUTTON,
    Material.SUGAR_CANE_BLOCK,
    Material.FENCE,
    Material.CAKE_BLOCK,
    Material.DIODE_BLOCK_OFF,
    Material.DIODE_BLOCK_ON
    };
    
    for (Material theMaterial : theTransparentBlocks)
    {
      __transparentBlocks.add((byte)theMaterial.getId());
    }
  }
}
