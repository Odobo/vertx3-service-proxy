package com.odobo.vertx3.serviceproxy;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.vertx.core.json.JsonObject;

import java.io.IOException;

/**
 * User: plenderyou
 * Date: 11/12/2015
 * Time: 5:32 PM
 */
public class JsonObjectDeserializer extends JsonDeserializer<JsonObject> {
    @Override
    public JsonObject deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final String text = jp.getCodec().readTree(jp).toString();
        return new JsonObject(text);
    }
}
