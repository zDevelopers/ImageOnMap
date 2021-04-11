/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2021)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2021)
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

package fr.moribus.imageonmap.migration;

import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.PosterMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;

class OldSavedPoster {
    private final String userName;
    private final String posterName;
    private final short[] mapsIds;

    public OldSavedPoster(Object rawData, String key) throws InvalidConfigurationException {
        posterName = key;
        List<String> data;
        try {
            data = (List<String>) rawData;
        } catch (ClassCastException ex) {
            throw new InvalidConfigurationException("Invalid map data : " + ex.getMessage());
        }

        if (data.size() < 2) {
            throw new InvalidConfigurationException(
                    "Poster data too short (given : " + data.size() + ", expected at least 2)");
        }
        userName = data.get(0);
        mapsIds = new short[data.size() - 1];

        for (int i = 1, c = data.size(); i < c; i++) {
            try {
                mapsIds[i - 1] = Short.parseShort(data.get(i));
            } catch (NumberFormatException ex) {
                throw new InvalidConfigurationException("Invalid map ID : " + ex.getMessage());
            }
        }
    }

    public boolean contains(OldSavedMap map) {
        short mapId = map.getMapId();

        for (short mapsId : mapsIds) {
            if (mapsId == mapId) {
                return true;
            }
        }

        return false;
    }

    public ImageMap toImageMap(UUID userUUID) {
        // Converts the maps IDs to int as MC 1.13.2+ uses integer ids
        final int[] mapsIdsInt = new int[mapsIds.length];
        Arrays.setAll(mapsIdsInt, i -> mapsIds[i]);

        return new PosterMap(userUUID, mapsIdsInt, null, "poster", 0, 0);
    }

    public void serialize(Configuration configuration) {
        ArrayList<String> data = new ArrayList<String>();
        data.add(userName);

        for (short mapId : mapsIds) {
            data.add(Short.toString(mapId));
        }

        configuration.set(posterName, data);

    }

    public boolean isMapValid() {
        for (short mapId : mapsIds) {
            if (!MapManager.mapIdExists(mapId)) {
                return false;
            }
        }
        return true;
    }

    public String getUserName() {
        return userName;
    }

    public short[] getMapsIds() {
        return mapsIds;
    }
}
