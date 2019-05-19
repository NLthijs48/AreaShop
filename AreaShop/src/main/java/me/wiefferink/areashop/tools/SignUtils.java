package me.wiefferink.areashop.tools;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;

/**
 * These are helper methods copied from {@link org.bukkit.material.Sign} to allow wall signs to be used
 * In newer bukkit versions 1.13+ wall signs are no longer a material Sign so casting fails
 * Use this instead to maintain backward compatibility
 * Note: All classes in org.bukkit.material.* are deprecated so even this might end up breaking eventually
 */
public class SignUtils {

	/**
	 * Check if a sign is a wall sign.
	 * @param materialData the material data of the sign
	 * @return boolean
	 */
	private static boolean isWallSign(MaterialData materialData) {
		return materialData.getItemType() == Material.WALL_SIGN;
	}

	/**
	 * Get the attached face of the sign.
	 * @param materialData the material data of the sign
	 * @return The attached BlockFace
	 */
	public static BlockFace getAttachedFace(MaterialData materialData) {
		if (isWallSign(materialData)) {
			byte data = materialData.getData();
			switch(data) {
				case 2:
					return BlockFace.SOUTH;
				case 3:
					return BlockFace.NORTH;
				case 4:
					return BlockFace.EAST;
				case 5:
					return BlockFace.WEST;
				default:
					return null;
			}
		} else {
			return BlockFace.DOWN;
		}
	}

	/**
	 * Get the facing direction of the sign.
	 * @param materialData the material data of the sign
	 * @return The facing BlockFace of the sign
	 */
	public static BlockFace getFacing(MaterialData materialData) {
		byte data = materialData.getData();
		if (!isWallSign(materialData)) {
			switch(data) {
				case 0:
					return BlockFace.SOUTH;
				case 1:
					return BlockFace.SOUTH_SOUTH_WEST;
				case 2:
					return BlockFace.SOUTH_WEST;
				case 3:
					return BlockFace.WEST_SOUTH_WEST;
				case 4:
					return BlockFace.WEST;
				case 5:
					return BlockFace.WEST_NORTH_WEST;
				case 6:
					return BlockFace.NORTH_WEST;
				case 7:
					return BlockFace.NORTH_NORTH_WEST;
				case 8:
					return BlockFace.NORTH;
				case 9:
					return BlockFace.NORTH_NORTH_EAST;
				case 10:
					return BlockFace.NORTH_EAST;
				case 11:
					return BlockFace.EAST_NORTH_EAST;
				case 12:
					return BlockFace.EAST;
				case 13:
					return BlockFace.EAST_SOUTH_EAST;
				case 14:
					return BlockFace.SOUTH_EAST;
				case 15:
					return BlockFace.SOUTH_SOUTH_EAST;
				default:
					return null;
			}
		} else {
			return getAttachedFace(materialData).getOppositeFace();
		}
	}

	/**
	 * Set the facing direction of the sign.
	 * @param materialData the material data of the sign
	 * @param face The block face to set to
	 */
	public static void setFacingDirection(MaterialData materialData, BlockFace face) {
		byte data;
		if (isWallSign(materialData)) {
			switch(face) {
				case NORTH:
					data = 2;
					break;
				case SOUTH:
					data = 3;
					break;
				case WEST:
					data = 4;
					break;
				case EAST:
				default:
					data = 5;
			}
		} else {
			switch(face) {
				case NORTH:
					data = 8;
					break;
				case SOUTH:
					data = 0;
					break;
				case WEST:
					data = 4;
					break;
				case EAST:
					data = 12;
					break;
				case SOUTH_SOUTH_WEST:
					data = 1;
					break;
				case SOUTH_WEST:
					data = 2;
					break;
				case WEST_SOUTH_WEST:
					data = 3;
					break;
				case WEST_NORTH_WEST:
					data = 5;
					break;
				case NORTH_WEST:
					data = 6;
					break;
				case NORTH_NORTH_WEST:
					data = 7;
					break;
				case NORTH_NORTH_EAST:
					data = 9;
					break;
				case NORTH_EAST:
					data = 10;
					break;
				case EAST_NORTH_EAST:
					data = 11;
					break;
				case EAST_SOUTH_EAST:
					data = 13;
					break;
				case SOUTH_SOUTH_EAST:
					data = 15;
					break;
				case SOUTH_EAST:
				default:
					data = 14;
			}
		}

		materialData.setData(data);
	}

}
