package lodran.creaturebox;

import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.CreatureType;
import org.bukkit.World;
import java.util.Random;
import org.bukkit.block.Block;

abstract class CB_Spawnable
{  
  // Private member variables.
  
  private String _creatureName;
  private Integer _creatureIndex;
  private boolean _natural;
  private HashMap <String, CB_Requirement> _requirements;
  
  // Constructor
  
  public CB_Spawnable(String inCreatureName,
                      Integer inCreatureIndex,
                      boolean inNatural,
                      HashMap <String, CB_Requirement> inRequirements)
  {
    this._creatureName = inCreatureName;
    this._creatureIndex = inCreatureIndex;
    this._natural = inNatural;
    this._requirements = inRequirements;
    
    _spawnableByName.put(inCreatureName, this);
    _spawnableByIndex.put(inCreatureIndex, this);
  }
  
  // Get the creature's name.
  
  public String getCreatureName()
  {
    return this._creatureName;
  }
  
  // Get the creature's index.
  
  public Integer getCreatureIndex()
  {
    return this._creatureIndex;
  }
  
  public CreatureType getCreatureType()
  {
    return null;
  }
  
  public boolean getNatural()
  {
    // return true if the creature can be spawned naturally (by the spawner itself).
    
    return _natural;
  }
  
  // Spawn a creature near the specified location, if the specified requirements can be met.
  //
  // This could be improved so that it always finds a suitable location to spawn the creature,
  //  if a suitable location exists.
  
  public LivingEntity spawnCreatureNear(Location inLocation,
                                    ArrayList<String> inRequirementKeys)
  {
    // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "CB_Spawnable.spawnCreatureNear");

    Random theRandom = CreatureboxPlugin.getRandom();
    
    World theWorld = inLocation.getWorld();
    
    for (int theTry = 1; theTry <= 20; theTry++)
    {      
      int theX = inLocation.getBlockX() + theRandom.nextInt(9) - 4;
      int theY = inLocation.getBlockY() + theRandom.nextInt(3) - 1;
      int theZ = inLocation.getBlockZ() + theRandom.nextInt(9) - 4;
      
      Location theSpawnLocation = new Location(theWorld, theX + 0.5, theY, theZ + 0.5);
      Block theSpawnBlock = theSpawnLocation.getBlock();
      
      LivingEntity theCreature = this.spawnCreatureAt(theSpawnLocation, inRequirementKeys);
      
      if (theCreature != null)
        return theCreature;
    }
    
    return null;
  }
  
  // Spawn a creature at the specified location, if the specified requirements can be met.
  
  abstract LivingEntity spawnCreatureAt(Location inLocation,
                                    ArrayList<String> inRequirementKeys);
  
  // Get a Spawnable for the specified creature name.
  
  static CB_Spawnable getSpawnableOf(String inCreatureName)
  {
    return CB_Spawnable._spawnableByName.get(inCreatureName);
  }
  
  // Get a Spawnable for the specified creature location.
  
  static CB_Spawnable getSpawnableOf(Integer inCreatureIndex)
  {
    return CB_Spawnable._spawnableByIndex.get(inCreatureIndex);
  }
  
  static CB_Spawnable getSpawnableOf(CreatureType inCreatureType)
  {
    return CB_Spawnable._spawnableByName.get(inCreatureType.toString().toLowerCase());
  }
  
  // Test to see if the specified location meets the specified requirements.
  
  public boolean maySpawnCreatureAt(Location inLocation,
                                    ArrayList<String> inRequirementKeys)
  {
    if (inRequirementKeys == null)
      return true;
    
    // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "maySpawnCreatureAt");
    for (String theRequirementKey : inRequirementKeys)
    {
      CB_Requirement theRequirement = _requirements.get(theRequirementKey);
      
      // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, theRequirementKey + " - " + theRequirement);

      if ((theRequirement != null) &&
          (_requirements.get(theRequirementKey).maySpawnCreatureAt(inLocation) == false))
      {
        return false;
      }
    }
    
    return true;
  }
    
  // Map of creature names to Spawnables.
  
  static HashMap<String, CB_Spawnable> _spawnableByName = new HashMap<String, CB_Spawnable>();
  
  // Map of indexes to Spawnables.
  
  static HashMap<Integer, CB_Spawnable> _spawnableByIndex = new HashMap<Integer, CB_Spawnable>();
  
  static ArrayList<CB_Spawnable> getSpawnables(ArrayList<String> inCreatureNames)
  {
    ArrayList<CB_Spawnable> theResult = new ArrayList<CB_Spawnable>();
    for (String theCreatureName : inCreatureNames)
    {
      theResult.add(_spawnableByName.get(theCreatureName));
    }
    
    return theResult;
  }
  
  static ArrayList<String> getCreatureNames(ArrayList<CB_Spawnable> inSpawnables)
  {
    ArrayList<String> theResult = new ArrayList<String>();
    for (CB_Spawnable theSpawnable : inSpawnables)
    {
      theResult.add(theSpawnable.getCreatureName());
    }
    return theResult;
  }
  
  static ArrayList<String> getCreatureNames()
  {
    ArrayList<String> theResult = new ArrayList<String>();
    
    for (Integer theIndex : _spawnableByIndex.keySet())
    {
      theResult.add(_spawnableByIndex.get(theIndex).getCreatureName());
    }
    
    return theResult;
  }
  
  static void initialize()
  {
    new CB_BukkitSpawnable(CreatureType.PIG, 90, true);
    new CB_BukkitSpawnable(CreatureType.CHICKEN, 93, true);
    new CB_BukkitSpawnable(CreatureType.COW, 92, true);
    new CB_BukkitSpawnable(CreatureType.SHEEP, 91, true);
    new CB_BukkitSpawnable(CreatureType.SQUID, 94, false);   // Natural spawning for squid is retarded.
    new CB_BukkitSpawnable(CreatureType.CREEPER, 50, true);
    new CB_BukkitSpawnable(CreatureType.GHAST, 56, true);
    new CB_BukkitSpawnable(CreatureType.PIG_ZOMBIE, 57, true);
    new CB_BukkitSpawnable(CreatureType.SKELETON, 51, true);
    new CB_BukkitSpawnable(CreatureType.SPIDER, 52, true);
    new CB_BukkitSpawnable(CreatureType.ZOMBIE, 54, true);
    new CB_BukkitSpawnable(CreatureType.SLIME, 55, false);    // Slimes never seem to spawn naturally
    new CB_BukkitSpawnable(CreatureType.MONSTER, 49, false);  // Monsters never seem to spawn naturally
    new CB_BukkitSpawnable(CreatureType.GIANT, 53, false);    // Giants never seem to spawn naturally
    new CB_BukkitSpawnable(CreatureType.WOLF, 95, true);
    new CB_BukkitSpawnable(CreatureType.CAVE_SPIDER, 59, true);
    new CB_BukkitSpawnable(CreatureType.ENDERMAN, 58, true);
    new CB_BukkitSpawnable(CreatureType.SILVERFISH, 60, true);
    new CB_BukkitSpawnable(CreatureType.BLAZE, 61, true);
    new CB_BukkitSpawnable(CreatureType.ENDER_DRAGON, 63, true);
    new CB_BukkitSpawnable(CreatureType.MAGMA_CUBE, 62, false);
    new CB_BukkitSpawnable(CreatureType.MUSHROOM_COW, 96, true);
    new CB_BukkitSpawnable(CreatureType.SNOWMAN, 97, true);
    new CB_BukkitSpawnable(CreatureType.VILLAGER, 120, true);
    
    
  }
  
  static
  {
    CB_Spawnable.initialize();
  }
}