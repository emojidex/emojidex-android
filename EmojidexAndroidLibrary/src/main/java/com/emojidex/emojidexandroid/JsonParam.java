package com.emojidex.emojidexandroid;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kou on 14/10/10.
 */
class JsonParam extends SimpleJsonParam {
    @JsonProperty("code_ja")    protected String name_ja = null;
    @JsonProperty("checksums")  protected Checksums checksums = null;

    public static class Checksums
    {
        @JsonProperty("svg")   public String svg = null;
        @JsonProperty("png")   public HashMap<String, String> png = null;
    }

    /**
     * Read json parameter from json file.
     * @param file  Json file.
     * @return      Json parameter array.
     */
    public static ArrayList<JsonParam> readFromFile(File file)
    {
        final ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            final InputStream is = new FileInputStream(file);
            JsonNode jsonNode = objectMapper.readTree(is);
            is.close();
            if(jsonNode.has("emoji"))
                jsonNode = jsonNode.get("emoji");

            final JsonParser jsonParser = jsonNode.traverse();
            ArrayList<JsonParam> result;
            if(jsonNode.isArray())
            {
                final TypeReference<ArrayList<JsonParam>> typeReference = new TypeReference<ArrayList<JsonParam>>(){};
                result = objectMapper.readValue(jsonParser, typeReference);
            }
            else
            {
                result = new ArrayList<JsonParam>();
                result.add(objectMapper.readValue(jsonParser, JsonParam.class));
            }
            return result;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        // If read failed, return ArrayList of empty.
        return new ArrayList<JsonParam>();
    }

    /**
     * Write json parameter to file.
     * @param file          Output file path.
     * @param jsonParams    Output json parameters.
     */
    public static void writeToFile(File file, ArrayList<JsonParam> jsonParams)
    {
        if( !file.getParentFile().exists() )
            file.getParentFile().mkdirs();

        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            final OutputStream os = new FileOutputStream(file);
            objectMapper.writeValue(os, jsonParams);
            os.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
