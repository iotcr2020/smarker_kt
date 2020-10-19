package com.anders.SMarker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anders.SMarker.utils.AlarmDlg;
import com.anders.SMarker.utils.AppVariables;
import com.anders.SMarker.utils.BottomNavigationViewHelper;
import com.anders.SMarker.utils.Tools;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.anders.SMarker.http.NetworkTask;

public class UserInfoActivity extends AppCompatActivity {

    private Uri imgUri, photoURI, albumURI;

    private String mCurrentPhotoPath;

    private static final int FROM_CAMERA = 0;
    private static final int FROM_ALBUM = 1;
    private static final int REQUEST_IMAGE_CROP = 4444;
    private boolean permission = false;
    private static int selectItem = -1;

    ImageView user_image;

    File photoFile,storageDir = null;
    File fileName = null;
    Uri contentUri;

    final String uploadFilePath = "storage/emulated/0/Pictures/userinfo/";
    private BottomNavigationView navigation;
    int serverResponseCode = 0;
    Bitmap btmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        AppVariables.activitySet = UserInfoActivity.this;
        initComponent();
        initToolbar();
        //LoadFamilyPhoto();

        /*user_image = (ImageView)findViewById(R.id.user_image);

        TextView user_name_val = (TextView)findViewById(R.id.user_name_val);
        TextView user_team_val = (TextView)findViewById(R.id.user_team_val);
        TextView user_hp_val = (TextView)findViewById(R.id.user_hp_val);
        TextView user_email_val = (TextView)findViewById(R.id.user_email_val);*/
        TextView helmet_id_val = (TextView)findViewById(R.id.helmet_id_val);
        TextView strip_id_val = (TextView)findViewById(R.id.strip_id_val);

        /*user_name_val.setText(AppVariables.User_Name);
        user_team_val.setText(AppVariables.User_Team);
        user_hp_val.setText(AppVariables.User_Phone_Number);
        user_email_val.setText(AppVariables.User_Email);*/
        helmet_id_val.setText(AppVariables.Helmet_Mac_Adress);
        strip_id_val.setText(AppVariables.Strip_Mac_Adress);
    }

    private void initComponent() {


        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.getMenu().findItem(R.id.bottom_info).setChecked(true);
        if(AppVariables.User_Permission.equals("Y")) {
            navigation.findViewById(R.id.bottom_team).setVisibility(View.VISIBLE);
        }else{
            navigation.findViewById(R.id.bottom_team).setVisibility(View.GONE);
        }
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.bottom_main:

                        if(AppVariables.activitySet!=null){
                            AppVariables.activitySet.finish();
                        }
                        MainActivity.navigation.getMenu().findItem(R.id.bottom_main).setChecked(true);
                        //navigation.getMenu().findItem(R.id.bottom_main).setChecked(true);
                        onBackPressed();
                        return true;
                    case R.id.bottom_work:
                         Intent intent2 = new Intent(getApplicationContext(), WorkMainActivity.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent2);
                        if(AppVariables.activitySet!=null){
                            AppVariables.activitySet.finish();
                        }
                        return true;
                    case R.id.bottom_message:
                        Intent intent3 = new Intent(getApplicationContext(), MessageListActivity.class);
                        intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent3);
                        if(AppVariables.activitySet!=null){
                            AppVariables.activitySet.finish();
                        }
                        return true;
                    case R.id.bottom_team:
                        Intent intent4 = new Intent(getApplicationContext(), ListTeamInfo.class);
                        intent4.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent4);
                        if(AppVariables.activitySet!=null){
                            AppVariables.activitySet.finish();
                        }
                        return true;
                }
                return false;
            }
        });

    }
    private void LoadFamilyPhoto(){
        //사진 있으면 뿌려주기
        /*SharedPreferences auto = getSharedPreferences("myimage", Activity.MODE_PRIVATE);
        if (auto !=null)
        {
            String image=auto.getString("myimagestrings", "");
            Bitmap bitmap = AppVariables.StringToBitMap(image);
            if(bitmap !=null) {
                ImageView iv = (ImageView) findViewById(R.id.user_image);
                iv.setImageBitmap(bitmap);
            }
        }*/
    }
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("프로필");
        toolbar.setTitleTextColor(getResources().getColor(R.color.mainColor));
        toolbar.setTitleTextAppearance(getApplicationContext(),R.style.TextAppearance_Subhead_Bold);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this,R.color.grey_60);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        Tools.changeMenuIconColor(menu, getResources().getColor(R.color.grey_60));

        MenuItem item = menu.findItem(R.id.btnEmergency);
        Drawable icon = getResources().getDrawable(R.drawable.emergency);
        icon.setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);
        item.setIcon(icon);

        MenuItem item_set = menu.findItem(R.id.action_settings);
        Drawable icon_set = getResources().getDrawable(R.drawable.popup_setting);
        icon_set.setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);
        item_set.setIcon(icon_set);

        if(AppVariables.User_Permission.equals("Y")){
           // MenuItem item_p = menu.findItem(R.id.bottom_team);
           // item_p.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }else if (item.getItemId()== R.id.btnEmergency){
            AlarmDlg.showAlarmDialog(this,"긴급"); //권한 허용 시 비상 알림 띄우기

        }else if (item.getItemId()== R.id.action_settings){
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);


        }
        return super.onOptionsItemSelected(item);
    }

    public void onPause(){
        super.onPause();
    }

    public void clickSetting(View view) {//메뉴 클릭 시 환경설정

        /*int id = view.getId();
        switch (id) {
            case R.id.user_image://사용자 이미지 추가
                carmeraImageSelect();
                break;
        }*/
    }

    private boolean tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
                //oast.makeText(UserInfoActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();

                if(!permission){
                    if(selectItem == 0){//사진 촬영
                        takePhoto();
                    }else if(selectItem == 1){//앨범에서 사진 선택
                        selectAlbum();
                    }else if(selectItem == 2){//사진 촬영 서버 전송
                        takePhoto();
                    }else if(selectItem == 3){//앨범에서 사진 선택, 서버 전송
                        selectAlbum();
                    }
                }
                permission = true;
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                // 권한 요청 실패
                permission = false;
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

        return permission;

    }


    private void carmeraImageSelect() {//사용자 이미지 넣기
        final List<String> ListItems = new ArrayList<>();
        ListItems.add("사진 촬영");
        ListItems.add("앨범에서 선택");
        ListItems.add("사진 촬영 및 서버 전송");
        ListItems.add("앨범 선택 및 서버 전송");
        final CharSequence[] items =  ListItems.toArray(new String[ ListItems.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText("이미지 설정하기");
        title.setBackgroundColor(getResources().getColor(R.color.teal_400));
        title.setPadding(15, 15, 15, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);

        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int pos) {
                selectItem = pos;
                if(tedPermission()){
                    if(pos == 0){//사진 촬영
                        takePhoto();
                    }else if(pos == 1){//앨범에서 사진 선택
                        selectAlbum();
                    }else if(pos == 2){//사진 촬영 및 서버 전송
                        takePhoto();
                    }else if(pos == 3){//앨범 선택 및 서버 전송
                        selectAlbum();
                    }
                }
            }
        });

        builder.show();

    }

    //사진 찍기 클릭

    public void takePhoto(){
        String state = Environment.getExternalStorageState();
        //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(Environment.MEDIA_MOUNTED.equals(state)){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(intent.resolveActivity(getPackageManager())!=null){
                try{
                    photoFile = createImageFile();
                }catch (IOException e){
                }
                if(photoFile!=null){
                    Uri providerURI = FileProvider.getUriForFile(this,getPackageName(),photoFile);
                    imgUri = providerURI;
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);
                    startActivityForResult(intent, FROM_CAMERA);
               }
            }
        }else{

            return;
        }
    }

    public File createImageFile() throws IOException{

        String imgFileName = AppVariables.User_Phone_Number + ".jpg";
        File imageFile= null;

        storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "userinfo");
        if(storageDir.exists()){
            DeleteDir();
        }else{
            storageDir.mkdirs();
        }

        imageFile = new File(storageDir,imgFileName);
        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;

    }
    //사진 삭제
    private void DeleteDir()
    {
        try{
            File file = new File(Environment.getExternalStorageDirectory() + "/Pictures/userinfo/");
            File[] flist = file.listFiles();

            for(int i = 0 ; i < flist.length ; i++)
            {
                flist[i].delete();

                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                scanIntent.setData(Uri.fromFile(flist[i]));
                sendBroadcast(scanIntent);

            }
            //callBroadCast();

        }catch(Exception e){Toast.makeText(getApplicationContext(), "파일 삭제 실패 ", Toast.LENGTH_SHORT).show();}
    }

    //앨범 선택 클릭
    public void selectAlbum(){
        //앨범에서 이미지 가져옴
        //앨범 열기
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setType("image/*");
        startActivityForResult(intent, FROM_ALBUM);
    }

    public void galleryAddPic(){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        fileName = new File(mCurrentPhotoPath);
        contentUri = Uri.fromFile(fileName);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        //Toast.makeText(this,"사진이 저장되었습니다",Toast.LENGTH_SHORT).show();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK){
            return;
        }

        switch (requestCode){
            case FROM_ALBUM : {
                //앨범에서 가져오기(로컬)
                if(data.getData()!=null){
                    try{
                        File albumFile = null;
                        albumFile = createImageFile();
                        photoURI = data.getData();
                        albumURI = Uri.fromFile(albumFile);

                        cropImage();

                    }catch (Exception e){
                    }
                }
                break;
            }
            case FROM_CAMERA : {
                //카메라 촬영
                try{
                    //Log.v("알림", "FROM_CAMERA 처리");
                    galleryAddPic();

                    StoreImage(getApplicationContext(), contentUri, fileName);
                    Bitmap image = BitmapFactory.decodeFile(mCurrentPhotoPath);
                    imageSave(image);


                    if(selectItem==2) {
                        new Thread(new Runnable() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        //Log.d("start", "uploading started...");
                                    }
                                });
                                imageUpload(uploadFilePath + "" + fileName.getName());
                            }
                        }).start();
                    }

                }catch (Exception e){
                }
                break;
            }
            case REQUEST_IMAGE_CROP: {
                try{
                    galleryAddPic();
                    Bitmap bitmap = MediaStore.Images.Media
                            .getBitmap(getContentResolver(), albumURI);
                    imageSave(bitmap);
                    imageUpload(mCurrentPhotoPath);

                    if(selectItem==3) {
                        new Thread(new Runnable() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                       // Log.d("start", "uploading started...");
                                    }
                                });
                                imageUpload(uploadFilePath + "" + fileName.getName());
                            }
                        }).start();
                    }
                }catch (Exception e){
                }
                break;
            }

        }
    }

    private void StoreImage(Context applicationContext, Uri imgUri, File photoFile) {
    }

    public void imageSave(Bitmap bitmap){
        try {

            // 이미지를 상황에 맞게 회전시킨다
            ExifInterface exif = new ExifInterface(mCurrentPhotoPath);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int exifDegree = exifOrientationToDegrees(exifOrientation);
            bitmap = AppVariables.rotateImage(bitmap, exifDegree);

            user_image.setImageBitmap(bitmap);

            //사진 값 저장
            String image = AppVariables.BitMapToString(bitmap);
            SharedPreferences pref = getSharedPreferences("myimage",MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("myimagestrings",image);

            editor.commit();
        }catch (Exception e){
        }
    }

    public void cropImage(){//앨범선택 - 사진 자르기
    }


    public int exifOrientationToDegrees(int exifOrientation) {//이미지 회전현상 해결
      if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        } return 0;
    }




    //사진 서버 업로드
    private int imageUpload(final String fileName) {
        String urlString = NetworkTask.API_IMAGE_UPLOAD_SERVER + "/" + AppVariables.User_Idx;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(fileName);
        if (!sourceFile.isFile()) {

            runOnUiThread(new Runnable() {
                public void run() {
                    Log.d("Source File not exist :","error"+uploadFilePath + "" + fileName);
                }
            });
            return 0;
        }
        else
        {

            try {
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs

                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""

                        + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                // create a buffer of  maximum size

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();
                Log.i("uploadFile", "HTTP Response is : "  + serverResponseMessage + ": " + serverResponseCode);
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(UserInfoActivity.this, "저장되었습니다.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch (MalformedURLException ex) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(UserInfoActivity.this, "연결 실패되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        //Toast.makeText(UserInfoActivity.this, "저장 오류가 발생했습니다. ", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return serverResponseCode;
        } // End else block
    }

    @Override
    public void onBackPressed() {
        MainActivity.navigation.getMenu().findItem(R.id.bottom_main).setChecked(true);
        super.onBackPressed();

    }


}

