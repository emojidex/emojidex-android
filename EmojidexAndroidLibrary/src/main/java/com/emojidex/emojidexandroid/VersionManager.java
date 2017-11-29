package com.emojidex.emojidexandroid;

import android.content.Context;
import android.net.Uri;

import com.emojidex.emojidexandroidlibrary.BuildConfig;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by kou on 17/05/26.
 */

public class VersionManager {
    private static final VersionManager instance = new VersionManager();

    public static VersionManager getInstance()
    {
        return instance;
    }

    private VersionManager()
    {
    }

    public void optimize()
    {
        final int version = Emojidex.getInstance().getUpdateInfo().getLastUpdateVersionCode();

        optimizeJson(version);
    }

    private void optimizeJson(int version)
    {
        // version <= 0.0.8
        if(version == 0)
        {
            // TODO refactoring
            // Replace '_' to ' ' in emoji codes.
            final Uri uri = EmojidexFileUtils.getLocalJsonUri();
            final ArrayList<Emoji> params = EmojidexFileUtils.readJsonFromFile(uri, new TypeReference<ArrayList<Emoji>>(){}.getType());
            if(params != null)
            {
                for(JsonParam param : params)
                    param.setCode(param.getCode().replace('_', ' '));
                EmojidexFileUtils.writeJsonToFile(uri, params);
            }
        }
    }
}
