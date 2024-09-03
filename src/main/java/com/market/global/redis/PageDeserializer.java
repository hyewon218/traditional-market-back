package com.market.global.redis;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PageDeserializer extends StdDeserializer<PageImpl> {
    private static final String CONTENT = "content";
    private static final String NUMBER = "number";
    private static final String SIZE = "size";
    private static final String TOTAL_ELEMENTS = "totalElements";

    public PageDeserializer() {
        super(PageImpl.class);
    }

    @Override
    public PageImpl deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        CollectionType valuesListType = null;
        List<?> list = new ArrayList<>();
        int pageNumber = -1;
        int pageSize = -1;
        long total = -1L;

        String propName = p.getCurrentName();
        do {
            p.nextToken();
            switch (propName) {
                case CONTENT:
                    valuesListType = ctxt.getTypeFactory().constructCollectionType(List.class, Object.class); // Adjust type as needed
                    list = ctxt.readValue(p, valuesListType);
                    break;
                case NUMBER:
                    pageNumber = ctxt.readValue(p, Integer.class);
                    break;
                case SIZE:
                    pageSize = ctxt.readValue(p, Integer.class);
                    break;
                case TOTAL_ELEMENTS:
                    total = ctxt.readValue(p, Long.class);
                    break;
                default:
                    p.skipChildren();
                    break;
            }
        } while ((propName = p.nextFieldName()) != null);

        return new PageImpl<>(list, PageRequest.of(pageNumber, pageSize), total);
    }
}


