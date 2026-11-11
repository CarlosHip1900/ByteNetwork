package com.carloship.craft.sdk.server.skin;

import com.carloship.craft.sdk.utils.FileUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.minestom.server.entity.PlayerSkin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class SkinCache {

    private static final File FILE = FileUtils.getOrCreateFile("common", "skin_cache.json");
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Cache<UUID, SkinEntry> CACHE = Caffeine.newBuilder().expireAfterAccess(TTL).build();
    private static final Cache<String, UUID> ID_CACHE = Caffeine.newBuilder().expireAfterAccess(TTL).build();

    private static final PlayerSkin DEFAULT = SkinManager.DEFAULT_NPC_SKIN;

    public static PlayerSkin fetchSkin(String playerName) {
        if (playerName == null) return DEFAULT;

        UUID id = ID_CACHE.getIfPresent(playerName);
        if (id == null) {
            SkinEntry entry = fetchSkinEntryByName(playerName);
            if (entry != null) {
                CACHE.put(entry.playerUuid(), entry);
                ID_CACHE.put(entry.playerName(), entry.playerUuid());
                return entry.toSkin();
            }
            return DEFAULT;
        }

        return fetchSkin(id);
    }

    public static PlayerSkin fetchSkin(UUID uuid) {
        if (uuid == null) return DEFAULT;

        SkinEntry entry = CACHE.getIfPresent(uuid);
        if (entry == null) {
            PlayerSkin fileSkin = fetchInFile(uuid);
            if (fileSkin != null) {
                return fileSkin;
            }
            return DEFAULT;
        }

        return entry.toSkin();
    }

    private static PlayerSkin fetchInFile(UUID uuid) {
        SkinEntry entry = readAllEntries().stream()
                .filter(e -> e.playerUuid().equals(uuid))
                .findFirst()
                .orElse(null);

        if (entry != null) {
            CACHE.put(entry.playerUuid(), entry);
            ID_CACHE.put(entry.playerName(), entry.playerUuid());
            return entry.toSkin();
        }
        return null;
    }

    private static SkinEntry fetchSkinEntryByName(String playerName) {
        return readAllEntries().stream()
                .filter(e -> e.playerName().equalsIgnoreCase(playerName))
                .findFirst()
                .orElse(null);
    }

    public static void loadAllSkins() {
        List<SkinEntry> entries = readAllEntries();
        for (SkinEntry entry : entries) {
            if (entry.playerUuid() != null && entry.playerName() != null) {
                CACHE.put(entry.playerUuid(), entry);
                ID_CACHE.put(entry.playerName(), entry.playerUuid());
            }
        }
        log.info("Loaded {} skins... in JSON file", entries.size());
    }

    public static void saveSkin(UUID uuid, String playerName, String texture, String signature) {
        SkinEntry entry = new SkinEntry(uuid, playerName, texture, signature);
        CACHE.put(uuid, entry);
        ID_CACHE.put(playerName, uuid);
        saveInFile(entry);
    }

    private static synchronized void saveInFile(SkinEntry newEntry) {
        List<SkinEntry> entries = new ArrayList<>(readAllEntries());

        entries.removeIf(e -> e.playerUuid().equals(newEntry.playerUuid()));
        entries.add(newEntry);

        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(entries, writer);
        } catch (IOException e) {
            log.error("Error saving skin to skin_cache.json", e);
        }
    }

    private static synchronized List<SkinEntry> readAllEntries() {
        if (!FILE.exists() || FILE.length() == 0) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(FILE)) {
            Type listType = new TypeToken<List<SkinEntry>>() {}.getType();
            List<SkinEntry> entries = GSON.fromJson(reader, listType);
            return entries != null ? entries : new ArrayList<>();
        } catch (IOException e) {
            log.error("Error reading skin_cache.json", e);
            return new ArrayList<>();
        }
    }

    public record SkinEntry(UUID playerUuid, String playerName, String texture, String signature) {
        public PlayerSkin toSkin() {
            return new PlayerSkin(texture, signature);
        }
    }
}