package com.example.young.gallerytest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.yongbeam.y_photopicker.util.photopicker.PhotoPagerActivity;
import com.yongbeam.y_photopicker.util.photopicker.PhotoPickerActivity;
import com.yongbeam.y_photopicker.util.photopicker.utils.YPhotoPickerIntent;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button cameraBtn;
    private Button galleryBtn;
    private ImageView pictureIv;
    private String imagePath;

    public final static int CAMERA_REQUEST_CODE = 1;
    public final static int GALLERY_REQUEST_CODE = 2;

    public static ArrayList<String> selectedPhotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBtn = (Button) findViewById(R.id.cameraBtn);
        galleryBtn = (Button) findViewById(R.id.galleryBtn);
        pictureIv = (ImageView) findViewById(R.id.pictureIv);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 카메라 앱 실행
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // 찍은 사진을 보관할 파일 객체를 만들어 보낸다.
                File picture = savePictureFile();

                if (picture != null) {
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picture));
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                }
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YPhotoPickerIntent intent = new YPhotoPickerIntent(MainActivity.this);
                intent.setMaxSelectCount(1);    // 선택할 수 있는 이미지의 개수 지정
                intent.setShowCamera(false);    // 카메라 실행 버튼 표시 여부
                intent.setShowGif(false);       // gif 이미지도 포함하여 갤러리를 보여줄 것인지
                intent.setSelectCheckBox(true); // 사진 선택할 때 테두리 색 변하기
                intent.setMaxGrideItemCount(3); // 한줄에 몇개의 사진을 보여줄 것인지 설정
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GALLERY_REQUEST_CODE :
                List<String> photos = null;
                if (resultCode == RESULT_OK ) { //&& requestCode == GALLERY_REQUEST_CODE) {
                    if (data != null) {
                        photos = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                    }
                    if (photos != null) {
                        selectedPhotos.addAll(photos);
                    }

                    // start image view
                    Intent startActivity = new Intent(this , PhotoPagerActivity.class);
                    startActivity.putStringArrayListExtra("photos" , selectedPhotos);
                    startActivity(startActivity);
                }
                break;

            case CAMERA_REQUEST_CODE :
                if (resultCode == RESULT_OK ) { //&& requestCode == CAMERA_REQUEST_CODE) {
                    BitmapFactory.Options factory = new BitmapFactory.Options();
                    factory.inJustDecodeBounds = false;
                    factory.inPurgeable = true;

                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath, factory);
                    pictureIv.setImageBitmap(bitmap);
                }
                break;

        }

    }

    private File savePictureFile() {

        // 사진 파일의 이름을 만든다.
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "IMG_" + timestamp;

        // 사진 파일이 저장될 장소를 구한다.
        // 외장메모리에서 사진을 저장하는 폴더를 찾아서 그곳에 MYAPP 이라는 폴더를 만든다.
        File pictureStorage = new File(Environment.DIRECTORY_PICTURES, "MYAPP/");

        // 만약 장소가 존재하지 않는다면 폴더를 새롭게 만든다.
        if (!pictureStorage.exists()) {
            pictureStorage.mkdirs(); // 폴더를 만들어준다.
        }

        try {
            File file = File.createTempFile(fileName, ".jpg", pictureStorage);

            // ImageView에 보여주기 위해 사진 파일의 절대 경로를 얻어온다.
            imagePath = file.getAbsolutePath();

            // 찍힌 사진을 갤러리 앱에 추가한다.
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(imagePath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);

            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
