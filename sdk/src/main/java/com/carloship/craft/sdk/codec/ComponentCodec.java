package com.carloship.craft.sdk.codec;

import com.carloship.craft.sdk.utils.TextUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.jspecify.annotations.NonNull;

import java.io.IOException;

@Slf4j
public class ComponentCodec {

    public final static class JSONSerializer extends JsonSerializer<Component>{

        @Override
        public void serialize(Component value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(TextUtils.MM.serialize(value));
        }

    }

    public static final class JSONDeserializer extends JsonDeserializer<Component>{

        @Override
        public Component deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String text = p.getText();
            return getComponent(text);
        }

    }

    public static class MongoCodec implements Codec<Component> {

        @Override
        public Component decode(BsonReader reader, DecoderContext decoderContext) {
            String txt = reader.readString();

            return getComponent(txt);
        }

        @Override
        public void encode(BsonWriter writer, Component value, EncoderContext encoderContext) {
            writer.writeString(TextUtils.MM.serialize(value));
        }

        @Override
        public Class<Component> getEncoderClass() {
            return Component.class;
        }
    }


    @NonNull
    private static Component getComponent(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        try {
            return TextUtils.MM.deserialize(text);
        } catch (Exception e) {
            System.err.println("Failed to deserialize Component: '" + text + "'");
            log.error(e.getMessage());
            return Component.text(text);
        }
    }
}
