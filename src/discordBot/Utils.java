package discordBot;

import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Utils {
    private static Calendar lastMatch;
    public static Calendar parseTime(String time){
        Calendar date = new GregorianCalendar();
        date.set(Calendar.YEAR, Integer.parseInt(time.substring(0, 4)));
        date.set(Calendar.MONTH, Integer.parseInt(time.substring(5, 7)) - 1);
        date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(time.substring(8, 10)));
        date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(11, 13)));
        date.set(Calendar.MINUTE, Integer.parseInt(time.substring(14,16)));
        date.set(Calendar.SECOND, Integer.parseInt(time.substring(17, 19)));
        return date;
    }

    public static Calendar getLastMatchTime() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal;/*
        Session s = Main.factory.openSession();
        Query<Calendar> q = s.createQuery("SELECT MAX(date) FROM data.MatchData", Calendar.class);
        Calendar time = q.uniqueResult();
        s.close();
        if(time == null){
            Calendar c = new GregorianCalendar();
            c.setTimeInMillis(0);
            return c;
        }else {
            return time;
        }*/
    }
}
