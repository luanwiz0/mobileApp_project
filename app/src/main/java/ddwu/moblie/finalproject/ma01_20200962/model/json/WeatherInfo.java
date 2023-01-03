package ddwu.moblie.finalproject.ma01_20200962.model.json;

public class WeatherInfo {
    private String category; // 분류
    private String fcstDate; // 예보 날짜
    private String fcstTime; // 예보 시각
    private String fcstValue; // 예보값(분류에 따라 다름)

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFcstDate() {
        return fcstDate;
    }

    public void setFcstDate(String fcstDate) {
        this.fcstDate = fcstDate;
    }

    public String getFcstTime() {
        return fcstTime;
    }

    public void setFcstTime(String fcstTime) {
        this.fcstTime = fcstTime;
    }

    public String getFcstValue() {
        return fcstValue;
    }

    public void setFcstValue(String fcstValue) {
        this.fcstValue = fcstValue;
    }

    @Override
    public String toString() {
        return "WeatherInfo{" +
                "category='" + category + '\'' +
                ", fcstDate='" + fcstDate + '\'' +
                ", fcstTime=" + fcstTime +
                ", fcstValue=" + fcstValue +
                '}';
    }
}
