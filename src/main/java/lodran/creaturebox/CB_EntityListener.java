package lodran.creaturebox;

import org.bukkit.Location;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.World;
import org.bukkit.entity.CreatureType;
// import org.bukkit.entity.Creature;
import java.util.Iterator;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import java.lang.Math;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Event;

public class CB_EntityListener extends EntityListener
{
  private final CreatureboxPlugin _plugin;
  
  /******************************************************************************/
  
  public CB_EntityListener(CreatureboxPlugin inPlugin)
  {
    _plugin = inPlugin;
  }
  
  /******************************************************************************/
  
  public void onEnable()
  {
    PluginManager theManager = _plugin.getServer().getPluginManager();
    
    theManager.registerEvent(Event.Type.CREATURE_SPAWN, this, Event.Priority.Normal, _plugin);
    // theManager.registerEvent(Event.Type.ENTITY_DEATH, this, Event.Priority.Normal, _plugin);
  }
  
  /******************************************************************************/
  
  @Override public void onCreatureSpawn(CreatureSpawnEvent inEvent)
  { 
    // _plugin.notifyDebuggers(ChatColor.AQUA + "creaturebox: onCreatureSpawn");
    
    if (inEvent.isCancelled())
      return;
    
    CreatureType theCreatureType = inEvent.getCreatureType();
    Location theLocation = inEvent.getLocation();
    World theWorld = theLocation.getWorld();
    
    // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "onCreatureSpawn - " + theCreatureType.toString().toLowerCase());
    
    int theMaxX = theLocation.getBlockX() + 4;
    int theMaxY = theLocation.getBlockY() + 1;
    int theMaxZ = theLocation.getBlockZ() + 4;
    
    for (int theX = theLocation.getBlockX() - 4; theX <= theMaxX; theX++)
    {
      for (int theY = theLocation.getBlockY() - 1; theY <= theMaxY; theY++)
      {
        for (int theZ = theLocation.getBlockZ() - 4; theZ <= theMaxZ; theZ++)
        {
          Location theSpawnerLocation = new Location(theWorld, theX, theY, theZ);
          Block theBlock = theSpawnerLocation.getBlock();
          if (theBlock.getType() == Material.MOB_SPAWNER)
          {
            if (_plugin.blockSpawnsAt(new CB_Location(theSpawnerLocation)))
            {
              CreatureSpawner theSpawner = (CreatureSpawner) theBlock.getState();
              if (theSpawner.getCreatureType().equals(theCreatureType))
              {
                inEvent.setCancelled(true);
                // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "onCreatureSpawn canceled spawn of " + theCreatureType.toString().toLowerCase());
              }
            }
          }
        }
      }
    }
  }
  
  /*
  @Override public void onEntityDeath(EntityDeathEvent inEvent)
  {
    Entity theEntity = inEvent.getEntity();
    
    if (theEntity instanceof Creature)
    {
      // System.out.println("onEntityDeath: " + theEntity.getEntityId() + " died");
      
      _plugin.creatureDied(theEntity.getEntityId());
    }
  }
   */
  
  /******************************************************************************/
}