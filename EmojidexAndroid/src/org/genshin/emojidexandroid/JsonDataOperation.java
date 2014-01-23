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
public class JsonDataOperation
{
    public static final int SUCCESS = 0;
    public static final int DONE = 1;
    public static final int FAILURE = 2;

    public static final String FAVORITES = "favorites";
    public static final String HISTORIES = "histories";

    public static int MAX_HISTORIES = 50;

    /**
     * load favorites/histories data from local file
     * @param context
     * @param filename  favorites or histories
     * @return keyCodes
     */
    public static ArrayList<List<Integer>> load(Context context, String filename)
    {
        String str = "";
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
        if (filename == FAVORITES)
        {
            if (duplicationCheck(data, keyCodes))
                return DONE;
        }

        // histories size check
        if (filename == HISTORIES)
            data = sizeCheck(data);

        // add data
        JSONArray array = new JSONArray();
        for (List<Integer> codes : data)
        {
            JSONArray tmp = new JSONArray();
            for (int code : codes)
                tmp.put(code);
            array.put(tmp);
        }

        JSONArray tmp = new JSONArray();
        for (int keyCode : keyCodes)
            tmp.put(keyCode);
        array.put(tmp);

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
        for (List<Integer> list :data)
        {
            if (list.size() != keyCodes.size())
                continue;

            for (int i = 0; i < list.size(); i++)
            {
                if (!list.get(i).equals(keyCodes.get(i)))
                    break;
                if (i == list.size() - 1)
                    data.remove(list);
            }
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
    private static boolean deleteAll(Context context, String filename)
    {
        return true;
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
        if (data.size() >= MAX_HISTORIES)
        {
            while (data.size() < MAX_HISTORIES)
                data.remove(0);
            return data;
        }
        else
            return data;
    }
}
