package org.genshin.emojidexandroid;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by nazuki on 2014/03/31.
 */
public class ListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        init();
    }

    private Spinner spinner;
    private EditText editText;
    private Button button;
    private ListView listView;
    private ListAdapter adapter;
    private List<EmojidexEmojiData> emojiList = new ArrayList<EmojidexEmojiData>();

    private void init()
    {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapter.add(getString(R.string.newest));
        arrayAdapter.add(getString(R.string.popular));
        arrayAdapter.add(getString(R.string.category));
        arrayAdapter.add(getString(R.string.emoji));
        spinner = (Spinner)findViewById(R.id.test_spinner);
        spinner.setAdapter(arrayAdapter);

        editText = (EditText)findViewById(R.id.test_edittext);

        button = (Button)findViewById(R.id.test_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String json = getData(spinner.getSelectedItemPosition(), editText.getText().toString());
                parse(json);
                setData();
            }
        });

        listView = (ListView)findViewById(R.id.test_listview);
        adapter = new ListAdapter(this, R.layout.applicationlist_view, emojiList);
        listView.setAdapter(adapter);
    }

    private String getData(int selected, String str)
    {
        String text = str;
        if (!text.equals(""))
        {
            try
            {
                text = URLEncoder.encode(text, "UTF-8");
            }
            catch (UnsupportedEncodingException e) { e.printStackTrace(); }
        }

        String uri = "";
        switch (selected)
        {
            // newest
            case 0:
                uri = "https://www.emojidex.com/api/v1/newest";
                break;
            // popular
            case 1:
                uri = "https://www.emojidex.com/api/v1/popular";
                break;
            // category
            case 2:
                uri = "https://www.emojidex.com/api/v1/search/categories?[q][name_cont]=" + text;
                break;
            // emoji
            case 3:
                uri = "https://www.emojidex.com/api/v1/search/emoji?[q][code_cont]=" + text;
                break;
        }

        Uri.Builder builder = new Uri.Builder();
        AsyncHttpRequestForGetJson getJsonTask = new AsyncHttpRequestForGetJson(uri);
        getJsonTask.execute(builder);
        String result = "";
        try
        {
            result = getJsonTask.get();
        }
        catch (InterruptedException e) { e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }

        return result;
    }

    private void parse(String json)
    {
        emojiList = new ArrayList<EmojidexEmojiData>();
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            JsonNode rootNode = mapper.readTree(json);
            JsonNode emojiNode = rootNode.path("emoji");
            emojiList = mapper.readValue(((Object)emojiNode).toString(),
                                          new TypeReference<ArrayList<EmojidexEmojiData>>(){});
        }
        catch (JsonProcessingException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace(); }
    }

    private void setData()
    {
        for (EmojidexEmojiData emoji : emojiList)
        {
            emoji.initialize(0, getApplicationContext());
        }
        for (int i = emojiList.size() - 1; i >= 0; i--)
        {
            if (emojiList.get(i).getIcon() == null)
                emojiList.remove(i);
        }

        adapter.clear();
        for (EmojidexEmojiData emoji : emojiList)
        {
            adapter.add(emoji);
        }
        adapter.notifyDataSetChanged();
    }

    private class ListAdapter extends ArrayAdapter<EmojidexEmojiData>
    {
        private LayoutInflater inflater;
        private int layout;
        private List<EmojidexEmojiData> list = new ArrayList<EmojidexEmojiData>();

        public ListAdapter(Context context, int resource, List<EmojidexEmojiData> objects) {
            super(context, resource, objects);

            inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
            layout = resource;
            list = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null)
                view = this.inflater.inflate(this.layout, null);

            // set icon & code
            ImageView icon = (ImageView)view.findViewById(R.id.application_list_icon);
            icon.setImageDrawable(list.get(position).getIcon());
            TextView textView = (TextView)view.findViewById(R.id.application_list_name);
            textView.setText(list.get(position).getName());
            return view;
        }
    }
}
