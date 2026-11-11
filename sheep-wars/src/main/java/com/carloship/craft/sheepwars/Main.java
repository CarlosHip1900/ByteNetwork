package com.carloship.craft.sheepwars;

import com.carloship.craft.sdk.SDKServerInitializer;

public class Main {

    static void main() {
        SDKServerInitializer.createServer(SheepWarsServer::new);
    }

}
