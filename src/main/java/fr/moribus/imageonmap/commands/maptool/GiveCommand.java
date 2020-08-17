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
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


@CommandInfo (name = "give", usageParameters = "<Player> <MapName> or <Player> <MapName> <Player where to find the map>")
public class GiveCommand extends IoMCommand
{
    @Override
    protected void run() throws CommandException
    {
        if(args.length < 2) throwInvalidArgument(I.t("You must give a valid player name and a map name."));

        final Player p = getPlayerParameter(0);

        ImageMap map;
        //TODO does not support map name with space
        Player player=null;
        if(args.length<4) {
            if (args.length == 2) {
                player = playerSender();
            }
            if (args.length == 3) {
                player = Bukkit.getPlayer(args[2]);
                if(player==null){
                    try{
                        playerSender().sendMessage(I.t("Player map store not found"));
                    }
                    catch (Exception e){

                    }
                    return;
                }
            }
            if (p == null) {
                player.sendMessage(I.t("Player not found"));
                return;
            }
            map = MapManager.getMap(player.getUniqueId(), args[1]);
            if (map == null) {
                player.sendMessage(I.t("Map not found"));
                return;
            }
            map.give(p);
        }
    }

    @Override
    public boolean canExecute(CommandSender sender)
    {
        return Permissions.GIVE.grantedTo(sender);
    }
}
