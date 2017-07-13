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
import java.util.List;

/**
 * Created by kou on 14/10/10.
 */
public class JsonParam {
    @JsonProperty("code")               private String code = null;
    @JsonProperty("moji")               private String moji = null;
    @JsonProperty("unicode")            private String unicode = null;
    @JsonProperty("category")           private String category = null;
    @JsonProperty("tags")               private List<String> tags = null;
    @JsonProperty("link")               private String link = null;
    @JsonProperty("base")               private String base = null;
    @JsonProperty("variants")           private List<String> variants = null;
    @JsonProperty("score")              private int score = 0;
    @JsonProperty("current_price")      private double current_price = 0.0;
    @JsonProperty("primary")            private boolean primary = true;
    @JsonProperty("registered_at")      private String registered_at = null;
    @JsonProperty("permalock")          private boolean permalock = false;
    @JsonProperty("copyright_lock")     private boolean copyright_lock = false;
    @JsonProperty("link_expiration")    private String link_expiration = null;
    @JsonProperty("lock_expiration")    private String lock_expiration = null;
    @JsonProperty("times_changed")      private long times_changed = 0;
    @JsonProperty("is_wide")            private boolean is_wide = false;
    @JsonProperty("times_used")         private long times_used = 0;
    @JsonProperty("attribution")        private String attribution = null;
    @JsonProperty("user_id")            private String user_id = null;
    @JsonProperty("checksums")          private Checksums checksums = null;
    @JsonProperty("favorited")          private long favorited = 0;

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

        public String get(EmojiFormat format)
        {
            if(format == EmojiFormat.SVG)
                return getSvg();
            return getPng(format);
        }

        public void set(EmojiFormat format, String checksum)
        {
            if(format == EmojiFormat.SVG)
                setSvg(checksum);
            else
                setPng(format, checksum);
        }
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getMoji()
    {
        return moji;
    }

    public void setMoji(String moji)
    {
        this.moji = moji;
    }

    public String getUnicode()
    {
        return unicode;
    }

    public void setUnicode(String unicode)
    {
        this.unicode = unicode;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public List<String> getTags()
    {
        return tags;
    }

    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }

    public String getLink()
    {
        return link;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    public String getBase()
    {
        return base;
    }

    public void setBase(String base)
    {
        this.base = base;
    }

    public List<String> getVariants()
    {
        return variants;
    }

    public void setVariants(List<String> variants)
    {
        this.variants = variants;
    }

    public int getScore()
    {
        return score;
    }

    public void setScore(int score)
    {
        this.score = score;
    }

    public double getCurrentPrice()
    {
        return current_price;
    }

    public void setCurrentPrice(double currentPrice)
    {
        this.current_price = currentPrice;
    }

    public boolean isPrimary()
    {
        return primary;
    }

    public void setPrimary(boolean primary)
    {
        this.primary = primary;
    }

    public String getRegisteredAt()
    {
        return registered_at;
    }

    public void setRegisteredAt(String registeredAt)
    {
        this.registered_at = registeredAt;
    }

    public boolean isPermalock()
    {
        return permalock;
    }

    public void setPermalock(boolean permalock)
    {
        this.permalock = permalock;
    }

    public boolean isCopyrightLock()
    {
        return copyright_lock;
    }

    public void setCopyrightLock(boolean copyrightLock)
    {
        this.copyright_lock = copyrightLock;
    }

    public String getLinkExpiration()
    {
        return link_expiration;
    }

    public void setLinkExpiration(String linkExpiration)
    {
        this.link_expiration = linkExpiration;
    }

    public String getLockExpiration()
    {
        return lock_expiration;
    }

    public void setLockExpiration(String lockExpiration)
    {
        this.lock_expiration = lockExpiration;
    }

    public long getTimesChanged()
    {
        return times_changed;
    }

    public void setTimesChanged(long timesChanged)
    {
        this.times_changed = timesChanged;
    }

    public boolean isWide()
    {
        return is_wide;
    }

    public void setWide(boolean wide)
    {
        this.is_wide = wide;
    }

    public long getTimesUsed()
    {
        return times_used;
    }

    public void setTimesUsed(long timesUsed)
    {
        this.times_used = timesUsed;
    }

    public String getAttribution()
    {
        return attribution;
    }

    public void setAttribution(String attribution)
    {
        this.attribution = attribution;
    }

    public String getUserID()
    {
        return user_id;
    }

    public void setUserID(String userID)
    {
        this.user_id = userID;
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

    public long getFavorited()
    {
        return favorited;
    }

    public void setFavorited(long favorited)
    {
        this.favorited = favorited;
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
