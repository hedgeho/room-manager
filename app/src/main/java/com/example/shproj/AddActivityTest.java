package com.example.shproj;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import static com.example.shproj.MainActivity.THRESHOLD_AHEAD;
import static com.example.shproj.MainActivity.connect;
import static com.example.shproj.MainActivity.log;
import static com.example.shproj.MainActivity.oneDay;
import static com.example.shproj.MainActivity.rooms;

public class AddActivityTest extends AppCompatActivity {

    int roomSelected = -1, mode = 0;
    Calendar calendarFrom, calendarTo, calendarDate;

    Calendar calendarDateFrom;
    int daysAhead;
    boolean[] weekdays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_test);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Новое бронирование");

        findViewById(R.id.tv_room).setOnClickListener(v -> {
            RoomDialog.display(getSupportFragmentManager());
        });

        ChipGroup chipGroup = findViewById(R.id.chipGroup);
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.chip_single) {
                findViewById(R.id.layout_single).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_repeat).setVisibility(View.INVISIBLE);
                mode = 0;
            } else {
                findViewById(R.id.layout_single).setVisibility(View.INVISIBLE);
                findViewById(R.id.layout_repeat).setVisibility(View.VISIBLE);
                mode = 1;
            }
        });

        EditText et = findViewById(R.id.et_eventname);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkIfComplete();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        weekdays = new boolean[7];
        int[] chipIds = {R.id.chip_monday, R.id.chip_tuesday, R.id.chip_wednesday, R.id.chip_thursday, R.id.chip_friday,
                R.id.chip_saturday, R.id.chip_sunday};
        Chip chip;
        for (int i = 0; i < 7; i++) {
            final int j = i;
            chip = findViewById(chipIds[i]);
            chip.setOnCheckedChangeListener((v, checked) -> {
                weekdays[j] = checked;
                checkIfComplete();
            });
        }

        findViewById(R.id.img_date).setOnClickListener(v -> {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTitleText("дата");

            CalendarConstraints.Builder builder1 = new CalendarConstraints.Builder();
            CalendarConstraints.DateValidator validator = new CalendarConstraints.DateValidator() {
                @Override
                public boolean isValid(long date) {
                    return date > System.currentTimeMillis() - oneDay && date <= System.currentTimeMillis() + THRESHOLD_AHEAD*oneDay;
                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {

                }
            };
            builder1.setValidator(validator);
            builder.setCalendarConstraints(builder1.build());
            builder.setSelection(System.currentTimeMillis());

            MaterialDatePicker<Long> picker = builder.build();
            picker.addOnPositiveButtonClickListener(selection -> {
                calendarDate = Calendar.getInstance();
                calendarDate.setTimeInMillis(selection);
                ((TextView) findViewById(R.id.tv_date)).setText(formatDate(calendarDate));
                checkIfComplete();
            });
            picker.show(getSupportFragmentManager(), "tag");
        });
        findViewById(R.id.r_img_date).setOnClickListener(v -> {
            MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
            builder.setTitleText("даты");

            CalendarConstraints.Builder builder1 = new CalendarConstraints.Builder();
            CalendarConstraints.DateValidator validator = new CalendarConstraints.DateValidator() {
                @Override
                public boolean isValid(long date) {
                    return date > System.currentTimeMillis() - oneDay && date <= System.currentTimeMillis() + THRESHOLD_AHEAD*oneDay;
                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {

                }
            };
            builder1.setValidator(validator);
            builder.setCalendarConstraints(builder1.build());

            MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
            picker.addOnPositiveButtonClickListener(selection -> {
                calendarDateFrom = Calendar.getInstance();
                calendarDateFrom.setTimeInMillis(selection.first);

                daysAhead = (int) ((selection.second - selection.first)/oneDay);
                log("days ahead " + daysAhead);
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(selection.second);

                TextView tv = findViewById(R.id.r_tv_date);
                tv.setText(String.format(Locale.getDefault(), "%02d.%02d - %02d.%02d",
                        calendarDateFrom.get(Calendar.DAY_OF_MONTH), calendarDateFrom.get(Calendar.MONTH)+1,
                        c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH)+1));

                checkIfComplete();
            });
            picker.show(getSupportFragmentManager(), "tag");
        });

        View.OnClickListener fromListener = v -> {
                MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(this);
                TimePicker timePicker = new TimePicker(this);
                timePicker.setIs24HourView(true);
                timePicker.setPadding(0, 8, 0, 0);
                b.setView(timePicker);
                b.setTitle("Время начала");
                b.setPositiveButton("ок", (dialog, which) -> {
                    TextView tv = findViewById(R.id.tv_from);
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                    c.set(Calendar.MINUTE, timePicker.getMinute());
                    calendarFrom = c;

                    tv.setText(String.format(Locale.getDefault(), "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
                    tv = findViewById(R.id.r_tv_from);
                    tv.setText(String.format(Locale.getDefault(), "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
                    checkIfComplete();
                });
                b.setNegativeButton("отмена", null);
                b.show();
        };
        findViewById(R.id.img_from).setOnClickListener(fromListener);
        findViewById(R.id.r_img_from).setOnClickListener(fromListener);
        View.OnClickListener toListener = v -> {
                MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(this);
                TimePicker timePicker = new TimePicker(this);
                timePicker.setIs24HourView(true);
                timePicker.setPadding(0, 8, 0, 0);
                b.setView(timePicker);
                b.setTitle("Время окончания");
                b.setPositiveButton("ок", (dialog, which) -> {
                    TextView tv = findViewById(R.id.tv_to);
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                    c.set(Calendar.MINUTE, timePicker.getMinute());
                    calendarTo = c;

                    tv.setText(String.format(Locale.getDefault(), "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
                    tv = findViewById(R.id.r_tv_to);
                    tv.setText(String.format(Locale.getDefault(), "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
                    checkIfComplete();
                });
                b.setNegativeButton("отмена", null);
                b.show();
        };
        findViewById(R.id.img_to).setOnClickListener(toListener);
        findViewById(R.id.r_img_to).setOnClickListener(toListener);
        findViewById(R.id.btn_submitevent).setOnClickListener(v -> {
            String reason = et.getText().toString().trim();

            if(mode == 0) {
                calendarFrom.set(Calendar.MONTH, calendarDate.get(Calendar.MONTH));
                calendarFrom.set(Calendar.DAY_OF_MONTH, calendarDate.get(Calendar.DAY_OF_MONTH));
                calendarFrom.set(Calendar.YEAR, calendarDate.get(Calendar.YEAR));

                calendarTo.set(Calendar.MONTH, calendarDate.get(Calendar.MONTH));
                calendarTo.set(Calendar.DAY_OF_MONTH, calendarDate.get(Calendar.DAY_OF_MONTH));
                calendarTo.set(Calendar.YEAR, calendarDate.get(Calendar.YEAR));

                new Thread(() -> {
                    SharedPreferences pref = getSharedPreferences("pref", 0);
                    String response = connect("reserve", "classNumber=" + rooms[roomSelected].classNumber +
                                    "&teacherId=" + pref.getInt("prsId", 0) + "&reason=" + reason +
                                    "&startTime=" + calendarFrom.getTimeInMillis() + "&endTime=" + calendarTo.getTimeInMillis(),
                            pref.getString("cookie", ""));
                    if (!response.equals("success"))
                        runOnUiThread(() -> Toast.makeText(this, "Что-то пошло не так", Toast.LENGTH_SHORT).show());
                    else
                        setResult(1);
                    runOnUiThread(this::finish);
                }).start();
            } else {
                Calendar start = Calendar.getInstance();
                start.setTimeInMillis(calendarDateFrom.getTimeInMillis());
                start.set(Calendar.HOUR_OF_DAY, calendarFrom.get(Calendar.HOUR_OF_DAY));
                start.set(Calendar.MINUTE, calendarFrom.get(Calendar.MINUTE));

                new Thread(() -> {
                    SharedPreferences pref = getSharedPreferences("pref", 0);
                    String response = connect("reserve_period", "classNumber=" + rooms[roomSelected].classNumber +
                                    "&teacherId=" + pref.getInt("prsId", 0) + "&reason=" + reason +
                                    "&startTime=" + start.getTimeInMillis() + "&endTime=" + calendarTo.getTimeInMillis() +
                                    "&daysCount=" + daysAhead + "&daysOfWeek=" + Arrays.toString(weekdays)
                                    .replaceAll("[\\[\\] ]", ""),
                            pref.getString("cookie", ""));
                    if (!response.equals("success"))
                        runOnUiThread(() -> Toast.makeText(this, "Что-то пошло не так", Toast.LENGTH_SHORT).show());
                    else
                        setResult(1);
                    runOnUiThread(this::finish);
                }).start();
            }
        });
    }

    void refreshRoom(int roomSelected) {
        this.roomSelected = roomSelected;
        Button button = findViewById(R.id.tv_room);
        button.setText(rooms[roomSelected].classNumber);
    }

    void checkIfComplete() {
        EditText et = findViewById(R.id.et_eventname);
        Button btn = findViewById(R.id.btn_submitevent);
        boolean flag = false;
        for (boolean b : weekdays) {
            flag = flag || b;
        }
        if(et.getText().toString().replaceAll(" ", "").length() == 0
            || roomSelected == -1 || calendarTo == null || calendarFrom == null || mode == 0 && calendarDate == null
            || mode == 1 && (calendarDateFrom == null || daysAhead == 0 || !flag))
            btn.setEnabled(false);
        else
            btn.setEnabled(true);
    }

    String formatDate(Calendar c) {
        String date;
        Calendar now = Calendar.getInstance();
        Calendar nextDay = Calendar.getInstance();
        nextDay.roll(Calendar.DAY_OF_MONTH, 1);
        if(Math.abs(c.getTimeInMillis() - now.getTimeInMillis()) < oneDay && now.get(Calendar.DAY_OF_MONTH) == c.get(Calendar.DAY_OF_MONTH))
            date = "Сегодня";
        else if(Math.abs(c.getTimeInMillis() - now.getTimeInMillis()) < 2*oneDay
                && nextDay.get(Calendar.DAY_OF_MONTH) == nextDay.get(Calendar.DAY_OF_MONTH))
            date = "Завтра";
        else
            date = String.format(Locale.getDefault(), "%02d.%02d", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH)+1);
        return date;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    class SpinnerTimePicker extends TimePicker {
        public SpinnerTimePicker(Context context) {
            super(context);

        }
    }
}
