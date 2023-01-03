package ddwu.moblie.finalproject.ma01_20200962;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ShowClothesActivity extends AppCompatActivity {

    final static int REQ_CODE = 100;

    ListView lvClothes;
    ClothesDBHelper dbHelper;
    ShowClothesAdapter clothesAdapter;
    Cursor cursor;

    boolean isUpdated = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbview);

        lvClothes = findViewById(R.id.lvClothes);
        dbHelper = new ClothesDBHelper(this);
        clothesAdapter = new ShowClothesAdapter(this, R.layout.listview_clothes, null);
        lvClothes.setAdapter(clothesAdapter);

        // 클릭 시 수정
        lvClothes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(ShowClothesActivity.this, UpdateClothesActivity.class);
                intent.putExtra("id", id);
                isUpdated = true;
                startActivityForResult(intent, REQ_CODE);
            }
        });

        // 롱클릭 시 삭제
        lvClothes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                final long targetId = id;	// id 값을 다이얼로그 객체 내부에서 사용하기 위하여 상수로 선언
                TextView tvName = view.findViewById(R.id.tvTitle);	// 리스트 뷰의 클릭한 위치에 있는 뷰 확인

                String dialogMessage = "'" + tvName.getText().toString() + "' 을 삭제하시겠습니까?";	// 클릭한 위치의 뷰에서 문자열 값 확인
                new AlertDialog.Builder(ShowClothesActivity.this).setTitle("삭제 확인")
                        .setMessage(dialogMessage)
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SQLiteDatabase db = dbHelper.getWritableDatabase();

                                String whereClause = ClothesDBHelper.COL_ID + "=?";
                                String[] whereArgs = new String[] { String.valueOf(targetId) };

                                db.delete(ClothesDBHelper.TABLE_NAME, whereClause, whereArgs);
                                dbHelper.close();
                                readAllContacts();		// 삭제 상태를 반영하기 위하여 전체 목록을 다시 읽음
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
                return true;
            }
        });
    }

    // 버튼 클릭 시 추가
    public void onClick(View v){
        switch(v.getId()){
            case R.id.btnAdd:
                Intent intent = new Intent(ShowClothesActivity.this, AddClothesActivity.class);
                isUpdated = true;
                startActivityForResult(intent, REQ_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isUpdated) {
            readAllContacts();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        cursor 사용 종료
        if (cursor != null) cursor.close();
    }

    private void readAllContacts() {
//        DB에서 데이터를 읽어와 Adapter에 설정
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        cursor = db.rawQuery("select * from " + ClothesDBHelper.TABLE_NAME, null);

        clothesAdapter.changeCursor(cursor);
        dbHelper.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_OK:
                isUpdated = true;        // update 를 수행하였을 경우
                break;
            case RESULT_CANCELED:
                isUpdated = false;        // update 를 취소하였을 경우
                break;
        }
    }

}
