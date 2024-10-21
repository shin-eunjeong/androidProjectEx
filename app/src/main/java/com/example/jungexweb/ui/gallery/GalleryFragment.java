package com.example.jungexweb.ui.gallery;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.*;      //android.graphics.Camera; 하드웨어로 해야한다. camera는 지원중단 2로 해야함.
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class GalleryFragment extends Fragment implements SurfaceHolder.Callback {

    private FragmentGalleryBinding binding;
    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private String imageFillPath;
    private Uri photoUri;
    // 사진 저장 시 갤러리 폴더에 바로 반영사항을 업데이트 시켜주려면 이 것이 필요하다(미디어 스캐닝)
    private MediaScanner mMediaScanner;
    /* 241021 동영상 촬영 관련 내용*/
    private Camera camera ;
    private CameraDevice cameraDevice;   //카메라를 제어함
    private CameraManager cameraManager; //Camera2는 cameraManager로 접근해야함. 카메라르 열때 사용
    private MediaRecorder mediaRecorder;   // surface통한 녹화 진행
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private Button btn_record;
    private SurfaceView surfaceView;  //동영상 미리보기 구현위해
    private SurfaceHolder surfaceHolder;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;
    private boolean isRecording = false;   // 현재 녹화중인가? 체크값



    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        final TextView textView = binding.textGallery;
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // 권한 체크 및 API 버전별 처리  **241021 동영상도 권한 부분이 있으나 동일하여 패스함
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
        //사진 버튼 클릭시 구현
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
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);  //29부터 deprecated됨.
                    }
                }
            }
        });

        //동영상 버튼 클릭시 구현
        cameraManager =(CameraManager) requireContext().getSystemService(Context.CAMERA_SERVICE);

        btn_record =(Button) binding.btnRecord;
        surfaceView = binding.gallerySurfaceView;
        surfaceHolder = surfaceView.getHolder();
        btn_record.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
/*                if (isRecording) {
                    stopRecordingVideo();
                } else {
                    startRecordingVideo();
                }*/
                if(isRecording){
                    mediaRecorder.stop();
                    mediaRecorder.release(); //완료후 객체 해제를 꼭 해야함.
                    camera.lock();
                    isRecording = false;
                }else {
                    //fragment에서는 그냥 runOnUiThread호출 안됨.
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "녹화가 시작되었습니다.", Toast.LENGTH_SHORT).show();
                            try {
                                mediaRecorder = new MediaRecorder();
                                camera.unlock();
                                mediaRecorder.setCamera(camera);
                                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);  //찍는다는 소리가 나게 함
                                mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);     //비디오 소스를 카메라에 넣는다.
                                mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));  //녹화 화질 해상도 결정
                                mediaRecorder.setOrientationHint(90);               //90도에 맞춰서 촬영
                                mediaRecorder.setOutputFile("/sdcard/test.mp4");  //저장 경로
                                mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());   //미리보고 화면 셋팅
                                mediaRecorder.prepare(); //준비하시고
                                mediaRecorder.start();
                                isRecording = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                                mediaRecorder.release();
                            }
                        }
                    });

                    camera = Camera.open();
                    camera.setDisplayOrientation(90);
                    surfaceView =(SurfaceView) binding.gallerySurfaceView;
                    surfaceHolder = surfaceView.getHolder();
                    surfaceHolder.addCallback(GalleryFragment.this);
                    surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

                }
            }
        });

        return root;
    }
    /*private void startRecordingVideo(){

        Toast.makeText(getContext(), "녹화가 시작되었습니다.", Toast.LENGTH_SHORT).show();

        try {
            setUpMediaRecorder();

            // 카메라 세션 생성 및 시작
            cameraDevice.createCaptureSession(Arrays.asList(mediaRecorder.getSurface(), surfaceHolder.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            cameraCaptureSession = session;
                            try {
                                // 프리뷰 및 녹화 시작
                                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                                captureRequestBuilder.addTarget(mediaRecorder.getSurface());
                                session.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
                                mediaRecorder.start();
                                isRecording = true;
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(getContext(), "Camera configuration failed!", Toast.LENGTH_SHORT).show();
                        }
                    }, backgroundHandler);

        } catch (Exception e) {
            e.printStackTrace();
            mediaRecorder.release();
        }
    }
    private void stopRecordingVideo() {
        mediaRecorder.stop();
        mediaRecorder.reset();
        isRecording = false;
        Toast.makeText(getContext(), "녹화가 종료되었습니다.", Toast.LENGTH_SHORT).show();
        // 세션 정리
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }
    }
    // Background Handler 설정 (필요 시)
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // MediaRecorder 설정
    private void setUpMediaRecorder() throws IOException {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 오디오 소스를 먼저 설정
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE); // 비디오 소스 설정

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile("/Card/Android/data/com.example.jungexweb/video/test.mp4");         //저장 할 곳 지정
        mediaRecorder.setVideoEncodingBitRate(10000000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(1920, 1080);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); // 오디오 인코더 설정
        mediaRecorder.setOrientationHint(90); // 회전 방향 설정
        mediaRecorder.prepare(); // 준비
    }
*/
    //동영상관련 종료

    //사진촬영관련설정
    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMDD_HHmms").format(new Date());
        String imageFileName = "jungExWeb_" + timeStamp ;
        File storegeDir = requireContext().getExternalFilesDir(DIRECTORY_PICTURES);
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
            //G내 PC\Galaxy Note9\Phone\Android\data\com.example.jungexweb\files\Pictures/ 폰이 기본 설정같다.
            values.put(MediaStore.Images.Media.RELATIVE_PATH, DIRECTORY_PICTURES + "/jungexweb/files");
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

    /* 동영상 촬영 관련 함수 3가지. surfaceCreated / surfaceChanged / surfaceDestroyed */
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {


    }

    private void refreshCamera(Camera camera) {
        if(surfaceHolder.getSurface() == null){
            return;
        }
        try{
            camera.stopPreview();
            //카메라 초기화 작업
        }catch (Exception e){
            e.printStackTrace();
        }
        setCamera(camera);
    }
    private void setCamera(Camera cam){
        camera = cam;
    }
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        //미리보기 화면감지하여 변화되면 카메라를 초기화 시키기 위해
        refreshCamera(camera);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }
}