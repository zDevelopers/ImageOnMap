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

import fr.moribus.imageonmap.i18n.I18n;
import java.util.Locale;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Base class of all GUIs.
 */
public abstract class GuiBase {
    private Player player;
    private Locale playerLocale;
    private GuiBase parent;
    private Listener listener;
    private boolean open = false;
    private boolean immune = false;

    /**
     * Asks the GUI to update its data, and display it to the player.
     */
    public void update() {
        onUpdate();
        onAfterUpdate();
    }

    /**
     * Closes this GUI. If it has a parent GUI, the parent is opened again.
     */
    public void close() {
        close(false);
    }

    protected void close(boolean immune) {
        registerClose();
        if (parent != null) {
            Gui.open(player, parent).setImmune(immune);
        }
    }

    void registerClose() {
        if (!open) {
            return;
        }

        open = false;
        Gui.registerGuiClose(this);
        if (listener != null) {
            Gui.unregisterEvents(listener);
        }

        onClose();
    }

    protected void onUpdate() {
    }

    protected void onAfterUpdate() {
    }

    protected Listener getEventListener() {
        return null;
    }

    protected void onClose() {
    }

    protected void open(Player player) {
        this.player = player;
        this.playerLocale = I18n.getPlayerLocale(player);

        Gui.registerGuiOpen(player, this);
        update();

        if (listener == null) {
            listener = getEventListener();
        }
        if (listener != null) {
            Gui.registerEvents(listener);
        }

        open = true;
    }

    public final boolean isOpen() {
        return open;
    }

    public final GuiBase getParent() {
        return parent;
    }

    void setParent(GuiBase parent) {
        if (parent == this) {
            throw new IllegalArgumentException("A GUI cannot be its own parent.");
        }
        this.parent = parent;
    }

    protected final Player getPlayer() {
        return player;
    }

    protected final Locale getPlayerLocale() {
        return playerLocale;
    }

    /**
     * Checks if the GUI is immune to the next close event, and consumes this immunity.
     *
     * @return {@code true} if the GUI was immune.
     */
    protected boolean checkImmune() {
        if (!immune) {
            return false;
        }
        immune = false;
        return true;
    }

    private void setImmune(boolean immune) {
        this.immune = immune;
    }
}
