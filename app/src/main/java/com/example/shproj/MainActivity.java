package com.example.shproj;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static Reservation[] reservations;
    String schedule;
    Map<Integer, String> teachers; //prsId -> fio
//    ActivityMainBinding binding;
    PageFragment[] fragments;
    String[] daysStrings = {"пн", "вт", "ср", "чт", "пт", "сб", "вс"};
    TextView[] daysTV;

    ViewPager pager;

    int day, dayOfWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        binding = ActivityMainBinding.inflate(getLayoutInflater());

        new Thread(() -> {
            long start = System.currentTimeMillis() - 2592000000L; // 30 days
            long end = System.currentTimeMillis() + 2592000000L;
            schedule = connect("https://shproj2020.herokuapp.com/schedule?startTime=" + start + "&endTime=" + end,
                    null);

            String teachers = connect("https://shproj2020.herokuapp.com/get_teacher_list", null);

            try {
                JSONArray tchr = new JSONArray(teachers);
                JSONObject obj;
                this.teachers = new HashMap<>();
                for (int i = 0; i < tchr.length(); i++) {
                    obj = tchr.getJSONObject(i);
                    this.teachers.put(obj.getInt("prsId"), obj.getString("fio"));
                }

                JSONArray array = new JSONArray(schedule);

                reservations = new Reservation[array.length()];
                for (int i = 0; i < reservations.length; i++) {
                    obj = array.getJSONObject(i);
                    reservations[i] = new Reservation();
                    reservations[i].classNumber = obj.getString("classNumber");
                    reservations[i].customerId = obj.getInt("customerId");
                    reservations[i].startTime = obj.getLong("startTime");
                    reservations[i].endTime = obj.getLong("endTime");
                    reservations[i].reason = obj.getString("reason");
                    reservations[i].reservationId = obj.getInt("reservationId");
                    reservations[i].teacherId = obj.getInt("teacherId");
                }
                runOnUiThread(() -> {

//                    ListView lv = MainActivity.this.findViewById(R.id.lv_main);

//                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, array);
//                    lv.setAdapter(adapter);
                });

            } catch (Exception e) {
                loge(e);
            }
        }).start();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        final long EPOCH = 1577826000000L; // 01/01/2020
        long oneDay = 86400000;
        int daysFromBeginning = (int) ((calendar.getTimeInMillis() - EPOCH)/oneDay); // days from 01/01/2020
        log("days from 01/01/2020: " + daysFromBeginning);

        fragments = new PageFragment[daysFromBeginning + 30]; // todo how many days in the future
        long date = EPOCH;
        for (int i = 0; i < fragments.length; i++) {
            fragments[i] = new PageFragment();
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(date);
            date += oneDay;
//            calendar.add(Calendar.DAY_OF_MONTH, i);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            fragments[i].c = calendar;
        }

        day = daysFromBeginning;
        pager = findViewById(R.id.pager);
        if(pager.getAdapter() == null) {
            MyFragmentPagerAdapter pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
            log("pager adapter created");
            pager.setAdapter(pagerAdapter);
        }
        pager.setCurrentItem(day);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                dayOfWeek = fragments[position].c.get(Calendar.DAY_OF_WEEK)-2;
                if(dayOfWeek == -1)
                    dayOfWeek = 6;
                day = position;
                log(day + ", dayofweek: " + dayOfWeek);
                makeDays(daysTV, dayOfWeek);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        daysTV = new TextView[7];
        calendar = Calendar.getInstance();
        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)-2;
        if(dayOfWeek == -1)
            dayOfWeek = 6;
        makeDays(daysTV, dayOfWeek);

    }

    public void makeDays(TextView[] tv, int selected) { // RAR 1.5.1 legacy: okras()
        for (int i = 0; i < 7; i++) {
            if(tv[i] == null) {
                tv[i] = new TextView(this);
                tv[i].setId(i);
                tv[i].setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                p.weight = (float) 1 / 7;
                tv[i].setLayoutParams(p);
                final int finalI = i;
                tv[i].setOnClickListener(v -> {
                    day -= (dayOfWeek - finalI);
                    dayOfWeek = finalI;
                    pager.setCurrentItem(day);
                });
                LinearLayout linear1 = findViewById(R.id.days);
                linear1.setWeightSum(1);
                linear1.addView(tv[i]);
            }
            ForegroundColorSpan color;

            if(i == selected){
                tv[i].setBackground(getDrawable(R.drawable.day_cell));
                tv[i].setTextColor(Color.parseColor("#38423B"));
                color = new ForegroundColorSpan(Color.parseColor("#38423B"));
            } else {
                tv[i].setBackground(null);
                tv[i].setTextColor(Color.BLACK);
                color = new ForegroundColorSpan(Color.BLACK);
            }

            String s = daysStrings[i] + "\n" + fragments[day - dayOfWeek + i].c.get(Calendar.DAY_OF_MONTH);
            Spannable spans = new SpannableString(s);
            spans.setSpan(new RelativeSizeSpan(1.3f), 0, s.indexOf("\n"), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans.setSpan(color, 0, s.indexOf("\n"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spans.setSpan(new RelativeSizeSpan(1.2f), s.indexOf("\n"), s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            spans.setSpan(color, s.indexOf("\n"), s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv[i].setText(spans);
        }
    }

    class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        MyFragmentPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }
    }

    class Reservation {
        int reservationId, teacherId, customerId;
        String classNumber, reason;
        long startTime, endTime;
    }

    static String connect(String url, String query) {
        return connect(url, query, false);
    }
    static String connect(String url, String query, boolean ignore) {
        if(!ignore)
            log("connect " + url + ", query: " + query);
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            if (query == null) {
                con.setRequestMethod("GET");
                con.connect();
            } else {
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                con.getOutputStream().write(query.getBytes());
                con.connect();
            }
            if(con.getResponseCode() != 200) {
                log("connect failed, code " + con.getResponseCode() + ", message: " + con.getResponseMessage());
//                log(url);
//                log("query: '" + query + "'");
                return "/" + con.getResponseCode();
            }
            if(con.getInputStream() != null) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line;
                StringBuilder result = new StringBuilder();
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
                if(!ignore)
                    log("connect result: " + result.toString());
                return result.toString();
            } else
                return "";
        } catch (Exception e) {
            loge(e);
            return "//";
        }
    }
    static <T> void log(T msg) { if(msg != null) Log.v("mylog", msg.toString()); else loge("null log");}
    static <T> void loge(T msg) {
        if(msg instanceof Exception)
            ((Exception) msg).printStackTrace();
        if(msg != null) Log.e("mylog", msg.toString()); else loge("null log");
    }
}
