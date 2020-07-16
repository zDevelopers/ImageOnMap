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

package fr.moribus.imageonmap.map;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.ui.MapItemManager;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class ImageMap implements ConfigurationSerializable
{
    public enum Type
    {
        SINGLE, POSTER
    }
    
    static public final int WIDTH = 128;
    static public final int HEIGHT = 128;

    /// The default display name of a map
    static public final String DEFAULT_NAME = I.t("Map");
    
    private String id;
    private final UUID userUUID;
    private final Type mapType;
    private String name;
    protected ImageMap(UUID userUUID, Type mapType)
    {
        this(userUUID, mapType, null, null);
    }
    
    protected ImageMap(UUID userUUID, Type mapType, String id, String name)
    {
        this.userUUID = userUUID;
        this.mapType = mapType;
        this.id = id;
        this.name = name;
        
        if(this.id == null)
        {
            if(this.name == null) this.name = DEFAULT_NAME;
            this.id = MapManager.getNextAvailableMapID(this.name, userUUID);
        }
    }
    
    
    public abstract int[] getMapsIDs();
    public abstract boolean managesMap(int mapID);
    public abstract int getMapCount();
    
    public boolean managesMap(ItemStack item)
    {
        if(item == null) return false;
        if(item.getType() != Material.FILLED_MAP) return false;
        return managesMap(MapManager.getMapIdFromItemStack(item));
    }
    
    public boolean give(Player player)
    {
        return MapItemManager.give(player, this);
    }
    
    public static File getFullImageFile(int mapIDstart, int mapIDend)
    {
        return new File(ImageOnMap.getPlugin().getImagesDirectory(), "_"+mapIDstart+"-"+mapIDend+".png");
    }
    
    /* ====== Serialization methods ====== */
    
    static public ImageMap fromConfig(Map<String, Object> map, UUID userUUID) throws InvalidConfigurationException
    {
        Type mapType;
        try
        {
            mapType = Type.valueOf((String) map.get("type"));
        }
        catch(ClassCastException ex)
        {
            throw new InvalidConfigurationException(ex);
        }
        
        switch(mapType)
        {
            case SINGLE: return new SingleMap(map, userUUID);
            case POSTER: return new PosterMap(map, userUUID);
            default: throw new IllegalArgumentException("Unhandled map type given");
        }
    }
    
    protected ImageMap(Map<String, Object> map, UUID userUUID, Type mapType) throws InvalidConfigurationException
    {
        this(userUUID, mapType,
                (String) getNullableFieldValue(map, "id"),
                (String) getNullableFieldValue(map, "name"));
        
    }
    
    protected abstract void postSerialize(Map<String, Object> map);
    
    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", getId());
        map.put("type", mapType.toString());
        map.put("name", getName());
        this.postSerialize(map);
        return map;
    }

    static public Integer[] getSize(Map<String, Object> map, UUID playerUUID, String id){

        ConfigurationSection section=MapManager.getPlayerMapStore(playerUUID).getToolConfig().getConfigurationSection("PlayerMapStore");

        if(section == null) return null;
        List<Map<String, Object>> list = (List<Map<String, Object>>) section.getList("mapList");
        if(list == null) return null;

        PluginLogger.info("list ");
        for(Map<String, Object> tMap : list)
        {
            PluginLogger.info(" "+tMap.toString());
            if(tMap.get("id").equals(id)) {

                    return new Integer[]{(Integer)tMap.get("columns"), (Integer)tMap.get("rows")};

            }
        }







        return null;
    }
    static protected <T> T getFieldValue(Map<String, Object> map, String fieldName) throws InvalidConfigurationException
    {
        T value = getNullableFieldValue(map, fieldName);
        if(value == null) throw new InvalidConfigurationException("Field value not found for \"" + fieldName + "\"");
        return value;
    }
    
    static protected <T> T getNullableFieldValue(Map<String, Object> map, String fieldName) throws InvalidConfigurationException
    {
        try
        {
            return (T)map.get(fieldName);
        }
        catch(ClassCastException ex)
        {
            throw new InvalidConfigurationException("Invalid field \"" + fieldName + "\"", ex);
        }
    }

    
    /* ====== Getters & Setters ====== */
    
    public UUID getUserUUID()
    {
        return userUUID;
    }

    public synchronized String getName()
    {
        return name;
    }
    
    public synchronized String getId()
    {
        return id;
    }

    public synchronized Type getType()
    {
        return mapType;
    }

    public synchronized void rename(String id, String name)
    {
        this.id = id;
        this.name = name;
    }
    
    public void rename(String name)
    {
        if(getName().equals(name)) return;
        rename(MapManager.getNextAvailableMapID(name, getUserUUID()), name);
    }
}
