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
import java.util.ArrayList;
import java.util.Collection;
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
    /**
     * The maximal amount of usernames to send to mojang per request
     * This allows not to overload mojang's service with too many usernames at a time
     */
    static private final int MOJANG_USERNAMES_PER_REQUEST = 100;
    
    /**
     * The maximal amount of requests to send to Mojang
     * The time limit for this amount is MOJANG_MAX_REQUESTS_TIME
     * Read : You can only send MOJANG_MAX_REQUESTS in MOJANG_MAX_REQUESTS_TIME seconds
     */
    static private final int MOJANG_MAX_REQUESTS = 600;
    
    /**
     * The timeframe for the Mojang request limit (in seconds)
     */
    static private final int MOJANG_MAX_REQUESTS_TIME = 600;
    
    /**
     * The minimum time between two requests to mojang (in milliseconds)
     */
    static private final int TIME_BETWEEN_REQUESTS = 200;
    
    /**
     * The (approximative) timestamp of the date when Mojang name changing feature
     * was announced to be released
     */
    static private final int NAME_CHANGE_TIMESTAMP = 1420844400;
    
    static private final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    static private final String TIMED_PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";
    
    static public Map<String, UUID> fetch(List<String> names) throws IOException, InterruptedException
    {
        return fetch(names, MOJANG_USERNAMES_PER_REQUEST);
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
            tempUUIDs = rawFetch(tempNames);
            UUIDs.putAll(tempUUIDs);
            Thread.sleep(TIME_BETWEEN_REQUESTS);
        }
        
        return UUIDs;
    }
    
    static private Map<String, UUID> rawFetch(List<String> names) throws IOException
    {
        Map<String, UUID> uuidMap = new HashMap<String, UUID>();
        HttpURLConnection connection = getPOSTConnection(PROFILE_URL);
        
        writeBody(connection, names);
        JSONArray array;
        try
        {
            array = (JSONArray) readResponse(connection);
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
    
    static public void fetchRemaining(Collection<String> names, Map<String, UUID> uuids) throws IOException, InterruptedException
    {
        ArrayList<String> remainingNames = new ArrayList<>();
        
        for(String name : names)
        {
            if(!uuids.containsKey(name)) remainingNames.add(name);
        }
        
        int timeBetweenRequests;
        if(remainingNames.size() > MOJANG_MAX_REQUESTS)
        {
            timeBetweenRequests = (MOJANG_MAX_REQUESTS / MOJANG_MAX_REQUESTS_TIME) * 1000;
        }
        else
        {
            timeBetweenRequests = TIME_BETWEEN_REQUESTS;
        }
        
        User user;
        for(String name : remainingNames)
        {
            user = fetchOriginalUUID(name);
            uuids.put(name, user.uuid);
            Thread.sleep(timeBetweenRequests);
        }
        
    }
    
    static private User fetchOriginalUUID(String name) throws IOException
    {
        HttpURLConnection connection = getGETConnection(TIMED_PROFILE_URL + name + "?at=" + NAME_CHANGE_TIMESTAMP);
        
        JSONObject object;
        
        try
        {
            object = (JSONObject) readResponse(connection);
        }
        catch(ParseException ex)
        {
            throw new IOException("Invalid response from server, unable to parse received JSON : " + ex.toString());
        }
        
        User user = new User();
        user.name = (String) object.get("name");
        user.uuid = fromMojangUUID((String)object.get("id"));
        return user;
    }
    
    static private HttpURLConnection getPOSTConnection(String url) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }
    
    static private HttpURLConnection getGETConnection(String url) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
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
    
    private static Object readResponse(HttpURLConnection connection) throws IOException, ParseException
    {
            return new JSONParser().parse(new InputStreamReader(connection.getInputStream()));
    }
    
    private static UUID fromMojangUUID(String id) //Mojang sends string UUIDs without dashes ...
    {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" +
                               id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + 
                               id.substring(20, 32));
    }
    
    static private class User
    {
        public String name;
        public UUID uuid;
    }
    
}

