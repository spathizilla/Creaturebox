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
    CreatureSpawner theSpawner = (CreatureSpawner) theBlock.getState();
    CB_Spawnable theSpawnable = CB_Spawnable.getSpawnableOf(theSpawner.getCreatureType());
    int theCreatureIndex = theSpawnable.getCreatureIndex();
    
    MaterialData theMaterial = new MaterialData(Material.MOB_SPAWNER, (byte) theCreatureIndex);
    ItemStack theItem = new ItemStack(Material.MOB_SPAWNER, 1, (short) theCreatureIndex);
    
    // Fix for mcMMO's weird berserk drop mechanics
    theLocation.getBlock().setType(Material.AIR);
    // End
    
    theWorld.dropItemNaturally(theLocation, theItem);
    
    CreatureboxPlugin.message(thePlayer, CreatureboxPlugin.messageNoise, theSpawnable.getCreatureName() + " spawner dropped.");
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
    
    CB_Spawnable theSpawnable = CB_Spawnable.getSpawnableOf(theCreatureIndex);
        
    if (theSpawnable == null)
      theSpawnable = CB_Spawnable.getSpawnableOf("pig");
    
    CB_Spawner theSpawner = _plugin.getSpawner(new CB_Location(theBlock.getLocation()));
    
    theSpawner.setSpawns(theSpawnable);
    
    _plugin.putSpawner(theSpawner);
    
    theSpawner.showSettings(thePlayer);
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
}
