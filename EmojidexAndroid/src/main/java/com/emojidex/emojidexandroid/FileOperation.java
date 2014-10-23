package com.emojidex.emojidexandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nazuki on 2014/01/22.
 */
public class FileOperation
{
    public static final int SUCCESS = 0;
    public static final int DONE = 1;
    public static final int FAILURE = 2;

    public static final String FAVORITES = "favorites";
    public static final String HISTORIES = "histories";
    public static final String DOWNLOAD = "download";
    public static final String SEARCH_RESULT = "search_result";
    public static final String KEYBOARD = "keyboard";
    public static final String SHARE = "share";

    public static int MAX_SIZE = 50;

    /**
     * load data -> array list
     * @param context
     * @param filename
     * @return keyCodes
     */
    public static ArrayList<String> load(Context context, String filename)
    {
        ArrayList<String> data = new ArrayList<String>();
        String str = loadFileFromLocal(context, filename);
        if (str.equals(""))
            return data;

        // convert array data
        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;
        try
        {
            JsonParser parser = factory.createJsonParser(str);
            rootNode = mapper.readValue(parser, JsonNode.class);
        }
        catch (IOException e) { e.printStackTrace();}
        for (int i = 0; i < rootNode.size(); i++)
            data.add(rootNode.get(i).textValue());

        return data;
    }

    /**
     * load data from local file
     * @param context
     * @param filename
     * @return
     */
    public static String loadFileFromLocal(Context context, String filename)
    {
        String str;
        StringBuilder builder = new StringBuilder();

        // read json data
        try
        {
            InputStream in = context.openFileInput(filename + ".json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            while ((str = reader.readLine()) != null)
                builder.append(str);
            str = new String(builder);
            reader.close();
            in.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return "";
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "";
        }

        return str;
    }

    /**
     * save favorites/histories data to local file
     * @param context
     * @param emojiName
     * @param filename favorites or histories
     * @return success or failure or already registered (use only favorites)
     */
    public static int save(Context context, String emojiName, String filename)
    {
        // current list
        ArrayList<String> data = load(context, filename);

        // duplication check
        if (filename.equals(FAVORITES))
        {
            if (duplicationCheck(data, emojiName))
                return DONE;
        }

        // size check
        data = sizeCheck(data);

        // add data
        JSONArray array = new JSONArray();
        array.put(emojiName);

        // add old data
        for (String name : data)
            array.put(name);

        if (saveFileToLocal(context, filename, array.toString()))
            return SUCCESS;
        else
            return  FAILURE;
    }

    /**
     * save file
     * @param context
     * @param filename
     * @param data
     * @return
     */
    public static boolean saveFileToLocal(Context context, String filename, String data)
    {
        // save data
        try
        {
            OutputStream out = context.openFileOutput(filename + ".json", Context.MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.append(data);
            writer.close();
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * delete favorite or history or download
     * @param context
     * @param emojiName
     * @return
     */
    public static boolean delete(Context context, String emojiName, String filename)
    {
        // current list
        ArrayList<String> data = load(context, filename);

        // delete data
        for (int i = data.size() - 1; i >= 0; i--)
        {
            if (data.get(i).equals(emojiName))
                data.remove(i);
        }

        // prepare json data
        JSONArray array = new JSONArray();
        for (String name : data)
            array.put(name);

        // save data
        if (saveFileToLocal(context, filename, array.toString()))
            return true;
        else
            return  false;
    }

    /**
     * delete file
     * @param context
     * @param filename
     * @return
     */
    public static boolean deleteFile(Context context, String filename)
    {
        return context.deleteFile(filename + ".json");
    }

    /**
     * duplication check
     * @param data
     * @param emojiName
     * @return
     */
    private static boolean duplicationCheck(ArrayList<String> data, String emojiName)
    {
        boolean check = false;
        for (String name : data)
        {
            if (name.equals(emojiName))
                check = true;
        }

        return check;
    }

    /**
     * histories size check
     * @param data
     * @return
     */
    private static ArrayList<String> sizeCheck(ArrayList<String> data)
    {
        if (data.size() >= MAX_SIZE)
        {
            while (data.size() >= MAX_SIZE)
                data.remove(data.get(MAX_SIZE - 1));
            return data;
        }
        else
            return data;
    }

    /**
     * set preferences
     * @param context
     * @param param keyboard's id or packageName
     * @return
     */
    public static boolean savePreferences(Context context, String param, String filename)
    {
        // save data
        try
        {
            OutputStream out = context.openFileOutput(filename + ".txt", Context.MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.append(param);
            writer.close();
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * load preferences
     * @param context
     * @return
     */
    public static String loadPreferences(Context context, String filename)
    {
        String str = "";
        StringBuilder builder = new StringBuilder();

        // read data
        try
        {
            InputStream in = context.openFileInput(filename + ".txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            while ((str = reader.readLine()) != null)
                builder.append(str);
            str = new String(builder);
            reader.close();
            in.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return str;
    }

    /**
     * Whether already registered.
     * @param context
     * @param filename
     * @param emojiName
     * @return
     */
    public static boolean searchEmoji(Context context, String filename, String emojiName)
    {
        ArrayList<String> data = load(context, filename);

        if (duplicationCheck(data, emojiName))
            return true;
        else
            return false;
    }

    /**
     * Save emoji data(json) and image to local.
     * @param context
     * @param emojiName
     * @return
     */
    public static int saveEmoji(Context context, String emojiName, Drawable emoji)
    {
        // duplicate check.
        boolean duplicate = duplicationCheckFromDownloadedData(context, emojiName);
        File file = context.getFileStreamPath(emojiName + ".png");
        if (file.exists() && duplicate)
            return DONE;

        // read the old data from local.
        List<EmojidexEmojiData> list = parseData(context);

        // prepare the data for save.
        String newData = prepareData(emojiName, list);
        if (newData.equals(""))
            return FAILURE;

        // save the json data.
        boolean result = saveFileToLocal(context, DOWNLOAD, newData);
        if (!result)
            return FAILURE;

        // save the image.
        try
        {
            OutputStream out = context.openFileOutput(emojiName + ".png", Context.MODE_PRIVATE);
            Bitmap bitmap = ((BitmapDrawable)emoji).getBitmap();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return FAILURE;
        }

        return SUCCESS;
    }

    /**
     * Delete the downloaded emoji.
     * @param context
     * @param emojiName
     */
    public static boolean deleteEmoji(Context context, String emojiName)
    {
        // read the old data from local.
        List<EmojidexEmojiData> list = parseData(context);

        // delete history.
        delete(context, emojiName, HISTORIES);

        // delete favorite.
        delete(context, emojiName, FAVORITES);


        // delete the data.
        boolean delete = false;
        for (EmojidexEmojiData emoji : list)
        {
            if (emoji.getTmpName().equals(emojiName))
            {
                delete = true;
                list.remove(emoji);
                break;
            }

            if (delete)
                break;;
        }

        // prepare data for save again.
        String newData = prepareData("", list);
        if (newData.equals(""))
            return false;

        boolean result = saveFileToLocal(context, DOWNLOAD, newData);
        if (!result)
            return false;

        return context.deleteFile(emojiName + ".png");
    }

    /**
     * Read the downloaded emoji data from local.
     * @param context
     * @return
     */
    private static List<EmojidexEmojiData> parseData(Context context)
    {
        List<EmojidexEmojiData> list = new ArrayList<EmojidexEmojiData>();

        // read data.
        String data = loadFileFromLocal(context, DOWNLOAD);
        if (data.equals(""))
            return list;

        ObjectMapper mapper = new ObjectMapper();
        try
        {
            JsonNode rootNode = mapper.readTree(data);
            JsonNode emojiNode = rootNode.path("emoji");
            list = mapper.readValue(((Object)emojiNode).toString(),
                                    new TypeReference<ArrayList<EmojidexEmojiData>>(){});
        }
        catch (JsonProcessingException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }

        return list;
    }

    /**
     * Prepare the data for save.
     * @param emojiName
     * @param list
     * @return
     */
    private static String prepareData(String emojiName, List<EmojidexEmojiData> list)
    {
        JSONArray array = new JSONArray();

        // add new data.
        if (!emojiName.equals(""))
        {
            JSONObject obj = new JSONObject();
            try
            {
                obj.put("id", emojiName);
                obj.put("code", emojiName);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                return "";
            }
            array.put(obj);
        }

        // add old data.
        for (EmojidexEmojiData emoji : list)
        {
            JSONObject obj = new JSONObject();
            try
            {
                obj.put("id", emoji.getTmpName());
                obj.put("code", emoji.getTmpName());
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                return "";
            }
            array.put(obj);
        }

        JSONObject obj = new JSONObject();
        try
        {
            obj.put("emoji", array);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return "";
        }

        return obj.toString();
    }

    /**
     * duplication check from download data.
     * @return
     */
    private static boolean duplicationCheckFromDownloadedData(Context context, String emojiName)
    {
        boolean result = false;

        // read data.
        List<EmojidexEmojiData> list = parseData(context);

        // check data.
        for (EmojidexEmojiData emoji : list)
        {
            if (emoji.getTmpName().equals(emojiName))
                result = true;

            if (result)
                break;;
        }

        return result;
    }
}
