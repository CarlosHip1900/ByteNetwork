package com.carloship.craft.sdk.server.npc.skin;

import com.carloship.craft.sdk.server.npc.impl.BaseNPC;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.metadata.avatar.MannequinMeta;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.player.ResolvableProfile;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.PacketSendingUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class BaseSkinPlayerNPC extends BaseNPC {

    private static final Tag<@NotNull Boolean> HAS_PREFIX = Tag.Boolean("has-prefix");
    private static final Component NPC_TAG = Component.text("[NPC]", NamedTextColor.YELLOW);

    public BaseSkinPlayerNPC() {
        super(EntityType.MANNEQUIN);
        this.editEntityMeta(MannequinMeta.class, meta->{
            meta.setImmovable(true);
        });
    }

    public BaseSkinPlayerNPC(@Nullable ResolvableProfile skin) {
        this();
        if(skin != null) setSkin(skin);
        setCustomNameVisible(false);
        set(DataComponents.CUSTOM_NAME, Component.empty());

        editEntityMeta(MannequinMeta.class, meta -> {
            meta.setDescription(null);
        });
    }

    public BaseSkinPlayerNPC clone(){
        AtomicReference<BaseSkinPlayerNPC> npc = new AtomicReference<>();
        this.editEntityMeta(MannequinMeta.class, meta->{
            BaseSkinPlayerNPC mannequinNPC = new BaseSkinPlayerNPC(meta.getProfile());
            mannequinNPC.editEntityMeta(MannequinMeta.class, meta1->{
                meta1.setImmovable(meta.isImmovable());
                meta1.setDescription(meta.getDescription());
            });
            npc.set(mannequinNPC);
        });

        BaseSkinPlayerNPC mannequin = npc.get();
        if(mannequin == null) {
            throw new NullPointerException("Mannequin NPC is null");
        }
        mannequin.tagHandler().updateContent(this.tagHandler().asCompound());

        return mannequin;
    }

    public void setSkin(@NotNull ResolvableProfile profile){
        editEntityMeta(MannequinMeta.class, meta-> meta.setProfile(profile));
    }

    public void setSkin(@NotNull PlayerSkin skin){
        setSkin(new ResolvableProfile(skin));
    }

    @Override
    public Component getName(){
        if(hasTag(HAS_PREFIX) && getTag(HAS_PREFIX)){
            return ((MannequinMeta)this.getEntityMeta()).getDescription();
        }
        return get(DataComponents.CUSTOM_NAME);
    }

    public void updateNPCForAudience(Audience audience, Component displayName, @Nullable Component aboveName, PlayerSkin playerSkin){
        Map<Integer, Metadata.Entry<?>> entries = Map.<Integer, Metadata.Entry<?>>of(
                MetadataDef.Mannequin.DESCRIPTION.index(), Metadata.OptComponent(displayName),
                MetadataDef.Player.CUSTOM_NAME.index(), Metadata.OptComponent(aboveName),
                MetadataDef.Player.CUSTOM_NAME_VISIBLE.index(), Metadata.Boolean(true),
                MetadataDef.Mannequin.PROFILE.index(), Metadata.ResolvableProfile(new ResolvableProfile(playerSkin))
        );


        PacketSendingUtils.sendPacket(audience, new EntityMetaDataPacket(this.getEntityId(), entries));
    }

}