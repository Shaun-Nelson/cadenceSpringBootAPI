package com.snelson.cadenceAPI.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SecureRandomTypeAdapter extends TypeAdapter<SecureRandom> {

    @Override
    public void write(JsonWriter out, SecureRandom value) throws IOException {
        out.beginObject();
        out.name("algorithm").value(value.getAlgorithm());
        out.endObject();
    }

    @Override
    public SecureRandom read(JsonReader in) throws IOException {
        in.beginObject();
        String algorithm = null;
        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("algorithm")) {
                algorithm = in.nextString();
            }
        }
        in.endObject();
        try {
            return SecureRandom.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Failed to create SecureRandom instance", e);
        }
    }
}

