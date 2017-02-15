package com.example.marcus.logixs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    final static private String APP_KEY = "4ascir3u867s00m";
    final static private String APP_SECRET = "hw0iu6nunu44gvc";
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private ArrayList<MyImage> images;
    private List dbList;
    private ImageAdapter imageAdapter;
    private ListView listView;
    private TextView printList;
    private Uri mCapturedImageURI;
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private MainActivity context;
    private String[] pathArray;
    private int imageCounter;
    public String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Test Context
        context = this;
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


    // START BTN LISTENER

    // Add Image Button Listener
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
                //TODO: REFACTOR - ITS CRASHING IF INDEX IS 0 FOR EXAMPLE
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

    // DROPBOX Upload Button Event Listener
    public void btnUploadOnClick(View view) {

        pathArray = new String[dbList.size()];

        for(int y = 0; y < dbList.size(); y++) {
            pathArray[y] = (String) dbList.iterator().next();
        }
        Log.i("Content", "" + pathArray[0]);
        Log.i("Content", "" + pathArray[1]);

        new UploadFile().execute(pathArray);
    }

    // DROPBOX AUTH Button Listener
    public void btnStartAuth(View view){
        mDBApi.getSession().startOAuth2Authentication(MainActivity.this);
    }


    // END BTN LISTENER

    // take a photo
    private void activeTakePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Date date = Calendar.getInstance().getTime();
            DateFormat formatter = new SimpleDateFormat("ddMMyyyyHH:mm");
            String today = formatter.format(date);
            fileName = today + ".jpg";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, fileName);
            mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

     // to gallery - Load Image
    private void activeGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    // ResultTask Load Image | Take Image
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
                    //TODO: TESTEN!
                    MyImage image = new MyImage();
                    image.setTitle("" + imageCounter);
                    imageCounter++;
                    image.setDescription("Ganz ruhig Saschi, is nur n Test");
                    image.setDatetime(System.currentTimeMillis());
                    image.setPath(picturePath);
                    dbList.add(picturePath);
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
                    image.setTitle("" + imageCounter);
                    imageCounter++;
                    image.setDescription("Ganz ruhig Saschi, is nur n Test");
                    image.setDatetime(System.currentTimeMillis());
                    image.setPath(picturePath);
                    dbList.add(picturePath);
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
    // TODO: Index Delete | ViewHolder Image Delete etc.
    public void editDbList(int ArrayIndex){
        if(dbList != null)
            dbList.remove(ArrayIndex);
        // TODO: images.remove(ArrayIndex); Zwecks Renderer nicht so einfach zu lösen
    }

    // ----------------- Some HelpFunctions --------------------------
    // Clear dbList after Upload
    public void clearDbList(){
        dbList.clear();
    }

    // ------------------------ END ----------------------------------

    // DROPBOX Resume Function after Auth. Comeback
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

    // ################ Dropbox Async + Background Upload #####################
    private class UploadFile extends AsyncTask<String, Long, Boolean> {

        private long mFileLen;
        private ProgressDialog mDialog;
        private int flag;
        private String mErrorMsg;
        private String appDirectoryName = "LogiXS/LogiPiXS/";
        //final static private String ACCOUNT_PREFS_NAME = "prefs";

        public UploadFile() {
            flag = 2;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(context);
            mDialog.setMax(dbList.size());
            mDialog.setMessage("Uploading...");
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDialog.setCancelable(false);
            mDialog.setProgress(0);
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String mFilePath;

                for (int j = 0; j < params.length; j++) {
                    Log.i("Params Content", "" + params[0]);
                    Log.i("Params Content", "" + params[1]);

                    FileInputStream fis;
                    int imgUploadCounter = j + 1;
                    mFilePath = params[j];
                    publishProgress(Long.parseLong("" + j));
                    File mFile = new File(mFilePath);
                    fis = new FileInputStream(mFile);
                    DropboxAPI.Entry response = mDBApi.putFile(appDirectoryName + imgUploadCounter + "_" + fileName, fis, mFile.length(), null, null);
                    Log.i("Number of Upload", "" + j);
                }
                imageCounter = 0;
                clearDbList();
                return true;

            } catch (DropboxUnlinkedException e) {
                // This session wasn't authenticated properly or user unlinked
                mErrorMsg = "This app wasn't authenticated properly.";
            } catch (DropboxFileSizeException e) {
                // File size too big to upload via the API
                mErrorMsg = "This file is too big to upload";
            } catch (DropboxPartialFileException e) {
                // We canceled the operation
                mErrorMsg = "Upload canceled";
            } catch (DropboxServerException e) {
                // Server-side exception. These are examples of what could happen,
                // but we don't do anything special with them here.
                mErrorMsg = "1";
                if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                    // Unauthorized, so we should unlink them. You may want to
                    // automatically log the user out in this case.
                    mErrorMsg = "2";
                } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                    // Not allowed to access this
                    mErrorMsg = "3";
                } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                    // path not found (or if it was the thumbnail, can't be
                    // thumbnailed)
                    mErrorMsg = "4";
                } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                    // user is over quota
                    mErrorMsg = "5";
                } else {
                    // Something else
                    mErrorMsg = "6";
                }
                // This gets the Dropbox error, translated into the user's language
                mErrorMsg = e.body.userError;
                if (mErrorMsg == null) {
                    mErrorMsg = e.body.error;
                }
            } catch (DropboxIOException e) {
                e.printStackTrace();
                // Happens all the time, probably want to retry automatically.
                mErrorMsg = "Network error.  Try again.";
            } catch (DropboxParseException e) {
                // Probably due to Dropbox server restarting, should retry
                mErrorMsg = "Dropbox error.  Try again.";
            } catch (DropboxException e) {
                // Unknown error
                mErrorMsg = "Unknown error.  Try again.";
            } catch (FileNotFoundException e) {
                mErrorMsg = "7";
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(Long... progress) {
            if (flag == 1) {
                int percent = (int) (100.0 * (double) progress[0] / mFileLen + 0.5);
                mDialog.setProgress(percent);
            } else if (flag == 2) {
                mDialog.setProgress(Integer.parseInt("" + progress[0]));
                super.onProgressUpdate(progress);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mDialog.dismiss();
            if (result) {
                Toast.makeText(context,"Successfully uploaded", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context,mErrorMsg,Toast.LENGTH_SHORT).show();
            }
        }
    }
}