package com.emojidex.emojidexandroid;

import android.os.Environment;

import java.io.File;

/**
 * Created by kou on 14/10/03.
 */
class PathUtils {
    static final String LOCAL_ROOT_PATH = Environment.getExternalStorageDirectory().getPath() + "/.emojidex";
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
     * Create emoji path from remote server.
     * @param name          Emoji name.
     * @param format        Emoji format.
     * @param rootPath      Server path.
     * @return      Emoji path.
     */
    public static String getRemoteEmojiPath(String name, EmojiFormat format, String rootPath)
    {
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
                + kind + "/"
                + JSON_FILENAME
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
}
