package ddwu.moblie.finalproject.ma01_20200962;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseInfo {
    private long now;
    private Date date;
    private SimpleDateFormat dateFormat;
    private String baseDate;
    private String baseTime;

    public BaseInfo(){
        now = System.currentTimeMillis();
        date = new Date(now);
    }

    public String getBaseDate(){
        dateFormat = new SimpleDateFormat("yyyyMMdd");
        baseDate = dateFormat.format(date);
        return baseDate;
    }

    public String getBaseTime(){
        dateFormat = new SimpleDateFormat("kk");
        int currentTime = Integer.parseInt(dateFormat.format(date));

        int base = 0;
        if(currentTime == 0 || currentTime == 1)
            base = 23;
        else if(currentTime % 3 == 2)
            base = currentTime;
        else
            base = (currentTime % 3 == 0 ? currentTime - 1 : currentTime - 2);

        if (base < 10)
            baseTime = "0" + base + "00";
        else
            baseTime = base + "00";

        return baseTime;
    }
}
