package lodran.creaturebox;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.block.BlockFace;

class CB_BukkitSpawnable extends CB_Spawnable
{
  private EntityType _EntityType;
  
  public CB_BukkitSpawnable(EntityType inEntityType,
                         Integer inCreatureIndex,
                            boolean inNatural)
  {
    super(inEntityType.toString().toLowerCase(),
          inCreatureIndex,
          inNatural,
          _requirementsByEntityType.get(inEntityType));
    
    _EntityType = inEntityType;
  }
  
  public EntityType getEntityType()
  {
    return _EntityType;
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
      
      try {
        theSpawn = theWorld.spawnCreature(inLocation, _EntityType);
      } catch (Exception e) {
    	System.out.println("[creaturebox] Unable to spawn '" + _EntityType.getName() + "' at co-ords: "+ inLocation.getX() + ", " + inLocation.getY() + ", " + inLocation.getZ());
      }
      
      CreatureboxPlugin.setSpawnControl(true);
      
      if(theSpawn == null) {
    	System.out.println("[creaturebox] Unable to spawn '" + _EntityType.getName() + "' at co-ords: "+ inLocation.getX() + ", " + inLocation.getY() + ", " + inLocation.getZ());
      }
      
      // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "spawned: " + theSpawn);
      // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "world: " + theWorld);
      // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "location: " + inLocation);
      
      return theSpawn;
    }
    
    return null;
  }
  
  private static HashMap<EntityType, HashMap <String, CB_Requirement>> _requirementsByEntityType = new HashMap<EntityType, HashMap <String, CB_Requirement>>();

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

    _requirementsByEntityType.put(EntityType.PIG, theCritterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.CHICKEN, theCritterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.COW, theCritterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.SHEEP, theCritterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.MUSHROOM_COW, theCritterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.VILLAGER, theCritterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.IRON_GOLEM, theCritterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.SNOWMAN, theCritterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.WOLF, theCritterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.OCELOT, theCritterRequirementsByKey);
    
    // Squid
    
    HashMap <String, CB_Requirement> theSquidRequirementsByKey = new HashMap <String, CB_Requirement>();
    
    theSquidRequirementsByKey.put("space", new CB_SquidRequirements());
    theSquidRequirementsByKey.put("player", thePlayerRequirement);
    
    _requirementsByEntityType.put(EntityType.SQUID, theSquidRequirementsByKey);

    // PigZombies

    HashMap <String, CB_Requirement> thePigZombieRequirementsByKey = new HashMap <String, CB_Requirement>();
    
    thePigZombieRequirementsByKey.put("space", new CB_PigZombieRequirements());
    thePigZombieRequirementsByKey.put("player", thePlayerRequirement);
    
    _requirementsByEntityType.put(EntityType.PIG_ZOMBIE, thePigZombieRequirementsByKey);
    
    HashMap <String, CB_Requirement> theMonsterRequirementsByKey = new HashMap <String, CB_Requirement>();

    theMonsterRequirementsByKey.put("space", new CB_MonsterRequirements());
    theMonsterRequirementsByKey.put("light", theDarkRequirement);
    theMonsterRequirementsByKey.put("player", thePlayerRequirement);
    
    _requirementsByEntityType.put(EntityType.CREEPER, theMonsterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.GHAST, theMonsterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.SKELETON, theMonsterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.SPIDER, theMonsterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.ZOMBIE, theMonsterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.SLIME, theMonsterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.GIANT, theMonsterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.CAVE_SPIDER, theMonsterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.ENDERMAN, theMonsterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.SILVERFISH, theMonsterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.BLAZE, theMonsterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.ENDER_DRAGON, theMonsterRequirementsByKey);
    _requirementsByEntityType.put(EntityType.MAGMA_CUBE, theMonsterRequirementsByKey);
       
    
  }
}
