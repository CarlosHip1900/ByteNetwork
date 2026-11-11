package com.carloship.craft.sdk.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
public class MojangUserUtils {

    private static final Pattern PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    private static final Gson GSON = new Gson();

    public static UUID fromStringWithoutDashes(String uuid) {
        String correctedUUID = PATTERN.matcher(uuid).replaceAll("$1-$2-$3-$4-$5");
        return UUID.fromString(correctedUUID);
    }

    public static UUID fromString(String uuid) {
        return UUID.fromString(uuid);
    }

    public static String toString(UUID uuid) {
        return uuid.toString();
    }

    public static String toStringWithoutDashes(UUID uuid) {
        return uuid.toString().replace("-", "");
    }

    public static CompletableFuture<Boolean> isPremiumAccount(UUID uuid) {
        String uuidStr = toStringWithoutDashes(uuid);

        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URI("https://sessionserver.mojang.com/session/minecraft/profile/" + uuidStr).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();

                // 200 = valid premium account
                return responseCode == HttpURLConnection.HTTP_OK;
            } catch (Exception e) {
                log.error("Error while check if player is premium {}", e.getMessage());
                return false;
            }
        });
    }

    public static CompletableFuture<UUID> requestPlayerUuid(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URI("https://api.mojang.com/users/profiles/minecraft/" + name).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();

                // 204 = player not found
                if (responseCode == 204) {
                    return null;
                }

                if (responseCode != 200) {
                    return null;
                    //throw new IllegalStateException(
                    //        "Failed to request UUID for " + name + " (code " + responseCode + ")"
                    //);
                }

                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {

                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                    }

                    JsonObject json = GSON.fromJson(sb.toString(), JsonObject.class);
                    String uuidStr = json.get("id").getAsString();

                    return fromStringWithoutDashes(uuidStr);
                }

            } catch (Exception e) {
                log.error("Error while requesting UUID for player {}", name, e);
                return null;
            }
        });
    }
}
