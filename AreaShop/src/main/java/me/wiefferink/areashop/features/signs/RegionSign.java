package me.wiefferink.areashop.features.signs;

import com.google.common.base.Objects;
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
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
	 * Get the facing of the sign as saved in the config.
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
	 * Get the material of the sign as saved in the config.
	 * @return Material of the sign, usually {@link Material#WALL_SIGN}, {@link Material#SIGN}, or one of the other wood types (different result for 1.13-), Material.AIR if none.
	 */
	public Material getMaterial() {
		String name = getRegion().getConfig().getString("general.signs." + key + ".signType");
		Material result = Materials.signNameToMaterial(name);
		return result == null ? Material.AIR : result;
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
		for(int i = 0; i < 4; i++) {
			signLines[i] = stateConfig.getString("line" + (i + 1));
			signEmpty &= (signLines[i] == null || signLines[i].isEmpty());
		}
		if(signEmpty) {
			block.setType(Material.AIR);
			return true;
		}

		// Place the sign back (with proper rotation and type) after it has been hidden or (indirectly) destroyed
		if(!Materials.isSign(block.getType())) {
			Material signType = getMaterial();
			// Don't do physics here, we first need to update the direction
			block.setType(signType, false);

			// This triggers a physics update, which pops the sign if not attached properly
			if (!AreaShop.getInstance().getBukkitHandler().setSignFacing(block, getFacing())) {
				AreaShop.warn("Failed to update the facing direction of the sign at", getStringLocation(), "to ", getFacing(), ", region:", getRegion().getName());
			}

			// Check if the sign has popped
			if(!Materials.isSign(block.getType())) {
				AreaShop.warn("Setting sign", key, "of region", getRegion().getName(), "failed, could not set sign block back");
				return false;
			}
		}

		// Save current rotation and type
		if(!regionConfig.isString("general.signs." + key + ".signType")) {
			getRegion().setSetting("general.signs." + key + ".signType", block.getType().name());
		}
		if(!regionConfig.isString("general.signs." + key + ".facing")) {
			BlockFace signFacing = AreaShop.getInstance().getBukkitHandler().getSignFacing(block);
			getRegion().setSetting("general.signs." + key + ".facing", signFacing == null ? null : signFacing.toString());
		}

		// Apply replacements and color and then set it on the sign
		Sign signState = (Sign) block.getState();
		for(int i = 0; i < signLines.length; i++) {
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
		if(stateConfig == null) {
			return false;
		}

		// Check the lines for the timeleft tag
		for(int i = 1; i <= 4; i++) {
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
		return Objects.hashCode(key, getRegion().getName());
	}
}
