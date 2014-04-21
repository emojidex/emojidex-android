package org.genshin.emojidexandroid;

import android.content.Context;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;

import java.io.BufferedReader;
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
    public static final String KEYBOARD = "keyboard";
    public static final String SHARE = "share";

    public static int MAX_SIZE = 50;

    /**
     * load favorites/histories data from local file
     * @param context
     * @param filename  favorites or histories
     * @return keyCodes
     */
    public static ArrayList<List<Integer>> load(Context context, String filename)
    {
        String str;
        StringBuilder builder = new StringBuilder();
        ArrayList<List<Integer>> data = new ArrayList<List<Integer>>();

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
            return data;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return  data;
        }

        if (str.equals(""))
            return  data;

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
        {
            List<Integer> list = new ArrayList<Integer>();
            for (int j = 0; j < rootNode.get(i).size(); j++)
                list.add(rootNode.get(i).get(j).intValue());
            data.add(list);
        }

        return data;
    }

    /**
     * save favorites/histories data to local file
     * @param context
     * @param keyCodes
     * @param filename favorites or histories
     * @return success or failure or already registered (use only favorites)
     */
    public static int save(Context context, List<Integer> keyCodes, String filename)
    {
        // current list
        ArrayList<List<Integer>> data = load(context, filename);

        // duplication check
        if (filename.equals(FAVORITES))
        {
            if (duplicationCheck(data, keyCodes))
                return DONE;
        }

        // size check
        data = sizeCheck(data);

        // add data
        JSONArray array = new JSONArray();
        JSONArray tmp = new JSONArray();
        for (int keyCode : keyCodes)
            tmp.put(keyCode);
        array.put(tmp);

        // add old data
        for (List<Integer> codes : data)
        {
            tmp = new JSONArray();
            for (int code : codes)
                tmp.put(code);
            array.put(tmp);
        }

        if (saveFile(context, filename, array))
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
    private static boolean saveFile(Context context, String filename, JSONArray data)
    {
        // save data
        try
        {
            OutputStream out = context.openFileOutput(filename + ".json", Context.MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.append(data.toString());
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
     * delete favorite
     * @param context
     * @param keyCodes
     * @return
     */
    public static boolean delete(Context context, List<Integer> keyCodes)
    {
        // current list
        ArrayList<List<Integer>> data = load(context, FAVORITES);

        // delete data
        boolean delete = false;
        for (List<Integer> list :data)
        {
            if (list.size() != keyCodes.size())
                continue;

            for (int i = 0; i < list.size(); i++)
            {
                if (!list.get(i).equals(keyCodes.get(i)))
                    break;
                if (i == list.size() - 1)
                {
                    delete = true;
                    data.remove(list);
                    break;
                }
            }
            if (delete)
                break;
        }

        // prepare json data
        JSONArray array = new JSONArray();
        for (List<Integer> codes : data)
        {
            JSONArray tmp = new JSONArray();
            for (int code : codes)
                tmp.put(code);
            array.put(tmp);
        }

        // save data
        if (saveFile(context, FAVORITES, array))
            return true;
        else
            return  false;
    }

    /**
     * delete al
     * @param context
     * @param filename favorites or histories
     * @return
     */
    public static boolean deleteAll(Context context, String filename)
    {
        return context.deleteFile(filename + ".json");
    }

    /**
     * duplication check
     * @param data
     * @param keyCodes
     * @return
     */
    private static boolean duplicationCheck(ArrayList<List<Integer>> data, List<Integer> keyCodes)
    {
        boolean check = false;
        for (List<Integer> list : data)
        {
            if (list.size() != keyCodes.size())
                continue;

            for (int i = 0; i < list.size(); i++)
            {
                if (!list.get(i).equals(keyCodes.get(i)))
                    break;
                if (i == list.size() - 1)
                    check = true;
            }
        }

        return check;
    }

    /**
     * histories size check
     * @param data
     * @return
     */
    private static ArrayList<List<Integer>> sizeCheck(ArrayList<List<Integer>> data)
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
     * @param keyCodes
     * @return
     */
    public static boolean searchFavorite(Context context, List<Integer> keyCodes)
    {
        ArrayList<List<Integer>> data = load(context, FAVORITES);

        if (duplicationCheck(data, keyCodes))
            return true;
        else
            return false;
    }
}
