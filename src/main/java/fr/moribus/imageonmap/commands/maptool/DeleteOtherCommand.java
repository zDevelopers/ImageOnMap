/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2020)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2020)
 *
 * This software is a computer program whose purpose is to allow insertion of
 * custom images in a Minecraft world.
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package fr.moribus.imageonmap.commands.maptool;

import fr.moribus.imageonmap.Permissions;
import fr.moribus.imageonmap.commands.IoMCommand;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.MapManagerException;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.commands.WithFlags;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

@CommandInfo (name =  "deleteother", usageParameters = "<player name> <map name>")
@WithFlags ({"confirm"})
public class DeleteOtherCommand extends IoMCommand
{
    @Override
    protected void run() throws CommandException
    {
    	/*if(!playerSender().hasPermission("imageonmap.delete.other")) {
    		warning(I.t("You do not have permission for this command. (imageonmap.delete.other)"));
    		return;
    	}*/
    	if(args.length < 2) {
    	    warning(I.t("Not enough parameters! Usage: /maptool deleteother <playername> <mapname>"));
    	    return;
        }


		Player player = null;
		UUID uuid = null;
		OfflinePlayer op = null;
        player = Bukkit.getPlayer(args[0]);
        if(player == null){
        	op = Bukkit.getOfflinePlayer(args[0]);
			if(op.hasPlayedBefore()) uuid = op.getUniqueId();
			else warning(I.t("We've never seen that player before!"));
        }
        else uuid = player.getUniqueId();
        if(player==null){
            warning(I.t("Player not found"));
            return;
        }
        ImageMap map = getMapFromArgs(player, 1, true);
		//ImageMap map = MapManager.getMap(uuid, mapName);
        
        if(player != null) MapManager.clear(player.getInventory(), map);
        
            try
            {
                MapManager.deleteMap(map);
                info(I.t("{gray}Map successfully deleted."));
            }
            catch (MapManagerException ex)
            {
                PluginLogger.warning(I.t("A non-existent map was requested to be deleted", ex));
                warning(ChatColor.RED+(I.t("This map does not exist.")));
            }
        }
   
    
    @Override
    protected List<String> complete() throws CommandException
    {
        if(args.length == 1) 
            return getMatchingMapNames(playerSender(), args[0]);

        return null;
    }

    @Override
    public boolean canExecute(CommandSender sender)
    {
        return Permissions.DELETEOTHER.grantedTo(sender);
    }
}
