package lodran.creaturebox;

import org.bukkit.block.Block;
import org.bukkit.Material;
import java.util.HashSet;
import org.bukkit.block.BlockFace;

// Materials that are breathable.

public class CB_BreathableRequirement extends CB_MaterialRequirement
{
  private static Material __BreathableMaterials[] =
  {
  Material.AIR,
  Material.SAPLING,
  Material.YELLOW_FLOWER,
  Material.RED_ROSE,
  Material.BROWN_MUSHROOM,
  Material.RED_MUSHROOM,
  Material.TORCH,
  Material.FIRE,
  Material.REDSTONE_WIRE,
  Material.CROPS,
  Material.SIGN_POST,
  Material.WOODEN_DOOR,
  Material.LADDER,
  Material.RAILS,
  Material.WALL_SIGN,
  Material.LEVER,
  Material.STONE_PLATE,
  Material.IRON_DOOR_BLOCK,
  Material.WOOD_PLATE,
  Material.REDSTONE_TORCH_OFF,
  Material.REDSTONE_TORCH_ON,
  Material.STONE_BUTTON,
  Material.SNOW,
  Material.SUGAR_CANE_BLOCK,
  Material.DIODE_BLOCK_OFF,
  Material.DIODE_BLOCK_ON,
  };
  
  public CB_BreathableRequirement(BlockFace inBlockFace)
  {
    super (inBlockFace, __BreathableMaterials);
  }
}

