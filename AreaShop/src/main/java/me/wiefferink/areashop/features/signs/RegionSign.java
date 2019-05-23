package me.wiefferink.areashop.features.signs;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.tools.Materials;
import me.wiefferink.areashop.tools.Utils;
import me.wiefferink.interactivemessenger.processing.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Sign that is connected to a region to display information and interact with the region.
 */
public class RegionSign {

	private final SignsFeature signsFeature;
	private final String key;

	public RegionSign(SignsFeature signsFeature, String key) {
		this.signsFeature = signsFeature;
		this.key = key;
	}

	/**
	 * Get the location of this sign.
	 * @return The location of this sign
	 */
	public Location getLocation() {
		return Utils.configToLocation(getRegion().getConfig().getConfigurationSection("general.signs." + key + ".location"));
	}

	/**
	 * Location string to be used as key in maps.
	 * @return Location string
	 */
	public String getStringLocation() {
		return SignsFeature.locationToString(getLocation());
	}

	/**
	 * Chunk string to be used as key in maps.
	 * @return Chunk string
	 */
	public String getStringChunk() {
		return SignsFeature.chunkToString(getLocation());
	}

	/**
	 * Get the region this sign is linked to.
	 * @return The region this sign is linked to
	 */
	public GeneralRegion getRegion() {
		return signsFeature.getRegion();
	}

	/**
	 * Remove this sign from the region.
	 */
	public void remove() {
		getLocation().getBlock().setType(Material.AIR);
		signsFeature.getSignsRef().remove(getStringLocation());
		SignsFeature.getAllSigns().remove(getStringLocation());
		SignsFeature.getSignsByChunk().get(getStringChunk()).remove(this);
		getRegion().setSetting("general.signs." + key, null);
	}

	/**
	 * Get the ConfigurationSection defining the sign layout.
	 * @return The sign layout config
	 */
	public ConfigurationSection getProfile() {
		return getRegion().getConfigurationSectionSetting("general.signProfile", "signProfiles", getRegion().getConfig().get("general.signs." + key + ".profile"));
	}

	/**
	 * Get the facing of the sign.
	 * @return BlockFace the sign faces, or null if unknown
	 */
	public BlockFace getFacing() {
		try {
			return BlockFace.valueOf(getRegion().getConfig().getString("general.signs." + key + ".facing"));
		} catch(NullPointerException | IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Get the material of the sign
	 * @return Material of the sign, normally {@link Material#WALL_SIGN} or {@link Material#SIGN_POST}, but could be something else or null.
	 */
	public Material getMaterial() {
		String name = getRegion().getConfig().getString("general.signs." + key + ".signType");
		if (name.contains("WALL_SIGN")) {
			return Materials.wallSign;
		} else if (name.contains("SIGN")) {
			return Materials.floorSign;
		}
		return Material.AIR;
	}

	/**
	 * Update this sign.
	 * @return true if the update was successful, otherwise false
	 */
	public boolean update() {
		// Ignore updates of signs in chunks that are not loaded
		Location signLocation = getLocation();
		if(signLocation == null
				|| signLocation.getWorld() == null
				|| !signLocation.getWorld().isChunkLoaded(signLocation.getBlockX() >> 4, signLocation.getBlockZ() >> 4)) {
			return false;
		}

		if(getRegion().isDeleted()) {
			return false;
		}

		YamlConfiguration regionConfig = getRegion().getConfig();
		ConfigurationSection signConfig = getProfile();
		Block block = signLocation.getBlock();
		if(signConfig == null || !signConfig.isSet(getRegion().getState().getValue())) {
			block.setType(Material.AIR);
			return true;
		}

		ConfigurationSection stateConfig = signConfig.getConfigurationSection(getRegion().getState().getValue());

		// Get the lines
		String[] signLines = new String[4];
		boolean signEmpty = true;
		for(int i = 0; i < 4; ++i) {
			signLines[i] = stateConfig.getString("line" + (i + 1));
			signEmpty &= (signLines[i] == null || signLines[i].isEmpty());
		}
		if(signEmpty) {
			block.setType(Material.AIR);
			return true;
		}

		// Place the sign back (with proper rotation and type) after it has been hidden or (indirectly) destroyed
		Sign signState = null;
		if(!Materials.isSign(block.getType())) {
			Material signType = getMaterial();
			block.setType(signType == null ? Material.AIR : signType, false);
			BlockState state = block.getState();
			if(!(state instanceof Sign)) {
				AreaShop.error("Setting sign", key, "of region", getRegion().getName(), "failed, could not set sign block back");
				return false;
			}
			signState = (Sign) state;
			BlockFace signFace = getFacing();
			if(signState.getData() instanceof org.bukkit.material.Sign) {
				org.bukkit.material.Sign signData = (org.bukkit.material.Sign) signState.getData();
				if(signFace != null) {
					signData.setFacingDirection(signFace);
					signState.setData(signData);
				}
			} else {
				// 1.14 method
				block.setType(signType);
				final org.bukkit.block.data.BlockData bs = block.getState().getBlockData();
				if(bs instanceof org.bukkit.block.data.type.WallSign) {
					((org.bukkit.block.data.type.WallSign) bs).setFacing(signFace);
				} else if(bs instanceof org.bukkit.block.data.type.Sign) {
					((org.bukkit.block.data.type.Sign) bs).setRotation(signFace);
				}
				block.setBlockData(bs);
				signState = (Sign) block.getState();
			}
		}
		if(signState == null) {
			signState = (Sign) block.getState();
		}

		if(!regionConfig.isString("general.signs." + key + ".signType")) {
			getRegion().setSetting("general.signs." + key + ".signType", block.getType().name());
		}
		if(!regionConfig.isString("general.signs." + key + ".facing")) {
			// Save current rotation and type
			BlockFace facing = null;
			if(signState.getData() instanceof org.bukkit.material.Sign) {
				facing = ((org.bukkit.material.Sign) block.getState().getData()).getFacing();
			} else {
				// 1.14 method
				org.bukkit.block.data.BlockData bs = signState.getBlockData();
				if(bs instanceof org.bukkit.block.data.type.WallSign) {
					facing = ((org.bukkit.block.data.type.WallSign) bs).getFacing();
				} else if(bs instanceof org.bukkit.block.data.type.Sign) {
					facing = ((org.bukkit.block.data.type.Sign) bs).getRotation();
				}
			}
			getRegion().setSetting("general.signs." + key + ".facing", facing == null ? "null" : facing.toString());
		}

		// Apply replacements and color and then set it on the sign
		for(int i = 0; i < signLines.length; ++i) {
			if(signLines[i] == null) {
				signState.setLine(i, "");
				continue;
			}
			signLines[i] = Message.fromString(signLines[i]).replacements(getRegion()).getSingle();
			signLines[i] = Utils.applyColors(signLines[i]);
			signState.setLine(i, signLines[i]);
		}
		signState.update();
		return true;
	}

	/**
	 * Check if the sign needs to update periodically.
	 * @return true if it needs periodic updates, otherwise false
	 */
	public boolean needsPeriodicUpdate() {
		ConfigurationSection signConfig = getProfile();
		if(signConfig == null || !signConfig.isSet(getRegion().getState().getValue().toLowerCase())) {
			return false;
		}
		ConfigurationSection stateConfig = signConfig.getConfigurationSection(getRegion().getState().getValue().toLowerCase());
		// Check the lines for the timeleft tag
		for(int i = 1; i <= 4; ++i) {
			String line = stateConfig.getString("line" + i);
			if(line != null && !line.isEmpty() && line.contains(Message.VARIABLE_START + AreaShop.tagTimeLeft + Message.VARIABLE_END)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Run commands when a player clicks a sign.
	 * @param clicker   The player that clicked the sign
	 * @param clickType The type of clicking
	 * @return true if the commands ran successfully, false if any of them failed
	 */
	public boolean runSignCommands(Player clicker, GeneralRegion.ClickType clickType) {
		ConfigurationSection signConfig = getProfile();
		if(signConfig == null) {
			return false;
		}
		ConfigurationSection stateConfig = signConfig.getConfigurationSection(getRegion().getState().getValue().toLowerCase());

		// Run player commands if specified
		List<String> playerCommands = new ArrayList<>();
		for(String command : stateConfig.getStringList(clickType.getValue() + "Player")) {
			// TODO move variable checking code to InteractiveMessenger?
			playerCommands.add(command.replace(Message.VARIABLE_START + AreaShop.tagClicker + Message.VARIABLE_END, clicker.getName()));
		}
		getRegion().runCommands(clicker, playerCommands);

		// Run console commands if specified
		List<String> consoleCommands = new ArrayList<>();
		for(String command : stateConfig.getStringList(clickType.getValue() + "Console")) {
			consoleCommands.add(command.replace(Message.VARIABLE_START + AreaShop.tagClicker + Message.VARIABLE_END, clicker.getName()));
		}
		getRegion().runCommands(Bukkit.getConsoleSender(), consoleCommands);

		return !playerCommands.isEmpty() || !consoleCommands.isEmpty();
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof RegionSign && ((RegionSign)object).getRegion().equals(this.getRegion()) && ((RegionSign)object).key.equals(this.key);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 41 * hash + Objects.hashCode(getRegion().getName());
		hash = 41 * hash + Objects.hashCode(this.key);
		return hash;
	}
}
