package lodran.creaturebox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.lang.NullPointerException;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.CreatureType;

class CB_Spawner
{
  public final static int randomPeriod = -1;
  public final static int defaultPeriod = randomPeriod;
  
  public final static int randomCount = -1;
  public final static int defaultCount = randomCount;
  
  public final static int defaultLimit = -1;
  
  private CreatureboxPlugin _plugin;
  
  private CB_Location _location;
  private ArrayList<CB_Spawnable> _spawns;
  private int _period = defaultPeriod;
  private int _count = defaultCount;
  private int _limit = defaultLimit;
  private ArrayList<String> _requirements = defaultRequirements();
  
  private int _delay = 0;
  private boolean _wasBlockPowered = false;
  private ArrayList<Integer> _creatures = new ArrayList<Integer>();
  
  private boolean _wasChunkLoaded = false;
  
  public CB_Spawner(CreatureboxPlugin inPlugin,
                    CB_Location inLocation)
  {
    this._plugin = inPlugin;
    this._location = inLocation;
    
    Block theBlock = inLocation.getBlock();

    assert (theBlock.getType() == Material.MOB_SPAWNER);
    
    CreatureSpawner theSpawner = (CreatureSpawner) theBlock.getState();
    CB_Spawnable theSpawnable = CB_Spawnable.getSpawnableOf(theSpawner.getCreatureType());
    
    this._spawns = new ArrayList<CB_Spawnable>();
    this._spawns.add(theSpawnable);
  }
    
  public CB_Spawner(CreatureboxPlugin inPlugin,
                    CB_Location inLocation,
                    ArrayList<CB_Spawnable> inSpawns)
  {
    this._plugin = inPlugin;
    this._location = inLocation;
    this._spawns = inSpawns;
  }
  
  public CB_Spawner(CreatureboxPlugin inPlugin)
  {
    this._plugin = inPlugin;
  }
    
  public CB_Location getLocation()
  {
    return this._location;
  }
  
  public CreatureType getCreatureType()
  {
    return (this._spawns.size() > 0) ? this._spawns.get(0).getCreatureType() : null;
  }
  
  public ArrayList<CB_Spawnable> getSpawns()
  {
    return this._spawns;
  }
  
  public void setSpawns(ArrayList<CB_Spawnable> inValue)
  {
    this._spawns = inValue;
    this._plugin.hasDirtySpawner();
  }
  
  public void setSpawns(CB_Spawnable inValue)
  {
    this._spawns = new ArrayList<CB_Spawnable>();
    this._spawns.add(inValue);
    this._plugin.hasDirtySpawner();
  }
  
  public int getPeriod()
  {
    return this._period;
  }
  
  public void setPeriod(int inValue)
  {
    this._period = inValue;
    this._plugin.hasDirtySpawner();
  }
  
  public int getCount()
  {
    return this._count;
  }
  
  public void setCount(int inValue)
  {
    this._count = inValue;
    this._plugin.hasDirtySpawner();
  }
  
  public int getLimit()
  {
    return this._limit;
  }
  
  public void setLimit(int inValue)
  {
    this._limit = inValue;
    this._plugin.hasDirtySpawner();
  }
  
  public ArrayList<String> getRequirements()
  {
    return this._requirements;
  }
  
  public void setRequirements(ArrayList<String> inValue)
  {
    this._requirements = inValue;
    this._plugin.hasDirtySpawner();
  }
  
  public boolean getNatural()
  {
    // Returns true if this can be run as a "natural" spawner - i.e. without
    //  spawning the creatures ourselves.
    
    return ((this._spawns.size() == 1) &&
            (this._spawns.get(0).getNatural()) &&
            (this._period == defaultPeriod) &&
            (this._count == defaultCount) &&
            (this._limit == defaultLimit) &&
            (this._requirements.size() == defaultRequirementsSize));
  }
  
  public HashMap<String, Object> getSettings()
  {
    HashMap<String, Object> theResult = new HashMap<String, Object>();
    
    theResult.put("Location", this._location.getSettings());
    theResult.put("Spawns", CB_Spawnable.getCreatureNames(this._spawns));
    theResult.put("Period", this._period);
    theResult.put("Count", this._count);
    theResult.put("Limit", this._limit);
    theResult.put("Requirements", this._requirements);
    theResult.put("Creatures", this._creatures);

    return theResult;
  }
  
  @SuppressWarnings(value="unchecked")

  public boolean putSettings(HashMap<String, Object> inSettings)
  {
    try
    {
      this._location = new CB_Location(_plugin,
                                       (HashMap<String, Object>)inSettings.get("Location"));
      this._spawns = CB_Spawnable.getSpawnables((ArrayList<String>) inSettings.get("Spawns"));
      this._period = (Integer)inSettings.get("Period");
      this._count = (Integer)inSettings.get("Count");
      this._limit = (Integer)inSettings.get("Limit");
      this._requirements = (ArrayList<String>)inSettings.get("Requirements");
      
      if (inSettings.get("Creatures") != null)
        this._creatures = (ArrayList<Integer>)inSettings.get("Creatures");
    }
    catch (NullPointerException inException)
    {
      // I don't care what the exception was.  Return false to indicate that I couldn't parse the settings.
      
      return false;
    }
    catch (Exception inException)
    {
      // I don't care what the exception was.  Return false to indicate that I couldn't parse the settings.
      
      return false;
    }
    
    return true;
  }
  
  public void showSettings(CommandSender inSender)
  {
    if (this.getNatural() == true)
    {
      CreatureboxPlugin.usage(inSender, "creaturebox: Natural " + (this._spawns.get(0).getCreatureName()) + " spawner.");
    }
    else
    {
      String theDescription = "creaturebox: Managed ";
      for (int theIndex = 0; theIndex < this._spawns.size(); theIndex++)
      {
        if (theIndex > 0)
        {
          if (theIndex < this._spawns.size() - 1)
            theDescription += " and ";
          else
            theDescription += ", ";
        }
        theDescription += _spawns.get(theIndex).getCreatureName();
      }
      theDescription += " spawner.";
      CreatureboxPlugin.usage(inSender, theDescription);
      
      CreatureboxPlugin.usage(inSender, "  period: " + this._period);
      CreatureboxPlugin.usage(inSender, "   count: " + this._count);
      CreatureboxPlugin.usage(inSender, "   limit: " + this._limit);
      
      theDescription = "requires: ";
      for (int theIndex = 0; theIndex < this._requirements.size(); theIndex++)
      {
        if (theIndex > 0)
        {
          if (theIndex < this._spawns.size() - 1)
            theDescription += " and ";
          else
            theDescription += ", ";
        }
        theDescription += this._requirements.get(theIndex);
      }
      theDescription += ".";

      CreatureboxPlugin.usage(inSender, theDescription);
    }
  }
  
  public boolean isChunkLoaded()
  {
    boolean theResult = this._location.isChunkLoaded();
    
    if (theResult != this._wasChunkLoaded)
      DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "isChunkLoaded: " + theResult);
    
    _wasChunkLoaded = theResult;
    
    return theResult;
  }
  
  public boolean isValid()
  {
    return (this._location.getBlock().getType() == Material.MOB_SPAWNER);
  }
    
  public void run(HashSet<Integer> inLivingIDs)
  {
    // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "CB_Spawner.run");
    
    this.updateCreatures(inLivingIDs);
        
    if (this._period == 0)
      return;
    
    if (this._delay <= 0)
    {
      if (this.spawnCreatures() == true)
      {
        this._delay = ((this._period < 0) ?
                       CreatureboxPlugin.getRandom().nextInt(10) + 20 :
                       this._period);
        
        return;
      }
    }
    
    this._delay--;
  }
  
  public boolean spawnCreatures()
  {
    // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "CB_Spawner.spawnCreatures");

    boolean theResult = false;
    
    if (_location.getBlock().isBlockPowered() == false)
    {
      int theCount = (this._count < 1) ? CreatureboxPlugin.getRandom().nextInt(4) + 1 : this._count;
      
      for (int theIndex = 0; theIndex < theCount; theIndex++)
      {
        LivingEntity theCreature = this.spawnCreature();
        if (theCreature != null)
        {
          this.creatureLives(theCreature);
          theResult = true;
        }
      }
    }
    
    return theResult;
  }
  
  public LivingEntity spawnCreature()
  {
    // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "CB_Spawner.spawnCreature");

    LivingEntity theResult = null;
    
    if (this.maySpawnCreature())// && this._plugin.maySpawnCreature())
    {
      int theSpawnableIndex = CreatureboxPlugin.getRandom().nextInt(this._spawns.size());
      CB_Spawnable theSpawnable = this._spawns.get(theSpawnableIndex);
      
      theResult = theSpawnable.spawnCreatureNear(this._location, _requirements);
    }
    
    return theResult;
  }
  
  public void runRedstone()
  {
    if (this._period == 0)
    {
      boolean isBlockPowered = _location.getBlock().isBlockPowered();

      // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "" + _wasBlockPowered + " -> " + isBlockPowered);
      
      if ((this._wasBlockPowered == true) && (isBlockPowered == false))
      {
        this.spawnCreatures();
      }
      
      this._wasBlockPowered = isBlockPowered;
    }
  }
  
  public void creatureLives(LivingEntity inCreature)
  {
    // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "" + inCreature.getEntityId() + " lives.");

    this._creatures.add(inCreature.getEntityId());
  }
    
  public void updateCreatures(HashSet<Integer> inLivingIDs)
  {
    // TODO: Optimize this - recomputing on a per-spawner basis is lousy.
        
    Iterator theIterator = this._creatures.iterator();
    
    while(theIterator.hasNext())
    {
      Integer theEntityId = (Integer) theIterator.next();
      
      if (inLivingIDs.contains(theEntityId) == false)
      {
        theIterator.remove();
        // DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "" + theEntityId + " died - count: " + this._creatures.size());
      }
    }
  }
  
  public boolean maySpawnCreature()
  {
    int theLimit = (this._limit == defaultLimit) ? 10 : this._limit;
    boolean theResult = (this._creatures.size() < theLimit);
    
    /*
    if (theResult == false)
    {
      DebuggerPlugin.notify(DebuggerPlugin.priorityNoise, "maySpawnCreature: no");
    }
     */
    
    return theResult;
  }

  public final static int defaultRequirementsSize = 4;

  static public ArrayList<String> defaultRequirements()
  {
    return _allRequirements;
  }
  
  static public boolean knowsRequirement(String inRequirement)
  {
    return _allRequirements.contains(inRequirement);
  }
  
  static final private ArrayList<String> _allRequirements = new ArrayList<String>();

  static
  {
    _allRequirements.add("player");
    _allRequirements.add("space");
    _allRequirements.add("surface");
    _allRequirements.add("light");
  }
}
