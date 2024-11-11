package com.example.awsencryptapp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;

import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class AWSUpload extends AppCompatActivity {

    private final String KEY = "AKIATTBXUEQ5SJO53MXT";
    private final String SECRET = "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";

    private static final String TAG = MainActivity.class.getSimpleName();
    public AmazonS3Client s3Client;
    private BasicAWSCredentials credentials;
    private static final int CHOOSING_IMAGE_REQUEST = 1234;

    private TextView tv_filename;
    private ImageView imageView;
    private EditText edt_filename;

    private Button upload,download,choosefile;


    private Uri fileUri;
    private Bitmap bitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_awsupload);
        getSupportActionBar().setTitle("AWS S3 Upload");

        imageView = findViewById(R.id.img_file);
        edt_filename = findViewById(R.id.file_name);
        tv_filename = findViewById(R.id.tv_file_name);
        choosefile = findViewById(R.id.choose_file);
        upload = findViewById(R.id.btn_upload);
        download = findViewById(R.id.btn_download);

        tv_filename.setText("");

       credentials = new BasicAWSCredentials(KEY,SECRET);
        s3Client = new AmazonS3Client(credentials);

        getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));

        // Initialize the AWSMobileClient if not initialized

        // Add a call to initialize AWSMobileClient
//        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
//            @Override
//            public void onComplete(AWSStartupResult awsStartupResult) {
//                SignInUI signin = (SignInUI) AWSMobileClient.getInstance().getClient(
//                        AWSUpload.this,
//                        SignInUI.class);
//                signin.login(
//                        AWSUpload.this,
//                        MainActivity.class).execute();
//            }
//        }).execute();

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                try {
//                    Amplify.addPlugin(new AWSS3StoragePlugin());
//                    Amplify.configure(getApplicationContext());
                    Log.i("StorageQuickstart", "All set and ready to go!");
                } catch (Exception exception) {
                    Log.e("StorageQuickstart", exception.getMessage(), exception);
                }
            }

            @Override
            public void onError(Exception exception) {
                Log.e("StorageQuickstart", "Initialization error.", exception);
            }
        });

        choosefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Image"),CHOOSING_IMAGE_REQUEST);
            }
        });


        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileUri != null) {

                    final String fileName = edt_filename.getText().toString();
                    if (!validateInputFileName(fileName)) {
                        return;
                    }
                    final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"/" + fileName);

                    createFile(getApplicationContext(), fileUri, file);

                    TransferUtility transferUtility =
                            TransferUtility.builder()
                                    .context(getApplicationContext())
                                    .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                                    .s3Client(s3Client)
                                    .build();
                    TransferObserver uploadObsever;
                    uploadObsever = transferUtility.upload("jsaS3/" + fileName + "." + getFileExtention(fileUri), file);

                    uploadObsever.setTransferListener(new TransferListener() {
                        @Override
                        public void onStateChanged(int id, TransferState state) {
                            if (TransferState.COMPLETED == state) {
                                Toast.makeText(getApplicationContext(), "Upload Completed", Toast.LENGTH_SHORT).show();
                                file.delete();
                            } else if (TransferState.FAILED == state) {
                                file.delete();
                            }
                        }

                        @Override
                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                            float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                            int percentDone = (int) percentDonef;
                            tv_filename.setText("ID:" + id + "|byteCurrent : " + bytesCurrent + "|byteTotal :" + bytesTotal + "|" + percentDone + "%");


                        }

                        @Override
                        public void onError(int id, Exception ex) {
                            ex.printStackTrace();
                        }
                    });

                }
            }
        });
    }









    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(bitmap != null)
        {
            bitmap.recycle();
        }

        if(requestCode == CHOOSING_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() !=null)
        {
            fileUri = data.getData();
            try
            {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),fileUri);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private  String getFileExtention(Uri uri)
    {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime =MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private boolean validateInputFileName(String filename)
    {
        if(TextUtils.isEmpty(filename))
        {
            Toast.makeText(this,"Enter file name !" , Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void createFile(Context context, Uri srcuri, File dstfile)
    {
        try
        {
            InputStream inputStream = context.getContentResolver().openInputStream(srcuri);
            if(inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstfile);
            IOUtils.copy(inputStream,outputStream);
            inputStream.close();
            outputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
