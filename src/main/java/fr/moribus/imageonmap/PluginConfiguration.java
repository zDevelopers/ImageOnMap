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

package fr.moribus.imageonmap;

import fr.zcraft.zlib.components.configuration.Configuration;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;

import java.util.Locale;

import static fr.zcraft.zlib.components.configuration.ConfigurationItem.item;


public final class PluginConfiguration extends Configuration
{
    static public ConfigurationItem<Locale> LANG = item("lang", Locale.class);

    static public ConfigurationItem<Boolean> COLLECT_DATA = item("collect-data", true);

    static public ConfigurationItem<Boolean> CHECK_FOR_UPDATES = item("check-for-updates", true);

    static public ConfigurationItem<Integer> MAP_GLOBAL_LIMIT = item("map-global-limit", 0, "Limit-map-by-server");
    static public ConfigurationItem<Integer> MAP_PLAYER_LIMIT = item("map-player-limit", 0, "Limit-map-by-player");

    static public ConfigurationItem<Boolean> SAVE_FULL_IMAGE = item("save-full-image", true);


    static public ConfigurationItem<Integer> LIMIT_SIZE_X = item("limit-map-size-x", 0);
    static public ConfigurationItem<Integer> LIMIT_SIZE_Y = item("limit-map-size-y", 0);

}
