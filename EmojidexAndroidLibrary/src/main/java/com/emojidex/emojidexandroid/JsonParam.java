package com.emojidex.emojidexandroid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;

/**
 * Created by kou on 14/10/10.
 */
public class JsonParam {
    private String code = null;
    private String moji = null;
    private String unicode = null;
    private String category = null;
    private List<String> tags = null;
    private String link = null;
    private String base = null;
    private List<String> variants = null;
    private int score = 0;
    private double current_price = 0.0;
    private boolean primary = true;
    private String registered_at = null;
    private boolean permalock = false;
    private boolean copyright_lock = false;
    private String link_expiration = null;
    private String lock_expiration = null;
    private long times_changed = 0;
    private boolean is_wide = false;
    private long times_used = 0;
    private String attribution = null;
    private String user_id = null;
    private Checksums checksums = null;
    private long favorited = 0;

    public static class Checksums
    {
        private String svg = null;

        @JsonProperty("")
        private HashMap<String, String> png = null;

        public String getSvg()
        {
            return svg;
        }

        public void setSvg(String svg)
        {
            this.svg = svg;
        }

        @JsonIgnore
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

        @JsonIgnore
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

    @JsonProperty("current_price")
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

    @JsonProperty("registered_at")
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

    @JsonProperty("copyright_lock")
    public boolean isCopyrightLock()
    {
        return copyright_lock;
    }

    public void setCopyrightLock(boolean copyrightLock)
    {
        this.copyright_lock = copyrightLock;
    }

    @JsonProperty("link_expiration")
    public String getLinkExpiration()
    {
        return link_expiration;
    }

    public void setLinkExpiration(String linkExpiration)
    {
        this.link_expiration = linkExpiration;
    }

    @JsonProperty("lock_expiration")
    public String getLockExpiration()
    {
        return lock_expiration;
    }

    public void setLockExpiration(String lockExpiration)
    {
        this.lock_expiration = lockExpiration;
    }

    @JsonProperty("times_changed")
    public long getTimesChanged()
    {
        return times_changed;
    }

    public void setTimesChanged(long timesChanged)
    {
        this.times_changed = timesChanged;
    }

    @JsonProperty("is_wide")
    public boolean isWide()
    {
        return is_wide;
    }

    public void setWide(boolean wide)
    {
        this.is_wide = wide;
    }

    @JsonProperty("times_used")
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

    @JsonProperty("user_id")
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
}
