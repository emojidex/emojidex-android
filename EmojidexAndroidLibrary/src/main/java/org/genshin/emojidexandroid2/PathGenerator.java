package org.genshin.emojidexandroid2;

import android.os.Environment;

/**
 * Created by kou on 14/10/03.
 */
class PathGenerator {
    private static final String LOCAL_ROOT_DIR = Environment.getExternalStorageDirectory().getPath() + "/emojidex";
    private static final String JSON_FILENAME = "emoji.json";

    /**
     * Get path of emojidex downloaded directory.
     * @return  Path of emojidex downloaded directory.
     */
    public static String getLocalRootPath()
    {
        return LOCAL_ROOT_DIR;
    }

    /**
     * Get relative path from root directory.
     * @param name      Emoji name.
     * @param format    Emoji format.
     * @param kind      Emoji kind.
     * @return  Relative path from root directory.
     */
    public static String getEmojiRelativePath(String name, EmojiFormat format, String kind)
    {
        return kind + "/"
                + format.getRelativeDir() + "/"
                + name + format.getExtension()
                ;
    }

    /**
     * Get relative path from root directory.
     * @param kind  Emoji kind.
     * @return  Relative path from root directory.
     */
    public static String getJsonRelativePath(String kind)
    {
        return kind + "/" + JSON_FILENAME;
    }
}
