package lodran.creaturebox;

import org.bukkit.Location;

public abstract class CB_Requirement
{
  public CB_Requirement() { }
  
  abstract public boolean maySpawnCreatureAt(Location inBlock);
}

