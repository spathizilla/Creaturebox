package lodran.creaturebox;

import org.bukkit.block.Block;
import org.bukkit.Material;
import java.util.HashSet;
import org.bukkit.block.BlockFace;

public class CB_WaterRequirement extends CB_MaterialRequirement
{
  static Material __waterMaterials[] =
  {
  Material.WATER,
  Material.STATIONARY_WATER
  };
  
  public CB_WaterRequirement(BlockFace inBlockFace)
  {
    super(inBlockFace, __waterMaterials);
  }
}

