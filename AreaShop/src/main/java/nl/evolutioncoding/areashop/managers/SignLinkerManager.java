package nl.evolutioncoding.areashop.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.regions.GeneralRegion;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.Sign;
import org.bukkit.util.BlockIterator;

public class SignLinkerManager implements Listener {
	private AreaShop plugin = null;
	private Map<UUID, SignLinker> signLinkers;
	private boolean eventsRegistered;
	
	public SignLinkerManager(AreaShop plugin) {
		this.plugin = plugin;
		signLinkers = new HashMap<UUID, SignLinker>();
		eventsRegistered = false;
	}
	
	/**
	 * Let a player enter sign linking mode
	 * @param player The player that has to enter sign linking mode
	 * @param profile The profile to use for the signs (null for default)
	 */
	public void enterSignLinkMode(Player player, String profile) {
		signLinkers.put(player.getUniqueId(), new SignLinker(player, profile));
		plugin.message(player, "linksigns-first");
		plugin.message(player, "linksigns-next");
		if(!eventsRegistered) {
			eventsRegistered = true;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}
	
	/**
	 * Let a player exit sign linking mode
	 * @param player The player that has to exit sign linking mode
	 */
	public void exitSignLinkMode(Player player) {
		signLinkers.remove(player.getUniqueId());
		if(eventsRegistered && signLinkers.isEmpty()) {
			eventsRegistered = false;
			HandlerList.unregisterAll(this);
		}
		plugin.message(player, "linksigns-stopped");
	}
	
	/**
	 * Check if the player is in sign linking mode
	 * @param player The player to check
	 * @return true if the player is in sign linking mode, otherwise false
	 */
	public boolean isInSignLinkMode(Player player) {
		return signLinkers.containsKey(player.getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		AreaShop.debug("PlayerInteractEvent of " + event.getPlayer().getName() + ", " + signLinkers.size() + " signlinkers");
		if(isInSignLinkMode(event.getPlayer())) {
			event.setCancelled(true);
			Player player = event.getPlayer();
			SignLinker linker = signLinkers.get(event.getPlayer().getUniqueId());
			if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				// Get the region
				BlockIterator blockIterator = new BlockIterator(player, 100);
				while(blockIterator.hasNext()) {
					Block next = blockIterator.next();
					List<GeneralRegion> regions = Utils.getASRegionsByLocation(next.getLocation());
					if(regions.size() == 1) {
						linker.setRegion(regions.get(0));
						return;
					} else if(regions.size() > 1) {
						Set<String> names = new HashSet<String>();
						for(GeneralRegion region : regions) {
							names.add(region.getName());
						}
						plugin.message(player, "linksigns-multipleRegions", Utils.createCommaSeparatedList(names));
						plugin.message(player, "linksigns-multipleRegionsAdvice");
						return;
					}					
				}
				// No regions found within the maximum range
				plugin.message(player, "linksigns-noRegions");
				return;			
			} else if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
				Block block = null;
				BlockIterator blockIterator = new BlockIterator(player, 100);
				while(blockIterator.hasNext() && block == null) {
					Block next = blockIterator.next();
					if(next.getType() != Material.AIR) {
						block = next;
					}
				}
				if(block == null || !(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)) {
					plugin.message(player, "linksigns-noSign");
					return;
				}
				
				GeneralRegion signRegion = plugin.getFileManager().getRegionBySignLocation(block.getLocation());
				if(signRegion != null) {
					plugin.message(player, "linksigns-alreadyRegistered", signRegion);
					return;
				}
				Sign sign = (Sign)block.getState().getData();
				linker.setSign(block.getLocation(), block.getType(), sign.getFacing());
				return;
			}
			
		}
	}
	
	/**
	 * Handle disconnection players
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeave(PlayerQuitEvent event) {
		AreaShop.debug("PlayerQuitEvent of " + event.getPlayer().getName() + ", " + signLinkers.size() + " signlinkers");
		signLinkers.remove(event.getPlayer().getUniqueId());
	}
	
	/**
	 * Class to keep track of the signlinking data
	 */
	private class SignLinker {
		private boolean hasSign = false;
		private boolean hasRegion = false;
		
		public Player linker = null;
		public String profile = null;
		
		public GeneralRegion region = null;
		
		public Location location = null;
		public Material type = null;
		public BlockFace facing = null;
				
		public SignLinker(Player linker, String profile) {
			this.linker = linker;
			this.profile = profile;
		}
		
		public void setRegion(GeneralRegion region) {
			this.region = region;
			hasRegion = true;
			if(!isComplete()) {
				plugin.message(linker, "linksigns-regionFound", region);
			}
			finalize();
		}
		
		public void setSign(Location location, Material type, BlockFace facing) {
			this.location = location;
			this.type = type;
			this.facing = facing;
			hasSign = true;
			if(!isComplete()) {
				plugin.message(linker, "linksigns-signFound", location.getBlockX(), location.getBlockY(), location.getBlockZ());
			}
			finalize();
		}
		
		public void finalize() {
			if(isComplete()) {
				region.addSign(location, type, facing, profile);
				if(profile == null) {
					plugin.message(linker, "addsign-success", region);
				} else {
					plugin.message(linker, "addsign-successProfile", region, profile);
				}
				region.updateSigns();
				reset();
				
				plugin.message(linker, "linksigns-next");
			}
		}
		
		public void reset() {
			hasSign = false;
			hasRegion = false;
		}
		
		public boolean isComplete() {
			return hasSign && hasRegion;
		}
	}
	

}
















