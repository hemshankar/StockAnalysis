package util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DateTimeUtil {

    public static boolean isWeekEnd(LocalDate localDate)
    {
        String dayOfWeek = localDate.getDayOfWeek().toString();
        if("SATURDAY".equalsIgnoreCase(dayOfWeek)||
                "SUNDAY".equalsIgnoreCase(dayOfWeek))
        {
            return true;
        }
        return false;
    }

    public static boolean isMarketUp(){
        LocalTime currentTime = LocalTime.now();
        if(currentTime.isAfter(LocalTime.parse( "9:00:00" ))
                && currentTime.isBefore(LocalTime.parse( "17:00:00" ))) {
            return true;
        }
        return false;
    }

    public static void callAtMarketEnd(TimerTask task){

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,17);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);

        Date date = cal.getTime();

        Timer timer = new Timer();
        timer.schedule(task, date);
    }

    public static void callAtNextMarketStart(TimerTask task){

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,9);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        cal.add(Calendar.DATE, 1);

        Date date = cal.getTime();

        Timer timer = new Timer();
        timer.schedule(task, date);
    }
}
