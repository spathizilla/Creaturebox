package lodran.creaturebox;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class CB_PigZombieRequirements extends CB_RequirementSet
{
  static CB_Requirement __pigZombieRequirements[] =
  {
  new CB_BreathableRequirement(BlockFace.UP),
  new CB_MovableRequirement(BlockFace.SELF),
  new CB_SolidRequirement(BlockFace.DOWN)
  };
  
  public CB_PigZombieRequirements()
  {
    super(__pigZombieRequirements);
  }
}
