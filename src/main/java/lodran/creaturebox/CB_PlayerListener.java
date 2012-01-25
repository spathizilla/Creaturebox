package lodran.creaturebox;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.Material;
import org.bukkit.inventory.PlayerInventory;

public class CB_PlayerListener extends PlayerListener
{
  /******************************************************************************/
  
  private final CreatureboxPlugin _plugin;
  
  /******************************************************************************/
  
  public CB_PlayerListener(final CreatureboxPlugin inPlugin)
  {
    this._plugin = inPlugin;
  }
  
  /******************************************************************************/

  public void onEnable()
  {
    PluginManager theManager = _plugin.getServer().getPluginManager();
    
    theManager.registerEvent(Event.Type.PLAYER_INTERACT, this, Event.Priority.Normal, _plugin);
  }
  
  /******************************************************************************/

  public void onPlayerInteract(PlayerInteractEvent inEvent)
  {
	 if(!inEvent.isCancelled()) {
		ItemStack theItemStack = inEvent.getItem();
		if ((theItemStack != null) && (theItemStack.getType() == Material.MOB_SPAWNER)) {
			if(CreatureboxPlugin._legacyData) {
				_plugin.setLastDurability(theItemStack.getDurability());
			} else {
				Enchantment enchant = Enchantment.getByName("OXYGEN");
				_plugin.setLastEnchant((short)theItemStack.getEnchantmentLevel(enchant));
			}
	 	}
	 }
  }
}