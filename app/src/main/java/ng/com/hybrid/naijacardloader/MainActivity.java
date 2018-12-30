package ng.com.hybrid.naijacardloader;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.jgabrielfreitas.core.BlurImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity {

    CardView copy;
    Button selecter;
    BlurImageView imgpreview;
    TextView textresult;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;
    Uri image_uri;

    String cameraPermission[];
    String storagePermission[];
    ClipboardManager clipboardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        copy = findViewById(R.id.copy);
        textresult = findViewById(R.id.textresult);
        imgpreview = findViewById(R.id.imgpreview);
        selecter = findViewById(R.id.selecter);
        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        imgpreview.setBlur(5);
        clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);

       selecter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowImageDialog();
            }
        });

       if(clipboardManager.hasPrimaryClip()){

       }

       copy.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               String text = textresult.getText().toString();

               if(!text.equals("")){
                   ClipData clipData = ClipData.newPlainText("text",text);
                   clipboardManager.setPrimaryClip(clipData);

                   Toast.makeText(getApplicationContext(),"Copied",Toast.LENGTH_SHORT).show();
               }
           }
       });

    }

    private void ShowImageDialog(){

        String[] items = {"Camera ","Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Select Image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

              if(which == 0){

                  if (!checkCameraPermission()) {
                  requestCameraPermission();

                  }else{
                      pickCamera();
                  }
              }

              if(which == 1){

                  if (!checkStoragePermission()) {
                      requestStoragePermission();

                  }else{
                      pickGallery();
                  }
              }

            }
        });
        dialog.create().show();
    }

    private void pickGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);

    }

    private void pickCamera() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"NewPic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"NaijaCardLoader");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission() {

        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);

    }

    private boolean checkStoragePermission() {

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);

    }

    private boolean checkCameraPermission() {

        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return  result && result1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

       switch (requestCode){

           case CAMERA_REQUEST_CODE:

               if(grantResults.length > 0){
                   boolean cameraAccepted = grantResults[0] ==
                           PackageManager.PERMISSION_GRANTED;
                   boolean writeStorageAccepted =
                   grantResults[0] == PackageManager.PERMISSION_GRANTED;

                   if(cameraAccepted && writeStorageAccepted){
                       pickCamera();
                   }else{
                       Toast.makeText(getApplicationContext(),"permission denied",Toast.LENGTH_SHORT).show();
                   }
               }
               break;
           case STORAGE_REQUEST_CODE:

               if(grantResults.length > 0){
                   boolean writeStorageAccepted =
                           grantResults[0] == PackageManager.PERMISSION_GRANTED;

                   if(writeStorageAccepted){
                       pickGallery();
                   }else{
                       Toast.makeText(getApplicationContext(),"permission denied",Toast.LENGTH_SHORT).show();
                   }
               }
             break;
       }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }

            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }

        }

      if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                imgpreview.setImageURI(resultUri);

                BitmapDrawable bitmapDrawable = (BitmapDrawable)imgpreview.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                if(!recognizer.isOperational()){
                    Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
                }else{
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = recognizer.detect(frame);
                    StringBuilder sb = new StringBuilder();

                    for(int i=0; i<items.size(); i++){
                        TextBlock  myItem = items.valueAt(i);
                        sb.append(myItem.getValue());
                        sb.append("\n");

                    }
                    textresult.setText(sb.toString());

                }
            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
                Toast.makeText(getApplicationContext(),""+error,Toast.LENGTH_SHORT).show();
            }

        }
    }
}
