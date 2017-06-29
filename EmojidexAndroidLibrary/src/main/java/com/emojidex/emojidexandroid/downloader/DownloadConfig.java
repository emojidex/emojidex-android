package com.emojidex.emojidexandroid.downloader;

import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.EmojidexFileUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;

/**
 * Download config.
 */
public class DownloadConfig implements Cloneable
{
    private LinkedHashSet<EmojiFormat> formats;
    private String sourceRootPath;
    private boolean forceFlag;
    private String username;
    private String authtoken;
    private String locale;

    /**
     * Construct download config.
     */
    public DownloadConfig()
    {
        this.formats = new LinkedHashSet<EmojiFormat>();
        sourceRootPath = EmojidexFileUtils.getRemoteRootPathDefault();
        forceFlag = false;
        username = null;
        authtoken = null;

        final Locale defaultLocale = Locale.getDefault();
        final boolean isJapan = defaultLocale.equals(Locale.JAPAN) || defaultLocale.equals(Locale.JAPANESE);
        locale = isJapan ? "ja" : "en";
    }

    @Override
    public DownloadConfig clone()
    {
        DownloadConfig result = null;

        try
        {
            result = (DownloadConfig)super.clone();
            result.formats = (LinkedHashSet<EmojiFormat>)formats.clone();
        }
        catch(CloneNotSupportedException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Add emoji format.
     * @param format    Emoji format.
     * @return          Self object.
     */
    public DownloadConfig addFormat(EmojiFormat format)
    {
        formats.add(format);
        return this;
    }

    /**
     * Remove emoji format.
     * @param format    Emoji format.
     * @return          Self object.
     */
    public DownloadConfig removeFormat(EmojiFormat format)
    {
        formats.remove(format);
        return this;
    }

    /**
     * Clear and set emoji formats.
     * @param formats   Emoji format collection.
     * @return          Self object.
     */
    public DownloadConfig setFormats(EmojiFormat... formats)
    {
        clearFormats();
        for(EmojiFormat format : formats)
            addFormat(format);
        return this;
    }


    /**
     * Clear emoji formats.
     * @return          Self object.
     */
    public DownloadConfig clearFormats()
    {
        formats.clear();
        return this;
    }

    /**
     * Get emoji format collection.
     * @return  Emoji format collection.
     */
    public Collection<EmojiFormat> getFormats()
    {
        return formats;
    }

    /**
     * Set source root path.
     * @param sourceRootPath    Source root path.
     *                          Default value is {@link EmojidexFileUtils#getRemoteRootPathDefault}.
     * @return                  Self object.
     */
    public DownloadConfig setSourceRootPath(String sourceRootPath)
    {
        this.sourceRootPath = sourceRootPath;
        return this;
    }

    /**
     * Get source root path.
     * Default value is {@link EmojidexFileUtils#getRemoteRootPathDefault}.
     * @return  Source root path.
     */
    public String getSourceRootPath()
    {
        return sourceRootPath;
    }

    /**
     * Set force download flag.
     * @param forceFlag     Force download flag.
     *                      Default value is false.
     * @return              Self object.
     */
    public DownloadConfig setForceFlag(boolean forceFlag)
    {
        this.forceFlag = forceFlag;
        return this;
    }

    /**
     * Get force download flag.
     * Default value is false.
     * @return  Force download flag.
     */
    public boolean getForceFlag()
    {
        return forceFlag;
    }

    /**
     * Set user parameter.
     * @param username      User name.
     *                      Default value is null.
     * @param authtoken     Auth token.
     *                      Default value is null.
     * @return              Self object.
     */
    public DownloadConfig setUser(String username, String authtoken)
    {
        this.username = username;
        this.authtoken = authtoken;

        return this;
    }

    /**
     * Get user name.
     * Default value is null.
     * @return  User name.
     */
    public String getUserName()
    {
        return username;
    }

    /**
     * Get user auth token.
     * Default value is null.
     * @return      Auth token.
     */
    public String getAuthToken()
    {
        return authtoken;
    }

    /**
     * Set locale.
     * @param locale    Locale.
     */
    public void setLocale(String locale)
    {
        this.locale = locale;
    }

    /**
     * Get locale.
     * @return      Locale.
     */
    public String getLocale()
    {
        return locale;
    }
}
