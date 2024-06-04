package com.snelson.cadenceAPI.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.util.regex.Pattern;

public class CustomGsonExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getDeclaredClass() == Pattern.class;
    }
    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
