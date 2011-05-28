package lodran.creaturebox;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import java.util.ArrayList;
import org.bukkit.Location;

public class CB_RequirementSet extends CB_Requirement
{
  private ArrayList<CB_Requirement> _requirements = new ArrayList<CB_Requirement>();
  
  public CB_RequirementSet(CB_Requirement inRequirements[])
  {
    super();
    
    for (CB_Requirement theRequirement : inRequirements)
    {
      _requirements.add(theRequirement);
    }
  }
  
  public boolean maySpawnCreatureAt(Location inLocation)
  {
    for (CB_Requirement theRequirement : _requirements)
    {
      if (theRequirement.maySpawnCreatureAt(inLocation) == false)
      {
        return false;
      }
    }
    
    return true;
  }
}
