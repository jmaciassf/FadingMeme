package khaos.fadingmeme;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static int IMG1 = 1, IMG2 = 2;
    private String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Seleccionar imagenes
        ImageView img1 = findViewById(R.id.imageView1);
        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Intent i = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, IMG1);
                }
                catch (Exception e){

                }
            }
        });
        ImageView img4 = findViewById(R.id.imageView4);
        img4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, IMG2);
            }
        });

        final Activity activity = this;

        ImageButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    action = "SAVE";
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 7);
                } else {
                    viewToBitmap();
                }
            }
        });

        ImageButton btnShare = findViewById(R.id.btnShare);
        btnShare.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    action = "SHARE";
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 7);
                } else {
                    share("");
                }
            }
        });

        ImageButton btnWhatsapp = findViewById(R.id.btnWhatsapp);
        btnWhatsapp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    action = "WHATSAPP";
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 7);
                } else {
                    share("WHATSAPP");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 7:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if(action == "SAVE")
                        viewToBitmap();
                    else
                        share(action);
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try{
            super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == RESULT_OK && null != data) {
                ImageView img1 = (ImageView) findViewById(R.id.imageView1);
                ImageView img21 = (ImageView) findViewById(R.id.imageView21);
                ImageView img22 = (ImageView) findViewById(R.id.imageView22);
                ImageView img31 = (ImageView) findViewById(R.id.imageView31);
                ImageView img32 = (ImageView) findViewById(R.id.imageView32);
                ImageView img4 = (ImageView) findViewById(R.id.imageView4);

                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    String path = getPathFromURI(selectedImageUri);
                    if (requestCode == IMG1) {
                        img1.setImageURI(selectedImageUri);
                        img21.setImageURI(selectedImageUri);
                        img31.setImageURI(selectedImageUri);
                    } else if (requestCode == IMG2) {
                        img22.setImageURI(selectedImageUri);
                        img32.setImageURI(selectedImageUri);
                        img4.setImageURI(selectedImageUri);
                    }
                }
            }
        }
        catch (Exception e){

        }
    }

    /* Get the real path from the URI */
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    public void share(String type){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        try {
            Toast.makeText(this,
                    "Sharing...", Toast.LENGTH_LONG).show();
            File imgFile = getImage();
            String path = imgFile.getPath();
            Uri bmpUri = Uri.parse("file://" + path);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/png");

            if(type == "WHATSAPP"){
                shareIntent.setPackage("com.whatsapp");
            }

            startActivity(Intent.createChooser(shareIntent, "Send your meme"));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void viewToBitmap() {
        try {
            Toast.makeText(this,
                    "Downloading...", Toast.LENGTH_LONG).show();
            File imgFile = getImage();
            DownloadManager downloadManager = (DownloadManager) this.getSystemService(DOWNLOAD_SERVICE);
            downloadManager.addCompletedDownload(imgFile.getName(), imgFile.getName(),
                    true, "image/png", imgFile.getAbsolutePath(), imgFile.length(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmapImage(){
        LinearLayout content = findViewById(R.id.layoutImages);
        content.setDrawingCacheEnabled(true);
        Bitmap bitmap = content.getDrawingCache();
        return  bitmap;
    }

    private File getImage(){
        ProgressDialog progress = ProgressDialog.show(this, "Loading", "Wait while loading...");

        LinearLayout content = findViewById(R.id.layoutImages);
        content.setDrawingCacheEnabled(true);
        Bitmap bitmap = content.getDrawingCache();
        File imgFile = null;
        FileOutputStream fos;
        try {
            // Create unique filename
            int counter = 0;
            do {
                imgFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/meme"+ ++counter +".png");
            }
            while (imgFile.exists());

            imgFile.createNewFile();
            fos = new FileOutputStream(imgFile);
            bitmap.compress(Bitmap.CompressFormat.PNG,70, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            content.setDrawingCacheEnabled(false);
            progress.dismiss();
        }

        return  imgFile;
    }
}
