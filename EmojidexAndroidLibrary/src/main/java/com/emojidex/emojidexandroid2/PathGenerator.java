package com.emojidex.emojidexandroid2;

import android.os.Environment;

/**
 * Created by kou on 14/10/03.
 */
class PathGenerator {
    static final String LOCAL_ROOT_PATH = Environment.getExternalStorageDirectory().getPath() + "/emojidex";
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
     * @param kind          Emoji kind.
     * @param rootPath      Server path.
     * @return      Emoji path.
     */
    public static String getRemoteEmojiPath(String name, EmojiFormat format, String kind, String rootPath)
    {
        return rootPath + "/"
                + kind + "/"
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
}
