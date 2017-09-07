package com.emojidex.emojidexandroid.downloader.arguments;

/**
 * Created by kou on 17/09/05.
 */

class ArgumentsUtils {
    public static <T> boolean equals(T v1, T v2)
    {
        if(v1 == null)
        {
            if(v2 != null)
                return false;
        }
        else
        {
            if(v2 == null)
                return false;

            return v1.equals(v2);
        }

        return true;
    }
}
