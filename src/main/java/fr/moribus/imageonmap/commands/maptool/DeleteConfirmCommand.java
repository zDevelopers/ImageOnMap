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

import fr.moribus.imageonmap.commands.IoMCommand;
import fr.moribus.imageonmap.map.ImageMap;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;

import java.util.List;

@CommandInfo (name =  "delete", usageParameters = "[tool name]")
public class DeleteConfirmCommand extends IoMCommand
{
    @Override
    protected void run() throws CommandException
    {
        ImageMap map = getMapFromArgs();
        tellRaw("{text:\"You are going to delete \",extra:[{text:\""+ map.getId() +"\",color:gold},{text:\". Are you sure ? \",color:white}," +
            "{text:\"[Confirm]\", color:green, clickEvent:{action:run_command,value:\"/maptool delete-noconfirm "+ map.getId() +"\"}, " + 
            "hoverEvent:{action:show_text,value:{text:\"This map will be deleted \",extra:[{text:\"forever\",color:red,bold:true,italic:true,underlined:true}, {text:\" !\", underlined:true}],underlined:true}}}]}");
        
    }
    
    @Override
    protected List<String> complete() throws CommandException
    {
        if(args.length == 1) 
            return getMatchingMapNames(playerSender(), args[0]);

        return null;
    }
}
