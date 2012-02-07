package lodran.creaturebox;

import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Listener;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;

/**
 * creaturebox block listener
 * @author lodran
 */
public class CB_BlockListener implements Listener
{
	/******************************************************************************/

	private final CreatureboxPlugin _plugin;

	/******************************************************************************/

	public CB_BlockListener(final CreatureboxPlugin inPlugin)
	{
		this._plugin = inPlugin;
	}

	/******************************************************************************/

	public void onEnable()
	{
		PluginManager theManager = _plugin.getServer().getPluginManager();
		theManager.registerEvents(this, this._plugin);
	}

	/******************************************************************************/

	@EventHandler(priority = EventPriority.MONITOR) 
	public void onBlockBreak(BlockBreakEvent inEvent)
	{
		Player player = inEvent.getPlayer();
		if (inEvent.isCancelled())
			return;
		
		Block spawner = inEvent.getBlock();

		if (spawner.getType() != Material.MOB_SPAWNER && spawner.getType() != Material.REDSTONE_TORCH_ON) {			
			return;
		}
		
		if(spawner.getType() == Material.MOB_SPAWNER) {
			if(_plugin.permission(player, "creaturebox.dropspawner", false) == false) {
				inEvent.setCancelled(true);
				CreatureboxPlugin.message(player, CreatureboxPlugin.messageError, "You do not have permission to break spawners.");
				return;
			}
			handleSpawnerBreak(inEvent);
		} else if(spawner.getType() == Material.REDSTONE_TORCH_ON) {
			handleTorchBreak(inEvent);
		}

	}

	/******************************************************************************/

	// Still handle mobspawner blocks but dont do anything with enchantments/durability.
	@EventHandler(priority = EventPriority.MONITOR)  
	public void onBlockPlace(BlockPlaceEvent inEvent)
	{
		Player thePlayer = inEvent.getPlayer();
		Block theBlock = inEvent.getBlock();

		if ((theBlock.getType() != Material.MOB_SPAWNER) ||
				(inEvent.isCancelled()))
			return;

		if (_plugin.permission(thePlayer, "creaturebox.placespawner", true) == false)
			return;

		CB_Spawnable theSpawnable = CB_Spawnable.getSpawnableOf("pig");

		String thePermission = "creaturebox.creature." + theSpawnable.getCreatureName().toLowerCase();
		if(_plugin.permission(thePlayer, thePermission, true) == false) {
			CreatureboxPlugin.message(thePlayer, CreatureboxPlugin.messageError, "You do not have permission to place " + theSpawnable.getCreatureName() + " spawners");
			inEvent.setCancelled(true);
			return;
		}

		CB_Spawner theSpawner = _plugin.getSpawner(new CB_Location(theBlock.getLocation()));
		Location theSpawnerLoc = theBlock.getLocation();
		theSpawner.setSpawns(theSpawnable);

		_plugin.putSpawner(theSpawner);

		theSpawner.showSettings(thePlayer);

		if(CreatureboxPlugin.showPlacements) {
			CreatureboxPlugin.logline(thePlayer.getName() + " placed a " + theSpawner.getCreatureType().getName() + " spawner in " + theSpawnerLoc.getWorld().getName() + " at: x: "+ theSpawnerLoc.getBlockX() +" y:"+ theSpawnerLoc.getBlockY()+ " z:" + theSpawnerLoc.getBlockZ());
		}
	}

	/******************************************************************************/

	@EventHandler(priority = EventPriority.MONITOR) 
	public void onBlockPhysics(BlockPhysicsEvent inEvent)
	{
		Block theBlock = inEvent.getBlock();

		if ((theBlock.getType() != Material.MOB_SPAWNER) ||
				(inEvent.isCancelled()))
			return;

		CB_Spawner theSpawner = _plugin.artificialSpawnerAt(new CB_Location(theBlock.getLocation()));

		if (theSpawner != null)
			theSpawner.runRedstone();
	}

	/******************************************************************************/
	public void handleTorchBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block target = event.getBlock();
		
		if(event.isCancelled()
				|| target.getType() != Material.REDSTONE_TORCH_ON 
				|| player.getItemInHand().getType() != Material.MONSTER_EGG) {
			return;
		}
		
		if(_plugin.permission(player, "creaturebox.placespawner", false) == false) {
			event.setCancelled(true);
			CreatureboxPlugin.message(player, CreatureboxPlugin.messageError, "You do not have permission to place spawners.");
			return;
		}
		
		ItemStack inHand = player.getItemInHand();
		Location loc = target.getLocation();

		CreatureType cType = CB_Utils.getMobFromDurability(inHand.getDurability());
		CB_Spawnable theSpawnable = CB_Spawnable.getSpawnableOf(cType);

		if (theSpawnable == null) {
			theSpawnable = CB_Spawnable.getSpawnableOf("pig");
		}

		String thePermission = "creaturebox.creature." + cType.getName().toLowerCase();
		if(_plugin.permission(player, thePermission, true) == false) {
			CreatureboxPlugin.message(player, CreatureboxPlugin.messageError, "You do not have permission to place " + cType.getName() + " spawners");
			event.setCancelled(true);
			return;
		}
		
		event.setCancelled(true);

		target.setType(Material.MOB_SPAWNER);
		CreatureSpawner spawner = (CreatureSpawner) target.getState();
		spawner.setCreatureType(cType);

		CB_Spawner theSpawner = _plugin.getSpawner(new CB_Location(loc));
		theSpawner.setSpawns(theSpawnable);
		_plugin.putSpawner(theSpawner);
		theSpawner.showSettings(player);

		if(CreatureboxPlugin.showPlacements) {
			CreatureboxPlugin.logline(player.getName() + " placed a " + cType.getName() + " spawner in " + loc.getWorld().getName() + " at: x: "+ loc.getBlockX() +" y:"+ loc.getBlockY()+ " z:" + loc.getBlockZ());
		}

		if(target.getState() instanceof CreatureSpawner && player.getGameMode() == GameMode.SURVIVAL) {
			if(inHand.getAmount() > 1) {
				inHand.setAmount(inHand.getAmount() - 1);
			} else {
				player.getInventory().setItemInHand(null);
			}
			player.updateInventory();
		}
	}
	
	/******************************************************************************/
	
	public void handleSpawnerBreak(BlockBreakEvent inEvent) {
		Player player = inEvent.getPlayer();
		Block spawner = inEvent.getBlock();
		World world = spawner.getWorld();
		Location loc = spawner.getLocation();

		CreatureSpawner theSpawner = (CreatureSpawner) spawner.getState();
		CB_Spawnable theSpawnable = CB_Spawnable.getSpawnableOf(theSpawner.getCreatureType());

		String thePermission = "creaturebox.creature." + theSpawnable.getCreatureName();
		if(_plugin.permission(player, thePermission, true) == false) {
			CreatureboxPlugin.message(player, CreatureboxPlugin.messageError, "You do not have permission to break " + theSpawnable.getCreatureName() + " spawners");
			inEvent.setCancelled(true);
			return;
		}

		// Fix for mcMMO's weird berserk drop mechanics
		loc.getBlock().setType(Material.AIR);
		// End
		
		int theCreatureIndex = theSpawnable.getCreatureIndex();
		ItemStack spawnegg = new ItemStack(Material.MONSTER_EGG, 1, (short) theCreatureIndex);
		world.dropItemNaturally(loc, spawnegg);

		if(CreatureboxPlugin.showPlacements) {
			CreatureboxPlugin.logline(player.getName() + " destroyed a " + theSpawner.getCreatureType().getName() + " spawner in " + loc.getWorld().getName() + " at: x: "+ loc.getBlockX() +" y:"+ loc.getBlockY()+ " z:" + loc.getBlockZ());
		}
	}

}
