package ddwu.moblie.finalproject.ma01_20200962.model.json;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WeatherForecastService {
    //경로 지정(base uri는 retrofit 생성할 때 지정해 주기 때문에 뒷부분만 지정)
    @GET("/1360000/VilageFcstInfoService_2.0/getVilageFcst")

    // Call type의 요청(request)을 위한 메소드 지정(실제 요청을 진행하지는 않음!!!!!)
    // 요청을 실행할 수 있는 Call<BoxOfficeType> 객체를 return
    // 어노테이션 = retrofit에서 자동으로 읽어들여서 메소드 구현
    // 메소드명 (매개변수1타입 이름, 매개변수2타입 이름, ...)
    Call<WeatherRoot> getResponse (@Query("serviceKey") String key, @Query("pageNo") String pageNo,
                                   @Query("numOfRows") String rowNum, @Query("dataType") String type,
                                   @Query("base_date") String date, @Query("base_time") String time,
                                   @Query("nx") String nx, @Query("ny") String ny);
}
