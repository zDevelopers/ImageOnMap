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
import fr.zcraft.zlib.components.rawtext.RawText;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandInfo (name =  "delete", usageParameters = "<map name> [--confirm]")
@WithFlags ({"confirm"})
public class DeleteCommand extends IoMCommand
{

    private static RawText deleteMsg(Class klass,ImageMap map){
       return new RawText(I.t("You are going to delete") + " ")
                .then(map.getId())
                .color(ChatColor.GOLD)
                .then(". " + I.t("Are you sure ? "))
                .color(ChatColor.WHITE)
                .then(I.t("[Confirm]"))
                .color(ChatColor.GREEN)
                .hover(new RawText(I.t("{red}This map will be deleted {bold}forever{red}!")))
                .command(klass, map.getId(), "--confirm")
                .build();
    }

    @Override
    protected void run() throws CommandException
    {
        ImageMap map = getMapFromArgs();

        if (!hasFlag("confirm"))
        {
            RawText msg = deleteMsg(getClass(),map);
            send(msg);
        }
        else
        {
            Player player = playerSender();
            MapManager.clear(player.getInventory(), map);

            try
            {
                MapManager.deleteMap(map);
                info(I.t("Map successfully deleted."));
            }
            catch (MapManagerException ex)
            {
                PluginLogger.warning(I.t("A non-existent map was requested to be deleted", ex));
                warning(I.t("This map does not exist."));
            }
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
        return Permissions.DELETE.grantedTo(sender);
    }
}
