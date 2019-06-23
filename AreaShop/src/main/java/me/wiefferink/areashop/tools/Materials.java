package me.wiefferink.areashop.tools;

import me.wiefferink.areashop.AreaShop;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Materials {

	private Materials() {

	}

	private static final HashSet<String> WALL_SIGN_TYPES = new HashSet<>(Arrays.asList(
		// 1.14+ types
		"ACACIA_WALL_SIGN",
		"BIRCH_WALL_SIGN",
		"DARK_OAK_WALL_SIGN",
		"JUNGLE_WALL_SIGN",
		"OAK_WALL_SIGN",
		"SPRUCE_WALL_SIGN",

		// Legacy types
		"LEGACY_WALL_SIGN",
		"WALL_SIGN"
	));
	private static final HashSet<String> FLOOR_SIGN_TYPES = new HashSet<>(Arrays.asList(
		// 1.14+ types
		"ACACIA_SIGN",
		"BIRCH_SIGN",
		"DARK_OAK_SIGN",
		"JUNGLE_SIGN",
		"OAK_SIGN",
		"SPRUCE_SIGN",

		// Legacy types
		"LEGACY_SIGN",
		"LEGACY_SIGN_POST",
		"SIGN",
		"SIGN_POST"
	));

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

	/**
	 * Get material based on a sign material name.
	 * @param name Name of the sign material
	 * @return null if not a sign, otherwise the material matching the name (when the material is not available on the current minecraft version, it returns the base type)
	 */
	public static Material signNameToMaterial(String name) {
		// Expected null case
		if (!isSign(name)) {
			return null;
		}

		Material result = null;
		if (legacyMaterials) {
			// 1.12 and lower just know SIGN_POST, WALL_SIGN and SIGN
			if (FLOOR_SIGN_TYPES.contains(name)) {
				result = Material.getMaterial("SIGN_POST");
			} else if (WALL_SIGN_TYPES.contains(name)) {
				result = Material.getMaterial("WALL_SIGN");
				if (result == null) {
					result = Material.getMaterial("SIGN");
				}
			}
		} else {
			// Try saved name (works for wood types on 1.14, regular types for below)
			result = Material.getMaterial(name);
			if (result == null) {
				// Cases for 1.13, which don't know wood types, but need new materials
				if (FLOOR_SIGN_TYPES.contains(name)) {
					// SIGN -> OAK_SIGN for 1.14
					result = Material.getMaterial("OAK_SIGN");
					// Fallback for 1.13
					if (result == null) {
						result = Material.getMaterial("SIGN");
					}
				} else if (WALL_SIGN_TYPES.contains(name)) {
					// WALL_SIGN -> OAK_WALL_SIGN for 1.14
					result = Material.getMaterial("OAK_WALL_SIGN");
					// Fallback for 1.13
					if (result == null) {
						result = Material.getMaterial("WALL_SIGN");
					}
				}
			}
		}

		if (result == null) {
			AreaShop.debug("Materials.get() null result:", name, "legacyMaterials:", legacyMaterials);
		}

		return result;
	}

	/**
	 * Check if a Material is a sign (of either the wall or floor type).
	 * @param material Material to check
	 * @return true if the given material is a sign
	 */
	public static boolean isSign(Material material) {
		return isSign(material.name());
	}

	/**
	 * Check if a Material is a sign (of either the wall or floor type).
	 * @param name String to check
	 * @return true if the given material is a sign
	 */
	public static boolean isSign(String name) {
		return name != null && (FLOOR_SIGN_TYPES.contains(name) || WALL_SIGN_TYPES.contains(name));
	}

}
