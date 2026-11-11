package com.carloship.craft.sdk.server.instance.event;

import com.carloship.craft.sdk.server.instance.ServerInstance;
import net.minestom.server.event.Event;

public record ServerInstanceCreateEvent(
        ServerInstance<?> instance
) implements Event { }
