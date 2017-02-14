package com.example.marcus.logixs;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    final static private String APP_KEY = "INSERT_APP_KEY";
    final static private String APP_SECRET = "INSERT_APP_SECRET";
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private ArrayList<MyImage> images;
    private List dbList;
    private ImageAdapter imageAdapter;
    private ListView listView;
    private EditText printList;
    private Uri mCapturedImageURI;
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Dropbox
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        // Construct the data source
        images = new ArrayList();
        // Create the adapter to convert the array to views
        imageAdapter = new ImageAdapter(this, images);
        // Attach the adapter to a ListView
        listView = (ListView) findViewById(R.id.main_list_view);
        listView.setAdapter(imageAdapter);
        // Path List for Dropbox
        dbList = Collections.synchronizedList(new ArrayList());
    }

    public void btnAddOnClick(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_box);
        dialog.setTitle("Decide");
        Button btnExit = (Button) dialog.findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.btnChoosePath).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                activeGallery();
            }
        });
        dialog.findViewById(R.id.btnTakePhoto).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                activeTakePhoto();
            }
        });

        // show dialog on screen
        dialog.show();
    }

    // Edit Buttons EventListener
    public void btnEditOnClick(View view){
        final Dialog Editdialog = new Dialog(this);
        Editdialog.setContentView(R.layout.custom_edit_box);
        Editdialog.setTitle("Decide");
        Button btnExit1 = (Button) Editdialog.findViewById(R.id.btnExit1);
        btnExit1.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Editdialog.dismiss();
            }
        });
        Editdialog.findViewById(R.id.btnDel0).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                editDbList(0);
            }});
        Editdialog.findViewById(R.id.btnDel1).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                editDbList(1);
            }});
        Editdialog.findViewById(R.id.btnDel2).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                editDbList(2);
            }});
        Editdialog.findViewById(R.id.btnDel3).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                editDbList(3);
            }});
        Editdialog.findViewById(R.id.btnDel3).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                editDbList(3);
            }});
        Editdialog.findViewById(R.id.btnDel4).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                int lastItem = dbList.size()-1;
                editDbList(lastItem);
            }});
        Editdialog.findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                clearDbList();
                Editdialog.dismiss();
            }});
        Editdialog.show();
    }

    // Dev Console - Testing List
    public void btnPrintOnClick(View view){
        final Dialog printDialog = new Dialog(this);
        printDialog.setContentView(R.layout.custom_print_box);
        printDialog.setTitle("Current Dropbox List");
        Button btnExit2 = (Button) printDialog.findViewById(R.id.btnExit2);
        btnExit2.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                printDialog.dismiss();
            }
        });
        printList = (EditText) view.findViewById(R.id.textPrintList);
        printList.setText("blblblb", EditText.BufferType.EDITABLE); // CRASH !!!

        printDialog.show();
    }

    /**
     * take a photo
     */
    private void activeTakePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            String fileName = "temp.jpg";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, fileName);
            mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    /**
     * to gallery
     */
    private void activeGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_LOAD_IMAGE:
                if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();
                    MyImage image = new MyImage();
                    image.setTitle("Number");
                    image.setDescription("Ganz ruhig Saschi, is nur n Test");
                    image.setDatetime(System.currentTimeMillis());
                    image.setPath(picturePath);
                    if(dbList.size() <= 5){
                    dbList.add(picturePath);}
                    images.add(image);
                }
            case REQUEST_IMAGE_CAPTURE:
                if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor = managedQuery(mCapturedImageURI, projection, null, null, null);
                    int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String picturePath = cursor.getString(column_index_data);
                    MyImage image = new MyImage();
                    image.setTitle("Number");
                    image.setDescription("Ganz ruhig Saschi, is nur n Test");
                    image.setDatetime(System.currentTimeMillis());
                    image.setPath(picturePath);
                    if(dbList.size() <= 5){
                    dbList.add(picturePath);}
                    images.add(image);
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Edit ImageArray/dbList - Delete all / index
    public void editDbList(int ArrayIndex){
        if(dbList != null)
        dbList.remove(ArrayIndex);
        //images.remove(ArrayIndex); Zwecks Renderer nicht so einfach zu lösen
    }

    // Clear dbList after Upload
    public void clearDbList(){
        dbList.clear();
    }

    // Dropbox Auth start
    public void getSession(){
        mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
    }
    // Dropbox comeback after db App started
    protected void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }
    // Dropbox Upload triggered by Button
    public void uploadImages(ArrayList List) throws DropboxException, FileNotFoundException {
        if(List.size() <= 5) {
            while (List.iterator().hasNext()) {
                String filePath = (String) List.iterator().next();
                File file = new File(filePath);
                FileInputStream inputStream = new FileInputStream(file);
                DropboxAPI.Entry response = mDBApi.putFile("/magnum-opus.txt", inputStream,
                        file.length(), null, null);
                Log.i("DbExampleLog", "The uploaded file's rev is: " + response.rev);
            }
        }
    }
}
