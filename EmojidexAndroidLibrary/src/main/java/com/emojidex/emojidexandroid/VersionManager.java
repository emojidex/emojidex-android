package com.emojidex.emojidexandroid;

import android.content.Context;
import android.net.Uri;

import com.emojidex.emojidexandroidlibrary.BuildConfig;

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
    private static final String filename = "emojidex_lib_version";

    public static VersionManager getInstance()
    {
        return instance;
    }

    private VersionManager()
    {;
    }

    public void save(Context context)
    {
        final Uri uri = EmojidexFileUtils.getLocalFileUri(filename);
        try
        {
            final OutputStream os = context.getContentResolver().openOutputStream(uri);
            os.write(BuildConfig.VERSION_CODE);
            os.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public int load(Context context)
    {
        final Uri uri = EmojidexFileUtils.getLocalFileUri(filename);
        try
        {
            String version = "";
            final InputStream is = context.getContentResolver().openInputStream(uri);
            byte[] buffer = new byte[16];
            int readByte;
            while( (readByte = is.read(buffer)) != -1 )
                version += new String(buffer, 0, readByte);
            is.close();
            return NumberFormat.getInstance().parse(version).intValue();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(ParseException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    public void optimize(Context context)
    {
        final int version = load(context);

        if(optimizeJson(context, version))
            save(context);
    }

    private boolean optimizeJson(Context context, int version)
    {
        boolean result = false;

        // version <= 0.0.8
        if(version == 0)
        {
            // Replace '_' to ' ' in emoji codes.
            final Uri uri = EmojidexFileUtils.getLocalJsonUri();
            final ArrayList<Emoji> params = EmojidexFileUtils.readJsonFromFile(uri);
            for(JsonParam param : params)
                param.setCode(param.getCode().replace('_', ' '));
            EmojidexFileUtils.writeJsonToFile(uri, params);

            result = true;
        }

        return result;
    }
}
