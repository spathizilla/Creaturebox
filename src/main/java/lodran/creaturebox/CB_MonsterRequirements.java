package lodran.creaturebox;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class CB_MonsterRequirements extends CB_RequirementSet
{
  static CB_Requirement __monsterRequirements[] =
  {
  new CB_BreathableRequirement(BlockFace.UP),
  new CB_MovableRequirement(BlockFace.SELF),
  new CB_SolidRequirement(BlockFace.DOWN)
  };
  
  public CB_MonsterRequirements()
  {
    super(__monsterRequirements);
  }
}
