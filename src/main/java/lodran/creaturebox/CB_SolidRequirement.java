package lodran.creaturebox;

import org.bukkit.block.Block;
import org.bukkit.Material;
import java.util.HashSet;
import org.bukkit.block.BlockFace;

// Materials that are solid enough for a creature walk on.

public class CB_SolidRequirement extends CB_MaterialRequirement
{
  private static Material __SolidMaterials[] =
  {
  Material.STONE,
  Material.GRASS,
  Material.DIRT,
  Material.COBBLESTONE,
  Material.WOOD,
  Material.BEDROCK,
  Material.SAND,
  Material.GRAVEL,
  Material.GOLD_ORE,
  Material.IRON_ORE,
  Material.COAL_ORE,
  Material.LOG,
  Material.SPONGE,
  Material.LAPIS_ORE,
  Material.LAPIS_BLOCK,
  Material.DISPENSER,
  Material.SANDSTONE,
  Material.NOTE_BLOCK,
  Material.WOOL,
  Material.GOLD_BLOCK,
  Material.IRON_BLOCK,
  Material.DOUBLE_STEP,
  Material.BRICK,
  Material.TNT,
  Material.BOOKSHELF,
  Material.MOSSY_COBBLESTONE,
  Material.OBSIDIAN,
  Material.MOB_SPAWNER,
  Material.CHEST,
  Material.DIAMOND_ORE,
  Material.DIAMOND_BLOCK,
  Material.WORKBENCH,
  Material.SOIL,
  Material.FURNACE,
  Material.BURNING_FURNACE,
  Material.REDSTONE_ORE,
  Material.GLOWING_REDSTONE_ORE,
  Material.ICE, 	
  Material.SNOW_BLOCK, 	
  Material.CLAY,
  Material.JUKEBOX,
  Material.PUMPKIN,
  Material.NETHERRACK,
  Material.SOUL_SAND,
  Material.GLOWSTONE,
  Material.JACK_O_LANTERN 	
  };
  
  public CB_SolidRequirement(BlockFace inBlockFace)
  {
    super (inBlockFace, __SolidMaterials);
  }
}

