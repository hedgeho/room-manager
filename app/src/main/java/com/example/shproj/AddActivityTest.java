package com.example.shproj;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Calendar;

import static com.example.shproj.MainActivity.connect;
import static com.example.shproj.MainActivity.rooms;

public class AddActivityTest extends AppCompatActivity {

    int roomSelected = -1;
    Calendar calendarFrom, calendarTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_test);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Новое бронирование");

        findViewById(R.id.tv_room).setOnClickListener(v -> {
            RoomDialog.display(getSupportFragmentManager());
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

        findViewById(R.id.img_from).setOnClickListener(v -> {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTitleText("начало");
            CalendarConstraints constraints;
//            builder.setCalendarConstraints(new CalendarConstraints(Month.))
            MaterialDatePicker<Long> picker = builder.build();
            picker.addOnPositiveButtonClickListener(selection -> {
                MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(this);
                TimePicker timePicker = new TimePicker(this);
                timePicker.setIs24HourView(true);
                b.setView(timePicker);
                b.setPositiveButton("ок", (dialog, which) -> {
                    TextView tv = findViewById(R.id.tv_from);
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(selection);
                    c.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                    c.set(Calendar.MINUTE, timePicker.getMinute());
                    calendarFrom = c;

                    tv.setText(c.get(Calendar.DAY_OF_MONTH) + "." + (c.get(Calendar.MONTH) + 1) + ", "
                            + timePicker.getHour() + ":" + timePicker.getMinute());
                    checkIfComplete();
                });
                b.setNegativeButton("отмена", null);
                b.show();
            });
            picker.show(getSupportFragmentManager(), "tag");
        });
        findViewById(R.id.img_to).setOnClickListener(v -> {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTitleText("окончание");
//            builder.setCalendarConstraints(new CalendarConstraints(Month.))
            MaterialDatePicker<Long> picker = builder.build();
            picker.addOnPositiveButtonClickListener(selection -> {
                MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(this);
                TimePicker timePicker = new TimePicker(this);
                timePicker.setIs24HourView(true);
                b.setView(timePicker);
                b.setPositiveButton("ок", (dialog, which) -> {
                    TextView tv = findViewById(R.id.tv_to);
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(selection);
                    c.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                    c.set(Calendar.MINUTE, timePicker.getMinute());
                    calendarTo = c;

                    tv.setText(c.get(Calendar.DAY_OF_MONTH) + "." + (c.get(Calendar.MONTH) + 1) + ", "
                            + timePicker.getHour() + ":" + timePicker.getMinute());
                    checkIfComplete();
                });
                b.setNegativeButton("отмена", null);
                b.show();
            });
            picker.show(getSupportFragmentManager(), "tag");
        });
        findViewById(R.id.btn_submitevent).setOnClickListener(v -> {
            String reason = et.getText().toString();
            new Thread(() -> {
                // todo handling errors and adding to rooms
                SharedPreferences pref = getSharedPreferences("pref", 0);
                String response = connect("reserve", "classNumber=" + rooms[roomSelected].classNumber +
                        "&teacherId=" + pref.getInt("prsId", 0) + "&reason=" + reason +
                        "&startTime=" + calendarFrom.getTimeInMillis() + "&endTime=" + calendarTo.getTimeInMillis(),
                        pref.getString("cookie", ""));
            }).start();
        });
        // todo filter using chips
    }

    void refreshRoom(int roomSelected) {
        this.roomSelected = roomSelected;
        Button button = findViewById(R.id.tv_room);
        button.setText(rooms[roomSelected].classNumber);
    }

    void checkIfComplete() {
        EditText et = findViewById(R.id.et_eventname);
        Button btn = findViewById(R.id.btn_submitevent);
        if(et.getText().toString().replaceAll(" ", "").length() == 0
            || roomSelected == -1 || calendarTo == null || calendarFrom == null)
            btn.setEnabled(false);
        else
            btn.setEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
