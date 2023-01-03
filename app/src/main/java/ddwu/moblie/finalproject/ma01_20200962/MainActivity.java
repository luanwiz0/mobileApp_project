package ddwu.moblie.finalproject.ma01_20200962;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.searchStartBtn:
                Intent intent1 = new Intent(this, SelectLocationActivity.class);
                startActivity(intent1);
                break;
            case R.id.showClothesBtn:
                Intent intent2 = new Intent(this, ShowClothesActivity.class);
                startActivity(intent2);
                break;
        }
    }
}