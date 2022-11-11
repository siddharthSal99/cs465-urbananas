package com.example.dailyart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
//import android.os.FileUtils;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;


// 获取image => https://www.geeksforgeeks.org/how-to-select-an-image-from-gallery-in-android/
// 保存image => https://www.youtube.com/watch?v=oLcxTunwaFk
// 分享图片和评论 => https://www.youtube.com/watch?v=_rBKy4z1yNU
// milestone & gallery 待定
public class Upload extends AppCompatActivity implements View.OnClickListener{
    // Buttons
    private Button AlbumButton;
    private Button GalleryButton;
    private Button MileStoneButton;
    private Button ShareButton;
    private Button SaveButton;

    // One Preview Image
    ImageView IVPreviewImage;

    // Image Path
    Uri ImageUri;
    String ImagePath;

    // Image Name
    String ImageName;

    // comment
    TextView Comment;

    // constant to compare
    // the activity result code
    int SELECT_PICTURE = 200;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);


        // register the UI widgets with their appropriate IDs
        AlbumButton = (Button)findViewById(R.id.Album);
        GalleryButton = (Button)findViewById(R.id.Gallery);
        MileStoneButton = (Button)findViewById(R.id.Milestone);
        ShareButton = (Button)findViewById(R.id.Share);
        SaveButton = (Button)findViewById(R.id.Save);
        IVPreviewImage = (ImageView)findViewById(R.id.IVPreviewImage);

        Comment = (TextView)findViewById(R.id.edit_text);

        // handle the Choose Image button to trigger
        // the image chooser function
        AlbumButton.setOnClickListener(this);
        GalleryButton.setOnClickListener(this);
        MileStoneButton.setOnClickListener(this);
        ShareButton.setOnClickListener(this);
        SaveButton.setOnClickListener(this);
    }


    // handle select image
    public void onClick(View v) {
        if (v.getId() == R.id.Album) {
            imageChooser();
        } else if(v.getId() == R.id.Save) {
            boolean saved = saveImage();
            if(!saved){
                Toast.makeText(this,"Save Image Failed",Toast.LENGTH_SHORT).show();
            }else{
 //             Toast.makeText(this,"Save Image Successfully",Toast.LENGTH_SHORT).show();
            }
        } else if(v.getId() == R.id.Share) {
            share();
        }
    }

    // this function is triggered when
    // the Select Image Button is clicked
    void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
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
                // todo:how to fix this(convert from uri into full path)?
//                String path = getPath(this,ImageUri);
//                System.out.println(path);

            }
        }
    }


    //handle save images
    private boolean saveImage() {
        //validate path
        if(ImagePath==null || ImagePath=="" || ImageName==null || ImageName==""){
            return false;
        }
        try {
            insertInPrivateStorage(ImageName,ImagePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void insertInPrivateStorage(String name, String path) throws IOException {
        FileOutputStream fos  = openFileOutput(name,MODE_APPEND);
        File file = new File(path);
        byte[] bytes = getBytesFromFile(file);
        fos.write(bytes);
        fos.close();
        Toast.makeText(getApplicationContext(),"File saved in :"+ getFilesDir() + "/"+name,Toast.LENGTH_SHORT).show();
    }

    private byte[] getBytesFromFile(File file) throws IOException {
        byte[] data = FileUtils.readFileToByteArray(file);
        return data;

    }

//    private String getFileName(Uri uri)
//    {
//        String result = null;
//        if (uri.getScheme().equals("content")) {
//            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
//            try {
//                if (cursor != null && cursor.moveToFirst()) {
//                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//                    result = cursor.getString(index);
//                }
//            } finally {
//                cursor.close();
//            }
//        }
//        if (result == null) {
//            result = uri.getPath();
//            int cut = result.lastIndexOf('/');
//            if (cut != -1) {
//                result = result.substring(cut + 1);
//            }
//        }
//        return result;
//    }
//    // find path
//    private String getPath(Context context, Uri uri) {
//        String path = null;
//        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
//        if (cursor == null) {
//            return null;
//        }
//        if (cursor.moveToFirst()) {
//            try {
//                int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
//                path = cursor.getString(index);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        cursor.close();
//        return path;
//    }
    //
//    public static File uriToFileApiQ(Uri uri, Context context) {
//        File file = null;
//        if (uri == null) return file;
//        //android10以上转换
//        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
//            file = new File(uri.getPath());
//        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
//            //把文件复制到沙盒目录
//            ContentResolver contentResolver = context.getContentResolver();
//            String displayName = System.currentTimeMillis() + Math.round((Math.random() + 1) * 1000)
//                    + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
//
//            try {
//                InputStream is = contentResolver.openInputStream(uri);
//                File cache = new File(context.getCacheDir().getAbsolutePath(), displayName);
//                FileOutputStream fos = new FileOutputStream(cache);
//                FileUtils.copy(is, fos);
//                file = cache;
//                fos.close();
//                is.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return file;
//    }

    // share function (image + text)
    private void share() {
        String shareBody = Comment.getText().toString();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("image/jpeg");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Subject Here");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM,ImageUri);
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}