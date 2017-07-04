package com.emojidex.emojidexandroid;

import android.content.Context;
import android.net.Uri;

import com.emojidex.emojidexandroidlibrary.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
            os.write(BuildConfig.VERSION_NAME.getBytes());
            os.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public String load(Context context)
    {
        final Uri uri = EmojidexFileUtils.getLocalFileUri(filename);
        try
        {
            String result = "";
            final InputStream is = context.getContentResolver().openInputStream(uri);
            byte[] buffer = new byte[16];
            int readByte;
            while( (readByte = is.read(buffer)) != -1 )
                result += new String(buffer, 0, readByte);
            is.close();
            return result;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public void optimize(Context context)
    {
        final String version = load(context);

        if(optimizeJson(context, version))
            save(context);
    }

    private boolean optimizeJson(Context context, String version)
    {
        // Replace '_' to ' ' in emoji codes.
        if(version == null)
        {
            final Uri uri = EmojidexFileUtils.getLocalJsonUri();
            final ArrayList<JsonParam> params = JsonParam.readFromFile(context, uri);
            for(JsonParam param : params)
                param.setCode(param.getCode().replace('_', ' '));
            JsonParam.writeToFile(context, uri, params);
            return true;
        }
        return false;
    }
}
