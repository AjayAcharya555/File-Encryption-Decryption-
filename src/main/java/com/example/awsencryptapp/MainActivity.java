package com.example.awsencryptapp;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.example.awsencryptapp.util.MyEncrypter;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Stream;

import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {

    public static final int SELECT_PHOTO = 1234;
    Uri uri;
    private static final String FILE_NAME_ENC ="image_enc";

    private static final String FILE_NAME_DEC ="image_dec.png";

    Button btn_enc,btn_dec,btn_aws;
    ImageView imgview,image_choose;
    File mydir;

    Button btn_choose_image;

    //Keys in this example i will hardcode in code
    //in  real life can save it on Firebase/API and get when runtime

    String my_key ="ojdzetaslsyhdrwk";
    String my_spec_key = "ralrieboiupmfvgo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_enc = findViewById(R.id.btn_encrypt);
        btn_dec = findViewById(R.id.btn_decrypt);
        btn_aws =findViewById(R.id.click_to_aws);
        imgview = findViewById(R.id.image_view);
        btn_choose_image= findViewById(R.id.btn_chooseimage);
        image_choose=findViewById(R.id.image_choose);




        btn_aws.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),AWSUpload.class);
                startActivity(intent);
            }
        });

        btn_choose_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent pickImageIntent = new Intent(Intent.ACTION_PICK);
//                pickImageIntent.setType("image/*");
//                startActivityForResult(pickImageIntent,SELECT_PHOTO);
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery,"selcet picture"),SELECT_PHOTO);
            }
        });


        mydir = new File(Environment.getExternalStorageDirectory().toString()+"/saved_images");
        mydir.mkdir();
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        btn_enc.setEnabled(true);
                        btn_dec.setEnabled(true);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        Toast.makeText(MainActivity.this,"you must enable permission",Toast.LENGTH_LONG).show();

                    }
                }).check();

        btn_dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                File outputFileDec = new File(mydir, String.valueOf(SELECT_PHOTO));
//                File encFile = new File(mydir, String.valueOf(SELECT_PHOTO));
                File outputFileDec = new File(mydir, FILE_NAME_DEC);
                File encFile = new File(mydir,FILE_NAME_ENC);
                try
                {
                    MyEncrypter.decryptToFile(my_key,my_spec_key, new FileInputStream(encFile),
                            new FileOutputStream(outputFileDec));

                    //after that for imag view
                    imgview.setImageURI(Uri.fromFile(outputFileDec));
//                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
//                    image_choose.setImageBitmap(bitmap);
                    //if you want delete the file after decrypt just keep this line
                   // outputFileDec.delete();

                    Toast.makeText(MainActivity.this,"Decyrpt",Toast.LENGTH_SHORT).show();


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_enc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //convert drawable to bitmap
//                Drawable drawable = ContextCompat.getDrawable(MainActivity.this,R.drawable.girl);
//                BitmapDrawable bitmapDrawable =(BitmapDrawable)drawable;
                image_choose =(ImageView)findViewById(R.id.image_choose);
                Bitmap bitmap =((BitmapDrawable)image_choose.getDrawable()).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
                InputStream is = new ByteArrayInputStream(stream.toByteArray());

               // File outputFileEnc = new File(mydir, String.valueOf(SELECT_PHOTO));
                File outputFileEnc = new File(mydir,FILE_NAME_ENC);
                try
                {
                    MyEncrypter.encryptToFile(my_key,my_spec_key,is,new FileOutputStream(outputFileEnc));
                    Toast.makeText(MainActivity.this,"Encrypted",Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SELECT_PHOTO && resultCode == RESULT_OK)
        {
            uri =data.getData();
            try
            {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                image_choose.setImageBitmap(bitmap);

            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
