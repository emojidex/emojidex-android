package com.emojidex.emojidexandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by kou on 15/07/30.
 */
public class SealGenerator {
    private final Context context;

    private Uri uri = null;
    private boolean useLow = false;

    /**
     * Construct object.
     * @param context   Context.
     */
    public SealGenerator(Context context)
    {
        this.context = context;
    }

    /**
     * Generate seal.
     * @param emojiName     Emoji name.
     */
    public void generate(String emojiName)
    {
        generate(Emojidex.getInstance().getEmoji(emojiName));
    }

    /**
     * Generate seal.
     * @param emoji     Emoji.
     */
    public void generate(Emoji emoji)
    {
        // Error check.
        if(emoji == null)
        {
            uri = null;
            return;
        }

        // Create temporary file.
        createTemporaryFile(emoji);
    }

    /**
     * Get seal uri.
     * @return  Seal uri.
     */
    public Uri getUri()
    {
        return uri;
    }

    /**
     * Get flag of use low quality image.
     * @return  true if use low quality image.
     */
    public boolean useLowQuality()
    {
        return useLow;
    }

    /**
     * Create temporary file.
     * @param emoji     Emoji.
     */
    private void createTemporaryFile(Emoji emoji)
    {
        final File temporaryFile = new File(context.getExternalCacheDir(), "tmp" + System.currentTimeMillis() + ".png");
        useLow = false;

        // If file not found, use default format.
        File file = new File(PathUtils.getLocalEmojiPath(emoji.getName(), EmojiFormat.toFormat(context.getString(R.string.emoji_format_seal))));
        if( !file.exists() )
        {
            file = new File(PathUtils.getLocalEmojiPath(emoji.getName(), EmojiFormat.toFormat(context.getString(R.string.emoji_format_default))));
            useLow = true;
        }

        // Create temporary file.
        try
        {
            // Load bitmap.
            final FileInputStream is = new FileInputStream(file);
            final Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();

            // Change background color to white.
            final Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            newBitmap.eraseColor(Color.WHITE);
            final Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(bitmap, 0, 0, null);

            // Save temporary file.
            final FileOutputStream os = new FileOutputStream(temporaryFile);
            newBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            uri = null;
            return;
        }

        uri = Uri.fromFile(temporaryFile);
    }
}
