package fr.moribus.imageonmap.commands.maptool;

import fr.moribus.imageonmap.Permissions;
import fr.moribus.imageonmap.commands.IoMCommand;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import org.bukkit.command.CommandSender;

@CommandInfo(name = "RemotePlacing", usageParameters = "[player name]:map name position rotation")
public class RemotePlacingCommand extends IoMCommand {
    @Override
    protected void run() throws CommandException {
        //if wall => need position and direction N/S/E/W
        //else if floor or ceiling => same + rotation
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return Permissions.REMOTE_PLACING.grantedTo(sender);
    }
}
