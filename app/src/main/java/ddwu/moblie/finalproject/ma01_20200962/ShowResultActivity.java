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

        // listView에 adapter 설정
        dbHelper = new ClothesDBHelper(this);
        clothesAdapter = new ShowClothesAdapter(this, R.layout.listview_clothes, null);
        lvSuggest.setAdapter(clothesAdapter);

        // intent 정보 받아오기: nX, nY, 시간 정보
        int nX = getIntent().getIntExtra("nX", 0);
        int nY = getIntent().getIntExtra("nY", 0);
        startHour = getIntent().getIntExtra("startHour", 0) * 100;
        endHour = getIntent().getIntExtra("endHour", 0) * 100;

        // 요청에 필요한 기본 정보 설정
        BaseInfo baseInfo = new BaseInfo();
        baseDate = baseInfo.getBaseDate();
        baseTime = baseInfo.getBaseTime();

        apiUrl = getResources().getString(R.string.api_url);
        apiKey = getResources().getString(R.string.weather_api_key);

        // retrofit 실행
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

    // 평균기온/최고기온/최저기온 알아내서 화면에 세팅
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

    // 비/눈 여부 알아내서 화면에 세팅
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

        if(isRain) tvRain.setText("비");
        if(isSnow) tvSnow.setText("눈");
        readAllContacts();
    }

    private void readAllContacts() {
//        DB에서 데이터를 읽어와 Adapter에 설정
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        if(avgTmp > 27){
            String[] selectionArgs = new String[5];
            selectionArgs[0] = "민소매";
            selectionArgs[1] = "반팔";
            selectionArgs[2] = "반바지";
            selectionArgs[3] = "린넨";
            selectionArgs[4] = "짧은치마";
            cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                    " where " + ClothesDBHelper.COL_CATEGORY + " IN (?, ?, ?, ?, ?)", selectionArgs);
        }
        else if(avgTmp > 22){
            String[] selectionArgs = new String[4];
            selectionArgs[0] = "반팔";
            selectionArgs[1] = "셔츠";
            selectionArgs[2] = "반바지";
            selectionArgs[3] = "긴바지";
            cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                    " where " + ClothesDBHelper.COL_CATEGORY + " IN (?, ?, ?, ?)", selectionArgs);
        }
        else if(avgTmp > 19){
            String[] selectionArgs = new String[5];
            selectionArgs[0] = "셔츠";
            selectionArgs[1] = "블라우스";
            selectionArgs[2] = "긴팔티";
            selectionArgs[3] = "긴바지";
            selectionArgs[4] = "슬랙스";
            cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                    " where " + ClothesDBHelper.COL_CATEGORY + " IN (?, ?, ?, ?, ?)", selectionArgs);
        }
        else if(avgTmp > 16){
            String[] selectionArgs = new String[6];
            selectionArgs[0] = "얇은 가디건";
            selectionArgs[1] = "니트";
            selectionArgs[2] = "맨투맨";
            selectionArgs[3] = "후드티";
            selectionArgs[4] = "긴바지";
            selectionArgs[5] = "슬랙스";
            cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                    " where " + ClothesDBHelper.COL_CATEGORY + " IN (?, ?, ?, ?, ?, ?)", selectionArgs);
        }
        else if(avgTmp > 11){
            String[] selectionArgs = new String[4];
            selectionArgs[0] = "자켓";
            selectionArgs[1] = "가디건";
            selectionArgs[2] = "니트";
            selectionArgs[3] = "긴바지";
            cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                    " where " + ClothesDBHelper.COL_CATEGORY + " IN (?, ?, ?, ?)", selectionArgs);
        }
        else if(avgTmp > 8){
            String[] selectionArgs = new String[4];
            selectionArgs[0] = "트랜치코트";
            selectionArgs[1] = "야상";
            selectionArgs[2] = "점퍼";
            selectionArgs[3] = "기모바지";
            cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                    " where " + ClothesDBHelper.COL_CATEGORY + " IN (?, ?, ?, ?)", selectionArgs);
        }
        else if(avgTmp > 4){
            String[] selectionArgs = new String[4];
            selectionArgs[0] = "코트";
            selectionArgs[1] = "야상";
            selectionArgs[2] = "점퍼";
            selectionArgs[3] = "기모바지";
            cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                    " where " + ClothesDBHelper.COL_CATEGORY + " IN (?, ?, ?, ?)", selectionArgs);
        }
        else{
            String[] selectionArgs = new String[2];
            selectionArgs[0] = "패딩";
            selectionArgs[1] = "기모바지";
            cursor = db.rawQuery( "select * from " + ClothesDBHelper.TABLE_NAME +
                    " where " + ClothesDBHelper.COL_CATEGORY + " IN (?, ?)", selectionArgs);
        }

        clothesAdapter.changeCursor(cursor);
        dbHelper.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        cursor 사용 종료
        if (cursor != null) cursor.close();
    }




    // build retrofit
    public void buildRetrofit(){
        Gson gson = new GsonBuilder().setLenient().create();
        try{ // builder를 통한 retrofit 객체 생성
            retrofit = new Retrofit.Builder()
                    .baseUrl(apiUrl) // url 지정
                    .addConverterFactory(GsonConverterFactory.create(gson)) // converter 생성: 결과 파싱 후 dto로 되돌려줌
                    .client(createOkHttpClient())
                    .build();
        }catch (Exception e){
                e.printStackTrace();
        }
    }

    // 인증서 관련 처리 클래스
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

        // 받아온 정보 로그로 확인
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);

        helper.setSSLOkHttp(builder);

        return builder.build();
    }

}
