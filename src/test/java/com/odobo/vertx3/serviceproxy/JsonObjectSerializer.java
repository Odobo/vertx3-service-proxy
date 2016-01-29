package com.odobo.vertx3.serviceproxy;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.vertx.core.json.JsonObject;

import java.io.IOException;

/**
 * User: plenderyou
 * Date: 11/12/2015
 * Time: 5:30 PM
 */
public class JsonObjectSerializer extends JsonSerializer<JsonObject> {
    @Override
    public void serialize(final JsonObject value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeObject( value.getMap() );
    }


}