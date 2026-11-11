package com.carloship.craft.sdk.codec;

import com.carloship.craft.sdk.server.leveling.Leveling;
import com.carloship.craft.sdk.server.leveling.XpCurve;
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

public class LevelingCodec {

    public static class JSONSerializer extends JsonSerializer<Leveling> {

        @Override
        public void serialize(Leveling value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeNumberField("level", value.getLevel());
            gen.writeNumberField("xp", value.getXp());

            if (value.getCurve() != null)
                gen.writeStringField("curve", value.getCurve().name());

            gen.writeEndObject();
        }
    }

    public static class JSONDeserializer extends JsonDeserializer<Leveling> {

        @Override
        public Leveling deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException {

            int level = 1;
            double xp = 0;
            XpCurve curve = null;

            if (p.currentToken() == null) {
                p.nextToken();
            }

            while (p.nextToken() != null) {

                if (p.currentToken().isStructEnd()) {
                    break;
                }

                String fieldName = p.currentName();
                p.nextToken();

                switch (fieldName) {
                    case "level":
                        level = p.getIntValue();
                        break;

                    case "xp":
                        xp = p.getDoubleValue();
                        break;

                    case "curve":
                        try {
                            curve = XpCurve.valueOf(p.getText());
                        } catch (IllegalArgumentException e) {
                            curve = XpCurve.LINEAR; // fallback
                        }
                        break;


                    default:
                        p.skipChildren();
                }
            }

            if (curve == null)
                throw new IllegalStateException("Leveling JSON missing XP curve");

            return new Leveling(curve, level, xp);
        }
    }

    public static class MongoCodec implements Codec<Leveling> {

        @Override
        public Leveling decode(BsonReader reader, DecoderContext decoderContext) {
            reader.readStartDocument();

            int level = reader.readInt32("level");
            double xp = reader.readDouble("xp");
            XpCurve curve = XpCurve.valueOf(reader.readString("curve"));

            reader.readEndDocument();

            return new Leveling(curve, level, xp);
        }

        @Override
        public void encode(BsonWriter writer, Leveling value, EncoderContext encoderContext) {
            writer.writeStartDocument();

            writer.writeInt32("level", value.getLevel());
            writer.writeDouble("xp", value.getXp());

            if (value.getCurve() == null)
                throw new IllegalStateException("Leveling cannot be encoded without XP curve");

            writer.writeString("curve", value.getCurve().name());

            writer.writeEndDocument();
        }

        @Override
        public Class<Leveling> getEncoderClass() {
            return Leveling.class;
        }
    }
}
