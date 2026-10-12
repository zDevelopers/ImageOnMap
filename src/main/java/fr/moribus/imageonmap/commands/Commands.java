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

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Registry of the plugin commands.
 */
public final class Commands {
    public static final String CHAT_PREFIX = "┃";
    private static final List<CommandGroup> commandGroups = new ArrayList<>();

    private Commands() {
    }

    /**
     * Registers a root command with its sub-commands.
     *
     * @param name            The name of the command.
     * @param commandsClasses The sub-commands.
     */
    @SafeVarargs
    public static void register(String name, Class<? extends Command>... commandsClasses) {
        commandGroups.add(new CommandGroup(new String[] {name}, commandsClasses));
    }

    /**
     * Registers a shortcut command, executing directly a sub-command of a registered command.
     *
     * @param commandGroupName The name of the registered root command.
     * @param commandClass     The sub-command to execute.
     * @param shortcutNames    The name of the shortcut, then its aliases.
     */
    public static void registerShortcut(String commandGroupName, Class<? extends Command> commandClass,
                                        String... shortcutNames) {
        final CommandGroup group = getMatchingCommandGroup(commandGroupName);
        if (group == null) {
            throw new IllegalArgumentException("Invalid command group name: " + commandGroupName);
        }
        commandGroups.add(new CommandGroup(group, commandClass, shortcutNames));
    }

    /**
     * Registers the commands in the server once it is ready to accept them.
     *
     * @param plugin The plugin.
     */
    public static void registerToServer(JavaPlugin plugin) {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            for (CommandGroup group : commandGroups) {
                event.registrar().register(group.getUsualName(), group.getDescription(), group.getAliases(), group);
            }
        });
    }

    /**
     * Unregisters all commands from this registry.
     */
    public static void clear() {
        commandGroups.clear();
    }

    /**
     * Gets the command matching the given class.
     *
     * @param commandClass The command class.
     * @return The matching command, or null if none were found.
     */
    public static Command getCommandInfo(Class<? extends Command> commandClass) {
        for (CommandGroup commandGroup : commandGroups) {
            final Command command = commandGroup.getCommandInfo(commandClass);
            if (command != null) {
                return command;
            }
        }
        return null;
    }

    private static CommandGroup getMatchingCommandGroup(String commandName) {
        for (CommandGroup commandGroup : commandGroups) {
            if (commandGroup.matches(commandName)) {
                return commandGroup;
            }
        }
        return null;
    }
}
