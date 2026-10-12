/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2021)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2021)
 *
 * Parts of this file are adapted from QuartzLib (https://github.com/zDevelopers/QuartzLib),
 * Copyright or © or Copr. ZDevelopers (2015 – 2021), governed by the CeCILL-B license.
 *
 * This software is a computer program whose purpose is to allow insertion of
 * custom images in a Minecraft world.
 *
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
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
 * knowledge of the CeCILL license and that you accept its terms.
 */

package fr.moribus.imageonmap.commands;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.gui.framework.GuiUtils;
import fr.moribus.imageonmap.tools.ChatColor;
import fr.moribus.imageonmap.tools.PluginLogger;
import fr.moribus.imageonmap.tools.Text;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

/**
 * The {@code help} sub-command, automatically added to every root command.
 */
@CommandInfo(name = "help", usageParameters = "<command name>")
public class HelpCommand extends Command {
    private static final int ITEMS_PER_PAGE = 8;

    @Override
    protected void run() throws CommandException {
        if (args.length < 1) {
            groupHelp(1);
        } else {
            if (args.length == 1 && args[0].startsWith("--page=")) {
                try {
                    groupHelp(Integer.parseInt(args[0].split("=")[1]));
                    return;
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                    // Not a page: displays the help of a command named like this.
                }
            }
            commandHelp();
        }
    }

    private void groupHelp(int page) {
        final List<Command> displayedCommands = new ArrayList<>();

        for (Command subCommand : commandGroup.getCommands()) {
            if (subCommand.canExecute(sender, args)) {
                displayedCommands.add(subCommand);
            }
        }

        if (sender instanceof Player) {
            info("");
        }

        final boolean paginate = sender instanceof Player;
        final int pagesCount = (int) Math.ceil((double) displayedCommands.size() / ITEMS_PER_PAGE);
        final int currentPage = page < 1 ? 1 : Math.min(page, pagesCount);
        final int from = paginate ? Math.max(0, (currentPage - 1) * ITEMS_PER_PAGE) : 0;
        final int to = paginate ? Math.min(from + ITEMS_PER_PAGE, displayedCommands.size()) : displayedCommands.size();

        displayHeader();
        for (int i = from; i < to; i++) {
            displayItem(displayedCommands.get(i));
        }
        if (paginate && pagesCount > 1) {
            displayFooter(currentPage, pagesCount);
        }
    }

    private void displayHeader() {
        final String header = ChatColor.BOLD + (commandGroup.getDescription().isEmpty()
                ? ImageOnMap.getPlugin().getName() + " help for /" + commandGroup.getUsualName()
                : commandGroup.getDescription());

        sender.sendMessage(sender instanceof Player
                ? GuiUtils.generatePrefixedFixedLengthString(
                ChatColor.BLUE + Commands.CHAT_PREFIX + " " + ChatColor.RESET, header)
                : header
        );
    }

    private void displayItem(Command command) {
        final String commandName = "/" + commandGroup.getUsualName() + " " + command.getName();
        final String description = commandGroup.getDescription(command.getName());

        String helpMessage = ChatColor.GOLD + commandName;
        if (description != null) {
            helpMessage += ChatColor.GOLD + ": " + ChatColor.WHITE + description;
        }

        final String formattedHelpMessage = sender instanceof Player
                ? GuiUtils.generatePrefixedFixedLengthString(ChatColor.GOLD + Commands.CHAT_PREFIX + " ", helpMessage)
                : helpMessage;

        sender.sendMessage(Text.legacy(formattedHelpMessage)
                .clickEvent(ClickEvent.suggestCommand(commandName + " "))
                .hoverEvent(HoverEvent.showText(Component.text(command.getUsageString()))));
    }

    private void displayFooter(int currentPage, int pagesCount) {
        final TextComponent.Builder footer = Component.text();

        if (currentPage > 1) {
            footer.append(Component.text("« Previous", NamedTextColor.GRAY)
                            .clickEvent(ClickEvent.runCommand(build("--page=" + (currentPage - 1))))
                            .hoverEvent(HoverEvent.showText(Component.text("Go to page " + (currentPage - 1)))))
                    .append(Component.text(" ⋅ ", NamedTextColor.GRAY));
        }

        footer.append(Component.text("Page " + currentPage + " of " + pagesCount, NamedTextColor.GRAY,
                TextDecoration.BOLD));

        if (currentPage < pagesCount) {
            footer.append(Component.text(" ⋅ ", NamedTextColor.GRAY))
                    .append(Component.text("Next »", NamedTextColor.GRAY)
                            .clickEvent(ClickEvent.runCommand(build("--page=" + (currentPage + 1))))
                            .hoverEvent(HoverEvent.showText(Component.text("Go to page " + (currentPage + 1)))));
        }

        sender.sendMessage(footer.build());
    }

    private void commandHelp() throws CommandException {
        final Command command = commandGroup.getMatchingCommand(args[0]);
        if (command == null) {
            error("The specified command does not exist.");
            return;
        }

        if (!command.canExecute(sender, args)) {
            warning("You do not have the permission to use this command.");
        }

        String message = "\n";
        message += GuiUtils.generatePrefixedFixedLengthString("§6" + Commands.CHAT_PREFIX + "§l ",
                ImageOnMap.getPlugin().getName() + " help for /" + command.getCommandGroup().getUsualName() + " "
                        + command.getName()) + "\n";
        message += GuiUtils.generatePrefixedFixedLengthString("§6" + Commands.CHAT_PREFIX + " ",
                "Usage: §r" + command.getUsageString()) + "\n";

        try {
            final String help = getHelpText(command);
            if (help.isEmpty()) {
                message += "§c" + Commands.CHAT_PREFIX + " There is no help message for this command.";
            } else {
                message += help;
            }
        } catch (IOException ex) {
            message += "§c" + Commands.CHAT_PREFIX + " Could not read help for this command.";
            PluginLogger.warning("Could not read help for the command: " + command.getName(), ex);
        }

        sender.sendMessage(message);
    }

    private String getHelpText(Command command) throws IOException {
        final String fileName = "help/" + commandGroup.getUsualName() + "/" + command.getName() + ".txt";
        final StringBuilder result = new StringBuilder();

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (stream == null) {
                return "";
            }

            try (Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8)) {
                while (scanner.hasNextLine()) {
                    result.append("§l§9" + Commands.CHAT_PREFIX + " §r").append(scanner.nextLine()).append("\n");
                }
            }
        }

        return result.toString().trim();
    }

    @Override
    protected List<String> complete() throws CommandException {
        if (args.length != 1) {
            return null;
        }

        final ArrayList<String> matches = new ArrayList<>();
        for (Command command : commandGroup.getCommands()) {
            if (command.getName().startsWith(args[0])) {
                matches.add(command.getName());
            }
        }
        return matches;
    }
}
