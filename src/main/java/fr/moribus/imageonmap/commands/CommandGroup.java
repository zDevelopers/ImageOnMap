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

import fr.moribus.imageonmap.tools.PluginLogger;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.bukkit.command.CommandSender;

/**
 * A root command (like {@code /maptool}) dispatching to sub-commands, or a shortcut command (like {@code /tomap})
 * executing a single sub-command directly.
 */
public class CommandGroup implements BasicCommand {
    private final CommandGroup shortcutCommandGroup;
    private final String[] names;
    private final Class<? extends Command>[] commandsClasses;
    private final ArrayList<Command> commands = new ArrayList<>();
    private final HashMap<String, String> commandsDescriptions = new HashMap<>();
    private String description = "";

    @SuppressWarnings("unchecked")
    CommandGroup(CommandGroup shortcutCommandGroup, Class<? extends Command> commandClass, String... names) {
        this.names = names;
        this.commandsClasses = new Class[] {commandClass};
        this.shortcutCommandGroup = shortcutCommandGroup;
        initCommands();
    }

    @SafeVarargs
    CommandGroup(String[] names, Class<? extends Command>... commandsClasses) {
        this.names = names;
        this.commandsClasses = commandsClasses;
        this.shortcutCommandGroup = null;
        initDescriptions();
        initCommands();
    }

    /**
     * Gets the command args from the given group args.
     *
     * @param args The group args.
     * @return The command args.
     */
    public static String[] getCommandArgsFromGroupArgs(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }

    private void initDescriptions() {
        final String fileName = "help/" + getUsualName() + ".txt";
        final InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);

        if (stream == null) {
            PluginLogger.warning("Could not load description file for the " + getUsualName() + " command");
            return;
        }

        final StringBuilder builder = new StringBuilder();
        boolean isGroupDescription = true;

        try (Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8)) {
            // Getting the group's description, and then each command's description
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                final int colonIndex = line.indexOf(':');

                if (isGroupDescription) {
                    final int firstSpaceIndex = line.indexOf(' ');
                    if (colonIndex > 0 && firstSpaceIndex > colonIndex) {
                        isGroupDescription = false;
                    }
                }

                if (isGroupDescription) {
                    builder.append(line).append('\n');
                } else if (colonIndex >= 0) {
                    commandsDescriptions.put(line.substring(0, colonIndex).trim(),
                            line.substring(colonIndex + 1).trim());
                }
            }
        }

        description = builder.toString().trim();
    }

    private void initCommands() {
        for (Class<? extends Command> commandClass : commandsClasses) {
            addCommand(commandClass);
        }

        if (!isShortcutCommand()) {
            addCommand(HelpCommand.class);
        }
    }

    private void addCommand(Class<? extends Command> commandClass) {
        try {
            final Command newCommand = commandClass.getConstructor().newInstance();
            newCommand.init(isShortcutCommand() ? shortcutCommandGroup : this);
            commands.add(newCommand);
        } catch (Exception ex) {
            PluginLogger.warning("Exception while initializing command", ex);
        }
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        executeMatchingCommand(commandSourceStack.getSender(), args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        return tabComplete(commandSourceStack.getSender(), args);
    }

    /**
     * Executes the command matching the args.
     *
     * @param sender The command's sender.
     * @param args   The command args.
     * @return true if a matching command was found.
     */
    public boolean executeMatchingCommand(CommandSender sender, String[] args) {
        if (isShortcutCommand()) {
            commands.get(0).execute(sender, args);
            return true;
        }

        if (args.length <= 0) {
            sender.sendMessage(getUsage());
            return false;
        }

        final Command command = getMatchingCommand(args[0]);
        if (command != null) {
            command.execute(sender, getCommandArgsFromGroupArgs(args));
        } else {
            sender.sendMessage(getUsage());
        }
        return command != null;
    }

    /**
     * Computes a list of possible autocomplete suggestions for the given partial arguments.
     *
     * @param sender The sender of the command.
     * @param args   The partial arguments.
     * @return A list of suggestions.
     */
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (isShortcutCommand()) {
            return commands.get(0).tabComplete(sender, args);
        }

        if (args.length <= 1) {
            return tabComplete(sender, args.length == 1 ? args[0] : null);
        }

        final Command command = getMatchingCommand(args[0]);
        if (command != null) {
            return command.tabComplete(sender, getCommandArgsFromGroupArgs(args));
        }
        return new ArrayList<>();
    }

    /**
     * Computes a list of possible autocomplete suggestions for the given command.
     *
     * @param sender      The sender of the command.
     * @param commandName The beginning of the name of the command.
     * @return A list of suggestions.
     */
    public List<String> tabComplete(CommandSender sender, String commandName) {
        final ArrayList<String> matchingCommands = new ArrayList<>();

        for (Command command : commands) {
            if (!command.canExecute(sender)) {
                continue;
            }
            if (commandName == null || command.getName().startsWith(commandName.toLowerCase())) {
                matchingCommands.add(command.getName());
            }
        }

        return matchingCommands;
    }

    /**
     * Gets the command matching the given name.
     *
     * @param commandName The command name.
     * @return The matching command, or null if none were found.
     */
    public Command getMatchingCommand(String commandName) {
        for (Command command : commands) {
            if (command.matches(commandName)) {
                return command;
            }
        }
        return null;
    }

    /**
     * Gets the command matching the given class.
     *
     * @param commandClass The command class.
     * @return The matching command, or null if none were found.
     */
    public Command getCommandInfo(Class<? extends Command> commandClass) {
        for (Command command : commands) {
            if (command.getClass().equals(commandClass)) {
                return command;
            }
        }
        return null;
    }

    /**
     * Checks if this command matches the given name.
     *
     * @param name The name of the command to test for.
     * @return if this command matches the given name.
     */
    public boolean matches(String name) {
        final String lowerName = name.toLowerCase();
        for (String commandName : names) {
            if (commandName.equals(lowerName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the names of all sub-commands.
     *
     * @return the names of all sub-commands.
     */
    public String[] getCommandsNames() {
        final String[] commandsNames = new String[commands.size()];
        for (int i = 0; i < commands.size(); i++) {
            commandsNames[i] = commands.get(i).getName();
        }
        return commandsNames;
    }

    protected String getUsage() {
        if (isShortcutCommand()) {
            return "§cUsage: " + commands.get(0).getUsageString();
        }
        return "§cUsage: /" + getUsualName() + " <" + String.join("|", getCommandsNames()) + ">";
    }

    public String getUsualName() {
        return names[0];
    }

    public List<String> getAliases() {
        return Arrays.asList(names).subList(1, names.length);
    }

    public Command[] getCommands() {
        return commands.toArray(new Command[0]);
    }

    /**
     * Returns the description of this command, as displayed in the help.
     *
     * @return The description.
     */
    public String getDescription() {
        if (isShortcutCommand()) {
            final String commandDescription = shortcutCommandGroup.getDescription(commands.get(0).getName());
            return commandDescription != null ? commandDescription : "";
        }
        return description;
    }

    public String getDescription(String commandName) {
        return commandsDescriptions.get(commandName);
    }

    public boolean isShortcutCommand() {
        return shortcutCommandGroup != null;
    }
}
