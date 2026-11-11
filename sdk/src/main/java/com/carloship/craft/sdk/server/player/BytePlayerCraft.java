package com.carloship.craft.sdk.server.player;

import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.instance.ServerInstance;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.server.skin.SkinManager;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Getter
@Setter
public class BytePlayerCraft extends Player {

    private BytePlayerData playerData;

    private ServerInstanceMode currentServerMode;
    private UUID currentServerInstanceId;

    public BytePlayerCraft(PlayerConnection playerConnection, GameProfile gameProfile) {
        super(playerConnection, gameProfile);
    }

    public void setCurrentInstance(ServerInstance<?> instance){
        setCurrentServerMode(instance.getInstanceMode());
        setCurrentServerInstanceId(instance.getServerId());
    }

    public void sendInfoMessage(String message){
        sendMessage(DMM("<gray>" + message));
    }

    public void sendErrorMessage(String message){
        sendMessage(DMM("<red>" + message));
    }

    public void sendSuccessMessage(String message){
        sendMessage(DMM("<green>" + message));
    }

    public Group getGroup(){
        return playerData.getGroup();
    }

    public Component getNametag(){
        return getGroup().isDefaultGroup() ?
                DMM("<gray>" + getUsername()) :
                playerData.getTag();
    }

    public void sendNotPermission() {
        sendErrorMessage("You do not have permission to execute this type of command!");
    }

    public void showPlayers() {
        instance.getPlayers().forEach(target -> {
            if (target.getUuid().equals(getUuid())) return;
            target.addViewer(this);
            addViewer(target);
        });
    }

    public void hidePlayers() {
        instance.getPlayers().forEach(target -> {
            if (target.getUuid().equals(getUuid())) return;
            target.removeViewer(this);
            removeViewer(target);
        });
    }

    @Override
    public @NotNull PlayerSkin getSkin() {
        var processedSkin =  super.getSkin();
        if (processedSkin == null) return SkinManager.DEFAULT_NPC_SKIN;
        return processedSkin;
    }

    public void sendEmptyMessage() {
        sendMessage("");
    }
}
