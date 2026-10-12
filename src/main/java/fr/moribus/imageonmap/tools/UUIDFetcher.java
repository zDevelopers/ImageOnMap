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

package fr.moribus.imageonmap.tools;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Retrieves player UUIDs from player names using Mojang's API.
 */
public final class UUIDFetcher {
    /**
     * The maximal amount of names sent in a single bulk request.
     */
    private static final int MAX_NAMES_PER_REQUEST = 10;

    /**
     * The delay between two requests, in milliseconds, to respect Mojang's rate limits.
     */
    private static final int TIME_BETWEEN_REQUESTS = 1000;

    private static final String BULK_PROFILE_URL =
            "https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname";
    private static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private UUIDFetcher() {
    }

    /**
     * Fetches the UUIDs of the given players.
     *
     * @param names The names of the players.
     * @return A map from names to UUIDs. Players not found are not included.
     * @throws IOException          If Mojang's API could not be reached.
     * @throws InterruptedException If the thread is interrupted while waiting between requests.
     */
    public static Map<String, UUID> fetch(List<String> names) throws IOException, InterruptedException {
        final Map<String, UUID> uuids = new HashMap<>();

        for (int i = 0; i < names.size(); i += MAX_NAMES_PER_REQUEST) {
            if (i > 0) {
                Thread.sleep(TIME_BETWEEN_REQUESTS);
            }

            final JsonArray body = new JsonArray();
            for (String name : names.subList(i, Math.min(i + MAX_NAMES_PER_REQUEST, names.size()))) {
                body.add(name);
            }

            final HttpRequest request = HttpRequest.newBuilder(URI.create(BULK_PROFILE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            final JsonElement response = JsonParser.parseString(send(request));
            if (response.isJsonArray()) {
                for (JsonElement profile : response.getAsJsonArray()) {
                    addProfile(profile, names, uuids);
                }
            }
        }

        return uuids;
    }

    /**
     * Fetches, one by one, the UUIDs of the players not already present in the given map.
     *
     * @param names The names of the players.
     * @param uuids The map to complete.
     * @throws IOException          If Mojang's API could not be reached.
     * @throws InterruptedException If the thread is interrupted while waiting between requests.
     */
    public static void fetchRemaining(Collection<String> names, Map<String, UUID> uuids)
            throws IOException, InterruptedException {
        for (String name : names) {
            if (uuids.containsKey(name)) {
                continue;
            }

            Thread.sleep(TIME_BETWEEN_REQUESTS);

            final HttpRequest request = HttpRequest.newBuilder(
                    URI.create(PROFILE_URL + URLEncoder.encode(name, StandardCharsets.UTF_8))).GET().build();
            final String response = send(request);
            if (!response.isBlank()) {
                addProfile(JsonParser.parseString(response), List.of(name), uuids);
            }
        }
    }

    private static String send(HttpRequest request) throws IOException, InterruptedException {
        final HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 404 || response.statusCode() == 204) {
            return "";
        }
        if (response.statusCode() / 100 != 2) {
            throw new IOException("Mojang API returned HTTP " + response.statusCode());
        }
        return response.body();
    }

    private static void addProfile(JsonElement element, Collection<String> requestedNames, Map<String, UUID> uuids) {
        if (!element.isJsonObject()) {
            return;
        }

        final JsonObject profile = element.getAsJsonObject();
        if (!profile.has("id") || !profile.has("name")) {
            return;
        }

        final String returnedName = profile.get("name").getAsString();
        final UUID uuid = fromMojangId(profile.get("id").getAsString());

        // Keeps the name as it was requested, as Mojang returns the name with its actual case.
        for (String requestedName : requestedNames) {
            if (requestedName.equalsIgnoreCase(returnedName)) {
                uuids.put(requestedName, uuid);
                return;
            }
        }
        uuids.put(returnedName, uuid);
    }

    private static UUID fromMojangId(String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16)
                + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
    }
}
