package com.example.shproj;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.shproj.AddActivity.formatSeats;
import static com.example.shproj.MainActivity.rooms;
import static com.example.shproj.MainActivity.teachers;

public class AdminActivity extends AppCompatActivity {

    int mode; // 0 - rooms, 1 - teachers
    static RoomAdapter roomAdapter;
    TeacherAdapter teacherAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mode = getIntent().getIntExtra("type", 0);

        setTitle(mode == 0?"Кабинеты":"Учителя");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EditText et = findViewById(R.id.et_input);
        et.setHint(mode == 0?"Кабинет":"Учитель");
        ListView lv = findViewById(R.id.admin_lv);

        if(mode == 0) {
            roomAdapter = new RoomAdapter();
            roomAdapter.list = Arrays.asList(rooms);
            lv.setAdapter(roomAdapter);
            lv.setOnItemClickListener((parent, view, position, id) -> {
                if(mode == 0) {
                    ChangeRoomDialog.display(getSupportFragmentManager(), (int) id);
                }
            });
        } else {
            teacherAdapter = new TeacherAdapter();
            teacherAdapter.list = Arrays.asList(teachers);
            lv.setAdapter(teacherAdapter);
        }

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0) {
                    if(mode == 0)
                        roomAdapter.list = Arrays.asList(rooms);
                    else
                        teacherAdapter.list = Arrays.asList(teachers);
                } else {
                    String query = s.toString().trim().toLowerCase();
                    if(mode == 0) {
                        ArrayList<MainActivity.Room> list = new ArrayList<>();
                        for (MainActivity.Room room : rooms) {
                            if (room.classNumber.toLowerCase().contains(query))
                                list.add(room);
                        }
                        roomAdapter.list = list;
                    } else {
                        ArrayList<MainActivity.Teacher> list = new ArrayList<>();
                        for (MainActivity.Teacher teacher : teachers) {
                            if(teacher.fio.toLowerCase().contains(query) || teacher.info.contains(query))
                                list.add(teacher);
                        }
                        teacherAdapter.list = list;
                    }
                }
                if(mode == 0)
                    roomAdapter.notifyDataSetChanged();
                else
                    teacherAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        findViewById(R.id.admin_fab_add).setOnClickListener(v -> {
            if(mode == 0) {
                ChangeRoomDialog.display(getSupportFragmentManager(), -1);
            }
        });
        if(mode == 1)
            findViewById(R.id.admin_fab_add).setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    class RoomAdapter extends BaseAdapter {

        List<MainActivity.Room> list;

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return list.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MainActivity.Room room = list.get(position);
            View view;
            if(convertView != null)
                view = convertView;
            else
                view = getLayoutInflater().inflate(R.layout.template_room, parent, false);
            TextView tv = view.findViewById(R.id.tv_roomnumber);
            tv.setText(room.classNumber);
            tv = view.findViewById(R.id.tv_roomtype);
            StringBuilder types = new StringBuilder();
            for (int i = 0; i < room.typeDescriptions.length; i++) {
                types.append(room.typeDescriptions[i]).append("; ");
            }
            if(types.length() > 0) {
                types.delete(types.length()-2, types.length());
            }
            if(types.length() == 0)
                tv.setVisibility(View.GONE);
            else {
                tv.setText(types.toString());
                tv.setVisibility(View.VISIBLE);
            }
            tv = view.findViewById(R.id.tv_seats);
            tv.setText(room.seats + " " + formatSeats(room.seats));
            tv = view.findViewById(R.id.tv_responsible);
            String fio = room.teacherResponsible.fio;
            String[] words = fio.split(" ");
            if(words.length == 3) {
                fio = words[0] + " " + words[1].charAt(0) + ". " + words[2].charAt(0) + ".";
            }
            tv.setText(fio);
            return view;
        }
    }
    class TeacherAdapter extends BaseAdapter {

        List<MainActivity.Teacher> list;

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MainActivity.Teacher teacher = list.get(position);
            View view;
            if(convertView != null)
                view = convertView;
            else
                view = getLayoutInflater().inflate(R.layout.template_teacher, parent, false);
            TextView tv = view.findViewById(R.id.tv_fio);
            tv.setText(teacher.fio);
            tv = view.findViewById(R.id.tv_addinfo);
            if(teacher.info.equals(""))
                tv.setVisibility(View.GONE);
            else {
                tv.setVisibility(View.VISIBLE);
                tv.setText(teacher.info);
            }
//            view.setOnClickListener(v -> {
////                et.setText(teacher.classNumber);
////                roomSelected = position;
//            });
            return view;
        }
    }
}
