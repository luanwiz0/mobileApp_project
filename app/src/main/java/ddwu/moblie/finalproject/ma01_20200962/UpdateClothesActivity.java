package ddwu.moblie.finalproject.ma01_20200962;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class UpdateClothesActivity extends AppCompatActivity {

    EditText etName;
    Spinner spinner;
    ImageView ivPhotoUpdate;
    ClothesDBHelper dbHelper;
    private String mCurrentPhotoPath;

    long id;
    String selectedCategory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        etName = findViewById(R.id.etUpdateName);
        spinner = findViewById(R.id.updateSpinner);
        ivPhotoUpdate = findViewById(R.id.ivPhotoUpdate);
        id = getIntent().getLongExtra("id", 0);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource
                (this, R.array.clothes_category, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCategory = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dbHelper = new ClothesDBHelper(this);
    }

    @SuppressLint("Range")
    @Override
    protected void onResume() {
        super.onResume();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                " where " + ClothesDBHelper.COL_ID + "=?", new String[] { String.valueOf(id) });
        while (cursor.moveToNext()) {
            etName.setText( cursor.getString( cursor.getColumnIndex(ClothesDBHelper.COL_NAME) ) );
            mCurrentPhotoPath = cursor.getString(cursor.getColumnIndex(ClothesDBHelper.COL_PHOTO));
            if(!mCurrentPhotoPath.equals("")) setPic();
        }
        cursor.close();
        dbHelper.close();
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnUpdate:
                /*id 를 기준으로 화면의 값으로 DB 업데이트*/
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues row = new ContentValues();
                row.put(ClothesDBHelper.COL_NAME, etName.getText().toString());
                row.put(ClothesDBHelper.COL_CATEGORY, selectedCategory);
                row.put(ClothesDBHelper.COL_PHOTO, mCurrentPhotoPath);
                String whereClause = ClothesDBHelper.COL_ID + "=?";
                String[] whereArgs = new String[] { String.valueOf(id) };
                int result = db.update(ClothesDBHelper.TABLE_NAME, row, whereClause, whereArgs);
                dbHelper.close();
                String msg = result > 0 ? "Updated!" : "Failed!";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);       // Intent 없이 실행결과 상태만 전달
                break;
            case R.id.btnUpdateCancel:
                setResult(RESULT_CANCELED);
                break;
        }
        finish();
    }

    /*사진의 크기를 ImageView에서 표시할 수 있는 크기로 변경*/
    private void setPic() {
        // Get the dimensions of the View
        int targetW = ivPhotoUpdate.getWidth();
        int targetH = ivPhotoUpdate.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
//        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        ivPhotoUpdate.setImageBitmap(bitmap);
    }
}
