package com.emojidex.emojidexandroid;

import android.content.Context;
import android.net.Uri;

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
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by kou on 14/10/10.
 */
public class JsonParam extends SimpleJsonParam {
    @JsonProperty("checksums")  private Checksums checksums = null;

    public static class Checksums
    {
        @JsonProperty("svg")   private String svg = null;
        @JsonProperty("png")   private HashMap<String, String> png = null;

        public String getSvg()
        {
            return svg;
        }

        public void setSvg(String svg)
        {
            this.svg = svg;
        }

        public String getPng(EmojiFormat format)
        {
            return png == null ? null : png.get(format.getResolution());
        }

        public void setPng(EmojiFormat format, String checksum)
        {
            if(png == null)
                png = new HashMap<String, String>();
            png.put(format.getResolution(), checksum);
        }
    }

    public Checksums getChecksums()
    {
        if(checksums == null)
            checksums = new Checksums();
        return checksums;
    }

    public void setChecksums(Checksums checksums)
    {
        this.checksums = checksums;
    }

    /**
     * Read json parameter from json file.
     * @param file  Json file.
     * @return      Json parameter array.
     */
    public static ArrayList<JsonParam> readFromFile(File file)
    {
        ArrayList<JsonParam> result = null;
        try
        {
            final InputStream is = new FileInputStream(file);
            result = readFromFile(is);
            is.close();
        }
        catch(IOException e)
        {
            result = new ArrayList<JsonParam>();
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Read json parameter from json uri.
     * @param context   Context.
     * @param uri       Json uri.
     * @return          Json parameter array.
     */
    public static ArrayList<JsonParam> readFromFile(Context context, Uri uri)
    {
        ArrayList<JsonParam> result = null;
        try
        {
            final InputStream is = context.getContentResolver().openInputStream(uri);
            result = JsonParam.readFromFile(is);
            is.close();
        }
        catch(IOException e)
        {
            result = new ArrayList<JsonParam>();
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Read json parameter from json input stream.
     * @param is    Json input stream.
     * @return      Json parameter array.
     */
    public static ArrayList<JsonParam> readFromFile(InputStream is)
    {
        try
        {
            final ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(is);
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
    public static void writeToFile(File file, Collection<JsonParam> jsonParams)
    {
        if( !file.getParentFile().exists() )
            file.getParentFile().mkdirs();

        try {
            final OutputStream os = new FileOutputStream(file);
            writeToFile(os, jsonParams);
            os.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Write json parameter to uri.
     * @param context       Context.
     * @param uri           Output uri.
     * @param jsonParams    Output json parameters.
     */
    public static void writeToFile(Context context, Uri uri, Collection<JsonParam> jsonParams)
    {
        // Create directory if uri is file and not found directory.
        if(uri.getScheme().equals("file"))
        {
            final File parentDir = new File(uri.getPath()).getParentFile();
            if( !parentDir.exists() )
                parentDir.mkdirs();
        }

        // Write json parameter to uri.
        try
        {
            final OutputStream os = context.getContentResolver().openOutputStream(uri);
            JsonParam.writeToFile(
                    os,
                    jsonParams
            );
            os.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Write json parameter to output stream.
     * @param os            Output stream.
     * @param jsonParams    Output json parameters.
     */
    public static void writeToFile(OutputStream os, Collection<JsonParam> jsonParams)
    {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(os, jsonParams);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
