package com.emojidex.emojidexandroid;

import android.os.Environment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * Created by kou on 14/10/03.
 */
class PathUtils {
    static final String REMOTE_ROOT_PATH_DEFAULT = "http://cdn.emojidex.com";
    static final String LOCAL_ROOT_PATH = Environment.getExternalStorageDirectory().getPath() + "/.emojidex";
    static final String API_ROOT_PATH = "https://www.emojidex.com/api/v1";
    static final String JSON_FILENAME = "emoji.json";

    /**
     * Create emoji path from local storage.
     * @param name      Emoji name.
     * @param format    Emoji format.
     * @return          Emoji path.
     */
    public static String getLocalEmojiPath(String name, EmojiFormat format)
    {
        return LOCAL_ROOT_PATH + "/"
                + format.getRelativeDir() + "/"
                + name + format.getExtension()
                ;
    }

    /**
     * Create json path from local storage.
     * @return  Json path.
     */
    public static String getLocalJsonPath()
    {
        return LOCAL_ROOT_PATH + "/" + JSON_FILENAME;
    }

    /**
     * Get default root path from remote server.
     * @return      Default root path.
     */
    public static String getRemoteRootPathDefault()
    {
        return REMOTE_ROOT_PATH_DEFAULT;
    }

    /**
     * Create emoji path from remote server.
     * @param name          Emoji name.
     * @param format        Emoji format.
     * @param rootPath      Server path.
     * @return      Emoji path.
     */
    public static String getRemoteEmojiPath(String name, EmojiFormat format, String rootPath)
    {
        try
        {
            name = URLEncoder.encode(name, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return rootPath + "/"
                + format.getRelativeDir() + "/"
                + name + format.getExtension()
                ;
    }

    /**
     * Create json path from remote server.
     * @param kind      Emoji kind.
     * @param rootPath  Server path.
     * @return      Json path.
     */
    public static String getRemoteJsonPath(String kind, String rootPath)
    {
        return rootPath + "/"
                + kind + "_emoji?detailed=true&locale="
                + getLocaleString()
                ;
    }

    /**
     * Create emoji path from assets.
     * @param name      Emoji name.
     * @param format    Emoji format.
     * @return          Emoji path.
     */
    public static String getAssetsEmojiPath(String name, EmojiFormat format)
    {
        return format.getRelativeDir() + "/"
                + name + format.getExtension()
                ;
    }

    /**
     * Get api root path from remote server.
     * @return      Default API root path.
     */
    public static String getAPIRootPath()
    {
        return API_ROOT_PATH;
    }

    /**
     * Get locale string.
     * @return      en or ja.
     */
    public static String getLocaleString()
    {
        Locale locale = Locale.getDefault();
        if (Locale.JAPAN.equals(locale))
        {
            return "ja";
        }
        else
        {
            return "en";
        }
    }
}
