package com.emojidex.emojidexandroid.downloader;

import com.emojidex.emojidexandroid.Emojidex;
import com.emojidex.emojidexandroid.EmojidexFileUtils;
import com.emojidex.emojidexandroid.EmojidexUser;
import com.emojidex.emojidexandroid.MojiCodes;
import com.emojidex.emojidexandroid.downloader.arguments.MojiCodesDownloadArguments;
import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.StringVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kou on 18/01/23.
 */

class MojiCodesDownloadTask extends AbstractDownloadTask {
    private final EmojidexUser user;

    /**
     * Construct object.
     * @param arguments     Download arguments.
     */
    public MojiCodesDownloadTask(MojiCodesDownloadArguments arguments)
    {
        super(arguments);

        user = Emojidex.getInstance().getUser();
    }

    @Override
    public TaskType getType()
    {
        return TaskType.MOJI_CODES;
    }

    @Override
    protected boolean download()
    {
        final MojiCodesDownloadArguments arg = (MojiCodesDownloadArguments)getArguments();

        final Client client = DownloaderUtils.createEmojidexClient(user);
        final com.emojidex.libemojidex.Emojidex.Data.MojiCodes srcMojiCodes = client.getIndexes().mojiCodes(arg.getLocale().getLocale());

        final ArrayList<String> mojiArray = new ArrayList<String>();
        {
            final StringVector srcMojiArary = srcMojiCodes.getMoji_array();
            final long size = srcMojiArary.size();
            for(int i = 0; i < size;  ++i)
                mojiArray.add(srcMojiArary.get(i));
        }

        final MojiCodes mojiCodes = new MojiCodes();
        mojiCodes.setMojiString(srcMojiCodes.getMoji_string());
        mojiCodes.setMojiArray(mojiArray);

        final HashMap<String, String> tmpMojiCodes = new HashMap<>();
        for (Map.Entry<String, String> entry : srcMojiCodes.getMoji_index().entrySet())
            tmpMojiCodes.put(entry.getKey(), entry.getValue());

        mojiCodes.setMojiIndex(tmpMojiCodes);
        EmojidexFileUtils.writeJsonToFile(
                EmojidexFileUtils.getLocalMojiCodesJsonUri(),
                mojiCodes
        );

        return true;
    }
}
