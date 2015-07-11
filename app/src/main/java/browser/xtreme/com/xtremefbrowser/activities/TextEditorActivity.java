package browser.xtreme.com.xtremefbrowser.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import browser.xtreme.com.xtremefbrowser.R;

/*
* Defines a text editor for text files
* */
public class TextEditorActivity extends ActionBarActivity {
    private String filePath;
    private EditText edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        edit = (EditText) findViewById(R.id.edit);

        Intent intent = getIntent();
        Uri data = intent.getData();
        filePath = data.getPath();
        getSupportActionBar().setTitle(FileUtils.getFile(filePath).getName());
        try {
            showFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String readFile() throws IOException {
        File file = new File(filePath);
        return FileUtils.readFileToString(file);

    }

    private void showFile() throws IOException {
        String contents = readFile();
        edit.setText(contents);
    }

    private void writeToFile(String contents) throws IOException {
        File file = new File(filePath);
        FileUtils.writeStringToFile(file, contents);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_text_editor, menu);
        return true;
    }

    private void showMessage() {
        new AlertDialog.Builder(this)
                .setTitle("Save")
                .setMessage("Do you want to save file?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //save
                        try {
                            writeToFile(edit.getText().toString());
                            TextEditorActivity.this.finish();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        TextEditorActivity.this.finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    @Override
    public void onBackPressed() {
        if (!isContentSame()) {
            showMessage();
        } else {
            super.onBackPressed();
        }
    }

    private boolean isContentSame() {
        try {
            return readFile().equals(edit.getText().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.save) {
            try {
                writeToFile(edit.getText().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
