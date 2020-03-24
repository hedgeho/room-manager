package com.example.shproj;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.LinkedList;

import static com.example.shproj.MainActivity.Reservation;
import static com.example.shproj.MainActivity.connect;
import static com.example.shproj.MainActivity.log;
import static com.example.shproj.MainActivity.reservations;
import static com.example.shproj.MainActivity.rooms;

public class AddActivity extends AppCompatActivity {

    String reason;
    int selectedRoom = -1;
    int mode = 0;
    Calendar date;
    Reservation[] filtered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        setTitle("Бронирование");

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView lv = findViewById(R.id.lv_rooms);
        lv.setAdapter(new RoomAdapter());

        date = Calendar.getInstance();

        EditText et = findViewById(R.id.add_et_name);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0 || selectedRoom == -1) {
                    hideButton();
                } else {
                    showButton();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        findViewById(R.id.fab_go).setOnClickListener(v -> {
            if(mode == 0) {
                findViewById(R.id.screen_room).setVisibility(View.INVISIBLE);
                findViewById(R.id.screen_date).setVisibility(View.VISIBLE);
                reason = et.getText().toString();
            } else if(mode == 1) {
                LinkedList<Reservation> list = new LinkedList<>();
                long sum = 0, start, end;
                for (Reservation res: reservations) {
                    if(res.startTime < date.getTimeInMillis() + 24 * 60 * 60000L && res.endTime > date.getTimeInMillis()) {
                        list.add(res);
                        start = res.startTime;
                        end = res.endTime;
                        if(res.startTime < date.getTimeInMillis()) {
                            start = date.getTimeInMillis();
                        }
                        if(res.endTime > date.getTimeInMillis() + 24 * 60 * 60000L) {
                            end = date.getTimeInMillis() + 24 * 60 * 60000L;
                        }
                        sum += (end - start);
                    }
                }
                if(24 * 60 * 60000L - sum < 15*60*1000)
                    Toast.makeText(this, "Этот день полностью занят", Toast.LENGTH_SHORT).show();
                else {
                    findViewById(R.id.screen_date).setVisibility(View.INVISIBLE);
                    findViewById(R.id.screen_time).setVisibility(View.VISIBLE);
                    filtered = list.toArray(new Reservation[0]);
                    hideButton();
                }
            } else if(mode == 2) {
                TimePicker picker = findViewById(R.id.picker);
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(date.getTimeInMillis());
                c.set(Calendar.HOUR_OF_DAY, picker.getHour());
                c.set(Calendar.MINUTE, picker.getMinute());
                long start = c.getTimeInMillis();
                picker = findViewById(R.id.picker2);
                c.set(Calendar.HOUR_OF_DAY, picker.getHour());
                c.set(Calendar.MINUTE, picker.getMinute());
                long end = c.getTimeInMillis();

                if(validateTime() == null) {
                    new Thread(() -> {
                        SharedPreferences pref = getSharedPreferences("pref", 0);
                        String response = connect("reserve", "classNumber=" + rooms[selectedRoom].classNumber +
                                "&teacherId=" + pref.getString("prsId", "") + "&reason=" + reason +
                                "&startTime=" + start + "&endTime=" + end, pref.getString("cookie", ""));
                        if(!response.equals("success"))
                            runOnUiThread(() -> Toast.makeText(this, "Что-то пошло не так", Toast.LENGTH_SHORT).show());
                        else
                            setResult(1);
                        finish();
                    }).start();
                }
            }
            mode++;
        });

        CalendarView calendar = findViewById(R.id.calendarView);
        calendar.setMinDate(System.currentTimeMillis());
        calendar.setMaxDate(System.currentTimeMillis() + (30L * 24 * 60 * 60000));
        calendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            date.set(Calendar.YEAR, year);
            date.set(Calendar.MONTH, month);
            date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        });

        TextView tv = findViewById(R.id.add_tv_error);
        TimePicker picker = findViewById(R.id.picker);
        picker.setIs24HourView(true);
        TimePicker.OnTimeChangedListener listener = (view, hourOfDay, minute) -> {
            String s = validateTime();
            if(s == null) {
                tv.setVisibility(View.INVISIBLE);
                showButton();
            } else {
                tv.setVisibility(View.VISIBLE);
                tv.setText(s);
                hideButton();
            }
        };
        picker.setOnTimeChangedListener(listener);
        picker = findViewById(R.id.picker2);
        picker.setIs24HourView(true);
        picker.setOnTimeChangedListener(listener);
    }

    String validateTime() {
        TimePicker picker = findViewById(R.id.picker),
                picker1 = findViewById(R.id.picker2);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date.getTimeInMillis());
        c.set(Calendar.HOUR_OF_DAY, picker.getHour());
        c.set(Calendar.MINUTE, picker.getMinute());
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(date.getTimeInMillis());
        c1.set(Calendar.HOUR_OF_DAY, picker1.getHour());
        c1.set(Calendar.MINUTE, picker1.getMinute());

        if(c.getTimeInMillis() > c1.getTimeInMillis()) {
            return "Время окончания меньше времени начала";
        } else if(c.getTimeInMillis() + 5*60000 > c1.getTimeInMillis())
            return "Мероприятие не может быть короче пяти минут";

        boolean ok = true;
        for (Reservation res : reservations) {
            if(res.startTime < c1.getTimeInMillis() && res.endTime > c.getTimeInMillis()) {
                ok = false;
                break;
            }
        }
        if(ok)
            return null;
        else
            return "На это время уже есть бронирование";
    }

    void showButton() {
        if(findViewById(R.id.fab_go).getVisibility() == View.VISIBLE)
            return;
        Animation animation1 = new AlphaAnimation(0.0f, 1.0f);
        animation1.setDuration(500);
        findViewById(R.id.fab_go).startAnimation(animation1);
        findViewById(R.id.fab_go).setVisibility(View.VISIBLE);
    }

    void hideButton() {
        findViewById(R.id.fab_go).setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        switch (mode) {
            case 1:
                findViewById(R.id.screen_date).setVisibility(View.INVISIBLE);
                findViewById(R.id.screen_room).setVisibility(View.VISIBLE);
                mode--;
                showButton();
                break;
            case 2:
                findViewById(R.id.screen_time).setVisibility(View.INVISIBLE);
                findViewById(R.id.screen_date).setVisibility(View.VISIBLE);
                mode--;
                showButton();
                break;
            default:
                super.onBackPressed();
        }
    }

    class RoomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return rooms.length;
        }

        @Override
        public Object getItem(int position) {
            return rooms[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if(convertView != null)
                view = convertView;
            else
                view = getLayoutInflater().inflate(R.layout.template_room, parent, false);
            TextView tv = view.findViewById(R.id.tv_roomnumber);
            tv.setText(rooms[position].classNumber);
            tv = view.findViewById(R.id.tv_roomtype);
            log("description: " + rooms[position].typeDescription);
            tv.setText(rooms[position].typeDescription);
            tv = view.findViewById(R.id.tv_seats);
            tv.setText(rooms[position].seats + " мест");
            tv = view.findViewById(R.id.tv_responsible);
            tv.setText(rooms[position].responsibleFio);
            view.setOnClickListener(v -> {
                TextView room = findViewById(R.id.tv_selected);
                room.setText(rooms[position].classNumber);
                room.setVisibility(View.VISIBLE);
                selectedRoom = position;
                Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
                animation1.setDuration(500);
                v.startAnimation(animation1);
                if(((EditText) findViewById(R.id.add_et_name)).getText().toString().length() == 0 || selectedRoom == -1) {
                    hideButton();
                } else {
                    showButton();
                }
            });
            return view;
        }
    }
}