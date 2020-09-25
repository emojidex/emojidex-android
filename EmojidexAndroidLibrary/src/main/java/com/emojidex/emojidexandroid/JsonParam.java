package com.emojidex.emojidexandroid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kou on 14/10/10.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
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
    private String created_at = null;
    private boolean r18 = false;
    private List<Combination> customizations = null;
    private List<Combination> combinations = null;

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

    public static class Combination
    {
        private String base;
        private List<Long> component_layer_order;
        private List<List<String>> components;
        private List<HashMap<String, Checksums>> checksums;

        public String getBase()
        {
            return base;
        }

        public void setBase(String base)
        {
            this.base = base;
        }

        @JsonProperty("component_layer_order")
        public List<Long> getComponentLayerOrder()
        {
            if(component_layer_order == null)
                component_layer_order = new ArrayList<Long>();
            return component_layer_order;
        }

        public void setComponentLayerOrder(List<Long> component_layer_order)
        {
            this.component_layer_order = component_layer_order;
        }

        public List<List<String>> getComponents()
        {
            if(components == null)
                components = new ArrayList<List<String>>();
            return components;
        }

        public void setComponents(List<List<String>> components)
        {
            this.components = components;
        }

        public List<HashMap<String, Checksums>> getChecksums()
        {
            if(checksums == null)
                checksums = new ArrayList<HashMap<String, Checksums>>();
            return checksums;
        }

        public void setChecksums(List<HashMap<String, Checksums>> checksums)
        {
            this.checksums = checksums;
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
        if(tags == null)
            tags = new ArrayList<String>();
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
        if(variants == null)
            variants = new ArrayList<String>();
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

    @JsonProperty("created_at")
    public String getCreatedAt()
    {
        return created_at;
    }

    public void setCreatedAt(String createdAt)
    {
        this.created_at = createdAt;
    }

    public boolean isR18()
    {
        return r18;
    }

    public void setR18(boolean r18)
    {
        this.r18 = r18;
    }

    public List<Combination> getCustomizations()
    {
        if(customizations == null)
            customizations = new ArrayList<Combination>();
        return customizations;
    }

    public void setCustomizations(List<Combination> customizations)
    {
        this.customizations = customizations;
    }

    public List<Combination> getCombinations()
    {
        if(combinations == null)
            combinations = new ArrayList<Combination>();
        return combinations;
    }

    public void setCombinations(List<Combination> combinations)
    {
        this.combinations = combinations;
    }

    /**
     * Copy parameter from src.
     * @param src   Source.
     */
    void copy(com.emojidex.libemojidex.Emojidex.Data.Emoji src)
    {
        setCode(src.getCode());
        setMoji(src.getMoji());
        setUnicode(src.getUnicode());
        setCategory(src.getCategory());

        // tags
        {
            final com.emojidex.libemojidex.StringVector srcTags = src.getTags();
            final long tagsCount = srcTags.size();
            List<String> destTags = getTags();
            destTags.clear();
            for(int i = 0;  i < tagsCount;  ++i)
                destTags.add(srcTags.get(i));
        }


        setLink(src.getLink());
        setBase(src.getBase());

        // variants
        {
            final com.emojidex.libemojidex.StringVector srcVariants = src.getVariants();
            final long variantsCount = srcVariants.size();
            List<String> destVariants = getVariants();
            destVariants.clear();
            for(int i = 0;  i < variantsCount;  ++i)
                destVariants.add(srcVariants.get(i));
        }

        setScore(src.getScore());
        setCurrentPrice(src.getCurrent_price());
        setPrimary(src.getPrimary());
        setRegisteredAt(src.getRegistered_at());
        setPermalock(src.getPermalock());
        setCopyrightLock(src.getCopyright_lock());
        setLinkExpiration(src.getLink_expiration());
        setLockExpiration(src.getLock_expiration());
        setTimesChanged(src.getTimes_changed());
        setWide(src.getIs_wide());
        setTimesUsed(src.getTimes_used());
        setAttribution(src.getAttribution());
        setUserID(src.getUser_id());

        // checksums
        copyChecksums(getChecksums(), src.getChecksums());

        setFavorited(src.getFavorited());
        setCreatedAt(src.getCreated_at());
        setR18(src.getR18());

        // customizations
        copyCombinations(getCustomizations(), src.getCustomizations());

        // combinations
        copyCombinations(getCombinations(), src.getCombinations());
    }

    /**
     * Copy checksums from src.
     * @param dest  Destination.
     * @param src   Source.
     */
    private void copyChecksums(Checksums dest, com.emojidex.libemojidex.Emojidex.Data.Checksums src)
    {
        dest.setSvg(src.sum("svg", ""));

        for(EmojiFormat format : EmojiFormat.values())
        {
            if(format == EmojiFormat.SVG)
                continue;

            dest.setPng(
                    format,
                    src.sum("png", format.getResolution())
            );
        }
    }

    private void copyCombinations(List<Combination> dest, com.emojidex.libemojidex.CombinationVector src)
    {
        final long count = src.size();

        for(int i = 0;  i < count;  ++i)
        {
            if(dest.size() <= i)
                dest.add(new Combination());

            final Combination destCombination = dest.get(i);
            final com.emojidex.libemojidex.Emojidex.Data.Combination srcCombination = src.get(i);

            // base
            destCombination.setBase(srcCombination.getBase());

            // component_layer_order
            {
                final List<Long> destCLO = destCombination.getComponentLayerOrder();
                final com.emojidex.libemojidex.UintVector srcCLO = srcCombination.getComponent_layer_order();
                final long cloCount = srcCLO.size();
                destCLO.clear();
                for(int j = 0;  j < cloCount;  ++j)
                    destCLO.add(srcCLO.get(j));
            }

            // components
            {
                final List<List<String>> destComponentsArray = destCombination.getComponents();
                final com.emojidex.libemojidex.StringVectorVector srcComponentsArray = srcCombination.getComponents();
                final long componentsArrayCount = srcComponentsArray.size();
                for(int j = 0;  j < componentsArrayCount;  ++j)
                {
                    if(destComponentsArray.size() <= j)
                        destComponentsArray.add(new ArrayList<String>());
                    final List<String> destComponents = destComponentsArray.get(j);
                    final com.emojidex.libemojidex.StringVector srcComponents = srcComponentsArray.get(j);
                    final long componentsCount = srcComponents.size();
                    destComponents.clear();
                    for(int k = 0;  k < componentsCount;  ++k)
                        destComponents.add(srcComponents.get(k));
                }
                while(destComponentsArray.size() > componentsArrayCount)
                    destComponentsArray.remove(componentsArrayCount);
            }

            // checksums
            {
                final List<HashMap<String, Checksums>> destChecksumsArray = destCombination.getChecksums();
                final com.emojidex.libemojidex.ChecksumsMapVector srcChecksumsArray = srcCombination.getChecksums();
                final long checksumsArrayCount = srcChecksumsArray.size();
                for(int j = 0;  j < checksumsArrayCount;  ++j)
                {
                    if(destChecksumsArray.size() <= j)
                        destChecksumsArray.add(new HashMap<String, Checksums>());
                    final HashMap<String, Checksums> destChecksumsMap = destChecksumsArray.get(j);
                    final com.emojidex.libemojidex.ChecksumsMap srcChecksumsMap = srcChecksumsArray.get(j);
                    destChecksumsMap.clear();
                    for(Map.Entry<String, com.emojidex.libemojidex.Emojidex.Data.Checksums> entry : srcChecksumsMap.entrySet())
                    {
                        final Checksums destChecksums = new Checksums();
                        copyChecksums(destChecksums, entry.getValue());
                        destChecksumsMap.put(entry.getKey(), destChecksums);
                    }
                }
                while(destChecksumsArray.size() > checksumsArrayCount)
                    destChecksumsArray.remove(checksumsArrayCount);
            }
        }

        // Remove trush.
        while(dest.size() > count)
            dest.remove(count);
    }


}
