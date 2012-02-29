package archimulator.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {
    public static long toTick(Date time) {
        return time.getTime();
    }

    public static Date fromTick(long tick) {
        return new Date(tick);
    }

    public static String toString(Date date) {
//        return new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(date);
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);
    }
}
