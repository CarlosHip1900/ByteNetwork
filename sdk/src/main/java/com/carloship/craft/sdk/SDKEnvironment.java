package com.carloship.craft.sdk;

import com.carloship.craft.sdk.utils.EnvVariable;

public class SDKEnvironment {

    public static final EnvVariable LIMIT_INSTANCES = new EnvVariable("server.instances.limit", "SERVER_INSTANCES_LIMIT");

    public static final EnvVariable SERVER_TYPE = new EnvVariable("server.game.type", "SERVER_GAME_TYPE");

    public static final EnvVariable SERVER_ID = new EnvVariable("server.id", "SERVER_ID");
    public static final EnvVariable CLUSTER_ID = new EnvVariable("server.cluster", "CLUSTER_ID");

    public static final EnvVariable REDIS_URL = new EnvVariable("sys.url.redis", "SYS_REDIS_URL");
    public static final EnvVariable MONGO_URL = new EnvVariable("sys.url.mongo", "SYS_MONGO_URL");

    public static final EnvVariable SERVER_PORT = new EnvVariable("minecraft.port", "MINECRAFT_PORT");
    public static final EnvVariable SERVER_HOST = new EnvVariable("minecraft.host", "MINECRAFT_HOST");

    public static final EnvVariable PROXY = new EnvVariable("minecraft.proxy.enabled", "MINECRAFT_PROXY_ENABLED");
    public static final EnvVariable PROXY_SECRET_KEY = new EnvVariable("minecraft.proxy.key", "MINECRAFT_PROXY_KEY");
    public static final EnvVariable PROXY_JOIN_REDIRECT_LOBBY = new EnvVariable("minecraft.proxy.redirect.lobby", "MINECRAFT_PROXY_REDIRECT_LOBBY");


}
