package org.genshin.emojidexandroid2;

import android.os.Environment;

/**
 * Created by kou on 14/10/03.
 */
class PathGenerator {
    private static final String rootDir = Environment.getExternalStorageDirectory().getPath() + "/emojidex";

    public static String Generate(String name, String kind, Emojidex.Format format)
    {
        return rootDir + "/" + kind + "/" + format.getRelativeDir() + "/" + name + format.getExtension();
    }
}
