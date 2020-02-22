/*
 * Copyright (C) 2013 Moribus
 * Copyright (C) 2015 ProkopyL <prokopylmc@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.moribus.imageonmap.ui;

import com.google.common.collect.ImmutableMap;
import fr.moribus.imageonmap.image.MapInitEvent;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.PosterMap;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.nbt.NBT;
import fr.zcraft.zlib.components.nbt.NBTCompound;
import fr.zcraft.zlib.components.nbt.NBTException;
import fr.zcraft.zlib.components.nbt.NBTList;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zlib.tools.reflection.NMSException;
import fr.zcraft.zlib.tools.text.MessageSender;
import fr.zcraft.zlib.tools.world.FlatLocation;
import fr.zcraft.zlib.tools.world.WorldUtils;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

abstract public class SplatterMapManager {
	private SplatterMapManager() {
	}

	static public ItemStack makeSplatterMap(PosterMap map) {
		final ItemStack splatter = new ItemStackBuilder(Material.FILLED_MAP).title(ChatColor.GOLD, map.getName())
				.title(ChatColor.DARK_GRAY, " - ").title(ChatColor.GRAY, I.t("Splatter Map"))
				.title(ChatColor.DARK_GRAY, " - ")
				.title(ChatColor.GRAY, I.t("{0} × {1}", map.getColumnCount(), map.getRowCount()))
				.loreLine(ChatColor.GRAY, map.getId()).loreLine()
				/// Title in a splatter map tooltip
				.loreLine(ChatColor.BLUE, I.t("Item frames needed"))
				/// Size of a map stored in a splatter map
				.loreLine(ChatColor.GRAY,
						I.t("{0} × {1} (total {2} frames)", map.getColumnCount(), map.getRowCount(),
								map.getColumnCount() * map.getRowCount()))
				.loreLine()
				/// Title in a splatter map tooltip
				.loreLine(ChatColor.BLUE, I.t("How to use this?"))
				.longLore(
						ChatColor.GRAY
								+ I.t("Place empty item frames on a wall, enough to host the whole map. Then, right-click on the bottom-left frame with this map."),
						40)
				.loreLine()
				.longLore(ChatColor.GRAY
						+ I.t("Shift-click one of the placed maps to remove the whole poster in one shot."), 40)
				.hideAttributes().craftItem();

		final MapMeta meta = (MapMeta) splatter.getItemMeta();
		meta.setMapId(map.getMapIdAt(0));
		meta.setColor(Color.GREEN);
		splatter.setItemMeta(meta);

		return addSplatterAttribute(splatter);
	}

	/**
	 * To identify image on maps for the auto-splattering to work, we mark the
	 * items using an enchantment maps are not supposed to have (Mending).
	 *
	 * Then we check if the map is enchanted at all to know if it's a splatter
	 * map. This ensure compatibility with old splatter maps from 3.x, where
	 * zLib's glow effect was used.
	 *
	 * An AttributeModifier (using zLib's attributes system) is not used,
	 * because Minecraft (or Spigot) removes them from maps in 1.14+, so that
	 * wasn't stable enough (and the glowing effect of enchantments is
	 * prettier).
	 *
	 * @param itemStack
	 *            The item stack to mark as a splatter map.
	 * @return The modified item stack. The instance may be different if the
	 *         passed item stack is not a craft item stack; that's why the
	 *         instance is returned.
	 */
	static public ItemStack addSplatterAttribute(final ItemStack itemStack) {
		try {
			final NBTCompound nbt = NBT.fromItemStack(itemStack);
			final NBTList enchantments = new NBTList();
			final NBTCompound protection = new NBTCompound();

			protection.put("id", "minecraft:mending");
			protection.put("lvl", 1);
			enchantments.add(protection);

			nbt.put("Enchantments", enchantments);

			return NBT.addToItemStack(itemStack, nbt, false);
		} catch (NBTException | NMSException e) {
			PluginLogger.error("Unable to set Splatter Map attribute on item", e);
			return itemStack;
		}
	}

	/**
	 * Checks if an item have the splatter attribute set (i.e. if the item is
	 * enchanted in any way).
	 *
	 * @param itemStack
	 *            The item to check.
	 * @return True if the attribute was detected.
	 */
	static public boolean hasSplatterAttributes(ItemStack itemStack) {
		try {
			final NBTCompound nbt = NBT.fromItemStack(itemStack);
			if (!nbt.containsKey("Enchantments"))
				return false;

			final Object enchantments = nbt.get("Enchantments");
			if (!(enchantments instanceof NBTList))
				return false;

			return !((NBTList) enchantments).isEmpty();
		} catch (NMSException e) {
			PluginLogger.error("Unable to get Splatter Map attribute on item", e);
			return false;
		}
	}

	static public boolean isSplatterMap(ItemStack itemStack) {
		return hasSplatterAttributes(itemStack) && MapManager.managesMap(itemStack);
	}

	static public boolean hasSplatterMap(Player player, PosterMap map) {
		Inventory playerInventory = player.getInventory();

		for (int i = 0; i < playerInventory.getSize(); ++i) {
			ItemStack item = playerInventory.getItem(i);
			if (isSplatterMap(item) && map.managesMap(item))
				return true;
		}

		return false;
	}

	static public boolean placeSplatterMap(ItemFrame startFrame, Player player) {
		ImageMap map = MapManager.getMap(player.getInventory().getItemInMainHand());

		if (!(map instanceof PosterMap))
			return false;
		PosterMap poster = (PosterMap) map;
		PosterWall wall = new PosterWall();
		// NEW
		if (startFrame.getFacing().equals(BlockFace.DOWN) || startFrame.getFacing().equals(BlockFace.UP)) {
			PluginLogger.info("UP or DOWN ");

			PosterOnASurface surface = new PosterOnASurface();
			FlatLocation startLocation = new FlatLocation(startFrame.getLocation(), startFrame.getFacing());
			FlatLocation endLocation = startLocation.clone().addH(poster.getColumnCount(), poster.getRowCount(),
					WorldUtils.get4thOrientation(player.getLocation()));
			PluginLogger.info("Before loc asign  ");
			surface.loc1 = startLocation;
			surface.loc2 = endLocation;
			PluginLogger.info("After loc asign  ");

			// todo impletation
			if (!surface.isValid(player)) {
				MessageSender.sendActionBarMessage(player,
						I.t("{ce}There is not enough space to place this map ({0} × {1}).", poster.getColumnCount(),
								poster.getRowCount()));
				return false;
			}

			int i = 0;
			for (ItemFrame frame : surface.frames) {
				PluginLogger.info("Test  " + frame.getName());
				BlockFace bf = WorldUtils.get4thOrientation(player.getLocation());
				int id = poster.getMapIdAtReverseZ(i, bf, startFrame.getFacing());

				Rotation rot = frame.getRotation();

				// NESW
				//PluginLogger.info("ordinal " + rot.ordinal() );
				/*switch(bf){
				case NORTH:
					frame.setRotation(Rotation.NONE);
					break;
				case EAST:
					frame.setRotation(Rotation.CLOCKWISE);
					break;
				case SOUTH:
					frame.setRotation(Rotation.FLIPPED);
					break;
				case WEST:
					frame.setRotation(Rotation.COUNTER_CLOCKWISE);
					break;
				}*/
				PluginLogger.info("bf ordinal "+ bf.ordinal());
				while (rot.ordinal() != 2*bf.ordinal()) {
					rot = rot.rotateClockwise();
					//PluginLogger.info("Rotation ordinal " + rot.ordinal() + "  bf ordinal " + bf.ordinal());
					frame.setRotation(rot);
				}
				switch (bf) {
				case UP:
					
					frame.setRotation(rot);
					break;
				case DOWN:
					rot=rot.rotateClockwise().rotateClockwise();
					frame.setRotation(rot);
					break;
				}
				
				rot = frame.getRotation();

				
				//PluginLogger.info("ordinal " + rot.ordinal() );
				frame.setItem(new ItemStackBuilder(Material.FILLED_MAP).nbt(ImmutableMap.of("map", id)).craftItem());
				MapInitEvent.initMap(id);
				++i;
			}
		} else {
			FlatLocation startLocation = new FlatLocation(startFrame.getLocation(), startFrame.getFacing());
			FlatLocation endLocation = startLocation.clone().add(poster.getColumnCount(), poster.getRowCount());

			wall.loc1 = startLocation;
			wall.loc2 = endLocation;
			PluginLogger.info("startLocation " + startLocation + " | endLocation " + endLocation);
			if (!wall.isValid()) {
				MessageSender.sendActionBarMessage(player,
						I.t("{ce}There is not enough space to place this map ({0} × {1}).", poster.getColumnCount(),
								poster.getRowCount()));
				return false;
			}

			int i = 0;
			for (ItemFrame frame : wall.frames) {
				int id = poster.getMapIdAtReverseY(i);
				frame.setItem(new ItemStackBuilder(Material.FILLED_MAP).nbt(ImmutableMap.of("map", id)).craftItem());
				MapInitEvent.initMap(id);
				++i;
			}
		}
		return true;
	}

	static public PosterMap removeSplatterMap(ItemFrame startFrame) {
		final ImageMap map = MapManager.getMap(startFrame.getItem());
		if (!(map instanceof PosterMap))
			return null;
		PosterMap poster = (PosterMap) map;
		if (!poster.hasColumnData())
			return null;
		FlatLocation loc = new FlatLocation(startFrame.getLocation(), startFrame.getFacing());
		ItemFrame[] matchingFrames=null;
		switch(startFrame.getFacing()){
		case UP:
		case DOWN:
			 matchingFrames = PosterOnASurface.getMatchingMapFrames(poster, loc,
					MapManager.getMapIdFromItemStack(startFrame.getItem()),startFrame.getFacing());
			break;
		case NORTH:
		case SOUTH:
		case EAST:
		case WEST:
			 matchingFrames = PosterWall.getMatchingMapFrames(poster, loc,
					MapManager.getMapIdFromItemStack(startFrame.getItem()));
			
		}
		

		if (matchingFrames == null)
			return null;

		for (ItemFrame frame : matchingFrames) {
			if (frame != null)
				frame.setItem(null);
		}

		return poster;
	}
}
