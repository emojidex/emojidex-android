package org.genshin.emojidexandroid;

import android.content.Context;
import android.content.res.Resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by nazuki on 2013/08/28.
 */
public class EmojiList {
    ArrayList<Emoji> emojis;
    ArrayList<String> categories;

    public EmojiList(Context context) {
        emojis = new ArrayList<Emoji>();
        categories = new ArrayList<String>();

        setEmojisFromJson(context);
        setCategories();
    }

    public ArrayList<Emoji> getEmojis() {
        return emojis;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    private void setEmojisFromJson(Context context) {
        // Create ObjectMapper
        ObjectMapper mapper = new ObjectMapper();

        // Get root node
        JsonNode rootNode = null;
        Resources res = context.getResources();
        InputStream is = res.openRawResource(R.raw.index);
        try { rootNode = mapper.readValue(is, JsonNode.class); }
        catch (JsonParseException e) { e.printStackTrace(); }
        catch (JsonMappingException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }

        // Get JSON data from root node and put in an Emoji
        JsonNode current;
        for (int i = 0; (current = rootNode.get(i)) != null; i++) {
            Emoji emoji = new Emoji();

            if (current.get("moji") != null) emoji.setMoji(current.get("moji").textValue());
            if (current.get("name") != null) emoji.setName(current.get("name").textValue());
            if (current.get("name-ja") != null) emoji.setNameJa(current.get("name-ja").textValue());
            if (current.get("category") != null) emoji.setCategory(current.get("category").textValue());
            if (current.get("unicode") != null) emoji.setUnicode(current.get("unicode").textValue());

            emojis.add(emoji);
        }
    }

    private void setCategories() {
        categories.add("all");

        for (Emoji emoji : emojis)
            if (!categories.contains(emoji.getCategory())) categories.add(emoji.getCategory());
    }
}
