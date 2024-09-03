package com.market.global.redis;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.Map;

public class PageSerializer extends StdSerializer<Page> {

    public PageSerializer() {
        super(Page.class);
    }

    @Override
    public void serializeWithType(Page value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        WritableTypeId id = typeSer.writeTypePrefix(gen, typeSer.typeId(value, JsonToken.START_OBJECT));
        writeFields(value, gen, serializers);
        typeSer.writeTypeSuffix(gen, id);
    }

    @Override
    public void serialize(Page value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        throw new IllegalCallerException("This method is not supposed to be called.");
    }

    private void writeFields(Page value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value.getContent() != null && value.getContent().size() > 0) {
            provider.defaultSerializeField("content", value.getContent(), gen);
        }
        gen.writeNumberField("size", value.getSize());
        gen.writeNumberField("number", value.getNumber());
        gen.writeNumberField("totalElements", value.getTotalElements());
    }
}


