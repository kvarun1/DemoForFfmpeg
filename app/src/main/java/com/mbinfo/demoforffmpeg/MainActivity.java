package com.mbinfo.demoforffmpeg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private static final int REQUEST_TAKE_GALLERY_VIDEO_ONE = 1001;
    private static final int REQUEST_TAKE_GALLERY_VIDEO_TWO = 1008;
    private Button firstbutton, secondbutton, mergebutton;
    String selectedVideoPathFirst, secondPath, mixpath;
    EditText seturl, seturlsecond;
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
               /* Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);*/
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
    /*    String outFile = null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cmdList.size(); i++)
        {
            cmdList.add("-i");
            cmdList.add(cmdList.get(i));
            sb.append("[").append(i).append(":0] [").append(i).append(":1]");
        }
        sb.append(" concat=n=").append(cmdList.size()).append(":v=1:a=1 [v] [a]");
        cmdList.add("-filter_complex");
        cmdList.add(sb.toString());
        cmdList.add("-map");
        cmdList.add("[v]");
        cmdList.add("-map");
        cmdList.add("[a]");
        cmdList.add("-preset");
        cmdList.add("ultrafast");
        cmdList.add(outFile);
        sb = new StringBuilder();
        for (String str : cmdList)
        {
            sb.append(str).append(" ");
        }
        String[] cmd = cmdList.toArray(new String[cmdList.size()]);*/
       // ffmpeg -i input1.mp4 -i input2.mp4 -filter_complex '[0:v]pad=iw*2:ih[int];[int][1:v]overlay=W/2:0[vid]' -map [vid] -c:v libx264 -crf 23 -preset veryfast output1.mp4
        String cmd[] = {"-y", "-i", selectedVideoPathFirst, "-i", secondPath, "-i", "-filter_complex",
                "[v0][0:a][v1][1:a][v2][2:a]concat=n=2:v=1:a=1",
                "-ab", "48000", "-ac", "2", "-ar", "22050", "-s", "-vcodec", "libx264","-crf","27","-preset", "ultrafast", mixpath};
        try {
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    long endTime = System.currentTimeMillis();
                    long result = endTime - startTime;
                    Toast.makeText(MainActivity.this, "Videos are merged", Toast.LENGTH_SHORT).show();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        permissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
}