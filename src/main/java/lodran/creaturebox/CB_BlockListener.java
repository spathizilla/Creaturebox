package lodran.creaturebox;

import java.util.HashSet;

import org.bukkit.entity.CreatureType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
// import org.bukkit.event.block.BlockRightClickEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Event;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

/**
 * creaturebox block listener
 * @author lodran
 */
public class CB_BlockListener extends BlockListener
{
  /******************************************************************************/
  
  private final CreatureboxPlugin _plugin;
  
  /******************************************************************************/
  
  public CB_BlockListener(final CreatureboxPlugin inPlugin)
  {
    this._plugin = inPlugin;
  }
  
  /******************************************************************************/
  
  public void onEnable()
  {
    PluginManager theManager = _plugin.getServer().getPluginManager();
    
    // theManager.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, this, Event.Priority.Normal, _plugin);
    theManager.registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.Monitor, _plugin);
    theManager.registerEvent(Event.Type.BLOCK_PLACE, this, Event.Priority.Monitor, _plugin);
    theManager.registerEvent(Event.Type.BLOCK_PHYSICS, this, Event.Priority.Monitor, _plugin);
    // theManager.registerEvent(Event.Type.SIGN_CHANGE, this, Event.Priority.Normal, _plugin);
    // theManager.registerEvent(Event.Type.REDSTONE_CHANGE, this, Event.Priority.Normal, _plugin);
  }
  
  /******************************************************************************/
  
  /*
  @Override public void onBlockRightClick(BlockRightClickEvent inEvent)
  {
    Player thePlayer = inEvent.getPlayer();
    Block theBlock = inEvent.getBlock();
    
    if (theBlock.getType() == Material.MOB_SPAWNER)
    {
      BlockState theBlockState = theBlock.getState();
      
      if (theBlockState instanceof CreatureSpawner)
      {
        CreatureSpawner theSpawner = (CreatureSpawner) theBlockState;

        CB_Spawnable theSpawnable = CB_Spawnable.getSpawnableOf(theSpawner.getCreatureType());
        
        CreatureboxPlugin.message(thePlayer, CreatureboxPlugin.messageNoise, "This is a " + theSpawnable.getCreatureName() + " spawner.");
      }
    }
  }
   */
  
  /******************************************************************************/
  
  @Override public void onBlockBreak(BlockBreakEvent inEvent)
  {
	if (inEvent.isCancelled())
		return;
	
    Player thePlayer = inEvent.getPlayer();
    Block theBlock = inEvent.getBlock();
    
    if (theBlock.getType() != Material.MOB_SPAWNER)
      return;
    
    if (_plugin.permission(thePlayer, "creaturebox.dropspawner", false) == false)
      return;
    
    World theWorld = theBlock.getWorld();
    Location theLocation = new Location(theWorld, theBlock.getX(), theBlock.getY(), theBlock.getZ(), 0, 0);
    Location theSpawnerLoc = theBlock.getLocation();
    CreatureSpawner theSpawner = (CreatureSpawner) theBlock.getState();
    CB_Spawnable theSpawnable = CB_Spawnable.getSpawnableOf(theSpawner.getCreatureType());
    int theCreatureIndex = theSpawnable.getCreatureIndex();
    
    //MaterialData theMaterial = new MaterialData(Material.MOB_SPAWNER, (byte) theCreatureIndex);
    ItemStack theItem = new ItemStack(Material.MOB_SPAWNER, 1, (short) theCreatureIndex);
    
    // Fix for mcMMO's weird berserk drop mechanics
    theLocation.getBlock().setType(Material.AIR);
    // End
    
    theWorld.dropItemNaturally(theLocation, theItem);
    
    CreatureboxPlugin.message(thePlayer, CreatureboxPlugin.messageNoise, theSpawnable.getCreatureName() + " spawner dropped.");
    
    if(CreatureboxPlugin.showPlacements) {
    	CreatureboxPlugin.logline(thePlayer.getName() + " destroyed a " + theSpawner.getCreatureType().getName() + " spawner in " + theSpawnerLoc.getWorld().getName() + " at: x: "+ theSpawnerLoc.getBlockX() +" y:"+ theSpawnerLoc.getBlockY()+ " z:" + theSpawnerLoc.getBlockZ());
    }
  }
  
  /******************************************************************************/
  
  @Override public void onBlockPlace(BlockPlaceEvent inEvent)
  {
    Player thePlayer = inEvent.getPlayer();
    Block theBlock = inEvent.getBlock();
    
    if ((theBlock.getType() != Material.MOB_SPAWNER) ||
        (inEvent.isCancelled()))
      return;
    
    if (_plugin.permission(thePlayer, "creaturebox.placespawner", true) == false)
      return;
    
    int theCreatureIndex = _plugin.getLastDurability();
        
    if(theCreatureIndex >= 1 && theCreatureIndex <= 17) {
    	// Ignore 0 because it is a pigspawner and works as is.
        World theWorld = theBlock.getWorld();
        Location theLocation = new Location(theWorld, theBlock.getX(), theBlock.getY(), theBlock.getZ(), 0, 0);
        theLocation.getBlock().setType(Material.AIR);
    	int theNCreatureIndex = correctSpawner(theCreatureIndex);
    	ItemStack theItem = new ItemStack(Material.MOB_SPAWNER, 1, (short) theNCreatureIndex);
    	theWorld.dropItemNaturally(theLocation, theItem);
    	String spawnerType = CB_Spawnable.getSpawnableOf(theNCreatureIndex).getCreatureName();
    	CreatureboxPlugin.logline("Replaced " + thePlayer.getName() + "'s old " + spawnerType + " spawner with a new one");
    	CreatureboxPlugin.message(thePlayer, CreatureboxPlugin.messageNoise, "Your old "+ spawnerType + " spawner was replaced with a new one. Place it again.");
    	return;
    }

	CB_Spawnable theSpawnable = CB_Spawnable.getSpawnableOf(theCreatureIndex);
    
    if (theSpawnable == null) {
      theSpawnable = CB_Spawnable.getSpawnableOf("pig");
    }
	
	String thePermission = "creaturebox.creature." + theSpawnable.getCreatureName();
	if(_plugin.permission(thePlayer, thePermission, true) == false) {
		CreatureboxPlugin.message(thePlayer, CreatureboxPlugin.messageError, "You do not have permission to place " + theSpawnable.getCreatureName() + " spawners");
		inEvent.setCancelled(true);
		return;
	}
	
    CB_Spawner theSpawner = _plugin.getSpawner(new CB_Location(theBlock.getLocation()));
    Location theSpawnerLoc = theBlock.getLocation();
    theSpawner.setSpawns(theSpawnable);
    
    _plugin.putSpawner(theSpawner);
    
    theSpawner.showSettings(thePlayer);
    
    if(CreatureboxPlugin.showPlacements) {
    	CreatureboxPlugin.logline(thePlayer.getName() + " placed a " + theSpawner.getCreatureType().getName() + " spawner in " + theSpawnerLoc.getWorld().getName() + " at: x: "+ theSpawnerLoc.getBlockX() +" y:"+ theSpawnerLoc.getBlockY()+ " z:" + theSpawnerLoc.getBlockZ());
    }
  }
  
  /******************************************************************************/
  
  @Override public void onBlockPhysics(BlockPhysicsEvent inEvent)
  {
    Block theBlock = inEvent.getBlock();
    
    if ((theBlock.getType() != Material.MOB_SPAWNER) ||
        (inEvent.isCancelled()))
      return;
    
    CB_Spawner theSpawner = _plugin.artificialSpawnerAt(new CB_Location(theBlock.getLocation()));
    
    if (theSpawner != null)
      theSpawner.runRedstone();
  }
  
  /******************************************************************************/
  public int correctSpawner(int theCreatureIndex) {
  	int theNCreatureIndex;
	switch(theCreatureIndex) {
		case 0:		theNCreatureIndex = 90 ; break;
		case 1:		theNCreatureIndex = 93; break;
		case 2:		theNCreatureIndex = 92; break;
		case 3:		theNCreatureIndex = 91; break;
		case 4:		theNCreatureIndex = 94; break;
		case 5:		theNCreatureIndex = 50; break;
		case 6:		theNCreatureIndex = 56; break;
		case 7:		theNCreatureIndex = 57; break;
		case 8:		theNCreatureIndex = 51; break;
		case 9:		theNCreatureIndex = 52; break;
		case 10:	theNCreatureIndex = 54; break;
		case 11:	theNCreatureIndex = 55; break;
		case 12:	theNCreatureIndex = 49; break;
		case 13:	theNCreatureIndex = 53; break;
		case 14:	theNCreatureIndex = 95; break;
		case 15:	theNCreatureIndex = 59; break;
		case 16:	theNCreatureIndex = 58; break;
		case 17:	theNCreatureIndex = 60; break;
		default: 	theNCreatureIndex = 90; break;
	}
	return theNCreatureIndex;
  }

}
