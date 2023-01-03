package ddwu.moblie.finalproject.ma01_20200962;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;

public class SelectTimeActivity extends AppCompatActivity {

    public final static String TAG = "SelectTimeActivity";

    TransferLatLng transferLatLng;
    LatLng latLng;
    TimePicker startTp;
    TimePicker endTp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step2_time);

        startTp = findViewById(R.id.startTp);
        endTp = findViewById(R.id.endTp);

        // 위도/경도 변환 수행
        latLng = new LatLng(getIntent().getDoubleExtra("lat", 0),
                getIntent().getDoubleExtra("lng", 0));
        transferLatLng = new TransferLatLng(latLng.latitude, latLng.longitude);
        transferLatLng.transfer();
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.btnOk:
                Intent intent = new Intent(SelectTimeActivity.this, ShowResultActivity.class);
                // 위도/경도 변환값
                intent.putExtra("nX", transferLatLng.getnX());
                intent.putExtra("nY", transferLatLng.getnY());
                // 시간
                intent.putExtra("startHour", startTp.getHour());
                intent.putExtra("endHour", endTp.getHour());

                startActivity(intent);
                break;
        }
    }
}
