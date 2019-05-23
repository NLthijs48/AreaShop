package me.wiefferink.areashop.tools;

import me.wiefferink.areashop.AreaShop;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class Materials {

	private Materials() {

	}

	private static boolean legacyMaterials = false;

	static {
		List<String> legacyMaterialVersions = Arrays.asList("1.7", "1.8", "1.9", "1.10", "1.11", "1.12");
		for(String legacyMaterialVersion : legacyMaterialVersions) {
			String version = Bukkit.getBukkitVersion();
			// Detects '1.8', '1.8.3', '1.8-pre1' style versions
			if(version.equals(legacyMaterialVersion)
					|| version.startsWith(legacyMaterialVersion + ".")
					|| version.startsWith(legacyMaterialVersion + "-")) {
				legacyMaterials = true;
				break;
			}
		}
	}

	public static final Material wallSign = get("WALL_SIGN");
	public static final Material floorSign = get("SIGN", "SIGN_POST");

	/**
	 * Get Material version independent.
	 * @param name Name in 1.13 and later (uppercase, with underscores)
	 * @return Material matching the name
	 */
	public static Material get(String name) {
		return get(name, null);
	}

	/**
	 * Get Material version independent.
	 * @param name Name in 1.13 and later (uppercase, with underscores)
	 * @param legacyName Name in 1.12 and earlier (uppercase, with underscores)
	 * @return Material matching the name
	 */
	public static Material get(String name, String legacyName) {
		Material result;
		if (legacyMaterials && legacyName != null) {
			result = Material.getMaterial(legacyName);
		} else {
			result = Material.getMaterial(name);
		}
		if (result == null) {
			result = Material.getMaterial("OAK_" + name);
		}

		if (result == null) {
			AreaShop.error("Materials.get() null result:", name, legacyName);
		}

		return result;
	}

	/**
	 * Check if a Material is a sign (of either the wall or floor type).
	 * @param material Material to check
	 * @return true if the given material is a sign
	 */
	public static boolean isSign(Material material) {
		return material != null && material.name().contains("SIGN");
	}

}
