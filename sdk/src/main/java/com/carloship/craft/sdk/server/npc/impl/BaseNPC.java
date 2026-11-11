package com.carloship.craft.sdk.server.npc.impl;

import com.carloship.craft.sdk.server.npc.ia.LookAtPlayerGoal;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityDespawnEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public abstract class BaseNPC extends EntityCreature {

    public static final Tag<@NotNull Consumer<InteractEvent<EntityAttackEvent>>> LEFT_CLICK_TAG = Tag.Transient("left-click-consumer");
    public static final Tag<@NotNull Consumer<InteractEvent<PlayerEntityInteractEvent>>> RIGHT_CLICK_TAG = Tag.Transient("right-click-consumer");

    private static final EventNode<EntityEvent> EVENT_NODE = EventNode.type("npc-events", EventFilter.ENTITY);
    private static final Sound DEFAULT_SOUND = Sound.sound().type(SoundEvent.BLOCK_NOTE_BLOCK_PLING).pitch(1).build();
    private final Tag<Sound> INTERACT_SOUND_TAG = Tag.Transient("npc-interact-sound");

    private Pos pos;

    static {
        EVENT_NODE.addListener(EntityDespawnEvent.class, event -> {
            if (!(event.getEntity() instanceof BaseNPC npc)) return;
            /*if (!NON_PLAYABLE_CHARACTERS.containsKey(npc.getEntityId())) {
                return;
            }
            NON_PLAYABLE_CHARACTERS.remove(npc.getEntityId());*/
        });

        EVENT_NODE.addListener(EntityAttackEvent.class, event -> {
            if (!(event.getEntity() instanceof Player)) return;
            if (!(event.getTarget() instanceof BaseNPC npc)) return;
            /*if (!NON_PLAYABLE_CHARACTERS.containsKey(event.getTarget().getEntityId())) {
                return;
            }*/
            npc.handle(event);
        });

        EVENT_NODE.addListener(PlayerEntityInteractEvent.class, event -> {
            if (event.getHand() == PlayerHand.OFF) return; // avoid duplicate call
            if (!(event.getTarget() instanceof BaseNPC npc)) return;
           /* if (!NON_PLAYABLE_CHARACTERS.containsKey(event.getTarget().getEntityId())) {
                return;
            }*/
            npc.handle(event);
        });

        if (EVENT_NODE.getParent() == null) {
            GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
            eventHandler.addChild(EVENT_NODE);
        }
    }

    public BaseNPC(EntityType entityType) {
        super(entityType);
    }

    public void speak(Audience audience, Component text) {
        Component message = Component.textOfChildren(
                getName().colorIfAbsent(NamedTextColor.YELLOW),
                Component.space(),
                text.colorIfAbsent(NamedTextColor.GRAY)
        );

        audience.sendMessage(message);
    }

    public Component getName() {
        return Objects.requireNonNullElse(get(DataComponents.CUSTOM_NAME), Component.text(entityType.name()));
    }

    public void lookClose() {
        lookClose(15);
    }

    public void lookClose(int range) {
        addAIGroup(
                List.of(new LookAtPlayerGoal(this)),
                List.of(new ClosestEntityTarget(this, range, entity -> entity instanceof Player))
        );
    }

    public void handle(@NotNull EntityAttackEvent event) {
        if (event.getTarget() != this) return;
        if (!(event.getEntity() instanceof Player player)) return;


        final Sound playerSound = this.getTag(INTERACT_SOUND_TAG.defaultValue(DEFAULT_SOUND));
        player.playSound(playerSound, event.getTarget());

        if (this.hasTag(LEFT_CLICK_TAG))
            this.getTag(LEFT_CLICK_TAG).accept(new InteractEvent<>(event, this, player));
    }

    public void handle(@NotNull PlayerEntityInteractEvent event) {
        if (event.getTarget() != this) return;
        if (event.getHand() != PlayerHand.MAIN) return;
        final Player player = event.getPlayer();


        player.playSound(Sound.sound()
                .type(SoundEvent.BLOCK_NOTE_BLOCK_PLING)
                .pitch(2)
                .build(), event.getTarget());

        if (this.hasTag(RIGHT_CLICK_TAG))
            this.getTag(RIGHT_CLICK_TAG).accept(new InteractEvent<>(event, this, player));
    }

    public BaseNPC setInteract(@Nullable Consumer<InteractEvent<PlayerEntityInteractEvent>> consumer) {
        if (consumer == null) {
            this.removeTag(RIGHT_CLICK_TAG);
            return this;
        }
        this.setTag(RIGHT_CLICK_TAG, consumer);
        return this;
    }

    public BaseNPC setAttack(@Nullable Consumer<InteractEvent<EntityAttackEvent>> consumer) {
        if (consumer == null) {
            this.removeTag(LEFT_CLICK_TAG);
            return this;
        }
        this.setTag(LEFT_CLICK_TAG, consumer);
        return this;
    }

    public BaseNPC setInteractSound(@Nullable Sound sound) {
        if (sound == null) {
            this.removeTag(INTERACT_SOUND_TAG);
            return this;
        }
        this.setTag(INTERACT_SOUND_TAG, sound);
        return this;
    }

    public BaseNPC setPos(Pos pos){
        this.pos = pos;
        return this;
    }

    public BaseNPC spawn(Instance instance, Pos pos){
        setInstance(instance, pos);
        return this;
    }

    public BaseNPC spawn(Instance instance){
        return spawn(instance, pos);
    }

    public static record InteractEvent<T extends EntityInstanceEvent>(
            @NotNull T event,
            @NotNull BaseNPC npc,
            @NotNull Player interacted
    ) {

    }
}