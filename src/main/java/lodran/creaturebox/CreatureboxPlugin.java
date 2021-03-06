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
import java.lang.reflect.Method;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
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
	public static boolean showPlacements = false;

	private static String MCSTACKVAR = "a";

	public FileConfiguration config;

	public static int _messagePriority = messageNoise;
	public static int _debugPriority = messageError;

	public boolean _enableRedstone = true;
	private static boolean _spawnControl = true;

	private final CB_Permissions _permissions = new CB_Permissions(this);
	private final CB_BlockListener _blockListener = new CB_BlockListener(this);
	private final CB_EntityListener _entityListener = new CB_EntityListener(this);

	private HashMap<CB_Location, CB_Spawner> _spawnersByLocation = new HashMap<CB_Location, CB_Spawner>();
	private boolean _hasLoadedSpawners = false;
	private boolean _hasDirtySpawner = false;

	// private HashMap<CB_Creature, CB_Spawner> _spawnersByCreature = new HashMap<CB_Creature, CB_Spawner>();

	private short _lastDurability;
	private short _lastEnchant;

	private static Random __random = new Random();

	/******************************************************************************/

	@Override public void onEnable()
	{    
		super.onEnable();

		ConnectionManager.initialize();

		_blockListener.onEnable();
		_entityListener.onEnable();
		_permissions.loadPermissionsPlugin();
		CB_DataSource.initialize(this);

		this.getServer().getScheduler().scheduleSyncRepeatingTask(this,
				this,
				20,
				20);

		//this.makeSpawnersFile();
		this.blockStacking();

		PluginDescriptionFile theDescription = this.getDescription();
		DebuggerPlugin.pluginName = theDescription.getName();

		System.out.println( theDescription.getName() + " version " + theDescription.getVersion() + " is enabled!" );
	}

	/******************************************************************************/

	@Override public void onDisable()
	{
		super.onDisable();
		this.getServer().getScheduler().cancelTasks(this);
		PluginDescriptionFile theDescription = this.getDescription();
		System.out.println( theDescription.getName() + " version " + theDescription.getVersion() + " is disabled." );
	}

	/******************************************************************************/

	public World getWorld(String name) {
		return this.getServer().getWorld(name);
	}

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

		if (inArguments[0].equalsIgnoreCase("import"))
			return this.onImportCommand(inSender,
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

		if (inArguments[0].equalsIgnoreCase("holding"))
			return this.onHoldingCommand(inSender,
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
					theSpawner.destroySpawner();
					theIterator.remove();
					this.hasDirtySpawner();
				}
			}
		}

		if (_hasDirtySpawner)
		{
			_hasDirtySpawner = false;
		}
	}

	/******************************************************************************/
		
	public boolean onHoldingCommand(CommandSender inSender,
			Command inCommand,
			String inCommandLabel,
			String[] inArguments)
	{
		if (!(inSender instanceof Player)) {
			CreatureboxPlugin.message(inSender, messageError, "holding command requires a player.");
			return false;
		}

		ItemStack item = ((Player) inSender).getItemInHand();
		if (item == null || item.getType() == Material.AIR) {
			return false;
		}

		if (item.getType() != Material.MOB_SPAWNER && item.getType() != Material.MONSTER_EGG) {
			return false;
		}

		int idura = item.getDurability();

		try {
			if(idura == 0) {
				CreatureboxPlugin.message(inSender, messageAlways, "You are holding a pig spawner.");
				return true;
			} 

			String spawnerType = CB_Spawnable.getSpawnableOf(idura).getCreatureName();
			if(item.getType() == Material.MOB_SPAWNER) {
				CreatureboxPlugin.message(inSender, messageAlways, "You are holding a "+ spawnerType +" spawner.");
			} else if(item.getType() == Material.MONSTER_EGG){ 
				CreatureboxPlugin.message(inSender, messageAlways, "You are holding a "+ spawnerType +" egg.");
				CreatureboxPlugin.message(inSender, messageAlways, "Break a Redstone Torch with the egg to place a spawner.");
			}
			return true;
		} catch (NullPointerException ex) {
			CreatureboxPlugin.message(inSender, messageError, "Uhoh, something went wrong. Please tell Spathi : " + ex);
			return true;
		}
	} 

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
		EntityType theEntityType = null;
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

			if (theEntityType == null)
			{
				theEntityType = theSpawnable.getEntityType();
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

		Player thePlayer = null;

		if (inSender instanceof Player)
		{
			// Insure that the player has permission to give spawners.

			thePlayer = (Player) inSender;

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
		ItemStack theItem = new ItemStack(Material.MONSTER_EGG, theCount, (short) theCreatureIndex);

		if (inSender != theTarget)
			CreatureboxPlugin.message(inSender, messageAlways,
					"giving " + theSpawnable.getCreatureName() + " egg to " + theTarget.getName() + ".");
		CreatureboxPlugin.message(theTarget, messageAlways, "enjoy your " + theSpawnable.getCreatureName() + " egg.");
		CreatureboxPlugin.message(theTarget, messageAlways, "Left click a Redstone Torch with this egg to place " + theSpawnable.getCreatureName() + " spawner.");

		if(thePlayer != null) {
			thePlayer.getInventory().addItem(theItem);
		} else {
			theWorld.dropItem(theLocation, theItem);
		}

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

		boolean creaturebox_set = this.permission(inSender, "creaturebox.set", false);
		boolean creaturebox_give = this.permission(inSender, "creaturebox.give", false);
		boolean creaturebox_dropspawner = this.permission(inSender, "creaturebox.dropspawner", false);
		boolean creaturebox_placespawner = this.permission(inSender, "creaturebox.placespawner", false);

		PluginDescriptionFile theDescription = this.getDescription();

		usage(inSender, "creaturebox " + theDescription.getVersion());
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
		EntityType theEntityType = theSpawnable.getEntityType();
		Location theLocation = thePlayer.getLocation();
		World theWorld = theLocation.getWorld();

		LivingEntity theSpawn = theWorld.spawnCreature(theLocation, theEntityType);

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
				theNaturalSpawner.setSpawnedType(inSpawner.getEntityType());
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

	public short getLastEnchant()
	{
		return _lastEnchant;
	}

	public void setLastEnchant(short inValue)
	{
		_lastEnchant = inValue;
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
		EntityType theEntityType = inSpawns.get(0).getEntityType();
		if (theEntityType == null)
			theEntityType = EntityType.PIG;

		theSpawner.setSpawnedType(inSpawns.get(0).getEntityType());

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
			inSender.sendMessage(__messageColorByIndex[inPriority] + "["+ pluginName + "] " + inMessage);
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

	public static void logline(String line) {
		System.out.println( "["+ pluginName + "] " +line  );
	}



	/******************************************************************************/

	protected void loadConfiguration()
	{
		this.config = this.getConfig();
		try {
			this.config.options().copyDefaults(true);
			this.saveConfig();
			CB_Config.initialize(config, this.getDataFolder());
		} catch (Exception e) {
			DebuggerPlugin.notify(DebuggerPlugin.priorityError, e.toString());
		}

		CreatureboxPlugin._messagePriority = CB_Config.messagePriority;
		CreatureboxPlugin._debugPriority = CB_Config.debugPriority;
		this._enableRedstone = CB_Config.enableRedstone;
		this._permissions.setOperatorPermissions(CB_Config.operatorPermissions);
		CreatureboxPlugin.showPlacements = CB_Config.showPlacements;

		super.loadConfiguration();
	}

	/******************************************************************************/

	protected void saveConfiguration()
	{
		this.saveConfig();
	}

	/******************************************************************************/

	private void blockStacking()
	{
		// Preventing spawners from stacking with different types
		// Credit to Nisovin for how this is done. 
        try {
            boolean ok = false;
            try {
                    Method method = net.minecraft.server.Item.class.getDeclaredMethod(MCSTACKVAR, boolean.class);
                    if (method.getReturnType() == net.minecraft.server.Item.class) {
                            method.setAccessible(true);
                            method.invoke(net.minecraft.server.Item.byId[52], true);
                            ok = true;
                    }
            } catch (Exception e) {
            }
            if (!ok) {
                    Field field = net.minecraft.server.Item.class.getDeclaredField("maxStackSize");
                    field.setAccessible(true);
                    field.setInt(net.minecraft.server.Item.byId[52], 1);
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
			try
			{
				ArrayList<HashMap<String, Object>> theSpawners = CB_DataSource.loadSpawners();

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
						System.out.println( "creaturebox: Malformed data" );
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
	
	private boolean onImportCommand(CommandSender inSender,
			Command inCommand,
			String inCommandLabel,
			String[] inArguments)
	{
		if(inSender instanceof Player) {
			inSender.sendMessage("[creaturebox] Importing old Spawners.yml");
		}
		
		if (_spawnersByLocation != null)
		{

			File theSpawnersFile = new File(this.getDataFolder(), "spawners.yml");
			if (!theSpawnersFile.exists()) {
				CB_Logger.info("Could not find old spawners.yml. Aborting import.");
				return true;
			}
			try
			{
				InputStream theSpawnersStream = new FileInputStream(theSpawnersFile);
				int size = 0;
				Yaml theParser = new Yaml();

				ArrayList<HashMap<String, Object>> theSpawners = (ArrayList<HashMap<String, Object>>) theParser.load(theSpawnersStream);

				for (HashMap<String, Object> theDescription : theSpawners)
				{
					HashMap<String, Object> locdata = (HashMap<String, Object>)theDescription.get("Location");
					
					Location temploc = new Location(this.getServer().getWorld((String) locdata.get("World")),
						          (Integer)locdata.get("X"),
						          (Integer)locdata.get("Y"),
						          (Integer)locdata.get("Z"));
					
					theDescription.remove("Location");
					theDescription.put("Location", temploc);
					
					
					ArrayList<String> spawndata = (ArrayList<String>) theDescription.get("Spawns");
					theDescription.remove("Spawns");
					theDescription.put("Spawns", (String) spawndata.get(0));
					
					theDescription.put("id", -1);
					
					CB_Spawner theSpawner = new CB_Spawner(this);

					if (theSpawner.putSettings(theDescription))
					{
						_spawnersByLocation.put(theSpawner.getLocation(),
								theSpawner);
						// To force a db update
						theSpawner.setCount(theSpawner.getCount());
						size++;
					}
					else
					{
						System.out.println( "creaturebox: Malformed data found in spawners.yml" );
					}
				}
				CB_Logger.info("Imported " + size + " managed spawners from spawners.yml");
				CB_Logger.info("Renaming spawners.yml to spawners.yml.old");
				if (!theSpawnersFile.renameTo(new File(CB_Config.dataDir.getAbsolutePath(), "spawners.yml.old"))) {
					CB_Logger.warning("Failed to rename spawners.yml! Please rename this manually!");
				}
				return true;
			}
			catch (Exception inException)
			{
				DebuggerPlugin.notify(DebuggerPlugin.priorityError, inException.toString());
			}
		}
		return false;
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
