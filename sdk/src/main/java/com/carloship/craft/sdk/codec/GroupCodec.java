package com.carloship.craft.sdk.codec;

import com.carloship.craft.sdk.server.group.Group;
import com.carloship.craft.sdk.server.group.Groups;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.io.IOException;

public class GroupCodec {

    public static final class JSONSerializer extends JsonSerializer<Group> {

        @Override
        public void serialize(Group value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getId());
        }
    }

    public static final class JSONDeserializer extends JsonDeserializer<Group> {

        @Override
        public Group deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            return Groups.fromId(p.getText());
        }
    }

    public static class MongoCodec implements Codec<Group> {

        @Override
        public Group decode(BsonReader reader, DecoderContext decoderContext) {
            return Groups.fromId(reader.readString());
        }

        @Override
        public void encode(BsonWriter writer, Group value, EncoderContext encoderContext) {
            writer.writeString(value.getId());
        }

        @Override
        public Class<Group> getEncoderClass() {
            return Group.class;
        }

    }



}
