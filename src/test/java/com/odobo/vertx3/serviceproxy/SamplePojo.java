package com.odobo.vertx3.serviceproxy;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.vertx.core.json.JsonObject;

/**
 * User: plenderyou
 * Date: 19/10/15
 * Time: 09:06
 */
public class SamplePojo {

    private String string;
    private int intValue;

    @JsonDeserialize(using = JsonObjectDeserializer.class)
    @JsonSerialize(using = JsonObjectSerializer.class)
    private JsonObject jsonObject;

    public String getString() {
        return string;
    }

    public SamplePojo setString(final String string) {
        this.string = string;
        return this;
    }

    public int getIntValue() {
        return intValue;
    }

    public SamplePojo setIntValue(final int intValue) {
        this.intValue = intValue;
        return this;
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public SamplePojo setJsonObject(final JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof SamplePojo)) return false;

        final SamplePojo that = (SamplePojo) o;

        if (intValue != that.intValue) return false;
        if (string != null ? !string.equals(that.string) : that.string != null) return false;
        return !(jsonObject != null ? !jsonObject.equals(that.jsonObject) : that.jsonObject != null);

    }

    @Override
    public int hashCode() {
        int result = string != null ? string.hashCode() : 0;
        result = 31 * result + intValue;
        result = 31 * result + (jsonObject != null ? jsonObject.hashCode() : 0);
        return result;
    }
}
