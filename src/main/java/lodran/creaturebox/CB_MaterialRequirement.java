package lodran.creaturebox;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import java.util.HashSet;
import org.bukkit.Material;

public class CB_MaterialRequirement extends CB_FaceRequirement
{
  private HashSet<Material> _materials = new HashSet<Material>();
    
  public CB_MaterialRequirement(BlockFace inBlockFace,
                             Material inMaterials[])
  {
    super(inBlockFace);
    
    for (Material theMaterial : inMaterials)
    {
      this._materials.add(theMaterial);
    }    
  }
  
  public boolean maySpawnCreatureAt(Location inLocation)
  {
    Material theMaterial = this.getFace(inLocation).getType();
    
    boolean theResult = this._materials.contains(theMaterial);
    
    return theResult;
  }
}
