package com.example.jungexweb.ui.gallery;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.jungexweb.databinding.FragmentGalleryBinding;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.DIRECTORY_PICTURES;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Permission;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private String imageFillPath;
    private Uri photoUri;
    // 사진 저장 시 갤러리 폴더에 바로 반영사항을 업데이트 시켜주려면 이 것이 필요하다(미디어 스캐닝)
    private MediaScanner mMediaScanner;


    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        final TextView textView = binding.textGallery;
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);



        // 권한 체크 및 API 버전별 처리
        if (Build.VERSION.SDK_INT < 29) {
            galleryViewModel.setText("SDK 29 미만: WRITE_EXTERNAL_STORAGE 권한 필요");
            textView.setText("SDK 29 미만...");
            System.out.println("SDK 29 미만...");

            // API 29 미만에서는 WRITE_EXTERNAL_STORAGE와 CAMERA 권한 요청
            TedPermission.create()
                    .setPermissionListener(new PermissionListener() {
                        Context context = getContext();

                        @Override
                        public void onPermissionGranted() {
                            if (context != null) {
                                Toast.makeText(context, "권한이 허용됨", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onPermissionDenied(List<String> deniedPermissions) {
                            if (context != null) {
                                Toast.makeText(context, "권한이 거부됨", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setDeniedMessage("권한을 허용해 주셔야 사용 가능합니다.")
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                    .check();

        } else if (Build.VERSION.SDK_INT >= 29 && Build.VERSION.SDK_INT < 30) {
            galleryViewModel.setText("SDK 29 이상 30 미만: WRITE_EXTERNAL_STORAGE 불필요");
            textView.setText("SDK 29 이상...");
            System.out.println("SDK 29 이상...");

            // API 29 이상에서는 WRITE_EXTERNAL_STORAGE가 불필요, CAMERA 권한만 요청
            TedPermission.create()
                    .setPermissionListener(new PermissionListener() {
                        Context context = getContext();

                        @Override
                        public void onPermissionGranted() {
                            if (context != null) {
                                Toast.makeText(context, "권한이 허용됨", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onPermissionDenied(List<String> deniedPermissions) {
                            if (context != null) {
                                Toast.makeText(context, "권한이 거부됨", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setDeniedMessage("권한을 허용해 주셔야 사용 가능합니다.")
                    .setPermissions(Manifest.permission.CAMERA)
                    .check();

        } else if (Build.VERSION.SDK_INT >= 30) {
            galleryViewModel.setText("SDK 30 이상: MANAGE_EXTERNAL_STORAGE 필요");
            textView.setText("SDK 30 이상...");
            System.out.println("SDK 30 이상...");

            // API 30 이상에서는 MANAGE_EXTERNAL_STORAGE 권한이 필요
            TedPermission.create()
                    .setPermissionListener(new PermissionListener() {
                        Context context = getContext();

                        @Override
                        public void onPermissionGranted() {
                            if (context != null) {
                                Toast.makeText(context, "권한이 허용됨", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onPermissionDenied(List<String> deniedPermissions) {
                            if (context != null) {
                                Toast.makeText(context, "권한이 거부됨", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setDeniedMessage("권한을 허용해 주셔야 사용 가능합니다.")
                    .setPermissions(Manifest.permission.CAMERA, Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                    .check();
        }



        binding.btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(null != intent.resolveActivity(requireContext().getPackageManager())){
                    File photoFile = null;
                    try{
                        photoFile = createImageFile();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    if(photoFile != null){
                        //photoURi = FileProvider.getUriForFile(getApplicationContext(),getPackageName(), photoFile);
                        photoUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName(), photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        //돌아올때 값을 가져다 주는 역활
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });

        return root;
    }
    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMDD_HHmms").format(new Date());
        String imageFileName = "jungExWeb_" + timeStamp ;
        File storegeDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storegeDir
        );
        imageFillPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //protected였으나 fragment에서는 public으로 되었다고 함. 변경처림
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK ){
            Bitmap bitmap = BitmapFactory.decodeFile(imageFillPath);
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(imageFillPath);
            }catch (IOException e){
                e.printStackTrace();
            }

            int exifOrientation ;
            int exifDegree;
            if(exif != null){
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientationToDegress(exifOrientation);
            }else{
                exifDegree = 0;
            }
            String result = "";
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault() );
            Date             curDate   = new Date(System.currentTimeMillis());
            String           filename  = formatter.format(curDate);

            //api 29이상의 이미지 저장
            ContentValues values = new ContentValues();
            //G내 PC\Galaxy Note9\Card\Android\data\com.example.jungexweb\files\Pictures/
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/jungexweb/files");
            values.put(MediaStore.Images.Media.IS_PENDING, true);
            Uri uri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (FileOutputStream out = (FileOutputStream) requireContext().getContentResolver().openOutputStream(uri)) {
                    // 비트맵을 저장
                    rotate(bitmap, exifDegree).compress(Bitmap.CompressFormat.PNG, 70, out);

                    // 저장 완료 후 상태 업데이트
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, false);
                    requireContext().getContentResolver().update(uri, values, null, null);

                    // 저장된 이미지를 ImageView에 불러오기
                    binding.ivResult.setImageURI(uri);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

/*  api28까지 코드

            String           strFolderName = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES) + File.separator + "jungexweb" + File.separator;
            File file = new File(strFolderName);
            if( !file.exists() )
                file.mkdirs();

            File f = new File(strFolderName + "/" + filename + ".png");
            result = f.getPath();

            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                result = "Save Error fOut";
            }


            // 비트맵 사진 폴더 경로에 저장
            rotate(bitmap,exifDegree).compress(Bitmap.CompressFormat.PNG, 70, fOut);

            try {
                fOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut.close();
                // 방금 저장된 사진을 갤러리 폴더 반영 및 최신화
                mMediaScanner.mediaScanning(strFolderName + "/" + filename + ".png");
            } catch (IOException e) {
                e.printStackTrace();
                result = "File close Error";
            }
            ((ImageView)binding.ivResult).setImageBitmap(rotate(bitmap, exifDegree));
            */

        }
    }

    private int exifOrientationToDegress(int exifOrientation){
//화면이 돌아갈때 이미지 화면도 돌아가게 하는 함수
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90){
            return 90;
        }else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180){
            return 180;
        }else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270){
            return 270;
        }else{
            return 0;
        }
    }

    private Bitmap rotate(Bitmap bitmap, float degree){
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),matrix, true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}