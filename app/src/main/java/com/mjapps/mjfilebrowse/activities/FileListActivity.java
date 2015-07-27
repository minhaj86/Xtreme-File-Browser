package com.mjapps.mjfilebrowse.activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mjapps.mjfilebrowse.R;
import com.mjapps.mjfilebrowse.util.ArrayAdapterWithIcon;
import com.mjapps.mjfilebrowse.util.BasicFileUtil;
import com.mjapps.mjfilebrowse.util.CacheCleaner;
import com.mjapps.mjfilebrowse.util.FileListAdapter;
import com.mjapps.mjfilebrowse.util.ZipUtils;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.comparator.SizeFileComparator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
* Lists all the files and performs main activities
* @author Kishan Kumar ganguly
* */
public class FileListActivity extends ActionBarActivity implements OnItemClickListener, android.support.v7.widget.ActionMenuView.OnMenuItemClickListener, OnItemLongClickListener, OnItemSelectedListener {
    private final Map<String, Integer> icons = new HashMap<String, Integer>() {
        {
            put("Facebook", R.mipmap.ic_facebook);
            put("Twitter", R.mipmap.ic_twitter);
            put("Gmail", R.mipmap.ic_gmail);
            put("Dropbox", R.mipmap.ic_dropbox);
            put("Google Drive", R.mipmap.ic_google);
            put("Skydrive", R.mipmap.ic_sky);
            put("Email", R.mipmap.ic_email);
        }

    };
    //variables
    private final int DELAY = 1000;
    private final Map<String, String> socialMediaMap = new HashMap<String, String>() {
        {
            put("com.facebook.katana", "Facebook");
            put("com.twitter.android", "Twitter");
            put("com.google.android.gm", "Gmail");
            put("com.dropbox.android", "Dropbox");
            put("com.google.android.apps.docs", "Google Drive");
            put("com.microsoft.skydrive", "Skydrive");
            put("com.android.email", "Email");
        }
    };
    private String path = "/mnt/";
    private List<String> currentList = new ArrayList<String>();
    private boolean doubleBackToExitPressedOnce;
    private Menu menu = null;
    private FileListAdapter adapter;
    private List<String> dropdownListItemNames;
    private Map<String, String> fileNameToPath = new HashMap<>();
    private Spinner storageDirectory;
    private String musicFormats[] = {"mp3", "wav", "wmv", "m4a","amr"};
    private String videoFormats[] = {"mp4", "flv", "FLV", "3gp"};
    private Map<String, Integer> fileIconMap = new HashMap<String, Integer>();
    private List<String> selectedPaths = new ArrayList<>();
    private boolean isClipboardSelected;
    private ListView listView;
    private List<String> packageList = new ArrayList<>();
    private String imageFormats[] = {"jpg", "JPG", "jpeg", "JPEG", "png", "PNG", "gif", "GIF","ogg","bmp"};
    private int copied = 0;
    private boolean searchDone;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        setUpNavigation();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.widget.ActionMenuView toolbarBottom = (android.support.v7.widget.ActionMenuView) findViewById(R.id.toolbar_bottom);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_file_list, toolbarBottom.getMenu());
        toolbarBottom.setOnMenuItemClickListener(this);
        menu = toolbarBottom.getMenu();
        listView = (ListView) findViewById(R.id.fileList);
        adapter = new FileListAdapter(this, currentList, fileIconMap, listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemLongClickListener(this);

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
                    if (Arrays.asList(musicFormats).contains(ext)) {
                        fileIconMap.put(file, R.mipmap.filetype_music);
                    } else if (Arrays.asList(videoFormats).contains(ext)) {
                        fileIconMap.put(file, R.mipmap.filetype_video);
                    } else if (new File(path + "/" + file).isDirectory()) {
                        fileIconMap.put(file, R.mipmap.filetype_dir);
                    } else if (Arrays.asList(imageFormats).contains(ext)) {
                        fileIconMap.put(file, R.mipmap.filetype_image);
                    } else if (ext.equals("apk")) {
                        fileIconMap.put(file, R.mipmap.filetype_apk);
                    } else if (ext.equals("zip")||ext.equals("rar")) {
                        fileIconMap.put(file, R.mipmap.filetype_zip);
                    } else if (ext.equals("pptx") || ext.equals("ppt")) {
                        fileIconMap.put(file, R.mipmap.filetype_ppt);
                    } else if (ext.equals("doc") || ext.equals("docx")) {
                        fileIconMap.put(file, R.mipmap.filetype_word);
                    }else if (ext.equals("htm") || ext.equals("html")) {
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
        if (dropdownListItemNames.get(position).equals("Results")) {

        } else {
            if (searchDone) {

                dropdownListItemNames.remove(dropdownListItemNames.size() - 2);
                setUpNavigationAdapter();
                position -= 1;

                searchDone = false;
                storageDirectory.setSelection(position);
            }
            resetLayout();
            String fileName = dropdownListItemNames.get(position);
            System.out.println(fileName + " poooo");
            path = fileNameToPath.get(fileName);
            TextView rootPath = (TextView) findViewById(R.id.rootPath);

            String root = new File(path).getParentFile().getName();
            if (root.equals("mnt")) {
                rootPath.setText("/");
            } else {
                rootPath.setText(root);
            }
            loadFolders();
            adapter.update(currentList, fileIconMap);

        }
    }

    private void navigateBack() {
        String name = new File(path).getParentFile().getName();

        int i = 0;
        for (String listItem : dropdownListItemNames) {
            if (listItem.equals(name)) {
                storageDirectory.setSelection(i);
                break;
            }
            i++;
        }
    }

    public void selectRoot(View view) {
        navigateBack();
    }

    private String setUpFilePath(String paramFileName) {
        String filePath;
        if (path.endsWith(File.separator)) {
            filePath = path + paramFileName;
        } else {
            filePath = path + File.separator + paramFileName;
        }
        return filePath;
    }

    //gets the selected paths
    public void populateSelectedFilePath(int position) {
        String fileName = currentList.get(position);
        String filePath = setUpFilePath(fileName);
        selectedPaths.add(filePath);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {


        String fileName = (String) currentList.get(position);
        String filePath = setUpFilePath(fileName);
        System.out.println(filePath + " KI");
        System.out.println(filePath);
        if (new File(filePath).isDirectory()) {
            dropdownListItemNames.add(fileName);
            fileNameToPath.put(fileName, filePath);
            setUpNavigationAdapter();
            storageDirectory.setSelection(dropdownListItemNames.size() - 1);

        } else {
            openFile(new File(filePath));
        }

    }

    private void openFile(File fileUrl) {
        // Create URI
        Uri uri = Uri.fromFile(fileUrl);
        String url = fileUrl.toString();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (url.contains(".doc") || url.contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if (url.contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if (url.contains(".apk")) {
            // PDF file
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else if (url.contains(".htm")||url.contains(".html")) {
            // PDF file
            intent.setDataAndType(uri, "text/html");
        }
        else if (url.contains(".ppt")||url.contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (url.contains(".xls")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (url.contains(".zip")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/zip");
        }else if(url.contains(".rar")){
            intent.setDataAndType(uri, "application/x-rar-compressed");
        }
        else if(url.contains(".gz")){
            intent.setDataAndType(uri, "application/gzip");
        }
        else if (url.contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (url.contains(".wav")){
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        }else if(url.contains(".mp3")){
            intent.setDataAndType(uri, "audio/mp3");
        }else if(url.contains(".mid")){
            intent.setDataAndType(uri, "audio/mid");
        }else if(url.contains(".midi")){
            intent.setDataAndType(uri, "audio/midi");
        }else if(url.contains(".ogg")){
            intent.setDataAndType(uri, "audio/x-ogg");
        }else if(url.contains(".amr")){
            intent.setDataAndType(uri, "audio/AMR");
        }else if (url.contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (url.contains(".jpg") || url.contains(".jpeg")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        }
        else if (url.contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/png");
        }else if (url.contains(".bmp")) {
            // JPG file
            intent.setDataAndType(uri, "image/bmp");
        }
        else if (url.contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");

        } else if (url.contains(".3gp") || url.contains(".mpg") || url.contains(".mpeg") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        }

        else {
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                   int pos, long id) {
        selectedPaths.clear();
        triggerLayout();
        isClipboardSelected = false;
        /*getActionBar().setIcon(R.mipmap.ic_action_accept);
        getActionBar().setTitle("");*/
        return true;
    }

    private void triggerLayout() {
        menu.findItem(R.id.settings).setVisible(true);
        menu.findItem(R.id.clipboard).setVisible(true);
        adapter.update(true);
    }

    private void resetLayout() {
        menu.findItem(R.id.settings).setVisible(false);
        if (!isClipboardSelected) {
            menu.findItem(R.id.clipboard).setVisible(false);
        }
        /*getActionBar().setIcon(R.mipmap.ic_launcher);
        getActionBar().setTitle(getResources().getString(R.string.app_name));*/
        adapter.update(false);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        if (path.substring(5, path.length() - 1).equals(dropdownListItemNames.get(0))) {
            this.doubleBackToExitPressedOnce = true;
            String msg = "Please click BACK again to exit";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
        if (searchDone) {
            dropdownListItemNames.remove(dropdownListItemNames.size() - 1);
            setUpNavigationAdapter();
            //storageDirectory.setSelection(dropdownListItemNames.size() - 1);
            navigateBack();
            searchDone = false;
        } else if (!menu.findItem(R.id.settings).isVisible()) {

            navigateBack();
        }
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, DELAY);
        resetLayout();


    }

    private void askForCreateSelection() {
        final CharSequence[] items = {
                "Folder", "File"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection
                try {
                    if (item == 0) {
                        askForFileName(path, 0);

                    } else {
                       /* new BasicFileUtil(path).createNewFile();*/
                        askForFileName(path, 1);

                    }
                } catch (Exception io) {
                    io.printStackTrace();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void askForFileName(final String path, final int itemNo) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText edittext = new EditText(this);
        alert.setMessage("Enter name");
        alert.setTitle("Create");

        alert.setView(edittext);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                String name = path + File.separator + edittext.getText().toString();
                try {
                    if (itemNo == 0) {
                        new BasicFileUtil(name).createDirectory();
                        viewUpdate();
                    } else {
                        new BasicFileUtil(name).createNewFile();
                        viewUpdate();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        alert.show();
    }

    private void askForRename(final String path) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText edittext = new EditText(this);
        alert.setMessage("Enter New name");
        alert.setTitle("Rename");

        alert.setView(edittext);
        edittext.setText(getFileName(path));
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                String name = edittext.getText().toString();
                new BasicFileUtil(path).rename(name);
                viewUpdate();
            }
        });
        alert.show();
    }

    private void askForSortType() {
        final CharSequence[] items = {
                "Name", "Date", "Size"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort By");
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sortByName();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection
                if (item == 0) {
                    sortByName();
                    dialog.dismiss();
                } else if (item == 1) {
                    sortByDate();
                    dialog.dismiss();
                } else if (item == 2) {
                    sortBySize();
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void viewUpdate() {
        resetLayout();
        loadFolders();
        adapter.update(currentList, fileIconMap);
    }

    private String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1, path.length());
    }

    private void sortByName() {
        Collections.sort(currentList, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.toLowerCase().compareTo(rhs.toLowerCase());
            }
        });
        adapter.update(currentList, fileIconMap);
        resetLayout();
    }

    private void sortBySize() {
        File file = new File(path);
        File[] files = file.listFiles();
        Arrays.sort(files, SizeFileComparator.SIZE_COMPARATOR);
        int i = 0;
        for (File filename : files) {
            if (!filename.getName().startsWith(".")) {
                currentList.set(i, filename.getName());
                i++;
            }
        }
        adapter.update(currentList, fileIconMap);
        resetLayout();
    }

    private void sortByDate() {
        File file = new File(path);
        File[] files = file.listFiles();
        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
        int i = 0;
        for (File filename : files) {
            if (!filename.getName().startsWith(".")) {
                currentList.set(i, filename.getName());
                i++;
            }
        }
        adapter.update(currentList, fileIconMap);
        resetLayout();
    }

    private void askForSelection() {
        final CharSequence[] items = {
                "copy", "cut", "delete"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make your selection");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection
                try {
                    if (item == 0) {

                        menu.findItem(R.id.settings).setVisible(false);
                        adapter.update(false);
                        copied = 1;
                        String msg = "Item copied to clipboard";
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                        isClipboardSelected = true;
                        resetLayout();

                    } else if (item == 1) {
                        menu.findItem(R.id.settings).setVisible(false);
                        adapter.update(false);
                        copied = 2;
                        String msg = "Item copied to clipboard";
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                        isClipboardSelected = true;
                        resetLayout();

                    } else {
                        for (String paths : selectedPaths) {
                            new BasicFileUtil(paths).deleteFile();
                            menu.findItem(R.id.settings).setVisible(false);
                            adapter.update(false);
                            viewUpdate();
                            resetLayout();
                        }
                    }
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clipboard) {
            if (isClipboardSelected) {
                try {
                    for (String paths : selectedPaths) {
                        if (copied == 1) {
                            new BasicFileUtil(paths, path + "/").copyFile();
                        } else if (copied == 2) {
                            new BasicFileUtil(paths, path + "/").moveFile();
                        }
                        viewUpdate();


                    }

                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Cannot paste as item already exists", Toast.LENGTH_SHORT).show();
                }
            } else {
                askForSelection();


            }
        }
        if (id == R.id.sort) {
            askForSortType();
        }
        if (id == R.id.compress) {
            for (String paths : selectedPaths) {
                try {
                    System.out.println(paths);
                    resetLayout();
                    new ZipUtils(paths).compress();
                    viewUpdate();
                } catch (net.lingala.zip4j.exception.ZipException e) {
                    e.printStackTrace();
                }
            }

        }
        if (id == R.id.decompress) {
            for (String paths : selectedPaths) {
                try {
                    new ZipUtils(paths).decompress();
                    resetLayout();
                    viewUpdate();
                } catch (net.lingala.zip4j.exception.ZipException e) {
                    e.printStackTrace();
                }
            }
        }
        if (id == R.id.rename) {
            if (selectedPaths.size() == 1) {
                String fileToRename = selectedPaths.get(0);
                askForRename(fileToRename);

            } else {
                for (String paths : selectedPaths) {
                    askForRename(paths);

                }
            }
        }
        if (id == R.id.clear_cache) {
            CacheCleaner mCacheCleaner = new CacheCleaner();
            mCacheCleaner.trimCache(getApplicationContext());
            mCacheCleaner.killCachedProcesses(getApplicationContext());
            resetLayout();
            viewUpdate();
        }
        if (id == R.id.search) {
            buildSearchAlertDialog();

        }
        if (id == R.id.create) {
            askForCreateSelection();
            viewUpdate();

        }
        if (id == R.id.share) {
            populatePackagesForSocialMedia();
            share();
        }
        if (id == R.id.share_via_bluetooth) {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

            if (btAdapter == null) {
                // Device does not support Bluetooth
                // Inform user that we're done.
                String msg = "Your device does not support bluetooth";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            } else {
                ArrayList<Uri> uris = new ArrayList<Uri>();
                int length = selectedPaths.size();
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_SEND_MULTIPLE);
                intent.setType("*/*");
                for (int i = 0; i < length; i++) {
                    File file = new File(selectedPaths.get(i));
                    uris.add(Uri.fromFile(file));
                }
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                getBluetoothPackage(intent);
                startActivity(intent);
            }
        }

        return super.onOptionsItemSelected(item);
    }


    private void populatePackagesForSocialMedia() {
        List<ApplicationInfo> packages;
        PackageManager pm;

        pm = getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (socialMediaMap.containsKey(packageInfo.packageName)) {
                packageList.add(packageInfo.packageName);
            }
        }
    }

    private void share() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        List<String> pkg = new ArrayList<>();
        List<Integer> iconList = new ArrayList<>();
        for (String pkgs : packageList) {
            pkg.add(socialMediaMap.get(pkgs));
            iconList.add(icons.get(socialMediaMap.get(pkgs)));
        }
        String[] pkgs = new String[pkg.size()];
        Integer[] icons = new Integer[iconList.size()];
        final String[] packageArray = pkg.toArray(pkgs);
        final Integer[] iconArray = iconList.toArray(icons);
        ListAdapter adapter = new ArrayAdapterWithIcon(FileListActivity.this, packageArray, iconArray);
        builder.setTitle("Share Via");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection

                final Intent emailIntent = new Intent(
                        Intent.ACTION_SEND_MULTIPLE);
                emailIntent.setType("*/*");
                for (Map.Entry<String, String> entry : socialMediaMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (value.equals(packageArray[item])) {
                        emailIntent.setPackage(key);
                        break;
                    }
                }
                ArrayList<Uri> uris = new ArrayList<Uri>();
                //convert from paths to Android friendly Parcelable Uri's

                for (int i = 0; i < selectedPaths.size(); i++) {
                    File fileIn = new File(selectedPaths.get(i));
                    Uri u = Uri.fromFile(fileIn);
                    uris.add(u);
                }
                if(emailIntent!=null) {
                    emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                    if(getApplicationContext().getPackageManager().queryIntentActivities(emailIntent,PackageManager.MATCH_DEFAULT_ONLY).size()>0){
                        startActivity(emailIntent);
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"An error occurred.Please check your internet connection or app status",Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });
        AlertDialog alert = builder.create();
        alert.show();
        packageList.clear();

    }

    private void getBluetoothPackage(Intent intent) {
        //select bluetooth
        PackageManager pm = getPackageManager();
        List<ResolveInfo> appsList = pm.queryIntentActivities(intent, 0);
        if (appsList.size() > 0) {
            String packageName = null;
            String className = null;
            for (ResolveInfo info : appsList) {
                packageName = info.activityInfo.packageName;
                if (packageName.equals("com.android.bluetooth")) {
                    className = info.activityInfo.name;
                    intent.setClassName(packageName, className);
                    break;// found
                }
            }
        }
    }

    private void buildSearchAlertDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText edittext = new EditText(this);
        alert.setMessage("Enter keyword");
        alert.setTitle("Search");
        alert.setView(edittext);

        alert.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //What ever you want to do with the value
                String text = edittext.getText().toString();
                search(text);
                searchDone = true;
                dropdownListItemNames.add("Results");
                setUpNavigationAdapter();
                TextView root = (TextView) findViewById(R.id.rootPath);
                root.setText("");
                storageDirectory.setSelection(dropdownListItemNames.size() - 1);
            }
        });
        alert.show();
    }

    /*    private String[] listFiles(List<String>fileNames,File root){

            if (!root.canRead()) {
                setTitle(getTitle() + " (inaccessible)");
            }
            //System.out.println(new File("/hello/world").getName());
            File[] list = root.listFiles();
            for(File file:list){
                if(file.isDirectory()){
                    listFiles(fileNames,file);
                }
                fileNames.add(file.getPath());

            }
            for(String s:fileNames){
                System.out.println("yyyyyyyyy "+s);
            }
            return  (String[])fileNames.toArray();
        }*/
    private void search(String searchText) {
        currentList.clear();
        fileIconMap.clear();
        File dir = new File(path);
        List<String> nam = new ArrayList<>();
        //listFiles(nam,dir);
        if (!dir.canRead()) {
            setTitle(getTitle() + " (inaccessible)");
        }
        System.out.println(new File("/hello/world").getName());
        String[] list = dir.list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".") && file.contains(searchText)) {
                    currentList.add(file);
                    String ext = getExtension(setUpFilePath(file));
                    if (Arrays.asList(musicFormats).contains(ext)) {
                        fileIconMap.put(file, R.mipmap.filetype_music);
                    } else if (Arrays.asList(videoFormats).contains(ext)) {
                        fileIconMap.put(file, R.mipmap.filetype_video);
                    } else if (new File(path + "/" + file).isDirectory()) {
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
            adapter.update(currentList, fileIconMap);
        }


    }

    public void removeSelectedFilePath(int position) {
        String fileName = currentList.get(position);
        String filePath = setUpFilePath(fileName);
        Iterator<String> it = selectedPaths.iterator();
        while (it.hasNext()) {
            String item = it.next();
            if (item.equals(filePath)) {
                it.remove();
            }
        }
    }
}
