package com.carloship.craft.sdk.server.instance.content;

import net.minestom.server.instance.Instance;

public interface InstanceContentObject {

     InstanceContentObject spawn(Instance instance);

    void destroy();

}