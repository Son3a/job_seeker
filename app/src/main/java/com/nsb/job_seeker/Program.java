package com.nsb.job_seeker;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class Program {
    public static String token = "";
    public static String url_dev = "http://192.168.1.10:8000";
    public static String url_product = "https://job-seeker-smy5.onrender.com";
    public static String sharedPreferencesName = "JobSharedPreference";

    public static String formatSalary(String salary) {
        NumberFormat df = NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setGroupingSeparator('.');
        dfs.setMonetaryDecimalSeparator('.');
        df.setMaximumFractionDigits(0);
        ((DecimalFormat) df).setDecimalFormatSymbols(dfs);
        return df.format(Integer.parseInt(salary)).substring(1);
    }

    public static long calculateTime(String timeCreate) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date timeNow = new Date(System.currentTimeMillis());
        Date create = format.parse(timeCreate);
        long difference = timeNow.getTime() - create.getTime();
        System.out.println("Time difference: " + difference);
        return difference;
    }

    public static String setTime(String timeCreate) throws ParseException {
        long time = calculateTime(timeCreate);
        int s, m, h, date, month, year;

        s = (int) (time / 1000);
        m = s / 60;
        h = m / 60;
        date = h / 60;
        month = date / 30;
        year = month / 365;

        if (year != 0) return "Cập nhật " + year + " năm trước";
        if (month != 0) return "Cập nhật " + month + " tháng trước";
        if (date != 0) return "Cập nhật " + date + " ngày trước";
        if (h != 0) return "Cập nhật " + h + " giờ trước";
        if (m != 0) return "Cập nhật " + m + " phút trước";
        return "Vừa mới cập nhật";
    }

    public static boolean checkValidDeadline(int day, int month, int year) {
        LocalDate currentdate = LocalDate.now();
        int currentDay = currentdate.getDayOfMonth();
        //Getting the current month
        int currentMonth = currentdate.getMonthValue();
        //getting the current year
        int currentYear = currentdate.getYear();
        System.out.println(currentDay + '/' + currentMonth + '/' + currentYear);

        if (year < currentYear) return false;
        if (month < currentMonth) return false;
        if (day < currentDay) return false;

        return true;
    }
}
