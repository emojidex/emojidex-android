package com.emojidex.emojidexandroid;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by kou on 14/10/07.
 */
public class DownloadConfig {
    public final HashSet<EmojiFormat> formats = new HashSet<EmojiFormat>();
    public final ArrayList<String> kinds = new ArrayList<String>();

    public String sourcePath = "http://assets.emojidex.com";
    public int threadCount = 8;
    public DownloadListener listener = null;

    /**
     * Construct DownloadConfig object.
     */
    public DownloadConfig()
    {
        // Set default values.
        final EmojiFormat defaultFormat = Emojidex.getInstance().getDefaultFormat();
        if(defaultFormat != null)
            formats.add(defaultFormat);

        kinds.add("utf");
        kinds.add("extended");
    }

    /**
     * Deep copy.
     * @param source    Source.
     */
    public void copy(DownloadConfig source)
    {
        formats.clear();
        formats.addAll(source.formats);

        kinds.clear();
        kinds.addAll(source.kinds);

        sourcePath = source.sourcePath;

        threadCount = source.threadCount;

        listener = source.listener;
    }
}
