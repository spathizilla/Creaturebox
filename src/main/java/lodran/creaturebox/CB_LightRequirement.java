package lodran.creaturebox;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public class CB_LightRequirement extends CB_FaceRequirement
{
  public CB_LightRequirement(BlockFace inBlockFace)
  {
    super(inBlockFace);
  }
  
  public boolean maySpawnCreatureAt(Location inLocation)
  {
    return this.getFace(inLocation).getState().getLightLevel() >= 9;
  }
}
