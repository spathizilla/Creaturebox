package lodran.creaturebox;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

public class CB_DarkRequirement extends CB_FaceRequirement
{
  public CB_DarkRequirement(BlockFace inBlockFace)
  {
    super(inBlockFace);
  }
  
  public boolean maySpawnCreatureAt(Location inLocation)
  {
    BlockState theBlockState = this.getFace(inLocation).getState();
    return this.getFace(inLocation).getState().getLightLevel() <= 7;
  }
}
