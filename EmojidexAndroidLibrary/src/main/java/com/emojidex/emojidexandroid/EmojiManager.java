package com.emojidex.emojidexandroid;

import android.content.Context;
import android.net.Uri;

import com.emojidex.emojidexandroidlibrary.BuildConfig;
import com.emojidex.emojidexandroidlibrary.R;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by kou on 14/10/03.
 */
public class EmojiManager {
    private final Context context;

    private final ArrayList<Emoji> emojies = new ArrayList<Emoji>();
    private final ArrayList<Emoji> utfEmojies = new ArrayList<Emoji>();
    private final ArrayList<Emoji> extendedEmojies = new ArrayList<Emoji>();
    private final ArrayList<Emoji> otherEmojies = new ArrayList<Emoji>();
    private final HashMap<String, Emoji> emojiTableFromName = new HashMap<String, Emoji>();
    private final HashMap<List<Integer>, Emoji> emojiTableFromCodes = new HashMap<List<Integer>, Emoji>();
    private final HashMap<String, ArrayList<Emoji>> categorizedEmojies = new HashMap<String, ArrayList<Emoji>>();

    private int nextOriginalCode;

    private static final int DIRTY_COUNT_MAX = 500;
    private static final int SAVE_DELAY = 3000;

    private int dirtyCount = 0;
    private boolean isSaving = false;

    private final Timer timer = new Timer();
    private TimerTask saveTask = null;

    /**
     * Construct EmojiManager object.
     */
    EmojiManager(Context context)
    {
        this.context = context.getApplicationContext();
        reset();
    }

    /**
     * Add emoji from json file.
     * @param uri   Json file uri.
     */
    public void add(Uri uri)
    {
        final ArrayList<Emoji> newEmojies = EmojidexFileUtils.readJsonFromFile(uri, new TypeReference<ArrayList<Emoji>>(){}.getType());

        if(newEmojies == null || newEmojies.isEmpty())
            return;

        // Initialize and add emoji.
        emojies.ensureCapacity(emojies.size() + newEmojies.size());

        for(Emoji emoji : newEmojies)
        {
            initialize(emoji);
        }
    }

    /**
     * Reset manager.
     */
    public void reset()
    {
        emojies.clear();
        utfEmojies.clear();
        extendedEmojies.clear();
        otherEmojies.clear();
        emojiTableFromName.clear();
        emojiTableFromCodes.clear();
        categorizedEmojies.clear();

        nextOriginalCode = context.getResources().getInteger(R.integer.original_code_start);
    }

    /**
     * Get emoji from emoji name.
     * @param name  Emoji name.
     * @return      Emoji.(If emoji is not found, return null.)
     */
    public Emoji getEmoji(String name)
    {
        return emojiTableFromName.get(name);
    }

    /**
     * Get emoji from emoji codes.
     * @param codes Emoji codes.
     * @return      Emoji.(If emoji is not found, return null.)
     */
    public Emoji getEmoji(List<Integer> codes)
    {
        return emojiTableFromCodes.get(codes);
    }

    /**
     * Get emoji list from category.
     * @param category  Category name.
     * @return          Emoji list.(If emoji list is not found, return null.)
     */
    public List<Emoji> getEmojiList(String category)
    {
        return categorizedEmojies.get(category);
    }

    /**
     * Ger all emoji list.
     * @return  All emoji list.
     */
    public List<Emoji> getAllEmojiList()
    {
        return emojies;
    }

    /**
     * Get UTF emoji list.
     * @return      UTF emoji list.
     */
    public List<Emoji> getUTFEmojiList()
    {
        return utfEmojies;
    }

    /**
     * Get extended emoji list.
     * @return      Extended emoji list.
     */
    public List<Emoji> getExtendedEmojiList()
    {
        return extendedEmojies;
    }

    /**
     * Get other emoji list.
     * @return      Other emoji list.
     */
    public List<Emoji> getOtherEmojiList()
    {
        return otherEmojies;
    }

    /**
     * Get category name list.
     * @return  Category name list.
     */
    public Collection<String> getCategoryNames()
    {
        return categorizedEmojies.keySet();
    }

    /**
     * Update emojies.
     * @param type      Download type.
     * @param sources   Sources.
     */
    public void updateEmojies(String type, com.emojidex.libemojidex.EmojiVector sources)
    {
        for(int i = 0;  i < sources.size();  ++i)
        {
            final com.emojidex.libemojidex.Emojidex.Data.Emoji src = sources.get(i);
            final String emojiName = src.getCode();
            Emoji emoji = getEmoji(emojiName);
            if(emoji == null)
            {
                emoji = new Emoji();

                copy(emoji, type, src);

                initialize(emoji);
            } else
            {
                copy(emoji, type, src);
            }
        }

        // Save json.
        save();
    }

    /**
     * Copy emoji parameter.
     * @param dest      Destination emoji.
     * @param type      Download type.
     * @param src       Source.
     */
    private void copy(Emoji dest, String type, com.emojidex.libemojidex.Emojidex.Data.Emoji src)
    {
        final String oldType = dest.getType();

        if(     oldType == null
            ||  (   !oldType.equals(type)
                &&  !oldType.equals("utf")
                &&  !oldType.equals("extended") )   )
        {
            dest.setType(type);

            if(dest.isInitialized())
            {
                otherEmojies.remove(dest);

                if(type.equals("utf"))
                    utfEmojies.add(dest);
                else if(type.equals("extended"))
                    extendedEmojies.add(dest);
                else
                    otherEmojies.add(dest);
            }
        }

        dest.copy(src);
    }

    /**
     * Update emoji image checksum.
     * @param emojiName     Emoji name.
     * @param format        Emoji format.
     */
    public void updateChecksum(String emojiName, EmojiFormat format)
    {
        final Emoji emoji = getEmoji(emojiName);

        if(emoji == null)
            return;

        emoji.getCurrentChecksums().set(
                format,
                emoji.getChecksums().get(format)
        );

        // Save json.
        save();
    }

    /**
     * Initialize and regist emoji.
     * @param emoji     New emoji.
     */
    private void initialize(Emoji emoji)
    {
        // Initialize.
        if(emoji.hasCodes())
            emoji.initialize(context);
        else
            emoji.initialize(context, nextOriginalCode++);

        // Add emoji to tables.
        emojies.add(emoji);

        if("utf".equals(emoji.getType()))
            utfEmojies.add(emoji);
        else if("extended".equals(emoji.getType()))
            extendedEmojies.add(emoji);
        else
            otherEmojies.add(emoji);

        emojiTableFromName.put(emoji.getCode(), emoji);
        emojiTableFromCodes.put(emoji.getCodes(), emoji);

        ArrayList<Emoji> categoryList = categorizedEmojies.get(emoji.getCategory());
        if(categoryList == null)
        {
            categoryList = new ArrayList<Emoji>();
            categorizedEmojies.put(emoji.getCategory(), categoryList);
        }
        categoryList.add(emoji);
    }

    /**
     * Save json.
     */
    private void save()
    {
        ++dirtyCount;

        if(isSaving)
        {
            return;
        }

        if(saveTask != null)
            saveTask.cancel();

        saveTask = new SaveTask();

        if(dirtyCount >= DIRTY_COUNT_MAX)
        {
            isSaving = true;
            timer.schedule(saveTask, 0);
        }
        else
        {
            timer.schedule(saveTask, SAVE_DELAY);
        }
    }

    /**
     * Json save task.
     */
    private class SaveTask extends TimerTask
    {
        @Override
        public void run()
        {
            isSaving = true;
            dirtyCount = 0;

            // Save emojies.
            EmojidexFileUtils.writeJsonToFile(
                    EmojidexFileUtils.getLocalJsonUri(),
                    emojies
            );

            // Save update info.
            Emojidex.getInstance().getUpdateInfo().save();

            isSaving = false;
            saveTask = null;

            if(dirtyCount > 0)
            {
                saveTask = new SaveTask();
                timer.schedule(saveTask, SAVE_DELAY);
            }
        }
    }
}
