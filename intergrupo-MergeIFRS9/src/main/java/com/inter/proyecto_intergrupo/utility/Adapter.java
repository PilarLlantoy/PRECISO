package com.inter.proyecto_intergrupo.utility;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.poi.ss.formula.functions.T;

import java.io.IOException;

public class Adapter<U> extends TypeAdapter<T> {
    public T read(JsonReader reader) throws IOException {
        return null;
    }

    public void write(JsonWriter writer, T obj) throws IOException {
        if (obj == null) {
            writer.nullValue();
            return;
        }
        writer.value(obj.toString());
    }
}
