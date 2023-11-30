package com.nsb.job_seeker.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.nsb.job_seeker.listener.EventKeyboard;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Constant {
    public static final String url_dev = "https://job-seeker-server.onrender.com";
//    public static final String url_dev = "http://192.168.137.1:8000";

    public static final String ROLE = "role";
    public static final String TOKEN = "token";
    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String COMPANY_ID = "companyId";
    public static final String sharedPreferencesName = "JobSharedPreference";
    public static final String KEY_IS_SIGNED_IN = "is_signed_in";
    public static final String USER_ROLE = "user";
    public static final String ADMIN_ROLE = "admin";
    public static final String KEY_COLLECTION_USERS = "user";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_COMPANY = "company";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_USER_ID = "id";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static final String JOB_MODEL = "jobModel";
    public static final String LIST_RELATED_JOB = "listRelatedJob";
    public static final String COMPANY_MODEL = "companyModel";
    public static final String AVATAR = "avatar";
    public static final String NAME = "NAME_USER";
    public static final String PHONE = "PHONE_USER";
    public static final String MAIL = "MAIL_USER";
    public static final String JOB_ID = "jobId";
    public static final String BROADCAST_AVATAR = "send_avatar";
    public static final String PASSWORD_DEFAULT = "123456";
    public static final String POSITION = "position";
    public static final String BROADCAST_SAVE_JOB = "broadSaveJob";
    public static List<String> idSavedJobs, idAppliedJob;

    public static HashMap<String, String> remoteMsgHeaders = null;

    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAz4h1nOM:APA91bG6UoIhNtTfOhnpeOy54ZDgEpTcjzEE0L8eJY0QRbg3B_aLxKex7gOP8Asl0RyuZKiFPs7EcwEjlW_Ilfix-iduL2nLMI7DGylIX9qVNYDJ5WySGJtZBAn55E_rgwRrV9rfFyHw"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );

        }
        return remoteMsgHeaders;
    }


    public static String getReadableDateTime(Date date) {
        if (compareDate(new Date(), date) == 0) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
        }
        Date dayBefore = new Date(new Date().getTime() - Duration.ofDays(1).toMillis());
        if (compareDate(dayBefore, date) == 0) {
            return "Hôm qua " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
        }

        return new SimpleDateFormat("MMMM dd", Locale.getDefault()).format(date);
    }

    private static int compareDate(Date date1, Date date2) {
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTime(date1);
        calendar2.setTime(date2);
        int day1 = calendar1.get(Calendar.DAY_OF_MONTH);
        int day2 = calendar2.get(Calendar.DAY_OF_MONTH);
        int month1 = calendar1.get(Calendar.MONTH);
        int month2 = calendar2.get(Calendar.MONTH);
        int year1 = calendar1.get(Calendar.YEAR);
        int year2 = calendar2.get(Calendar.YEAR);

        if (year1 > year2) {
            return 1;
        } else if (year1 < year2) {
            return -1;
        }

        //year1 == year2
        if (month1 > month2) {
            return 1;
        } else if (month1 < month2) {
            return -1;
        }

        //year1 == year2, month1 == month2
        if (day1 > day2) {
            return 1;
        } else if (day1 < day2) {
            return -1;
        }

        //year1 == year2, month1 == month2, day1 == day2
        return 0;
    }

    public static String formatSalary(String salary) {
        NumberFormat df = NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setGroupingSeparator('.');
        df.setMaximumFractionDigits(0);
        dfs.setCurrencySymbol("");
        ((DecimalFormat) df).setDecimalFormatSymbols(dfs);

        if (!salary.contains("-")) {
            if (!salary.matches(".*\\d.*")) return salary;

            String money = salary.replaceAll("[^0-9]", "");
            return df.format(Integer.parseInt(money));
        } else {
            String salarys[] = salary.split("-");
            String moneySign = salarys[0].replaceAll("[0-9]", "").trim();
            salarys[0] = salarys[0].replaceAll("[^0-9]", "");
            salarys[1] = salarys[1].replaceAll("[^0-9]", "");

            String salaryFormat = df.format(Integer.parseInt(salarys[0])) + "-" + df.format(Integer.parseInt(salarys[1])) + moneySign;
            return salaryFormat;
        }
    }

    public static long calculateTime(String deadline) throws ParseException {
        if (deadline == null) return -1;
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date timeNow = new Date(System.currentTimeMillis());
        Date create = format.parse(deadline);
        long difference = (create.getTime() - timeNow.getTime());
        Log.d("Deadline", create.toString());
        return difference;
    }

    public static String setTime(String timeDeadline) throws ParseException {
        long time = calculateTime(timeDeadline);
        Log.d("Deadline", time + "");
        if (time < 0) {
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

    public static String formatTimeMMDDYYYY(String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat output = new SimpleDateFormat("MM-dd-yyyy");

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

    public static String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Bitmap getBitmapFromEncodedString(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static int getNumFromString(String str) {
        return Integer.parseInt(str.replaceAll("[^0-9]", ""));
    }

    public static void eventKeyBoard(View view, EventKeyboard eventKeyboard) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                view.getWindowVisibleDisplayFrame(r);
                int screenHeight = view.getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    // keyboard is opened
                    eventKeyboard.showKeyboard();
                } else {
                    // keyboard is closed
                    eventKeyboard.hideKeyboard();
                }
            }
        });
    }
}
