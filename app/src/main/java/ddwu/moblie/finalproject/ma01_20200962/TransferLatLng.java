package ddwu.moblie.finalproject.ma01_20200962;

public class TransferLatLng {
    private double lat;
    private double lng;
    private int nX;
    private int nY;

    public TransferLatLng(){}

    public TransferLatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getnX() {
        return nX;
    }

    public void setnX(int nX) {
        this.nX = nX;
    }

    public int getnY() {
        return nY;
    }

    public void setnY(int nY) {
        this.nY = nY;
    }

    public void transfer() {
        // 기초 변수 지정
        double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도 1(degree)
        double SLAT2 = 60.0; // 투영 위도 2(degree)
        double OLON = 126.0; // 기준점 경도
        double OLAT = 38.0; // 기준점 위도
        double OX = 43; // 기준점 X좌표
        double OY = 136; // 기준점 Y좌표

        // 기초 설정
        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double oLon = OLON * DEGRAD;
        double oLat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2) / Math.log(sn));
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + oLat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        // 위경도 -> 좌표 변환 실행
        double ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lng * DEGRAD - oLon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        nX = (int)Math.floor(ra * Math.sin(theta) + OX + 0.5);
        nY = (int)Math.floor(ro - ra * Math.cos(theta) + OY + 0.5);
    }
}
