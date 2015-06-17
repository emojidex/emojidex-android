package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.List;

public class CatalogActivity extends Activity
{
    static final String TAG = MainActivity.TAG + ":Catalog";

    private GridView gridView;

    private Emojidex emojidex;
    private List<Emoji> currentCatalog;
    private String currentCategory;

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
    }

    private void initGridView()
    {
        gridView = (GridView)findViewById(R.id.grid_view);
        gridView.setAdapter(new CatalogAdapter(this, currentCatalog));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO: タップした時の動作(Toastは削除)
                Emoji emoji = currentCatalog.get(position);
                Toast.makeText(CatalogActivity.this, emoji.name, Toast.LENGTH_SHORT).show();
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

    private void changeCategory(String categoryName) {
        if (categoryName.equals(currentCategory)) return;

        currentCategory = categoryName;

        if (categoryName.equals(getString(R.string.ime_category_id_history)))
        {
            // TODO: 履歴
        }
        else if (categoryName.equals(getString(R.string.ime_category_id_search)))
        {
            // TODO: 検索
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
        // TODO: 検索した後の処理
        Intent intent = new Intent(CatalogActivity.this, SearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
