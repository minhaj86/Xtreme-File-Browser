package com.mjapps.mjfilebrowse.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.mjapps.mjfilebrowse.R;
import com.mjapps.mjfilebrowse.util.FileListAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* Attach files
* @author Kishan Kumar ganguly
* */
public class FileAttachmentActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    private Intent returnedFileIntent;
    private String path = "/mnt/";
    private ArrayList<String> currentList = new ArrayList<String>();
    private FileListAdapter adapter;
    private List<String> dropdownListItemNames;
    private Map<String, String> fileNameToPath = new HashMap<>();
    private Spinner storageDirectory;
    private String musicFormats[] = {"mp3", "wav", "wmv", "m4a"};
    private String videoFormats[] = {"mp4", "flv", "FLV", "3gp"};
    private String imageFormats[] = {"jpg", "JPG", "jpeg", "JPEG", "png", "PNG", "gif", "GIF"};
    private Map<String, Integer> fileIconMap = new HashMap<String, Integer>();
    private ListView listView;
    private Uri fileUri;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_attach);
        setUpNavigation();
        returnedFileIntent =
                new Intent("com.example.myapp.ACTION_RETURN_FILE");
        setResult(Activity.RESULT_CANCELED, null);
        listView = (ListView) findViewById(R.id.fileList);
        adapter = new FileListAdapter(this, currentList, fileIconMap, listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

    }

    private void setUpNavigation() {
        storageDirectory = (Spinner) findViewById(R.id.storageDirectory);
        dropdownListItemNames = getAttachedCards();
        setUpNavigationAdapter();
    }

    private void setUpNavigationAdapter() {
        ArrayAdapter<String> dropdown;
        int layout = android.R.layout.simple_list_item_1;
        dropdown = new ArrayAdapter<>(this, layout, dropdownListItemNames);
        storageDirectory.setAdapter(dropdown);
        storageDirectory.setOnItemSelectedListener(this);

    }

    private String[] loadFolders() {
        currentList.clear();
        fileIconMap.clear();
        setTitle(path);
        File dir = new File(path);
        if (!dir.canRead()) {
            setTitle(getTitle() + " (inaccessible)");
        }
        String[] list = dir.list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    currentList.add(file);
                    String ext = getExtension(setUpFilePath(file));
                    System.out.println(ext);
                    if (Arrays.asList(musicFormats).contains(ext)) {
                        fileIconMap.put(file, R.mipmap.filetype_music);
                    } else if (Arrays.asList(videoFormats).contains(ext)) {
                        fileIconMap.put(file, R.mipmap.filetype_video);
                    } else if (new File(path+"/"+file).isDirectory()) {
                        fileIconMap.put(file, R.mipmap.filetype_dir);
                    } else if (Arrays.asList(imageFormats).contains(ext)) {
                        fileIconMap.put(file, R.mipmap.filetype_image);
                    } else if (ext.equals("apk")) {
                        fileIconMap.put(file, R.mipmap.filetype_apk);
                    } else if (ext.equals("zip")||ext.equals("rar")) {
                        fileIconMap.put(file, R.mipmap.filetype_zip);
                    } else if (ext.equals("htm") || ext.equals("html")) {
                        fileIconMap.put(file, R.mipmap.filetype_html);
                    } else {
                        fileIconMap.put(file, R.mipmap.filetype_generic);
                    }
                }
            }
        }

        String[] arr = new String[currentList.size()];
        return currentList.toArray(arr);

    }

    private String getExtension(String filePath) {
        return filePath.substring(filePath.lastIndexOf(".") + 1);
    }

    private List<String> getAttachedCards() {
        List<String> cards = new ArrayList<String>();
        File dir = new File("/mnt");
        String[] list = dir.list();
        if (list != null) {
            for (String file : list) {
                if (file.contains("sd") || file.contains("Sd")) {
                    cards.add(file);
                    fileNameToPath.put(file, path + file + "/");
                }
            }
        }
        return cards;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        String fileName = dropdownListItemNames.get(position);
        path = fileNameToPath.get(fileName);

        loadFolders();
        adapter.update(currentList, fileIconMap);

    }

    //sets up file path
    private String setUpFilePath(String paramFileName) {
        String filePath;
        if (path.endsWith(File.separator)) {
            filePath = path + paramFileName;
        } else {
            filePath = path + File.separator + paramFileName;
        }
        return filePath;
    }
    //attach files with intents

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {


        String fileName = (String) currentList.get(position);
        String filePath = setUpFilePath(fileName);

        if (new File(filePath).isDirectory()) {
            dropdownListItemNames.add(fileName);
            fileNameToPath.put(fileName, filePath);
            setUpNavigationAdapter();
            storageDirectory.setSelection(dropdownListItemNames.size() - 1);

        } else {
            File file = new File(filePath);
            // Grant temporary read permission to the content URI
            returnedFileIntent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // Put the Uri and MIME type in the result Intent
            returnedFileIntent.setData(Uri.fromFile(file));
            // Set the result
            FileAttachmentActivity.this.setResult(Activity.RESULT_OK,
                    returnedFileIntent);
            Toast.makeText(getApplicationContext(), "Added", Toast.LENGTH_SHORT).show();
            FileAttachmentActivity.this.finish();

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu paramMenu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_attach, paramMenu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.ok) {
            FileAttachmentActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

}

