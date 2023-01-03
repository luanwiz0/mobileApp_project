package ddwu.moblie.finalproject.ma01_20200962;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SelectLocationActivity extends AppCompatActivity implements OnMapReadyCallback{

    public final static String TAG = "LOCATION";
    private GoogleMap mGoogleMap;
    private List<Marker> markerList;

    EditText etLocationName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step1_map);

        etLocationName = findViewById(R.id.etLocationName);
        mapLoad();
    }

    private void mapLoad() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                LatLng latLng = (LatLng) marker.getTag();

                String dialogMessage = "해당 지역을 선택하시겠습니까?";
                new AlertDialog.Builder(SelectLocationActivity.this).setTitle("선택 확인")
                        .setMessage(dialogMessage)
                        .setPositiveButton("선택", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(SelectLocationActivity.this, SelectTimeActivity.class);
                                intent.putExtra("lat", latLng.latitude);
                                intent.putExtra("lng", latLng.longitude);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
                return true;
            }
        });
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.btnSearchLocation:
                String locationName = etLocationName.getText().toString();
                if(locationName != ""){
                    new GeoTask().execute(locationName);
                }
        }
    }

    class GeoTask extends AsyncTask<String, Void, List<Address>>{
        Geocoder geocoder = new Geocoder(SelectLocationActivity.this, Locale.getDefault());
        @Override
        protected List<Address> doInBackground(String... strings) {
            List<Address> addresses = null;
            try{
                addresses = geocoder.getFromLocationName(strings[0], 5);
            }catch(IOException e){
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerList = new ArrayList<>();

            if (addresses != null) {
                for (Address addr : addresses){
                    markerOptions.position(new LatLng(addr.getLatitude(), addr.getLongitude()));
                    markerOptions.title(addr.getAddressLine(0));
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker
                            (BitmapDescriptorFactory.HUE_AZURE));

                    Marker marker = mGoogleMap.addMarker(markerOptions);
                    marker.setTag(new LatLng(addr.getLatitude(), addr.getLongitude()));
                    markerList.add(marker);
                }
            }
        }
    }
}
