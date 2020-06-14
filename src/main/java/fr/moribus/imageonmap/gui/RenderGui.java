/*
 * Plugin UHCReloaded : Alliances
 *
 * Copyright ou © ou Copr. Amaury Carrade (2016)
 * Idées et réflexions : Alexandre Prokopowicz, Amaury Carrade, "Vayan".
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */

package fr.moribus.imageonmap.gui;

import fr.moribus.imageonmap.image.ImageUtils;
import fr.zcraft.quartzlib.components.gui.ActionGui;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.tools.items.ItemStackBuilder;
import fr.zcraft.quartzlib.tools.items.TextualBanners;
import java.net.URL;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;


public class RenderGui extends ActionGui {
    final URL url;

    boolean resize = false;
    int width = 0;
    int height = 0;

    boolean originalSizeLoaded = false;
    int originalWidth = 0;
    int originalHeight = 0;

    ImageUtils.ScalingType scaling = ImageUtils.ScalingType.NONE;

    public RenderGui(URL url) {
        this.url = url;
    }

    @Override
    protected void onUpdate() {
        setTitle(I.t("Image Editor"));
        setHeight(6);

        action("toggle_resize", 0, new ItemStackBuilder(Material.PAINTING)
                .title(ChatColor.LIGHT_PURPLE, ChatColor.BOLD + I.t("Resize image"))
                .loreLine(ChatColor.DARK_GRAY, resize ? I.t("Enabled") : I.t("Disabled"))
                .loreSeparator()
                .longLore(ChatColor.GRAY,
                        I.t("You can automatically resize the image to a certain number of blocks (or item frames)."))
                .loreSeparator()
                .loreLine(ChatColor.BLUE, I.t("Original size (in blocks)"))
                .loreLine(ChatColor.GRAY,
                        originalSizeLoaded ? I.t("{0} × {1}", originalWidth, originalHeight) : I.t("Loading..."))
                .loreSeparator()
                .longLore(resize ? I.t("{gray}» {white}Click{gray} to disable resize") :
                        I.t("{gray}» {white}Click{gray} to enable resize"))
        );

        injectSizeEditor(2, true);
        injectSizeEditor(11, false);
    }

    /**
     * Injects the size editor in the GUI.
     *
     * @param slot    The slot where the editor must start.
     * @param isWidth True to inject a width-size editor; false to inject a height-editor.
     */
    private void injectSizeEditor(int slot, final boolean isWidth) {
        final String action_key = isWidth ? "width_" : "height_";
        final String currentSize = ChatColor.DARK_GRAY + I.t("Current size: {0} × {1}", width, height);

        action(action_key + "_decrement_10", slot++, getBannerButton(false, true, resize)
                .title(ChatColor.RED, I.t("- 10"))
                .loreLine(currentSize)
                .loreSeparator()
                .longLore(isWidth
                        ? I.t("{gray}» {white}Click{gray} to decrease the image's width by 10 blocks")
                        : I.t("{gray}» {white}Click{gray} to decrease the image's height by 10 blocks")
                )
        );

        action(action_key + "_decrement_1", slot++, getBannerButton(false, false, resize)
                .title(ChatColor.RED, I.t("- 1"))
                .loreLine(currentSize)
                .loreSeparator()
                .longLore(isWidth
                        ? I.t("{gray}» {white}Click{gray} to decrease the image's width by one block")
                        : I.t("{gray}» {white}Click{gray} to decrease the image's height by one block")
                )
        );

        action(action_key + "_increment_1", slot++, getBannerButton(true, false, resize)
                .title(ChatColor.GREEN, I.t("+ 1"))
                .loreLine(currentSize)
                .loreSeparator()
                .longLore(isWidth
                        ? I.t("{gray}» {white}Click{gray} to increase the image's width by one block")
                        : I.t("{gray}» {white}Click{gray} to increase the image's height by one block")
                )
        );

        action(action_key + "_increment_10", slot++, getBannerButton(true, true, resize)
                .title(ChatColor.GREEN, I.t("+ 10"))
                .loreLine(currentSize)
                .loreSeparator()
                .longLore(isWidth
                        ? I.t("{gray}» {white}Click{gray} to increase the image's width by 10 blocks")
                        : I.t("{gray}» {white}Click{gray} to increase the image's height by 10 blocks")
                )
        );


        /*(action_key + "_set_values", slot++, getBannerButton(false, false, resize)
                .title(ChatColor.BLUE, I.t("set the size"))
                .loreLine(currentSize)
                .loreSeparator()
                .longLore(isWidth
                        ? I.t("{gray}» {white}Click{gray} to set the image's width")
                        : I.t("{gray}» {white}Click{gray} to set the image's height")
                )
        );*/
        slot++;
    }

    /**
     * Creates a banner for the +/- buttons.
     * + are green, - are red
     * short steps are light, long steps are dark
     * disabled banners are in grayscale
     *
     * @param positive true for a + banner
     * @param longStep true for a darker banner
     * @param disabled true for a grayscale banner
     * @return The banner in a builder.
     */
    private ItemStackBuilder getBannerButton(boolean positive, boolean longStep, boolean disabled) {
        //final char symbol = positive ? '+' : '-'; //TODO this need rework have something that work but need QL update
        final char symbol = positive ? '*' : '-';
        final DyeColor background;

        if (disabled) {
            background = longStep ? DyeColor.BLACK : DyeColor.GRAY;
        } else {
            if (positive) {
                background = longStep ? DyeColor.GREEN : DyeColor.LIME;
            } else {
                background = longStep ? DyeColor.RED : DyeColor.PINK;
            }
        }

        return new ItemStackBuilder(TextualBanners.getCharBanner(symbol, background, DyeColor.BLACK));
    }
}
