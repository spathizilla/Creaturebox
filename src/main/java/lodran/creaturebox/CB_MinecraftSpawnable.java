package lodran.creaturebox;

import org.bukkit.entity.LivingEntity;
import org.bukkit.Location;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

class CB_MinecraftSpawnable extends CB_Spawnable
{  
  public CB_MinecraftSpawnable(String inCreatureName,
                               Integer inCreatureIndex,
                               HashMap <String, CB_Requirement> inRequirements)
  {
    super(inCreatureName,
          inCreatureIndex,
          false,
          inRequirements);
  }
  
  LivingEntity spawnCreatureAt(Location inLocation,
                           ArrayList<String> inRequirementKeys)
  {
    return null;
  }
}
