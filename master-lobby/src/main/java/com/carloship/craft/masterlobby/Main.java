package com.carloship.craft.masterlobby;

import com.carloship.craft.sdk.SDKServerInitializer;

public class Main {

    static void main() {
        SDKServerInitializer.createServer(MasterServer::new);
    }

}
