package com.example.shproj;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;

import java.util.HashMap;

import static com.example.shproj.MainActivity.connect;
import static com.example.shproj.MainActivity.loge;
import static com.example.shproj.MainActivity.roomTypes;
import static com.example.shproj.MainActivity.rooms;
import static com.example.shproj.MainActivity.teachers;

public class AddRoomActivity extends AppCompatActivity {

    Integer[] roomTypeIDs;
    String[] roomTypeDescriptions;
    int selectedItem = -1;
    EditText et_roomnum, et_seats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        roomTypeIDs = roomTypes.keySet().toArray(new Integer[0]);
        roomTypeDescriptions = roomTypes.values().toArray(new String[0]);

        et_roomnum = findViewById(R.id.et_roomnum);
        et_roomnum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkIfDone();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_seats = findViewById(R.id.et_seats);
        et_seats.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkIfDone();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        ListView lv = findViewById(R.id.lv_roomtypes);
        TextView tv = findViewById(R.id.tv_roomtype);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                roomTypeDescriptions);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener((parent, view, position, id) -> {
            Animation animation1 = new AlphaAnimation(0.3f, 1.0f);
            animation1.setDuration(500);
            view.startAnimation(animation1);
            tv.setVisibility(View.VISIBLE);
            tv.setText(roomTypeDescriptions[position]);
            selectedItem = position;
            checkIfDone();
        });
        findViewById(R.id.fab_roomtype).setOnClickListener(v -> {
            MainActivity.Room[] tmp = new MainActivity.Room[rooms.length+1];
            System.arraycopy(rooms, 0, tmp, 0, rooms.length);
            MainActivity.Room room = new MainActivity.Room();
            room.classNumber = et_roomnum.getText().toString();
            room.seats = Integer.parseInt(et_seats.getText().toString());
            room.responsible = getSharedPreferences("pref", 0).getInt("prsId", 0);
            room.responsibleFio = teachers.get(room.responsible);
            room.classType = roomTypeIDs[selectedItem];
            room.typeDescription = roomTypeDescriptions[selectedItem];
            tmp[rooms.length] = room;
            rooms = tmp;

            new Thread(() -> {
                String response = connect("add_room", "classNumber=" + room.classNumber +
                       "&seats=" + room.seats +
                        "&classType=" + room.classType + "&responsible=" + room.responsible,
                        getSharedPreferences("pref", 0).getString("cookie", ""));
                if(response.length() > 0 && response.charAt(0) == '/')
                    runOnUiThread(() -> Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show());
                runOnUiThread(this::finish);
            }).start();
        });
    }
    void checkIfDone() {
        EditText et = findViewById(R.id.et_roomnum);
        if(et.getText().toString().replaceAll(" ", "").equals(""))
            return;
        et = findViewById(R.id.et_seats);
        if(et.getText().toString().replaceAll(" ", "").equals(""))
            return;
        if(selectedItem != -1) {
            findViewById(R.id.fab_roomtype).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.fab_roomtype).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Добавить тип класса").setIcon(R.drawable.add)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            onBackPressed();
        else if(item.getItemId() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Добавление типа класса");
            builder.setMessage("Введите описание типа");
            EditText et = new EditText(this);
            builder.setView(et);
            builder.setPositiveButton("ok", (a, b) -> {
                String text = et.getText().toString();
                if(text.replaceAll(" ", "").length() == 0) {
                    Toast.makeText(this, "Введите описание", Toast.LENGTH_SHORT).show();
                } else {
                    new Thread(() -> {
                        String s = connect("add_room_type", "typeDescription=" + text,
                                getSharedPreferences("pref", 0).getString("cookie", ""));
                        if(s.length() > 0 && s.charAt(0) == '/') {
                            runOnUiThread(() -> Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show());
                        } else {
                            s = connect("get_room_types_list", null);
                            try {
                                JSONArray array = new JSONArray(s);
                                roomTypes = new HashMap<>();
                                for (int i = 0; i < array.length(); i++) {
                                    roomTypes.put(array.getJSONObject(i).getInt("typeId"),
                                            array.getJSONObject(i).getString("typeDescription"));
                                }
                                roomTypeIDs = roomTypes.keySet().toArray(new Integer[0]);
                                roomTypeDescriptions = roomTypes.values().toArray(new String[0]);
                                runOnUiThread(() -> {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                                            roomTypeDescriptions);
                                    ((ListView) findViewById(R.id.lv_roomtypes)).setAdapter(adapter);
                                });

                            } catch (Exception e) {
                                loge(e);
                            }
                        }
                    }).start();
                }
            });
            builder.setNegativeButton("отмена", null);
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }
}
