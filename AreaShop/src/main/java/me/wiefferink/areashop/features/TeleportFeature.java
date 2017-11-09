package me.wiefferink.areashop.features;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.tools.Utils;
import me.wiefferink.areashop.tools.Value;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeleportFeature extends RegionFeature {

	private static ArrayList<Material> canSpawnIn = new ArrayList<>(Arrays.asList(Material.WOOD_DOOR, Material.WOODEN_DOOR, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE));
	private static ArrayList<Material> cannotSpawnOn = new ArrayList<>(Arrays.asList(Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.CACTUS, Material.IRON_FENCE, Material.FENCE_GATE, Material.THIN_GLASS, Material.NETHER_FENCE, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE, Material.STAINED_GLASS_PANE));
	private static ArrayList<Material> cannotSpawnBeside = new ArrayList<>(Arrays.asList(Material.LAVA, Material.STATIONARY_LAVA, Material.CACTUS));

	public TeleportFeature() {
	}


	public TeleportFeature(GeneralRegion region) {
		this.region = region;
	}

	/**
	 * Get the teleportlocation set for this region.
	 * @return The teleport location, or null if not set
	 */
	public Location getTeleportLocation() {
		return Utils.configToLocation(region.getConfigurationSectionSetting("general.teleportLocation"));
	}

	/**
	 * Check if the region has a teleportLocation specified.
	 * @return true if the region has a teleportlocation, false otherwise
	 */
	public boolean hasTeleportLocation() {
		return region.getConfigurationSectionSetting("general.teleportLocation") != null;
	}

	/**
	 * Set the teleport location of this region.
	 * @param location The location to set as teleport location
	 */
	public void setTeleport(Location location) {
		if(location == null) {
			region.setSetting("general.teleportLocation", null);
		} else {
			region.setSetting("general.teleportLocation", Utils.locationToConfig(location, true));
		}
	}

	/**
	 * Teleport a player to the region or sign.
	 * @param player            Player that should be teleported
	 * @param toSign            true to teleport to the first sign of the region, false for teleporting to the region itself
	 * @param checkRestrictions Set to true if teleport permissions should be checked, false otherwise, also toggles cross-world check
	 * @return true if the teleport succeeded, otherwise false
	 */
	public boolean teleportPlayer(Player player, boolean toSign, boolean checkRestrictions) {

		// Check basics
		if(region.getWorld() == null) {
			region.message(player, "general-noWorld");
			return false;
		}
		if(region.getRegion() == null) {
			region.message(player, "general-noRegion");
			return false;
		}

		if(checkRestrictions) {
			// Check correct world
			if(!region.getBooleanSetting("general.teleportCrossWorld") && !player.getWorld().equals(region.getWorld())) {
				region.message(player, "teleport-wrongWorld", player.getWorld().getName());
				return false;
			}

			boolean owner = player.getUniqueId().equals(region.getOwner());
			boolean friend = region.getFriendsFeature().getFriends().contains(player.getUniqueId());
			boolean available = region.isAvailable();
			// Teleport to sign instead if they dont have permission for teleporting to region
			if((!toSign && owner && !player.hasPermission("areashop.teleport") && player.hasPermission("areashop.teleportsign")
					|| !toSign && !owner && !friend && !player.hasPermission("areashop.teleportall") && player.hasPermission("areashop.teleportsignall")
					|| !toSign && !owner && friend && !player.hasPermission("areashop.teleportfriend") && player.hasPermission("areashop.teleportfriendsign")
					|| !toSign && !owner && !friend && available && !player.hasPermission("areashop.teleportavailable") && player.hasPermission("areashop.teleportavailablesign"))) {
				region.message(player, "teleport-changedToSign");
				toSign = true;
			}
			// Check permissions
			if(owner && !available && !player.hasPermission("areashop.teleport") && !toSign) {
				region.message(player, "teleport-noPermission");
				return false;
			} else if(!owner && !available && !player.hasPermission("areashop.teleportall") && !toSign && !friend) {
				region.message(player, "teleport-noPermissionOther");
				return false;
			} else if(!owner && !available && !player.hasPermission("areashop.teleportfriend") && !toSign && friend) {
				region.message(player, "teleport-noPermissionFriend");
				return false;
			} else if(available && !player.hasPermission("areashop.teleportavailable") && !toSign) {
				region.message(player, "teleport-noPermissionAvailable");
				return false;
			} else if(owner && !available && !player.hasPermission("areashop.teleportsign") && toSign) {
				region.message(player, "teleport-noPermissionSign");
				return false;
			} else if(!owner && !available && !player.hasPermission("areashop.teleportsignall") && toSign && !friend) {
				region.message(player, "teleport-noPermissionOtherSign");
				return false;
			} else if(!owner && !available && !player.hasPermission("areashop.teleportfriendsign") && toSign && friend) {
				region.message(player, "teleport-noPermissionFriendSign");
				return false;
			} else if(available && !player.hasPermission("areashop.teleportavailablesign") && toSign) {
				region.message(player, "teleport-noPermissionAvailableSign");
				return false;
			}
		}

		// Get the starting location
		Value<Boolean> toSignRef = new Value<>(toSign);
		Location startLocation = getStartLocation(player, toSignRef);
		toSign = toSignRef.get();

		boolean insideRegion;
		if(toSign) {
			insideRegion = region.getBooleanSetting("general.teleportToSignIntoRegion");
		} else {
			insideRegion = region.getBooleanSetting("general.teleportIntoRegion");
		}

		// Check locations starting from startLocation and then a cube that increases
		// radius around that (until no block in the region is found at all cube sides)
		Location safeLocation = startLocation;
		ProtectedRegion worldguardRegion = region.getRegion();
		boolean blocksInRegion = worldguardRegion.contains(startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ());
		if(!blocksInRegion && insideRegion) {
			region.message(player, "teleport-blocked");
			return false;
		}

		// Tries limit tracking
		int radius = 1;
		int checked = 1;
		int maxTries = plugin.getConfig().getInt("maximumTries");

		// Tracking of which sides to continue the search
		boolean done = isSafe(safeLocation) && ((blocksInRegion && insideRegion) || (!insideRegion));
		boolean northDone = false, eastDone = false, southDone = false, westDone = false, topDone = false, bottomDone = false;
		boolean continueThisDirection;

		while(((blocksInRegion && insideRegion) || (!insideRegion)) && !done) {
			blocksInRegion = false;

			// North side
			continueThisDirection = false;
			for(int x = -radius + 1; x <= radius && !done && !northDone; x++) {
				for(int y = -radius + 1; y < radius && !done; y++) {
					safeLocation = startLocation.clone().add(x, y, -radius);
					if(safeLocation.getBlockY() > 256 || safeLocation.getBlockY() < 0) {
						continue;
					}
					if(worldguardRegion.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						continueThisDirection = true;
					}
				}
			}
			northDone = northDone || !continueThisDirection;

			// East side
			continueThisDirection = false;
			for(int z = -radius + 1; z <= radius && !done && !eastDone; z++) {
				for(int y = -radius + 1; y < radius && !done; y++) {
					safeLocation = startLocation.clone().add(radius, y, z);
					if(safeLocation.getBlockY() > 256 || safeLocation.getBlockY() < 0) {
						continue;
					}
					if(worldguardRegion.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						continueThisDirection = true;
					}
				}
			}
			eastDone = eastDone || !continueThisDirection;

			// South side
			continueThisDirection = false;
			for(int x = radius - 1; x >= -radius && !done && !southDone; x--) {
				for(int y = -radius + 1; y < radius && !done; y++) {
					safeLocation = startLocation.clone().add(x, y, radius);
					if(safeLocation.getBlockY() > 256 || safeLocation.getBlockY() < 0) {
						continue;
					}
					if(worldguardRegion.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						continueThisDirection = true;
					}
				}
			}
			southDone = southDone || !continueThisDirection;

			// West side
			continueThisDirection = false;
			for(int z = radius - 1; z >= -radius && !done && !westDone; z--) {
				for(int y = -radius + 1; y < radius && !done; y++) {
					safeLocation = startLocation.clone().add(-radius, y, z);
					if(safeLocation.getBlockY() > 256 || safeLocation.getBlockY() < 0) {
						continue;
					}
					if(worldguardRegion.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						continueThisDirection = true;
					}
				}
			}
			westDone = westDone || !continueThisDirection;

			// Top side
			continueThisDirection = false;
			// Middle block of the top
			if((startLocation.getBlockY() + radius) > 256) {
				topDone = true;
			}
			if(!done && !topDone) {
				safeLocation = startLocation.clone().add(0, radius, 0);
				if(worldguardRegion.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
					checked++;
					done = isSafe(safeLocation) || checked > maxTries;
					blocksInRegion = true;
					continueThisDirection = true;
				}
			}
			for(int r = 1; r <= radius && !done && !topDone; r++) {
				// North
				for(int x = -r + 1; x <= r && !done; x++) {
					safeLocation = startLocation.clone().add(x, radius, -r);
					if(worldguardRegion.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						continueThisDirection = true;
					}
				}
				// East
				for(int z = -r + 1; z <= r && !done; z++) {
					safeLocation = startLocation.clone().add(r, radius, z);
					if(worldguardRegion.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						continueThisDirection = true;
					}
				}
				// South side
				for(int x = r - 1; x >= -r && !done; x--) {
					safeLocation = startLocation.clone().add(x, radius, r);
					if(worldguardRegion.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						continueThisDirection = true;
					}
				}
				// West side
				for(int z = r - 1; z >= -r && !done; z--) {
					safeLocation = startLocation.clone().add(-r, radius, z);
					if(worldguardRegion.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						continueThisDirection = true;
					}
				}
			}
			topDone = topDone || !continueThisDirection;

			// Bottom side
			continueThisDirection = false;
			// Middle block of the bottom
			if(startLocation.getBlockY() - radius < 0) {
				bottomDone = true;
			}
			if(!done && !bottomDone) {
				safeLocation = startLocation.clone().add(0, -radius, 0);
				if(worldguardRegion.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
					checked++;
					done = isSafe(safeLocation) || checked > maxTries;
					blocksInRegion = true;
					continueThisDirection = true;
				}
			}
			for(int r = 1; r <= radius && !done && !bottomDone; r++) {
				// North
				for(int x = -r + 1; x <= r && !done; x++) {
					safeLocation = startLocation.clone().add(x, -radius, -r);
					if(worldguardRegion.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						continueThisDirection = true;
					}
				}
				// East
				for(int z = -r + 1; z <= r && !done; z++) {
					safeLocation = startLocation.clone().add(r, -radius, z);
					if(worldguardRegion.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						continueThisDirection = true;
					}
				}
				// South side
				for(int x = r - 1; x >= -r && !done; x--) {
					safeLocation = startLocation.clone().add(x, -radius, r);
					if(worldguardRegion.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						continueThisDirection = true;
					}
				}
				// West side
				for(int z = r - 1; z >= -r && !done; z--) {
					safeLocation = startLocation.clone().add(-r, -radius, z);
					if(worldguardRegion.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						continueThisDirection = true;
					}
				}
			}
			bottomDone = bottomDone || !continueThisDirection;

			// Increase cube radius
			radius++;
		}
		if(done && isSafe(safeLocation)) {
			if(toSign) {
				region.message(player, "teleport-successSign");

				// Let the player look at the sign
				Vector playerVector = safeLocation.toVector();
				playerVector.setY(playerVector.getY() + player.getEyeHeight(true));
				Vector signVector = region.getSignsFeature().getSigns().get(0).getLocation().toVector().add(new Vector(0.5, 0.5, 0.5));
				Vector direction = playerVector.clone().subtract(signVector).normalize();
				safeLocation.setYaw(180 - (float)Math.toDegrees(Math.atan2(direction.getX(), direction.getZ())));
				safeLocation.setPitch(90 - (float)Math.toDegrees(Math.acos(direction.getY())));
			} else {
				region.message(player, "teleport-success");
			}

			player.teleport(safeLocation);
			AreaShop.debug("Found location: " + safeLocation.toString() + " Tries: " + (checked - 1));
			return true;
		} else {
			region.message(player, "teleport-noSafe", checked - 1, maxTries);
			AreaShop.debug("No location found, checked " + (checked - 1) + " spots of max " + maxTries);
			return false;
		}
	}

	/**
	 * Teleport a player to the region or sign when he has permissions for it.
	 * @param player Player that should be teleported
	 * @param toSign true to teleport to the first sign of the region, false for teleporting to the region itself
	 * @return true if the teleport succeeded, otherwise false
	 */
	public boolean teleportPlayer(Player player, boolean toSign) {
		return teleportPlayer(player, toSign, true);
	}

	/**
	 * Teleport a player to the region when he has permissions for it.
	 * @param player Player that should be teleported
	 * @return true if the teleport succeeded, otherwise false
	 */
	public boolean teleportPlayer(Player player) {
		return teleportPlayer(player, false, true);
	}

	/**
	 * Checks if a certain location is safe to teleport to.
	 * @param location The location to check
	 * @return true if it is safe, otherwise false
	 */
	private boolean isSafe(Location location) {
		Block feet = location.getBlock();
		Block head = feet.getRelative(BlockFace.UP);
		Block below = feet.getRelative(BlockFace.DOWN);
		Block above = head.getRelative(BlockFace.UP);

		// Check the block at the feet and head of the player
		if((feet.getType().isSolid() && !canSpawnIn.contains(feet.getType())) || feet.isLiquid()) {
			return false;
		} else if((head.getType().isSolid() && !canSpawnIn.contains(head.getType())) || head.isLiquid()) {
			return false;
		} else if(!below.getType().isSolid() || cannotSpawnOn.contains(below.getType()) || below.isLiquid()) {
			return false;
		} else if(above.isLiquid() || cannotSpawnBeside.contains(above.getType())) {
			return false;
		}

		// Get all blocks around the player (below foot level, foot level, head level and above head level)
		Set<Material> around = new HashSet<>();
		for(int y = 0; y <= 3; y++) {
			for(int x = -1; x <= 1; x++) {
				for(int z = -1; z <= 1; z++) {
					// Skip blocks in the column of the player
					if(x == 0 && z == 0) {
						continue;
					}

					around.add(below.getRelative(x, y, z).getType());
				}
			}
		}

		// Check the blocks around the player
		for(Material material : around) {
			if(cannotSpawnBeside.contains(material)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get the start location of a safe teleport search.
	 * @param player The player to get it for
	 * @param toSign true to try teleporting to the first sign, false for teleporting to the region
	 * @return The start location
	 */
	private Location getStartLocation(Player player, Value<Boolean> toSign) {
		Location startLocation = null;
		ProtectedRegion worldguardRegion = region.getRegion();

		// Try to get sign location
		List<SignsFeature.RegionSign> signs = region.getSignsFeature().getSigns();
		boolean signAvailable = !signs.isEmpty();
		if(toSign.get()) {
			if(signAvailable) {
				// Use the location 1 below the sign to prevent weird spawing above the sign
				startLocation = signs.get(0).getLocation(); //.subtract(0.0, 1.0, 0.0);
				startLocation.setPitch(player.getLocation().getPitch());
				startLocation.setYaw(player.getLocation().getYaw());

				// Move player x blocks away from the sign
				double distance = region.getDoubleSetting("general.teleportSignDistance");
				if(distance > 0) {
					BlockFace facing = region.getSignsFeature().getSigns().get(0).getFacing();
					Vector facingVector = new Vector(facing.getModX(), facing.getModY(), facing.getModZ())
							.normalize()
							.multiply(distance);
					startLocation.setX(startLocation.getBlockX() + 0.5);
					startLocation.setZ(startLocation.getBlockZ() + 0.5);
					startLocation.add(facingVector);
				}
			} else {
				// No sign available
				region.message(player, "teleport-changedToNoSign");
				toSign.set(false);
			}
		}

		// Use teleportation location that is set for the region
		if(startLocation == null && hasTeleportLocation()) {
			startLocation = getTeleportLocation();
		}

		// Calculate a default location
		if(startLocation == null) {
			// Set to block in the middle, y configured in the config
			com.sk89q.worldedit.Vector middle = com.sk89q.worldedit.Vector.getMidpoint(worldguardRegion.getMaximumPoint(), worldguardRegion.getMinimumPoint());
			String configSetting = region.getStringSetting("general.teleportLocationY");
			if("bottom".equalsIgnoreCase(configSetting)) {
				middle = middle.setY(worldguardRegion.getMinimumPoint().getBlockY());
			} else if("top".equalsIgnoreCase(configSetting)) {
				middle = middle.setY(worldguardRegion.getMaximumPoint().getBlockY());
			} else if("middle".equalsIgnoreCase(configSetting)) {
				middle = middle.setY(middle.getBlockY());
			} else {
				try {
					int vertical = Integer.parseInt(configSetting);
					middle = middle.setY(vertical);
				} catch(NumberFormatException e) {
					AreaShop.warn("Could not parse general.teleportLocationY: '" + configSetting + "'");
				}
			}
			startLocation = new Location(region.getWorld(), middle.getX(), middle.getY(), middle.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
		}

		// Set location in the center of the block
		startLocation.setX(startLocation.getBlockX() + 0.5);
		startLocation.setZ(startLocation.getBlockZ() + 0.5);

		return startLocation;
	}


}
