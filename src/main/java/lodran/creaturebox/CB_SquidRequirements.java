package lodran.creaturebox;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class CB_SquidRequirements extends CB_RequirementSet
{
  static CB_Requirement __squidRequirements[] =
  {
  new CB_WaterRequirement(BlockFace.UP),
  new CB_WaterRequirement(BlockFace.SELF),
  };
  
  public CB_SquidRequirements()
  {
    super(__squidRequirements);
  }
}
