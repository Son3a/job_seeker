package com.nsb.job_seeker;

import static java.lang.Math.abs;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Program {
    public static final String url_dev = "https://job-seeker-server.onrender.com";

    public static String url_dev_img = url_dev + "/images";
    public static final String ROLE = "role";
    public static final String TOKEN = "token";
    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String COMPANY_ID = "companyId";
    public static final String sharedPreferencesName = "JobSharedPreference";
    public static final String LIST_SAVED_JOB = "listSavedJob";
    public static final String KEY_IS_SIGNED_IN = "is_signed_in";
    public static final String USER_ROLE = "user";
    public static final String ADMIN_ROLE = "admin";
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_USER_ID = "userIdFB";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEU_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEU_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static final String JOB_MODEL = "jobModel";
    public static final String LIST_RELATED_JOB = "listRelatedJob";
    public static List<String> idSavedJobs;

    public static String avatar;

    public static HashMap<String, String> remoteMsgHeaders = null;

    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAApmSiZK0:APA91bHPvzSMi7ykwF7MJd5pWzs-2tAFJ6iWbFJGFtAp4TmN5XB9fhFvLprCI7Dwy-7XuQyzC4binn91MjhIa_NEs-i2AQW6uPMvygHjEXUm-VOe4V72Pxc1jXQ7wZsW2oYFj-WhgWZF"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );

        }
        return remoteMsgHeaders;
    }

    public static String formatSalary(String salary) {
        Log.d("salary", salary);
        NumberFormat df = NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setGroupingSeparator('.');
        df.setMaximumFractionDigits(0);
        dfs.setCurrencySymbol("");
        ((DecimalFormat) df).setDecimalFormatSymbols(dfs);

        if (!salary.contains("-")) {
            if (!salary.matches(".*\\d.*")) return salary;

            String money = salary.replaceAll("[^0-9]", "");
            String moneySign = salary.replaceAll("[0-9]", "").trim();
            return df.format(Integer.parseInt(money)) + moneySign;
        } else {
            String salarys[] = salary.split("-");
            String moneySign = salarys[0].replaceAll("[0-9]", "").trim();
            salarys[0] = salarys[0].replaceAll("[^0-9]", "");
            salarys[1] = salarys[1].replaceAll("[^0-9]", "");

            String salaryFormat = df.format(Integer.parseInt(salarys[0])) + "-" + df.format(Integer.parseInt(salarys[1])) + moneySign;
            return salaryFormat;
        }
    }

    public static long calculateTime(String timeCreate) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date timeNow = new Date(System.currentTimeMillis());
        Date create = format.parse(timeCreate);
        long difference = (timeNow.getTime() - create.getTime());
        return difference;
    }

    public static String setTime(String timeDeadline) throws ParseException {
        long time = calculateTime(timeDeadline);
        if(time < 0){
            return null;
        }
        int s, m, h, date, month, year;

        s = (int) (time / 1000);
        m = s / 60;
        h = m / 60;
        date = h / 24;
        month = date / 30;
        year = month / 365;

        if (year != 0) return year + " năm";
        if (month != 0) return month + " tháng";
        if (date != 0) return date + " ngày";
        if (h != 0) return h + " giờ";
        if (m != 0) return m + " phút";
        return null;
    }

    public static String formatTimeDDMMYYYY(String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat output = new SimpleDateFormat("dd-MM-yyyy");

        String newTime = output.format(sdf.parse(time));
        return newTime;
    }

    public static boolean checkValidDeadline(int day, int month, int year) {
        LocalDate currentdate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentdate = LocalDate.now();
        }
        int currentDay = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentDay = currentdate.getDayOfMonth();
        }
        //Getting the current month
        int currentMonth = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentMonth = currentdate.getMonthValue();
        }
        //getting the current year
        int currentYear = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            currentYear = currentdate.getYear();
        }

        if (year > currentYear) return true;
        if (year == currentYear && month > currentMonth) return true;
        if (year == currentYear && month == currentMonth && day > currentDay) return true;

        return false;
    }

    public static void addNewLine(CharSequence text, int lengthBefore, int lengthAfter, EditText edtText) {
        if (lengthAfter > lengthBefore) {
            if (text.toString().length() == 1) {
                text = "• " + text;
                edtText.setText(text);
                edtText.setSelection(edtText.getText().length());
            }

            if (text.toString().endsWith("\n")) {
                text = text.toString().replace("\n", "\n• ");
                text = text.toString().replace("• •", "•");
                text = text.toString().replace("\n• \n• ", "\n• ");
                text = text.toString().replace("• \n• ", "• ");
                edtText.setText(text);
                edtText.setSelection(edtText.getText().length());
            }
        }
    }

    public static String formatStringFromBullet(String oldString) {
        String newString = "";
        String temp = "";
        String[] listString = oldString.substring(1).split("•");
        for (int i = 0; i < listString.length; i++) {
            temp = listString[i].trim().replace(".", "");
            newString = newString + temp + ".";
        }
        return newString;
    }


    public static String formatStringToBullet(String string) {
        boolean containsHTMLTag = string.matches(".*\\<[^>]+>.*");
        String[] listString;
        String newString = "";
        if (containsHTMLTag) {
            String htmlString = String.valueOf(Html.fromHtml(string, Html.FROM_HTML_MODE_LEGACY));
            Log.d("HTML", htmlString);
            listString = htmlString.split("\n");
        } else {
            listString = string.split("\\.");
        }

        for (int i = 0; i < listString.length; i++) {
            String term = listString[i].trim();
            if (!term.equals("")) {
                newString = newString + "• " + term + "\n";
            }
        }

        return newString.substring(0, newString.length() - 1);
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
