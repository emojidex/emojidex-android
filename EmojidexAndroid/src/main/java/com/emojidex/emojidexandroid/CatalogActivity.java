package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

public class CatalogActivity extends Activity
{
    private GridView gridView;

    private Emojidex emojidex;
    private List<Emoji> currentCatalog;
    private String currentCategory;

    private SaveDataManager historyManager;
    private SaveDataManager searchManager;

    private boolean showResult = false;
    private RadioButton searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        setTheme(R.style.IMETheme);

        initData();
        initGridView();
        initCategory();
    }

    private void initData()
    {
        emojidex = Emojidex.getInstance();
        emojidex.initialize(this);

        currentCatalog = emojidex.getAllEmojiList();
        currentCategory = getString(R.string.ime_category_id_all);

        historyManager = new SaveDataManager(this, SaveDataManager.Type.CatalogHistory);
        historyManager.load();
        searchManager = new SaveDataManager(this, SaveDataManager.Type.CatalogSearch);
        searchManager.load();

        searchButton = (RadioButton)findViewById(R.id.catalog_search);
    }

    private void initGridView()
    {
        gridView = (GridView)findViewById(R.id.grid_view);
        gridView.setAdapter(new CatalogAdapter(this, currentCatalog));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Emoji emoji = currentCatalog.get(position);
                sendEmoji(emoji);
            }
        });
    }

    private void initCategory()
    {
        CategoryManager categoryManager = CategoryManager.getInstance();
        categoryManager.initialize(this);

        RadioGroup group = (RadioGroup)findViewById(R.id.grid_button);

        for(int i = 0; i < categoryManager.getCategoryCount(); i++)
        {
            RadioButton newButton = new RadioButton(this);

            newButton.setText(categoryManager.getCategoryText(i));
            newButton.setContentDescription(categoryManager.getCategoryId(i));
            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickCategoryButton(v);
                }
            });

            group.addView(newButton);
        }
    }

    public void onClickCategoryButton(View v)
    {
        changeCategory(v.getContentDescription().toString());
    }

    private void changeCategory(String categoryName)
    {
        if (categoryName.equals(currentCategory)) return;

        currentCategory = categoryName;

        if (categoryName.equals(getString(R.string.ime_category_id_history)))
        {
            List<String> emojiNames = historyManager.getEmojiNames();
            currentCatalog = createEmojiList(emojiNames);
            gridView.setAdapter(new CatalogAdapter(this, currentCatalog));
        }
        else if (categoryName.equals(getString(R.string.ime_category_id_search)))
        {
            List<String> emojiNames = searchManager.getEmojiNames();
            currentCatalog = createEmojiList(emojiNames);
            gridView.setAdapter(new CatalogAdapter(this, currentCatalog));
        }
        else if (categoryName.equals(getString(R.string.ime_category_id_all)))
        {
            currentCatalog = emojidex.getAllEmojiList();
            gridView.setAdapter(new CatalogAdapter(this, currentCatalog));
        }
        else
        {
            currentCatalog = emojidex.getEmojiList(categoryName);
            gridView.setAdapter(new CatalogAdapter(this, currentCatalog));
        }
    }

    public void onClickSearchButton(View view)
    {
        showResult = true;

        Intent intent = new Intent(CatalogActivity.this, SearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("Catalog", true);
        startActivity(intent);
    }

    private void sendEmoji(Emoji emoji)
    {
        historyManager.addFirst(emoji.getName());

        // TODO: シールを送る
//        Intent intent = new Intent(this, SendSealActivity.class);
//        intent.putExtra(Intent.EXTRA_TEXT, emoji.getName());
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
    }

    private ArrayList<Emoji> createEmojiList(List<String> emojiNames)
    {
        emojidex = Emojidex.getInstance();

        ArrayList<Emoji> emojies = new ArrayList<>(emojiNames.size());
        for (String emojiName : emojiNames)
        {
            emojies.add(emojidex.getEmoji(emojiName));
        }

        return emojies;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        historyManager.save();
        searchManager.save();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (showResult)
        {
            showResult = false;
            searchManager.load();
            searchButton.performClick();
        }
    }
}
