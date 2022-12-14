package com.example.dailyart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import org.apache.commons.io.FileUtils;
//import android.os.FileUtils;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


// 获取image => https://www.geeksforgeeks.org/how-to-select-an-image-from-gallery-in-android/
// 保存image => https://www.youtube.com/watch?v=oLcxTunwaFk
// 分享图片和评论 => https://www.youtube.com/watch?v=_rBKy4z1yNU
// milestone & gallery 待定
public class Upload extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    // Buttons
    private Button AlbumButton;
    private Button GalleryButton;
    //private Button MileStoneButton;
    private Button ShareButton;
    private Button SaveButton;
    private ProgressBar mypb;
    private SharedPreferences sharedPref;
    private ArrayList<String> userTags;// = new ArrayList<String>(Arrays.asList(new String[]{"General", "Normal", "+ New Tag"}));
    private ArrayList<String> spinnerItems;
    private Switch simpleSwitch;

    // One Preview Image
    ImageView IVPreviewImage;

    // Image Path
    Uri ImageUri;
    String ImagePath;

    // Image Name
    // String ImageName;

    // comment
    TextView Comment;

    // constant to compare
    // the activity result code
    int SELECT_PICTURE = 200;
    int Camera = 300;

//    boolean isMileStone = false;

    String tagPath = "General";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        setContentView(R.layout.activity_upload);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        SharedPreferences sharedPref = getSharedPreferences("CurrentUserID", MODE_PRIVATE);

        sharedPref = getSharedPreferences("CurrentUserID", MODE_PRIVATE);
        String userID = sharedPref.getString("UserID", "");
        userTags = SharedPrefUtil.getStringList(getApplicationContext(),"USER_TAGS_"+userID);
        if(userTags == null){
            userTags = new ArrayList<String>(Arrays.asList(new String[]{"General", "MileStone"}));
            SharedPrefUtil.saveStringList(getApplicationContext(),userTags,"USER_TAGS_"+userID);
        }
        spinnerItems = new ArrayList<String>(userTags);
        spinnerItems.add("+ New Tag");
        spinnerItems.add("- Delete Tag");
        spinnerItems.add(0,"<None>");
        spinnerItems.remove("General");
        spinnerItems.remove("MileStone");
        // register the UI widgets with their appropriate IDs
        AlbumButton = (Button)findViewById(R.id.Album);
        GalleryButton = (Button)findViewById(R.id.Gallery);
        //MileStoneButton = (Button)findViewById(R.id.Milestone);
        ShareButton = (Button)findViewById(R.id.Share);
        SaveButton = (Button)findViewById(R.id.Save);
        IVPreviewImage = (ImageView)findViewById(R.id.IVPreviewImage);
        Comment = (TextView)findViewById(R.id.edit_text);
        simpleSwitch = (Switch)findViewById(R.id.simpleSwitch);

        // handle the Choose Image button to trigger
        // the image chooser function
        AlbumButton.setOnClickListener(this);
        GalleryButton.setOnClickListener(this);
        //MileStoneButton.setOnClickListener(this);
        ShareButton.setOnClickListener(this);
        SaveButton.setOnClickListener(this);

        //Drop down menu
        Spinner spinner = (Spinner) findViewById(R.id.available_tags);

        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,spinnerItems);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(aa);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(this);
    }


    // handle select image
    public void onClick(View v) {
        if (v.getId() == R.id.Album) {
            imageChooser();
        } else if(v.getId() == R.id.Save) {
            
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
            String newImagePath = saveImageToFile(timeStamp);
            String critiquePath = saveCommentToFile(timeStamp);
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Removes other Activities from stack
            startActivity(intent);
        } else if(v.getId() == R.id.Share) {
            share();
        }
//        else if(v.getId() == R.id.Milestone) {
//            isMileStone = !isMileStone;
//
//            SharedPreferences.Editor editor = sharedPref.edit();
//            int galleryCount = sharedPref.getInt("GalleryCount", 0);
//
////            if(isMileStone){
////                MileStoneButton.setBackgroundColor(Color.GRAY);
////                editor.putInt("GalleryCount", galleryCount + 1);
////            }else{
////                MileStoneButton.setBackgroundColor(Color.parseColor("#DDD83B"));
////                editor.putInt("GalleryCount", galleryCount - 1);
////            }
//
//            editor.apply();
//
//        }
        else if(v.getId() == R.id.Gallery) {
            openCamera();
        }
    }

    // this function is triggered when
    // the Select Image Button is clicked
    public void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }
    public void openCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        if(intent.resolveActivity(getPackageManager()) != null){
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,"com.example.dailyart.fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                startActivityForResult(intent,Camera);
            }
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        ImageUri = Uri.fromFile(image);
        ImagePath = image.getAbsolutePath();
        return image;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    IVPreviewImage.setImageURI(selectedImageUri);
                }

                // Process Uri +Path + Name
                ImageUri = selectedImageUri;
                Log.d("IMAGE FROM ALBUM", selectedImageUri.toString());
                ImagePath = getPath(this,ImageUri);
            }
            else if(requestCode == Camera) {
                if(ImageUri!= null){
                    IVPreviewImage.setImageURI(ImageUri);
                }
            }
        }
    }

    private String saveCommentToFile(String timeStamp){
        SharedPreferences sharedPref = getSharedPreferences("CurrentUserID", MODE_PRIVATE);
        String userID = sharedPref.getString("UserID", "");
        if(Comment.getText()==null || Comment.getText().toString()==""){
            Toast.makeText(this,"Input the comment Before Save",Toast.LENGTH_SHORT).show();
            return "";
        }
        try{
            File path = Environment.getExternalStorageDirectory();
            File dir = null;
            String finalDir;
            if(simpleSwitch.isChecked()){
                finalDir = path + "/Daily Art/Files/"+ "user_"+ userID +"/" +"MileStone";
                saveCritiqueFileStream(finalDir,timeStamp);
            }
            finalDir = path + "/Daily Art/Files/" + "user_"+ userID +"/" + this.tagPath;
            if (!this.tagPath.equals("<None>")) {
                saveCritiqueFileStream(finalDir, timeStamp);
            }
            saveCritiqueFileStream(path + "/Daily Art/Files/" + "user_"+ userID +"/" +"General",timeStamp);
            //Toast
            String fileName = timeStamp+ ".jpg";
            Toast.makeText(this,fileName+" File saved in :"+dir,Toast.LENGTH_SHORT).show();
            return finalDir + "/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
            return "";
        }
    }
    private String saveImageToFile(String timeStamp){
        SharedPreferences sharedPref = getSharedPreferences("CurrentUserID", MODE_PRIVATE);
        String userID = sharedPref.getString("UserID", "");
        if(ImagePath==null || ImagePath==""){
            Toast.makeText(this,"Choose an Image Before Save",Toast.LENGTH_SHORT).show();
            return "";
        }
        try{
            File path = Environment.getExternalStorageDirectory();
            File dir = null;
            String finalDir;
            if(simpleSwitch.isChecked()){
                finalDir = path + "/Daily Art/Files/"+ "user_"+ userID +"/" +"MileStone";
                saveImageFileStream(finalDir,timeStamp);
            }
            finalDir = path + "/Daily Art/Files/" + "user_"+ userID +"/" + this.tagPath;
            if (!this.tagPath.equals("<None>")) {
                saveImageFileStream(finalDir, timeStamp);
            }
            saveImageFileStream(path + "/Daily Art/Files/" + "user_"+ userID +"/" +"General",timeStamp);
            String fileName = timeStamp+".jpg";
            return finalDir + "/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
            return "";
        }

    }

    private void saveImageFileStream(String savePath, String timeStamp) throws IOException{
        File dir = new File(savePath);

        dir.mkdirs();
        //file name
        String fileName = timeStamp+".jpg";
        File file = new File(dir,fileName);

        FileOutputStream fos = new FileOutputStream(file);
        File image= new File(ImagePath);
        byte[] bytes = getBytesFromFile(image);
        fos.write(bytes);
        fos.close();
        //Toast
        Toast.makeText(this,fileName+" File saved in :"+dir,Toast.LENGTH_SHORT).show();


    }

    private void saveCritiqueFileStream(String savePath, String timeStamp) throws IOException{
        File dir = new File(savePath);
        dir.mkdirs();
        //file name
        String fileName = timeStamp+".txt";
        File file = new File(dir,fileName);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(Comment.getText().toString());
        bw.close();
    }

    private byte[] getBytesFromFile(File file) throws IOException {
        byte[] data = FileUtils.readFileToByteArray(file);
        return data;
    }

    private String getFileName(Uri uri)
    {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(index);
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    // find path
    private String getPath(Context context, Uri uri) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        if (cursor.moveToFirst()) {
            try {
                int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                path = cursor.getString(index);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return path;
    }


    // share function (image + text)
    private void share() {
        String userID = sharedPref.getString("UserID", "");
        SharedPreferences.Editor editor = sharedPref.edit();
        int shareCount = sharedPref.getInt("ShareCount_" + userID, 0);
        editor.putInt("ShareCount_" + userID, shareCount + 1);
        editor.apply();

        String shareBody = Comment.getText().toString();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("image/jpeg");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
//        Log.d("BAD_PATH", ImageUri.toString().replaceAll("file://","content://"));
        sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM,Uri.parse(ImageUri.toString().replaceAll("file://","content://")));
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    public void load(View view) {
        int progress=mypb.getProgress();
        progress=10;
        mypb.setProgress(progress);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String selectedTag = (String) parent.getItemAtPosition(pos);
        if (selectedTag.equalsIgnoreCase("+ New Tag")){
            // open add tag dialog
            Spinner spinner = (Spinner) findViewById(R.id.available_tags);
            spinner.setSelection(0);
            showNewTagAlertDialog(new View(getApplicationContext()));
        } else if (selectedTag.equalsIgnoreCase("- Delete Tag")) {
            Spinner spinner = (Spinner) findViewById(R.id.available_tags);
            spinner.setSelection(0);
            showDeleteTagAlertDialog(new View(getApplicationContext()));
        } else {
            this.tagPath = selectedTag;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void showNewTagAlertDialog(View view) {

        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(this);
        builder.setTitle("New Tag");

        // set the custom layout
        View customLayout = getLayoutInflater().inflate(R.layout.new_tag_dialog,null);
        builder.setView(customLayout);

        // add a button
        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = customLayout.findViewById(R.id.new_tag_edit_text);
                        addNewTag(editText.getText().toString());
                    }
                });

        builder.setNeutralButton(
                "CANCEL",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showDeleteTagAlertDialog(View view) {

        // Create an alert builder
        AlertDialog.Builder builder
                = new AlertDialog.Builder(this);
        builder.setTitle("New Tag");

        // set the custom layout
        View customLayout = getLayoutInflater().inflate(R.layout.delete_tag_dialog,null);
        builder.setView(customLayout);
        Spinner tagSpinner = (Spinner) customLayout.findViewById(R.id.delete_tags_spinner);
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,userTags);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        tagSpinner.setAdapter(aa);
        tagSpinner.setSelection(0);

        tagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // add a button
        builder.setPositiveButton(
                "DELETE TAG",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String tagToDelete = (String) tagSpinner.getSelectedItem();
                        if (tagToDelete.equals("General")){
                            Toast.makeText(getApplicationContext(), "Cannot delete General gallery", Toast.LENGTH_SHORT).show();
                        } else {
                            deleteTag(tagToDelete);
                        }
                    }
                });

        builder.setNeutralButton(
                "CANCEL",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addNewTag(String data) {
        SharedPreferences sharedPref = getSharedPreferences("CurrentUserID", MODE_PRIVATE);
        String userID = sharedPref.getString("UserID", "");
        userTags.add(0,data);
        spinnerItems.add(1,data);
        SharedPrefUtil.saveStringList(getApplicationContext(),userTags,"USER_TAGS_"+userID);
        Spinner spinner = (Spinner) findViewById(R.id.available_tags);
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,spinnerItems);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(aa);
        spinner.setSelection(0);
        Toast.makeText(this, data + " added to tag list", Toast.LENGTH_SHORT).show();
    }

    private void deleteTag(String data){
        SharedPreferences sharedPref = getSharedPreferences("CurrentUserID", MODE_PRIVATE);
        String userID = sharedPref.getString("UserID", "");
        userTags.remove(data);
        spinnerItems.remove(data);
        SharedPrefUtil.saveStringList(getApplicationContext(),userTags,"USER_TAGS_"+userID);
        Spinner spinner = (Spinner) findViewById(R.id.available_tags);
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,spinnerItems);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(aa);
        spinner.setSelection(0);
        Toast.makeText(this, data + " removed from tag list", Toast.LENGTH_SHORT).show();
    }



}