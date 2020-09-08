package com.emojidex.emojidexandroid.imageloader;

import android.net.Uri;
import android.os.AsyncTask;

import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.EmojidexFileUtils;

import java.io.File;

import japngasm.APNGAsm;
import japngasm.APNGFrame;

class ImageLoadTask extends AsyncTask<Void, Void, ImageLoadTask.LoadResult> {
    private static int nextHandle = 0;

    private final int handle;
    private final ImageLoadArguments arguments;

    public static class LoadResult
    {
        public ImageLoadArguments arguments;
        public ImageParam param;
        public boolean succeeded;
    }

    public ImageLoadTask(ImageLoadArguments arguments)
    {
        super();

        handle = nextHandle++;
        this.arguments = arguments;
    }

    public int getHandle()
    {
        return handle;
    }

    public ImageLoadArguments getArguments()
    {
        return arguments;
    }

    @Override
    protected LoadResult doInBackground(Void... voids)
    {
        final LoadResult result = new LoadResult();
        result.arguments = arguments;

        final File tmpFile = createTemporaryFile(result.arguments);

        result.param = createImageParam(tmpFile.getAbsolutePath(), result.arguments.getFormat());

        result.succeeded = tmpFile.exists();

        EmojidexFileUtils.deleteFiles(tmpFile);

        return result;
    }

    /**
     * Create temporary file.
     * @param arguments     Image load arguments.
     * @return              Temporary file.
     */
    private synchronized File createTemporaryFile(ImageLoadArguments arguments)
    {
        final String emojiName = arguments.getEmojiName();
        final EmojiFormat format = arguments.getFormat();
        final Uri uri = EmojidexFileUtils.getLocalEmojiUri(emojiName, format);
        final File tmpFile = new File(EmojidexFileUtils.getTemporaryPath() + format.getExtension());
        final Uri tmpUri = Uri.fromFile(tmpFile);

        if( !EmojidexFileUtils.copyFile(uri, tmpUri) )
            EmojidexFileUtils.deleteFiles(tmpFile);

        return tmpFile;
    }

    /**
     * Create image parameter.
     * @param path      Image file path.
     * @param format    Image format.
     * @return          Image parameter.
     */
    private ImageParam createImageParam(String path, EmojiFormat format)
    {
        final APNGAsm apngasm = new APNGAsm();
        apngasm.disassemble(path);

        final int frameCount = Math.max((int)apngasm.getFrames().size(), 1);

        final ImageParam param = new ImageParam();
        param.frames = new ImageParam.Frame[frameCount];

        if(param.hasAnimation())
        {
            // Set param.
            param.oneShot = apngasm.getLoops() == 1;
            param.skipFirst = apngasm.isSkipFirst();

            // Create temporary image files.
            final String tmpDir = EmojidexFileUtils.getTemporaryPath() + "/";
            final File tmpFile = new File(tmpDir);
            tmpFile.mkdirs();
            apngasm.savePNGs(tmpDir);

            // Load frames.
            for(int i = 0;  i < frameCount;  ++i)
            {
                final ImageParam.Frame frame = new ImageParam.Frame();

                final String tmpPath = tmpDir + i + ".png";
                frame.bitmap = ImageLoadUtils.loadBitmap(arguments.getResources(), tmpPath, format);

                final APNGFrame af = apngasm.getFrames().get(i);
                frame.duration = (int)(1000 * af.delayNum() / af.delayDen());

                param.frames[i] = frame;
            }

            EmojidexFileUtils.deleteFiles(tmpFile);
        }
        else
        {
            final ImageParam.Frame frame = new ImageParam.Frame();
            frame.bitmap = ImageLoadUtils.loadBitmap(arguments.getResources(), path, format);
            param.frames[0] = frame;
        }

        return param;
    }

    @Override
    protected void onPostExecute(LoadResult result)
    {
        final ImageLoader loader = ImageLoader.getInstance();

        loader.reload(result);
        loader.finishTask(handle);
        loader.notifyToListener(handle, result.arguments.getFormat(), result.arguments.getEmojiName());
    }
}
