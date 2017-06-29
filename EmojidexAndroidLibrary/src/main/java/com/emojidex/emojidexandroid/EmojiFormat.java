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
    PNG_XXHDPI(".png", "xxhdpi"),
    PNG_XXXHDPI(".png", "xxxhdpi"),
    PNG_HANKO(".png", "hanko"),
    PNG_SEAL(".png", "seal"),
    PNG_PX8(".png", "px8"),
    PNG_PX16(".png", "px16"),
    PNG_PX32(".png", "px32"),
    PNG_PX64(".png", "px64"),
    PNG_PX128(".png", "px128"),
    PNG_PX256(".png", "px256"),
    PNG_PX512(".png", "px512"),
    ;

    private final String extension;
    private final String resolution;

    /**
     * Convert resolution name to format.
     * @param resolution    Resolution name.
     * @return              Format of resolution.(If resolution is not found, return null.)
     */
    public static EmojiFormat toFormat(String resolution)
    {
        for(EmojiFormat format : EmojiFormat.values())
            if (resolution.equals(format.resolution))
                return format;
        return null;
    }

    /**
     * Get extension of format.
     * @return  Extension of format.
     */
    public String getExtension()
    {
        return extension;
    }

    /**
     * Get resolution of format.
     * @return  Resolution.
     */
    public String getResolution()
    {
        return resolution;
    }

    /** Construct EmojiFormat object..(private) */
    private EmojiFormat(String extension, String resolution)
    {
        this.extension = extension;
        this.resolution = resolution;
    }
}
