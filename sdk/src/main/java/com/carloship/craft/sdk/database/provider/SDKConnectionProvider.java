package com.carloship.craft.sdk.database.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor @Builder
public class SDKConnectionProvider {

    private String ip;
    private int port;
    private String database;
    private String username;
    private String password;
    private String table;

    @Contract("_, _, _, _ -> new")
    public static @NotNull SDKConnectionProvider localhost(String database, String username, String password, String table) {
        return new SDKConnectionProvider("localhost", 3306, database, username, password, table);
    }

    @Contract("_, _ -> new")
    public static @NotNull SDKConnectionProvider localhost(String database, String table) {
        return new SDKConnectionProvider("localhost", 3306, database, "root", "root", table);
    }

    public static @NotNull SDKConnectionProvider localhost(int port , String database, String table) {
        return new SDKConnectionProvider("localhost", port, database, "root", "root", table);
    }

    public static @NotNull SDKConnectionProvider redisLocalhost() {
        return new SDKConnectionProvider("localhost", 6379, null, "root", "root", null);
    }

    @Contract("_, _, _, _, _, _ -> new")
    public static @NotNull SDKConnectionProvider of(String ip, int port, String database, String username, String password, String table) {
        return new SDKConnectionProvider(ip, port, database, username, password, table);
    }

    public String getSQLUrl() {
        return "jdbc:mysql://" + ip + ":" + port + "/" + database;
    }

    public String getMongoURL() {
        StringBuilder sb = new StringBuilder("mongodb://");

        if (username != null && !username.isBlank()) {
            sb.append(username);
            if (password != null && !password.isBlank()) {
                sb.append(":").append(password);
            }
            sb.append("@");
        }

        sb.append(ip).append(":").append(port);

        if (database != null && !database.isBlank()) {
            sb.append("/").append(database);
        }

        sb.append("?authSource=byte");

        return sb.toString();
    }


    public String getRedisURL() {
        return "redis://" + ip + ":" + port;
    }

    public static @NotNull SDKConnectionProvider fromMongoUrl(String url, String databaseName) {
        if (url == null || !url.startsWith("mongodb://")) {
            throw new IllegalArgumentException("Invalid MongoDB URL: " + url);
        }

        // Remove protocol
        String stripped = url.substring("mongodb://".length());

        String credentialsPart = null;
        String hostPart;

        if (stripped.contains("@")) {
            String[] split = stripped.split("@", 2);
            credentialsPart = split[0];
            hostPart = split[1];
        } else {
            hostPart = stripped;
        }

        String username = null;
        String password = null;

        if (credentialsPart != null) {
            String[] creds = credentialsPart.split(":", 2);
            username = creds[0];
            password = creds.length > 1 ? creds[1] : null;
        }

        String host = hostPart;
        int port = 27017;

        if (hostPart.contains(":")) {
            String[] hostSplit = hostPart.split(":", 2);
            host = hostSplit[0];
            port = Integer.parseInt(hostSplit[1]);
        }

        return new SDKConnectionProvider(
                host,
                port,
                databaseName,
                username,
                password,
                null
        );
    }

    public static @NotNull SDKConnectionProvider fromMongoUrl(String url) {
        return fromMongoUrl(url, "warcane");
    }

}
