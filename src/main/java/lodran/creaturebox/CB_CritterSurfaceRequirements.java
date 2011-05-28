package lodran.creaturebox;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class CB_CritterSurfaceRequirements extends CB_RequirementSet
{
  static CB_Requirement __requirements[] =
  {
  new CB_GrassRequirement(BlockFace.DOWN),
  };
  
  public CB_CritterSurfaceRequirements()
  {
    super(__requirements);
  }
}
