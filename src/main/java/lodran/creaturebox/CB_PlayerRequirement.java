package lodran.creaturebox;

import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.block.BlockFace;

public class CB_PlayerRequirement extends CB_Requirement
{
  public CB_PlayerRequirement()
  {
  }
  
  public boolean maySpawnCreatureAt(Location inLocation)
  {
    for (Player thePlayer : inLocation.getWorld().getPlayers())
    {
      Location thePlayerLocation = thePlayer.getLocation();
      double theDX = Math.abs(inLocation.getX() - thePlayerLocation.getX());
      double theDY = Math.abs(inLocation.getY() - thePlayerLocation.getY());
      double theDZ = Math.abs(inLocation.getZ() - thePlayerLocation.getZ());
      
      if ((theDX < 24) &&
          (theDY < 24) &&
          (theDZ < 24))
      {
        return true;
      }
    }
    
    return false;
  }
}
