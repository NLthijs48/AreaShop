package nl.evolutioncoding.areashop.features;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.events.notify.RegionUpdateEvent;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

public class SignDisplayFeature extends Feature implements Listener {

	private static final String GENERAL_SIGNS = "general.signs.";

    public SignDisplayFeature(AreaShop plugin) {
		super(plugin);
	}

	@EventHandler
	public void regionUpdate(RegionUpdateEvent event) {
		updateSigns(event.getRegion());
	}

	/**
	 * Update the signs connected to this region
	 * @return true if the update was successful, otherwise false
	 */
	public boolean updateSigns(GeneralRegion region) {
		if(region.isDeleted()) {
			return false;
		}
		YamlConfiguration config = region.getConfig();
		boolean result = true;
		Set<String> signs = null;
		if(config.getConfigurationSection("general.signs") != null) {
			signs = config.getConfigurationSection("general.signs").getKeys(false);
		}
		if(signs == null) {
			return true;
		}
		for(String sign : signs) {
			Location location = Utils.configToLocation(config.getConfigurationSection(GENERAL_SIGNS + sign + ".location"));
			if(location == null) {
				AreaShop.debug("Sign location incorrect region=" + region.getName() + ", signKey=" + sign);
				result = false;
			} else {
				// Get the profile set in the config
				String profile = config.getString(GENERAL_SIGNS + sign + ".profile");
				if(profile == null || profile.length() == 0) {
					profile = region.getStringSetting("general.signProfile");
				}
				// Get the prefix
				String prefix = "signProfiles." + profile + "." + region.getState().getValue().toLowerCase() + ".";
				// Get the lines
				String[] signLines = new String[4];
				signLines[0] = plugin.getConfig().getString(prefix + "line1");
				signLines[1] = plugin.getConfig().getString(prefix + "line2");
				signLines[2] = plugin.getConfig().getString(prefix + "line3");
				signLines[3] = plugin.getConfig().getString(prefix + "line4");
				// Check if the sign should be present
				Block block = location.getBlock();
				if(!plugin.getConfig().isSet(prefix)
						|| ((signLines[0] == null || signLines[0].length() == 0)
						&& (signLines[1] == null || signLines[1].length() == 0)
						&& (signLines[2] == null || signLines[2].length() == 0)
						&& (signLines[3] == null || signLines[3].length() == 0))) {
					block.setType(Material.AIR);
				} else {
					Sign signState = null;
					if(block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) {
						Material signType;
						try {
							signType = Material.valueOf(config.getString(GENERAL_SIGNS + sign + ".signType"));
						} catch(NullPointerException | IllegalArgumentException e) {
							signType = null;
						}
						if(signType != Material.WALL_SIGN && signType != Material.SIGN_POST) {
							AreaShop.debug("  setting sign failed");
							continue;
						}
						block.setType(signType);
						signState = (Sign)block.getState();
						org.bukkit.material.Sign signData = (org.bukkit.material.Sign)signState.getData();
						BlockFace signFace;
						try {
							signFace = BlockFace.valueOf(config.getString(GENERAL_SIGNS + sign + ".facing"));
						} catch(NullPointerException | IllegalArgumentException e) {
							signFace = null;
						}
						if(signFace != null) {
							signData.setFacingDirection(signFace);
							signState.setData(signData);
						}
					}
					if(signState == null) {
						signState = (Sign)block.getState();
					}
					org.bukkit.material.Sign signData = (org.bukkit.material.Sign)signState.getData();
					if(!config.isString(GENERAL_SIGNS + sign + ".signType")) {
						region.setSetting(GENERAL_SIGNS + sign + ".signType", signState.getType().toString());
					}
					if(!config.isString(GENERAL_SIGNS + sign + ".facing")) {
						region.setSetting(GENERAL_SIGNS + sign + ".facing", signData.getFacing().toString());
					}
					// Apply replacements and color and then set it on the sign
					for(int i = 0; i < signLines.length; i++) {
						if(signLines[i] == null) {
							signState.setLine(i, "");
							continue;
						}
						signLines[i] = region.applyAllReplacements(signLines[i]);
						signLines[i] = Utils.applyColors(signLines[i]);
						signState.setLine(i, signLines[i]);
					}
					signState.update();
				}
			}
		}
		return result;
	}
}
