package com.example.dailyart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.DialogInterface;

public class EditCritiqueActivity extends AppCompatActivity {
    private ProgressBar mypb;
    private TextInputEditText et;
    private Uri critique_file;
    private Uri image_file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_critique);
        setContentView(R.layout.activity_edit_critique);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        ImageView iv = (ImageView) findViewById(R.id.focused_image);
        et = (TextInputEditText) findViewById(R.id.edit_critique_text);
        Button save = (Button) findViewById(R.id.save_button);
        Button delete = (Button) findViewById(R.id.delete_button);
        Intent iin= getIntent();
        Bundle b = iin.getExtras();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(b!=null)
                {
                    critique_file = (Uri) b.get("CRITIQUE_FILEPATH");
                    saveCommentToFile(critique_file.toString());
                }
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //delete image and comment
                if (critique_file != null && image_file != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditCritiqueActivity.this);

                    builder.setCancelable(true);
                    builder.setTitle("Confirm to delete image and critique");
                    builder.setMessage("This is an Alert Dialog's Message");

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String critiqueFilePath = critique_file.getPath();
                            String imageFilePath = image_file.getPath();
                            File critiqueFile = new File(critiqueFilePath);
                            File imageFile = new File(imageFilePath);
                            if (critiqueFile.exists()) {
                                if (critiqueFile.delete()) {
                                    System. out. println("file Deleted :" + critiqueFile);
                                } else {
                                    System. out. println("file not Deleted :" + critiqueFile);
                                }
                            }
                            if (imageFile.exists()) {
                                if (imageFile.delete()) {
                                    System. out. println("file Deleted :" + imageFile);
                                } else {
                                    System. out. println("file not Deleted :" + imageFile);
                                }
                            }
                            Intent returnBtn = new Intent(getApplicationContext(),
                                    MainActivity.class);

                            startActivity(returnBtn);
                        }
                    });
                    builder.show();
                }
                }
            });


        if(b!=null)
        {
            image_file = (Uri) b.get("IMAGE_FILEPATH");
            Log.d("RECEIVED PATH", image_file.toString());
            iv.setImageURI(image_file);

            critique_file = (Uri) b.get("CRITIQUE_FILEPATH");
            String currCritique = readFromFile(getApplicationContext(),critique_file.toString());
            et.setText(currCritique);
        }
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

    private String readFromFile(Context context, String filepath) {
        String ret = "";
        try {
            FileInputStream inputStream = new FileInputStream (new File(filepath));
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append("").append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        return ret;
    }


    private String saveCommentToFile(String filepath){
        if(et.getText()==null || et.getText().toString()==""){
            Toast.makeText(this,"Input the comment Before Save",Toast.LENGTH_SHORT).show();
            return "";
        }
        try{
            String fileName = filepath;
            File file = new File(filepath);
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(et.getText().toString());
            bw.close();
            //Toast
            Toast.makeText(this,fileName+" File saved in :"+filepath,Toast.LENGTH_SHORT).show();
            return filepath;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
            return "";
        }
    }
}