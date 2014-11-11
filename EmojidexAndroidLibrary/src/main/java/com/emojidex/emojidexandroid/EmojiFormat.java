package com.emojidex.emojidexandroid;

/**
 * Created by kou on 14/10/07.
 */
public enum EmojiFormat
{
    SVG(".svg", "."),
    PNG_LDPI(".png", "ldpi"),
    PNG_MDPI(".png", "mdpi"),
    PNG_HDPI(".png", "hdpi"),
    PNG_XHDPI(".png", "xhdpi"),
    PNG_PX8(".png", "px8"),
    PNG_PX16(".png", "px16"),
    PNG_PX32(".png", "px32"),
    PNG_PX64(".png", "px64"),
    PNG_PX128(".png", "px128"),
    PNG_PX256(".png", "px256"),
    ;

    private final String extension;
    private final String relativeDir;

    /**
     * Convert resolution name to format.
     * @param resolution    Resolution name.
     * @return              Format of resolution.(If resolution is not found, return null.)
     */
    public static EmojiFormat toFormat(String resolution)
    {
        for(EmojiFormat format : EmojiFormat.values())
            if (resolution.equals(format.relativeDir))
                return format;
        return null;
    }

    /**
     * Get extension of format.
     * @return  Extension of format.
     */
    String getExtension()
    {
        return extension;
    }

    /**
     * Get relative directory path of format.
     * @return  Relative directory path.
     */
    String getRelativeDir()
    {
        return relativeDir;
    }

    /** Construct EmojiFormat object..(private) */
    private EmojiFormat(String extension, String relativeDir)
    {
        this.extension = extension;
        this.relativeDir = relativeDir;
    }
}
