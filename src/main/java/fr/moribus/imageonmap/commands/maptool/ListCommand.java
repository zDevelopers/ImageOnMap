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

package fr.moribus.imageonmap.commands.maptool;

import fr.moribus.imageonmap.Permissions;
import fr.moribus.imageonmap.commands.IoMCommand;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.PosterMap;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.rawtext.RawText;
import fr.zcraft.zlib.components.rawtext.RawTextPart;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zlib.tools.text.RawMessage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandInfo (name = "list")
public class ListCommand extends IoMCommand
{
    @Override
    protected void run() throws CommandException
    {
        Player player = playerSender();
        List<ImageMap> mapList = MapManager.getMapList(player.getUniqueId());
        
        if(mapList.isEmpty())
        {
            info(I.t("No map found."));
            return;
        }
        
        info(I.tn("{white}{bold}{0} map found.", "{white}{bold}{0} maps found.", mapList.size()));

        RawTextPart rawText = new RawText("");
        rawText = addMap(rawText, mapList.get(0));

        for(int i = 1, c = mapList.size(); i < c; i++)
        {
            rawText = rawText.then(", ").color(ChatColor.GRAY);
            rawText = addMap(rawText, mapList.get(i));
        }

        RawMessage.send(player, rawText.build());
    }

    private RawTextPart<?> addMap(RawTextPart<?> rawText, ImageMap map)
    {
        final String size = map.getType() == ImageMap.Type.SINGLE ? "1 × 1" : ((PosterMap) map).getColumnCount() + " × " + ((PosterMap) map).getRowCount();

        return rawText
                .then(map.getId())
                .color(ChatColor.WHITE)
                .command(GetCommand.class, map.getId())
                .hover(new ItemStackBuilder(Material.MAP)
                                .title(ChatColor.GREEN + "" + ChatColor.BOLD + map.getName())
                                .lore(ChatColor.GRAY + map.getId() + ", " + size)
                                .lore("")
                                .lore(I.t("{white}Click{gray} to get this map"))
                                .hideAttributes()
                                .item()
                );
    }

    @Override
    public boolean canExecute(CommandSender sender)
    {
        return Permissions.LIST.grantedTo(sender);
    }
}
