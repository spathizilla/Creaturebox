package lodran.creaturebox;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

abstract class CB_FaceRequirement extends CB_Requirement
{
  BlockFace _blockFace;
  
  public CB_FaceRequirement(BlockFace inBlockFace)
  {
    super();
    
    _blockFace = inBlockFace;
  }
  
  public Block getFace(Location inLocation)
  {
	Block above = inLocation.getBlock().getRelative(_blockFace);
    return above;
  }
}
