package com.mbinfo.demoforffmpeg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.mbinfo.demoforffmpeg.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private static final int REQUEST_TAKE_GALLERY_VIDEO_ONE = 1001;
    private static final int REQUEST_TAKE_GALLERY_VIDEO_TWO = 1008;
    String selectedVideoPathFirst;
    String secondPath;
    String mixpath;
    FFmpeg ffmpeg;
    List<String> cmdList = new ArrayList<>();
    long startTime;
    PermissionUtil permissionUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        permissionUtil = new PermissionUtil();
        checkAndRequestPermission();
        initlayout();
    }

    private void initlayout() {
        ffmpeg = FFmpeg.getInstance(MainActivity.this);
        binding.firstbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_TAKE_GALLERY_VIDEO_ONE);


            }
        });
        binding.secondbutton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_TAKE_GALLERY_VIDEO_TWO);


            }
        });
        binding.merge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mergevideo();
            }
        });
    }

    private void mergevideo() {
               String cmd[] = {"-y", "-i", selectedVideoPathFirst, "-i", secondPath, "-i", "-filter_complex",
                "[v0][0:a][v1][1:a][v2][2:a]concat=n=2:v=1:a=1",
                "-ab", "48000", "-ac", "2", "-ar", "22050", "-s", "-vcodec", "libx264", "-crf", "27", "-preset", "ultrafast", mixpath};
        try {
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    try {
                        long endTime = System.currentTimeMillis();
                        long result = endTime - startTime;
                        saveVideo();
                        Toast.makeText(MainActivity.this, "Videos are merged", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(String message) {
                }

                @Override
                public void onProgress(String message) {

                }

                @Override
                public void onStart() {
                    startTime = System.currentTimeMillis();
                }

                @Override
                public void onFinish() {

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    private void saveVideo() throws URISyntaxException {
        File createdvideo = null;
        ContentResolver resolver = getContentResolver();
        String videoFileName = "video_" + System.currentTimeMillis() + ".mp4";
        ContentValues valuesvideos;
        valuesvideos = new ContentValues();
        if (Build.VERSION.SDK_INT >= 29) {
            valuesvideos.put(MediaStore.Video.Media.RELATIVE_PATH, "Demo/" + "Folder");
            valuesvideos.put(MediaStore.Video.Media.TITLE, videoFileName);
            valuesvideos.put(MediaStore.Video.Media.DISPLAY_NAME, videoFileName);
            valuesvideos.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            valuesvideos.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);

            Uri collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            mixpath = String.valueOf(resolver.insert(collection, valuesvideos));


        } else {

            String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Environment.DIRECTORY_MOVIES + "/" + "YourFolder";

            createdvideo = new File(directory, videoFileName);

            valuesvideos.put(MediaStore.Video.Media.TITLE, videoFileName);
            valuesvideos.put(MediaStore.Video.Media.DISPLAY_NAME, videoFileName);
            valuesvideos.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            valuesvideos.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);

            valuesvideos.put(MediaStore.Video.Media.DATA, createdvideo.getAbsolutePath());
            mixpath = String.valueOf(getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, valuesvideos));


        }


        if (Build.VERSION.SDK_INT >= 29) {
            valuesvideos.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
            valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 1);


        }


        ParcelFileDescriptor pfd;

        try {
            pfd = getContentResolver().openFileDescriptor(Uri.parse(mixpath), "w");

            FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());

            // get the already saved video as fileinputstream

            // The Directory where your file is saved
            File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "Folder");


            //Directory and the name of your video file to copy
            File videoFile = new File(storageDir, "Myvideo");

            FileInputStream in = new FileInputStream(videoFile);


            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {

                out.write(buf, 0, len);
            }


            out.close();
            in.close();
            pfd.close();


        } catch (Exception e) {

            e.printStackTrace();
        }


        if (Build.VERSION.SDK_INT >= 29) {
            valuesvideos.clear();
            valuesvideos.put(MediaStore.Video.Media.IS_PENDING, 0);
            getContentResolver().update(Uri.parse(mixpath), valuesvideos, null, null);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_GALLERY_VIDEO_ONE) {
            // the code for request one
            Uri selectedVideoUri = data.getData();
            selectedVideoPathFirst = getPath(selectedVideoUri);
            cmdList.add(selectedVideoPathFirst);
            binding.videourl.setText(selectedVideoPathFirst);
        } else if (requestCode == REQUEST_TAKE_GALLERY_VIDEO_TWO) {
            // the code for request two
            Uri selectedVideoUri = data.getData();
            secondPath = getPath(selectedVideoUri);
            cmdList.add(secondPath);
            binding.secondedit.setText(secondPath);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);

    }


    public String getPath(Uri uri) {
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(uri, projection, null, null, null);

            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }


    private void checkAndRequestPermission() {
        permissionUtil = new PermissionUtil();
        permissionUtil.checkAndRequestPermissions(this,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}
