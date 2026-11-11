package com.carloship.craft.sdk.mapper;

import com.carloship.craft.sdk.codec.ComponentCodec;
import com.carloship.craft.sdk.codec.GroupCodec;
import com.carloship.craft.sdk.codec.LevelingCodec;
import com.mongodb.MongoClientSettings;
import lombok.Getter;
import org.bson.UuidRepresentation;
import org.bson.codecs.Codec;
import org.bson.codecs.UuidCodec;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public final class SDKMongoMapper {

    private static final List<Codec<?>> CODECS = new ArrayList<>();

    static {
        CodecRegistry base = MongoClientSettings.getDefaultCodecRegistry();

        CODECS.add(new UuidCodec(UuidRepresentation.STANDARD));
        CODECS.add(new GroupCodec.MongoCodec());
        CODECS.add(new LevelingCodec.MongoCodec());
        CODECS.add(new ComponentCodec.MongoCodec());

    }

    public static void registerCodec(Codec<?> codec) {
        CODECS.add(codec);
    }

    public static void registerCodecs(Collection<Codec<?>> codecs) {
        CODECS.addAll(codecs);
    }

    public static void registerCodecs(Codec<?>... codecs) {
        CODECS.addAll(List.of(codecs));
    }

    public static Codec<?>[] toArray() {
        return CODECS.toArray(new Codec[0]);
    }

}