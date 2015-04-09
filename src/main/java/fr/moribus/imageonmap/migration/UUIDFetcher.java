/*
 * Copyright (C) 2013 Moribus
 * Copyright (C) 2015 ProkopyL <prokopylmc@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.moribus.imageonmap.migration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

abstract public class UUIDFetcher 
{
    static private final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft"; //The URI of the name->UUID API from Mojang
    static private final JSONParser jsonParser = new JSONParser();

    static public Map<String, UUID> fetch(List<String> names) throws IOException
    {
        Map<String, UUID> uuidMap = new HashMap<String, UUID>();
        HttpURLConnection connection = createConnection();
        
        writeBody(connection, names);
        
        JSONArray array;
        try
        {
            array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
        }
        catch(ParseException ex)
        {
            throw new IOException("Invalid response from server, unable to parse received JSON : " + ex.toString());
        }
        
        for (Object profile : array) 
        {
            JSONObject jsonProfile = (JSONObject) profile;
            String id = (String) jsonProfile.get("id");
            String name = (String) jsonProfile.get("name");
            uuidMap.put(name, fromMojangUUID(id));
        }
        
        
        return uuidMap;
    }
    
    static public Map<String, UUID> fetch(List<String> names, int limitByRequest) throws IOException, InterruptedException
    {
        Map<String, UUID> UUIDs = new HashMap<String, UUID>();
        int requests = (names.size() / limitByRequest) + 1;
        
        List<String> tempNames;
        Map<String, UUID> tempUUIDs;
        
        for(int i = 0; i < requests; i++)
        {
            tempNames = names.subList(limitByRequest * i, Math.min((limitByRequest * (i+1)) - 1, names.size()));
            tempUUIDs = fetch(tempNames);
            UUIDs.putAll(tempUUIDs);
            Thread.sleep(400);
        }
        
        return UUIDs;
    }

    private static HttpURLConnection createConnection() throws IOException 
    {
        URL url = new URL(PROFILE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    
    private static void writeBody(HttpURLConnection connection, List<String> names) throws IOException 
    {
        OutputStream stream = connection.getOutputStream();
        String body = JSONArray.toJSONString(names);
        stream.write(body.getBytes());
        stream.flush();
        stream.close();
    }


    private static UUID fromMojangUUID(String id) //Mojang sends string UUIDs without dashes ...
    {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" +
                               id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + 
                               id.substring(20, 32));
    }

}

