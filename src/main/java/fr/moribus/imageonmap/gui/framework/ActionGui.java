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

package fr.moribus.imageonmap.gui.framework;

import fr.moribus.imageonmap.tools.ItemStackBuilder;
import fr.moribus.imageonmap.tools.PluginLogger;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * An inventory GUI where items are buttons triggering actions, handled by methods annotated with {@link GuiAction}.
 */
public abstract class ActionGui extends InventoryGui {
    private static final String ACTION_HANDLER_NAME = "action_";

    private final Class<? extends ActionGui> guiClass = this.getClass();
    private final HashMap<Integer, Action> actions = new HashMap<>();

    protected void action(String name, int slot, ItemStackBuilder item) {
        action(name, slot, item.item());
    }

    protected void action(String name, int slot) {
        action(name, slot, (ItemStack) null);
    }

    /**
     * Creates a new action.
     *
     * @param name The name of the action.
     * @param slot The slot of the action button.
     * @param item The item of the action button.
     */
    protected void action(String name, int slot, ItemStack item) {
        if (slot > getSize() || slot < 0) {
            throw new IllegalArgumentException("Illegal slot ID");
        }
        actions.put(slot, new Action(name, slot, item, getActionHandler(guiClass, name)));
    }

    /**
     * Changes the item of an action.
     *
     * @param name The name of the action.
     * @param item The new item of the action button.
     */
    protected void updateAction(String name, ItemStack item) {
        for (Action action : actions.values()) {
            if (action.name.equals(name)) {
                action.item = item;
                return;
            }
        }
        throw new IllegalArgumentException("Unknown action name : " + name);
    }

    @Override
    protected abstract void onUpdate();

    @Override
    public void update() {
        actions.clear();
        super.update();
    }

    @Override
    protected void populate(Inventory inventory) {
        for (Action action : actions.values()) {
            inventory.setItem(action.slot, action.item);
        }
    }

    @Override
    protected void onClick(InventoryClickEvent event) {
        // The user clicked in their own inventory
        if (event.getRawSlot() >= event.getInventory().getSize()
                && !event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
            return;
        }

        event.setCancelled(true);
        callAction(actions.get(event.getRawSlot()), event);
    }

    private void callAction(Action action, InventoryClickEvent event) {
        if (action == null || action.callback == null) {
            return;
        }

        try {
            if (action.callback.getParameterTypes().length == 1) {
                action.callback.invoke(this, event);
            } else {
                action.callback.invoke(this);
            }
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            PluginLogger.error("Could not invoke GUI action handler", ex);
        } catch (InvocationTargetException ex) {
            PluginLogger.error("Error while invoking action handler {0} of GUI {1}",
                    ex.getCause(), action.name, guiClass.getName());
        }
    }

    private boolean isActionHandlerValid(Method method) {
        final int parameterCount = method.getParameterTypes().length;
        if (parameterCount >= 2) {
            return false;
        }
        if (parameterCount == 1 && method.getParameterTypes()[0] != InventoryClickEvent.class) {
            return false;
        }

        try {
            method.setAccessible(true);
        } catch (SecurityException ex) {
            return false;
        }

        return true;
    }

    private Method getActionHandler(Class<?> klass, String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        do {
            for (Method method : klass.getDeclaredMethods()) {
                final GuiAction actionAnnotation = method.getAnnotation(GuiAction.class);
                if (actionAnnotation == null) {
                    continue;
                }

                if (!actionAnnotation.value().isEmpty()) {
                    if (actionAnnotation.value().equals(name) && isActionHandlerValid(method)) {
                        return method;
                    }
                } else {
                    final String methodName = method.getName();
                    if (methodName.equals(name) && isActionHandlerValid(method)) {
                        return method;
                    }
                    if (methodName.startsWith(ACTION_HANDLER_NAME)
                            && methodName.substring(ACTION_HANDLER_NAME.length()).equals(name)
                            && isActionHandlerValid(method)) {
                        return method;
                    }
                }
            }

            klass = klass.getSuperclass();
        } while (klass != null);

        return null;
    }

    protected boolean hasActions() {
        return !actions.isEmpty();
    }

    private static final class Action {
        private final String name;
        private final int slot;
        private final Method callback;
        private ItemStack item;

        private Action(String name, int slot, ItemStack item, Method callback) {
            this.name = name;
            this.slot = slot;
            this.item = item;
            this.callback = callback;
        }
    }
}
