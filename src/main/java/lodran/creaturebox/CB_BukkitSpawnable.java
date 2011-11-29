package lodran.creaturebox;

import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.block.BlockFace;

class CB_BukkitSpawnable extends CB_Spawnable
{
  private CreatureType _creatureType;
  
  public CB_BukkitSpawnable(CreatureType inCreatureType,
                         Integer inCreatureIndex,
                            boolean inNatural)
  {
    super(inCreatureType.toString().toLowerCase(),
          inCreatureIndex,
          inNatural,
          _requirementsByCreatureType.get(inCreatureType));
    
    _creatureType = inCreatureType;
  }
  
  public CreatureType getCreatureType()
  {
    return _creatureType;
  }  
  
  LivingEntity spawnCreatureAt(Location inLocation,
                           ArrayList<String> inRequirementKeys)
  {
    // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "CB_BukkitSpawnable.spawnCreatureAt");

    if (this.maySpawnCreatureAt(inLocation, inRequirementKeys))
    {
      World theWorld = inLocation.getWorld();
      
      CreatureboxPlugin.setSpawnControl(false);
      LivingEntity theSpawn = null;
      
      // TEMPFIX -- Monsters are broken
      if(_creatureType == CreatureType.MONSTER) {
        _creatureType = CreatureType.ZOMBIE;
      }
      
      try {
        theSpawn = theWorld.spawnCreature(inLocation, _creatureType);
      } catch (Exception e) {
    	System.out.println("[creaturebox] Unable to spawn '" + _creatureType.getName() + "' at co-ords: "+ inLocation.getX() + ", " + inLocation.getY() + ", " + inLocation.getZ());
      }
      
      CreatureboxPlugin.setSpawnControl(true);
      
      if(theSpawn == null) {
    	System.out.println("[creaturebox] Unable to spawn '" + _creatureType.getName() + "' at co-ords: "+ inLocation.getX() + ", " + inLocation.getY() + ", " + inLocation.getZ());
      }
      
      // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "spawned: " + theSpawn);
      // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "world: " + theWorld);
      // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "location: " + inLocation);
      
      return theSpawn;
    }
    
    return null;
  }
  
  private static HashMap<CreatureType, HashMap <String, CB_Requirement>> _requirementsByCreatureType = new HashMap<CreatureType, HashMap <String, CB_Requirement>>();

  static
  {
    CB_Requirement theLightRequirement = new CB_LightRequirement(BlockFace.SELF);
    CB_Requirement theDarkRequirement = new CB_DarkRequirement(BlockFace.SELF);
    CB_Requirement thePlayerRequirement = new CB_PlayerRequirement();

    HashMap <String, CB_Requirement> theCritterRequirementsByKey = new HashMap <String, CB_Requirement>();
    
    theCritterRequirementsByKey.put("space", new CB_CritterSpaceRequirements());
    theCritterRequirementsByKey.put("surface", new CB_CritterSurfaceRequirements());
    theCritterRequirementsByKey.put("light", theLightRequirement);
    theCritterRequirementsByKey.put("player", thePlayerRequirement);
    
    // Critters

    _requirementsByCreatureType.put(CreatureType.PIG, theCritterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.CHICKEN, theCritterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.COW, theCritterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.SHEEP, theCritterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.MUSHROOM_COW, theCritterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.VILLAGER, theCritterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.SNOWMAN, theCritterRequirementsByKey);
    
    
    // Squid
    
    HashMap <String, CB_Requirement> theSquidRequirementsByKey = new HashMap <String, CB_Requirement>();
    
    theSquidRequirementsByKey.put("space", new CB_SquidRequirements());
    theSquidRequirementsByKey.put("player", thePlayerRequirement);
    
    _requirementsByCreatureType.put(CreatureType.SQUID, theSquidRequirementsByKey);

    // PigZombies

    HashMap <String, CB_Requirement> thePigZombieRequirementsByKey = new HashMap <String, CB_Requirement>();
    
    thePigZombieRequirementsByKey.put("space", new CB_PigZombieRequirements());
    thePigZombieRequirementsByKey.put("player", thePlayerRequirement);
    
    _requirementsByCreatureType.put(CreatureType.PIG_ZOMBIE, thePigZombieRequirementsByKey);
    
    HashMap <String, CB_Requirement> theMonsterRequirementsByKey = new HashMap <String, CB_Requirement>();

    theMonsterRequirementsByKey.put("space", new CB_MonsterRequirements());
    theMonsterRequirementsByKey.put("light", theDarkRequirement);
    theMonsterRequirementsByKey.put("player", thePlayerRequirement);
    
    _requirementsByCreatureType.put(CreatureType.CREEPER, theMonsterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.GHAST, theMonsterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.SKELETON, theMonsterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.SPIDER, theMonsterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.ZOMBIE, theMonsterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.SLIME, theMonsterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.MONSTER, theMonsterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.GIANT, theMonsterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.WOLF, theCritterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.CAVE_SPIDER, theMonsterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.ENDERMAN, theMonsterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.SILVERFISH, theMonsterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.BLAZE, theMonsterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.ENDER_DRAGON, theMonsterRequirementsByKey);
    _requirementsByCreatureType.put(CreatureType.MAGMA_CUBE, theMonsterRequirementsByKey);
       
    
  }
}
