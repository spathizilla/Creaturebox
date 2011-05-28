package lodran.creaturebox;

import org.bukkit.block.Block;
import org.bukkit.Material;
import java.util.HashSet;
import org.bukkit.block.BlockFace;

public class CB_GrassRequirement extends CB_MaterialRequirement
{
  static Material __grassMaterials[] = {
  Material.GRASS,
  };
  
  public CB_GrassRequirement(BlockFace inBlockFace)
  {
    super(inBlockFace, __grassMaterials);
  }
}

