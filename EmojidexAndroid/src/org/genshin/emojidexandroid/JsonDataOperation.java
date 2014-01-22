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
     * @param mode  favorites or histories
     * @return keyCodes
     */
    public static ArrayList<List<Integer>> load(Context context, String mode)
    {
        String str = "";
        StringBuilder builder = new StringBuilder();
        ArrayList<List<Integer>> data = new ArrayList<List<Integer>>();

        // read json data
        try
        {
            InputStream in = context.openFileInput(mode + ".json");
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
     * @param mode favorites or histories
     * @return success or failure or already registered (use only favorites mode)
     */
    public static int save(Context context, List<Integer> keyCodes, String mode)
    {
        // current list
        ArrayList<List<Integer>> data = load(context, mode);

        // duplication check
        if (mode == FAVORITES)
        {
            if (duplicationCheck(data, keyCodes))
                return DONE;
        }

        // histories size check
        if (mode == HISTORIES)
            data = sizeCheck(data);

        // save data
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

        try
        {
            OutputStream out = context.openFileOutput(mode + ".json", Context.MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.append(array.toString());
            writer.close();
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
