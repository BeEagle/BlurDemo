package com.anarchy.blurdemo;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import com.anarchy.blurdemo.blur.BlurUtils;
import com.anarchy.blurdemo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final int PICK = 1;
    BlurInfo mBlurInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(ArrayAdapter.createFromResource(this, R.array.blur_mode, android.R.layout.simple_spinner_dropdown_item), new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                mBlurInfo.selectedPosition = itemPosition;
                return true;
            }
        });
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mBlurInfo = new BlurInfo(this);
        mBlurInfo.setSourceBitmap(R.drawable.th);
        binding.setVariable(com.anarchy.blurdemo.BR.info,mBlurInfo);
    }



    public void choosePicture(View view){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.static_bitmap:
                if(item.isChecked()){
                    item.setChecked(false);
                    BlurUtils.scaledType = BlurUtils.SCALED_DEFAULT;
                }else {
                    item.setChecked(true);
                    BlurUtils.scaledType = BlurUtils.SCALED_WITH_FILTER;
                }
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK && resultCode == RESULT_OK && null != data){
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            if(cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                mBlurInfo.setSourceBitmap(BitmapFactory.decodeFile(picturePath));
            }
        }
    }
}
