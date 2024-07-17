# 오늘 뭐입지?
### 옷차림 추천 앱
2022.11.01 ~ 2022.12.27

<br>

# 프로젝트 소개
- 날씨에 맞는 옷차림을 쉽고 빠르게 알려주는 android 앱
- 선택한 위치 및 시간의 날씨 정보(예보)에 따른 옷차림 추천
- 외출 전 옷차림 결정에 도움
- 기상청 Open API, Google Map API 이용
- 개인 프로젝트

<br>

# 주요 기능
- 자신이 가진 옷차림 목록 관리(추가, 수정, 삭제)
- 입력한 지역 및 시간 정보를 토대로 일기예보 제공
- 기온에 맞는 옷차림 추천

![image](https://github.com/user-attachments/assets/daccd37a-891d-4c2b-978f-57715310a433)
![image](https://github.com/user-attachments/assets/6a9c437a-56ea-4303-bc0b-61b3f5d996d5)
![image](https://github.com/user-attachments/assets/9ab2e4b6-f5d8-455d-b22e-0a5504ad3bda)

### [상세 매뉴얼 확인](https://github.com/user-attachments/files/16261631/01_20200962.pdf)

<br>

# 소스코드
### 기상청 api 이용
- ShowResultActivity.java
```java
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
```
```java
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
```
### 인증서 처리 
- 인증서 관련 오류 발생 -> 인증서 처리 클래스를 추가하는 것으로 해결
```java
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
```
