package com.carloship.craft.sdk.server.chataction;

import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.utils.TriConsumer;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class ChatAction {

    private final BytePlayerCraft player;

    private TriConsumer<BytePlayerCraft, String, ChatActionState> response;

    private Predicate<String> validator = msg -> true;
    private String cancelWord = "cancelar";

    public ChatAction whenResponse(TriConsumer<BytePlayerCraft, String, ChatActionState> response) {
        this.response = response;
        return this;
    }

    public ChatAction validator(Predicate<String> validator) {
        this.validator = validator;
        return this;
    }

    public ChatAction cancelWord(String word) {
        this.cancelWord = word;
        return this;
    }

    public ChatAction expireAfter(Duration duration) {

        player.scheduler()
                .buildTask(this::expire)
                .delay(duration)
                .schedule();

        return this;
    }

    public void handle(String message) {

        if (message.equalsIgnoreCase(cancelWord)) {
            cancel();
            return;
        }

        if (!validator.test(message)) {

            ChatActionCache.remove(player);

            if (response != null) response.accept(player, null, ChatActionState.INVALIDATED);

            player.sendErrorMessage("Texto inválido!");
            return;
        }

        complete(message);
    }

    private void complete(String message) {
        if (!ChatActionCache.inAction(player)) return;

        ChatActionCache.remove(player);

        if (response != null)
            response.accept(player, message, ChatActionState.SUCCESS);
    }

    private void cancel() {

        if (!ChatActionCache.inAction(player)) return;

        ChatActionCache.remove(player);

        if (response != null)
            response.accept(player, null, ChatActionState.CANCELED);
    }

    private void expire() {

        if (!ChatActionCache.inAction(player)) return;

        ChatActionCache.remove(player);

        if (response != null)
            response.accept(player, null, ChatActionState.EXPIRED);
    }
}