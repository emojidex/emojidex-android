package com.emojidex.emojidexandroid;

import android.content.Context;

import com.emojidex.emojidexandroidlibrary.R;

import java.util.ArrayList;

/**
 * Created by kou on 17/05/26.
 */
class VersionManager {
    private static final VersionManager instance = new VersionManager();

    private ArrayList<OptimizeEmojiMethod> optimizeEmojiMethods = new ArrayList<OptimizeEmojiMethod>();

    /**
     * Get singleton instance.
     * @return      Singleton instance.
     */
    public static VersionManager getInstance()
    {
        return instance;
    }

    /**
     * Private constructor.
     */
    private VersionManager()
    {
    }

    /**
     * Initialize version manager.
     * @param context   Context.
     */
    public void initialize(Context context)
    {
        createOptimizeEmojiMethods(context);
    }

    /**
     * Optimize emoji.
     * @param emoji     Target emoji.
     */
    public void optimizeEmoji(Emoji emoji)
    {
        for(OptimizeEmojiMethod method : optimizeEmojiMethods)
            method.optimize(emoji);
    }

    /**
     * Create optimize emoji methods.
     * @param context   Context.
     */
    private void createOptimizeEmojiMethods(Context context)
    {
        final int version = Emojidex.getInstance().getUpdateInfo().getLastUpdateVersionCode();

        // version <= 0.0.8
        if(version == 0)
        {
            optimizeEmojiMethods.add(new OptimizeEmojiMethod() {
                @Override
                public void optimize(Emoji emoji)
                {
                    // Replace '_' to ' ' in emoji codes.
                    emoji.setCode(emoji.getCode().replace('_', ' '));
                    Emojidex.getInstance().getUpdateInfo().update(true);
                }
            });
        }

        // version <= 0.0.14
        if(version <= 4)
        {
            final int ORIGINAL_CODE_START = context.getResources().getInteger(R.integer.original_code_start);
            optimizeEmojiMethods.add(new OptimizeEmojiMethod() {
                @Override
                public void optimize(Emoji emoji)
                {
                    // Clear moji if moji is original code.
                    final String moji = emoji.getMoji();
                    if(     moji != null
                        &&  !moji.isEmpty()
                        &&  moji.codePointAt(0) >= ORIGINAL_CODE_START  )
                    {
                        emoji.setMoji("");
                        Emojidex.getInstance().getUpdateInfo().update(true);
                    }
                }
            });
        }
    }

    /**
     * Optimize emoji method interface.
     */
    interface OptimizeEmojiMethod
    {
        void optimize(Emoji emoji);
    }
}
