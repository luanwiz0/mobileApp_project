package ddwu.moblie.finalproject.ma01_20200962;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import ddwu.moblie.finalproject.ma01_20200962.model.json.WeatherForecastService;
import ddwu.moblie.finalproject.ma01_20200962.model.json.WeatherInfo;
import ddwu.moblie.finalproject.ma01_20200962.model.json.WeatherRoot;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ShowResultActivity extends AppCompatActivity {

    public static final String TAG = "ShowResultActivity";

    private Retrofit retrofit;
    private WeatherForecastService weatherForecastService;
    String apiUrl;
    String apiKey;

    List<WeatherInfo> weatherTmpList;
    List<WeatherInfo> weatherPtyList;
    String baseDate;
    String baseTime;
    int startHour;
    int endHour;

    TextView tvAverage;
    TextView tvHighTemp;
    TextView tvLowTemp;
    TextView tvRain;
    TextView tvSnow;

    ListView lvSuggest;
    ShowClothesAdapter clothesAdapter;
    ClothesDBHelper dbHelper;
    Cursor cursor;
    int avgTmp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvAverage = findViewById(R.id.tvAverage);
        tvHighTemp = findViewById(R.id.tvHighTemp);
        tvLowTemp = findViewById(R.id.tvLowTemp);
        tvRain = findViewById(R.id.tvRain);
        tvSnow = findViewById(R.id.tvSnow);
        lvSuggest = findViewById(R.id.lvSuggest);

        // listView??? adapter ??????
        dbHelper = new ClothesDBHelper(this);
        clothesAdapter = new ShowClothesAdapter(this, R.layout.listview_clothes, null);
        lvSuggest.setAdapter(clothesAdapter);

        // intent ?????? ????????????: nX, nY, ?????? ??????
        int nX = getIntent().getIntExtra("nX", 0);
        int nY = getIntent().getIntExtra("nY", 0);
        startHour = getIntent().getIntExtra("startHour", 0) * 100;
        endHour = getIntent().getIntExtra("endHour", 0) * 100;

        // ????????? ????????? ?????? ?????? ??????
        BaseInfo baseInfo = new BaseInfo();
        baseDate = baseInfo.getBaseDate();
        baseTime = baseInfo.getBaseTime();

        apiUrl = getResources().getString(R.string.api_url);
        apiKey = getResources().getString(R.string.weather_api_key);

        // retrofit ??????
        if(retrofit == null) buildRetrofit();
        weatherForecastService = retrofit.create(WeatherForecastService.class);

        Call<WeatherRoot> apiCall = weatherForecastService.getResponse(apiKey, "1", "1000",
                "JSON", baseDate, baseTime, String.valueOf(nX), String.valueOf(nY));

        apiCall.enqueue(apiCallBack);
    }

    Callback<WeatherRoot> apiCallBack = new Callback<WeatherRoot>() {
        @Override
        public void onResponse(Call<WeatherRoot> call, Response<WeatherRoot> response) {
            if(response.isSuccessful()){
                WeatherRoot weatherRoot = response.body();
                List<WeatherInfo> list = weatherRoot.getResponse().getBody().getItems().getItem();

                weatherTmpList = new ArrayList<>();
                weatherPtyList = new ArrayList<>();
                for(WeatherInfo info : list){
                    if(Integer.parseInt(info.getFcstTime()) == startHour){
                        if(info.getCategory().equals("TMP"))
                            weatherTmpList.add(info);
                        else if(info.getCategory().equals("PTY")) {
                            weatherPtyList.add(info);
                            if(startHour == endHour)
                                break;
                            startHour += 100;
                        }
                    }
                    if(startHour == 2400)
                        startHour = 0;
                }

                setTmpTextView();
                setPtyTextView();
            }
        }

        @Override
        public void onFailure(Call<WeatherRoot> call, Throwable t) {
            Log.e(TAG, t.toString());
        }
    };

    // ????????????/????????????/???????????? ???????????? ????????? ??????
    public void setTmpTextView(){
        avgTmp = 0;
        int highTmp = -100;
        int lowTmp = 100;

        for(WeatherInfo info : weatherTmpList){
            int tmp = Integer.parseInt(info.getFcstValue());
            avgTmp += tmp;
            highTmp = Math.max(tmp, highTmp);
            lowTmp = Math.min(tmp, lowTmp);
        }
        avgTmp = avgTmp / weatherTmpList.size();

        tvAverage.setText(String.valueOf(avgTmp));
        tvHighTemp.setText(String.valueOf(highTmp));
        tvLowTemp.setText(String.valueOf(lowTmp));
    }

    // ???/??? ?????? ???????????? ????????? ??????
    public void setPtyTextView(){
        boolean isRain = false;
        boolean isSnow = false;

        for(WeatherInfo info : weatherPtyList){
            int value = Integer.parseInt(info.getFcstValue());
            if(value > 0){
                isRain = true;
                if(value == 2 || value == 3)
                    isSnow = true;
            }
            if(isRain && isSnow)
                break;
        }

        if(isRain) tvRain.setText("???");
        if(isSnow) tvSnow.setText("???");
        readAllContacts();
    }

    private void readAllContacts() {
//        DB?????? ???????????? ????????? Adapter??? ??????
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        if(avgTmp > 27){
            String[] selectionArgs = new String[5];
            selectionArgs[0] = "?????????";
            selectionArgs[1] = "??????";
            selectionArgs[2] = "?????????";
            selectionArgs[3] = "??????";
            selectionArgs[4] = "????????????";
            cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                    " where " + ClothesDBHelper.COL_CATEGORY + " IN (?, ?, ?, ?, ?)", selectionArgs);
        }
        else if(avgTmp > 22){
            String[] selectionArgs = new String[4];
            selectionArgs[0] = "??????";
            selectionArgs[1] = "??????";
            selectionArgs[2] = "?????????";
            selectionArgs[3] = "?????????";
            cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                    " where " + ClothesDBHelper.COL_CATEGORY + " IN (?, ?, ?, ?)", selectionArgs);
        }
        else if(avgTmp > 19){
            String[] selectionArgs = new String[5];
            selectionArgs[0] = "??????";
            selectionArgs[1] = "????????????";
            selectionArgs[2] = "?????????";
            selectionArgs[3] = "?????????";
            selectionArgs[4] = "?????????";
            cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                    " where " + ClothesDBHelper.COL_CATEGORY + " IN (?, ?, ?, ?, ?)", selectionArgs);
        }
        else if(avgTmp > 16){
            String[] selectionArgs = new String[6];
            selectionArgs[0] = "?????? ?????????";
            selectionArgs[1] = "??????";
            selectionArgs[2] = "?????????";
            selectionArgs[3] = "?????????";
            selectionArgs[4] = "?????????";
            selectionArgs[5] = "?????????";
            cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                    " where " + ClothesDBHelper.COL_CATEGORY + " IN (?, ?, ?, ?, ?, ?)", selectionArgs);
        }
        else if(avgTmp > 11){
            String[] selectionArgs = new String[4];
            selectionArgs[0] = "??????";
            selectionArgs[1] = "?????????";
            selectionArgs[2] = "??????";
            selectionArgs[3] = "?????????";
            cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                    " where " + ClothesDBHelper.COL_CATEGORY + " IN (?, ?, ?, ?)", selectionArgs);
        }
        else if(avgTmp > 8){
            String[] selectionArgs = new String[4];
            selectionArgs[0] = "???????????????";
            selectionArgs[1] = "??????";
            selectionArgs[2] = "??????";
            selectionArgs[3] = "????????????";
            cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                    " where " + ClothesDBHelper.COL_CATEGORY + " IN (?, ?, ?, ?)", selectionArgs);
        }
        else if(avgTmp > 4){
            String[] selectionArgs = new String[4];
            selectionArgs[0] = "??????";
            selectionArgs[1] = "??????";
            selectionArgs[2] = "??????";
            selectionArgs[3] = "????????????";
            cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                    " where " + ClothesDBHelper.COL_CATEGORY + " IN (?, ?, ?, ?)", selectionArgs);
        }
        else{
            String[] selectionArgs = new String[2];
            selectionArgs[0] = "??????";
            selectionArgs[1] = "????????????";
            cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                    " where " + ClothesDBHelper.COL_CATEGORY + " IN (?, ?)", selectionArgs);
        }

        clothesAdapter.changeCursor(cursor);
        dbHelper.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        cursor ?????? ??????
        if (cursor != null) cursor.close();
    }




    // build retrofit
    public void buildRetrofit(){
        Gson gson = new GsonBuilder().setLenient().create();
        try{ // builder??? ?????? retrofit ?????? ??????
            retrofit = new Retrofit.Builder()
                    .baseUrl(apiUrl) // url ??????
                    .addConverterFactory(GsonConverterFactory.create(gson)) // converter ??????: ?????? ?????? ??? dto??? ????????????
                    .client(createOkHttpClient())
                    .build();
        }catch (Exception e){
                e.printStackTrace();
        }
    }

    // ????????? ?????? ?????? ?????????
    class SelfSigningHelper{
        private SSLContext sslContext;
        private TrustManagerFactory tmf;

        CertificateFactory cf;
        Certificate ca;
        InputStream caInput = null;

        public SelfSigningHelper() {
            setUp();
        }

        public void setUp() {
            try{
                cf = CertificateFactory.getInstance("X.509");
                caInput = getResources().openRawResource(R.raw.cert);
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
                caInput.close();

                // Create a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                // Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

                // Create an SSLContext that uses our TrustManager
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);

            }catch(CertificateException | KeyStoreException | IOException |
                    NoSuchAlgorithmException | KeyManagementException e){
                e.printStackTrace();
            }
        }

        public OkHttpClient.Builder setSSLOkHttp(OkHttpClient.Builder builder){
            builder.sslSocketFactory(sslContext.getSocketFactory(),
                    (X509TrustManager) tmf.getTrustManagers()[0]);
            return builder;
        }
    }

    private OkHttpClient createOkHttpClient() {
        SelfSigningHelper helper = new SelfSigningHelper();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // ????????? ?????? ????????? ??????
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);

        helper.setSSLOkHttp(builder);

        return builder.build();
    }

}
