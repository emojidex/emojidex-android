package com.emojidex.emojidexandroid;

import java.util.LinkedHashSet;

/**
 * Download config.
 */
public class DownloadConfig implements Cloneable
{
    private LinkedHashSet<EmojiFormat> formats;
    private String sourceRootPath;
    private boolean forceFlag;

    /**
     * Construct download config.
     * @param formats   Emoji format collection.
     */
    public DownloadConfig(EmojiFormat... formats)
    {
        this.formats = new LinkedHashSet<EmojiFormat>();
        setFormats(formats);
        sourceRootPath = EmojidexFileUtils.getRemoteRootPathDefault();
        forceFlag = false;
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
     */
    public void addFormat(EmojiFormat format)
    {
        formats.add(format);
    }

    /**
     * Remove emoji format.
     * @param format    Emoji format.
     */
    public void removeFormat(EmojiFormat format)
    {
        formats.remove(format);
    }

    /**
     * Clear and set emoji formats.
     * @param formats   Emoji format collection.
     */
    public void setFormats(EmojiFormat... formats)
    {
        this.formats.clear();
        for(EmojiFormat format : formats)
        {
            this.formats.add(format);
        }
    }

    /**
     * Get emoji format collection.
     * @return  Emoji format collection.
     */
    public java.util.Collection<EmojiFormat> getFormats()
    {
        return formats;
    }

    /**
     * Set source root path.
     * Default value is EmojidexFileUtils.getRemoteRootPathDefault()
     * @param sourceRootPath    Source root path.
     */
    public void setSourceRootPath(String sourceRootPath)
    {
        this.sourceRootPath = sourceRootPath;
    }

    /**
     * Get source root path.
     * @return  Source root path.
     */
    public String getSourceRootPath()
    {
        return sourceRootPath;
    }

    /**
     * Set force download flag.
     * Default value is false.
     * @param forceFlag     Force download flag.
     */
    public void setForceFlag(boolean forceFlag)
    {
        this.forceFlag = forceFlag;
    }

    /**
     * Get force download flag.
     * @return  Force download flag.
     */
    public boolean getForceFlag()
    {
        return forceFlag;
    }
}
