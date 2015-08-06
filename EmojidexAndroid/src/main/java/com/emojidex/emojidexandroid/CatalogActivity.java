package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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

    private boolean isPick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        setTheme(R.style.IMETheme);

        initData();
        initGridView();
        initCategory();

        // Check intent.
        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();

        if(action.equals(Intent.ACTION_PICK))
        {
            if("image/png".equals(type) || "image/*".equals(type))
            {
                isPick = true;
            }
        }
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
        final SealDownloader downloader = new SealDownloader(this);
        final String emojiName = emoji.getName();

        downloader.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(downloader.isCanceled())
                    return;

                historyManager.addFirst(emojiName);

                final SealGenerator generator = new SealGenerator(CatalogActivity.this);
                generator.generate(emojiName);

                if(generator.useLowQuality())
                {
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(CatalogActivity.this);
                    alertDialog.setMessage(R.string.send_seal_not_found);
                    alertDialog.setPositiveButton(R.string.send_seal_not_found_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            sendIntent(generator.getUri());
                        }
                    });
                    alertDialog.show();

                    return;
                }

                sendIntent(generator.getUri());
            }
        });

        downloader.download(emojiName);
    }

    private void sendIntent(Uri uri)
    {
        if(isPick)
        {
            final Intent intent = new Intent();
            intent.setData(uri);
            setResult(RESULT_OK, intent);
        }
        else
        {
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(intent, null));
        }

        finish();
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
