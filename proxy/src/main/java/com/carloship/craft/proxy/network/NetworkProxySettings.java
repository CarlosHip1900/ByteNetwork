package com.carloship.craft.proxy.network;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kyori.adventure.text.Component;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Data
@AllArgsConstructor
public class NetworkProxySettings {

    private Component firstLine;
    private Component secondLine;
    private int maxPlayers;

    public Component motd(){
        return firstLine.appendNewline().append(secondLine);
    }

    public static final Component DEFAULT_FIRST_LINE = DMM("<gradient:#00FFFF:#87CEFA><bold>BYTE NETWORK</bold></gradient> <bold><dark_gray>/ <gold>26.2");
    public static final Component DEFAULT_SECOND_LINE = DMM("<dark_gray> > <dark_aqua>Em desenvolvimento... <dark_gray>(Minigames & Survival)");

    public static NetworkProxySettings createDefault(){
        return new NetworkProxySettings(DEFAULT_FIRST_LINE, DEFAULT_SECOND_LINE, 64);
    }

    public void resetMotd() {
        setFirstLine(DEFAULT_FIRST_LINE);
        setSecondLine(DEFAULT_SECOND_LINE);
    }
}
