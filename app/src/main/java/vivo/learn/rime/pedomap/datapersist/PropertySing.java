package vivo.learn.rime.pedomap.datapersist;

/**
 * Created by Administrator on 2017/9/19.
 */

public class PropertySing {
    private static final PropertySing ourInstance = new PropertySing();
    private static boolean heatMapEnable = false;

    private PropertySing() {
    }

    public static PropertySing getInstance() {
        return ourInstance;
    }

    public static boolean isHeatMapEnable() {
        return heatMapEnable;
    }

    public static void setHeatMapEnable(boolean flag) {
        heatMapEnable = flag;
    }
}
