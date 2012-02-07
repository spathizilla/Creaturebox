package lodran.creaturebox;

import org.bukkit.Location;
import org.bukkit.Server;
import java.util.HashMap;
import org.bukkit.Chunk;

class CB_Location extends Location
{
  public CB_Location(Location inValue)
  {
    super(inValue.getWorld(),
          inValue.getBlockX(), inValue.getBlockY(), inValue.getBlockZ());
  }

  public CB_Location(CreatureboxPlugin inPlugin, String world, Double x, Double y, Double z)
  {
    super(inPlugin.getServer().getWorld(world), x, y, z);
  }
  
  public CB_Location(CreatureboxPlugin inPlugin,
                     HashMap<String, Object> inSettings)
  {
    super(inPlugin.getServer().getWorld((String) inSettings.get("World")),
          (Integer)inSettings.get("X"),
          (Integer)inSettings.get("Y"),
          (Integer)inSettings.get("Z"));
 
    if (this.getWorld() == null)
      throw new NullPointerException();
  }    
  
  @Override public boolean equals(Object inOtherObject)
  {
    if (inOtherObject instanceof CB_Location)
    {
      CB_Location theOtherLocation = (CB_Location) inOtherObject;
      return ((this.getWorld().getName().equals(theOtherLocation.getWorld().getName())) &&
              (this.getBlockX() == theOtherLocation.getBlockX()) &&
              (this.getBlockY() == theOtherLocation.getBlockY()) &&
              (this.getBlockZ() == theOtherLocation.getBlockZ()));
    }

    return false;
  }
  
  @Override public int hashCode()
  {
    int theResult = this.getWorld().getName().hashCode();
    theResult = HashCodeUtil.hash(theResult, this.getBlockX());
    theResult = HashCodeUtil.hash(theResult, this.getBlockY());
    theResult = HashCodeUtil.hash(theResult, this.getBlockZ());
    
    return theResult;
  }
  
  public HashMap<String, Object> getSettings()
  {
    HashMap<String, Object> theResult = new HashMap<String, Object>();
    theResult.put("World", this.getWorld().getName());
    theResult.put("X", this.getBlockX());
    theResult.put("Y", this.getBlockY());
    theResult.put("Z", this.getBlockZ());
    return theResult;
  }
  
  public CB_Location(HashMap<String, Object> inSettings,
                     Server inServer)
  {
    super(inServer.getWorld((String) inSettings.get("World")),
          (Integer)inSettings.get("X"),
          (Integer)inSettings.get("Y"),
          (Integer)inSettings.get("Z"));
  }
  
  
  public boolean isChunkLoaded()
  {
    boolean theResult = this.getWorld().isChunkLoaded(this.getChunk());
    return theResult;
  }
  
  public Chunk getChunk()
  {
    return this.getWorld().getChunkAt(this);
  }
}