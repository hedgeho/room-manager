package com.example.shproj;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.shproj.AddActivity.formatSeats;
import static com.example.shproj.MainActivity.rooms;
import static com.example.shproj.MainActivity.teachers;
import static com.example.shproj.PageFragment.formatFio;

public class AdminActivity extends AppCompatActivity {

    int mode; // 0 - rooms, 1 - teachers
    static RoomAdapter roomAdapter;
    TeacherAdapter teacherAdapter;
    int prsIdSelected = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mode = getIntent().getIntExtra("type", 0);

        setTitle(mode == 0?"Классы":"Учителя");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EditText et = findViewById(R.id.et_input);
        et.setHint(mode == 0?"Класс":"Учитель");
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
//            view.setLongClickable(true);
//            view.setOnLongClickListener(v -> {
//                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
//                builder.setTitle("Предупреждение");
//                builder.setMessage("Удалить класс?");
//                builder.setPositiveButton("Да", (a, b) -> {
//                    // todo delete_room request
//                });
//                builder.setNegativeButton("отмена", null);
//                builder.show();
//                return true;
//            });
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
            view.setLongClickable(true);
            view.setOnLongClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                builder.setTitle("Предупреждение");
                builder.setMessage("Удалить класс?");
                builder.setPositiveButton("Да", (a, b) -> {
                    // todo delete_room request
                });
                builder.setNegativeButton("отмена", null);
                builder.show();
                return true;
            });
            return view;
        }
    }
    class ACTVAdapter extends BaseAdapter implements Filterable {

        TeacherFilter filter;
        List<MainActivity.Teacher> list, fullList;
        View dialog;

        ACTVAdapter(View v) {
            fullList = Arrays.asList(teachers);
            list = new ArrayList<>(fullList);
            dialog = v;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return list.get(position).personId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if(convertView != null)
                view = convertView;
            else
                view = getLayoutInflater().inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            TextView tv = view.findViewById(android.R.id.text1);
            tv.setText(formatFio(list.get(position).fio));
            view.setOnClickListener(v -> {
                AutoCompleteTextView actv = dialog.findViewById(R.id.actv_responsible);
                actv.setText(formatFio(list.get(position).fio));
                prsIdSelected = list.get(position).personId;
                actv.dismissDropDown();
            });
            return view;
        }

        @Override
        public Filter getFilter() {
            if(filter == null) {
                filter = new TeacherFilter();
            }
            return filter;
        }

        class TeacherFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if(constraint == null || constraint.length() == 0) {
                    results.values = new ArrayList<>(fullList);
                    results.count = fullList.size();
                } else {
                    String query = constraint.toString().trim().toLowerCase();
                    ArrayList<MainActivity.Teacher> list = new ArrayList<>();
                    for (MainActivity.Teacher t: fullList) {
                        if(t.fio.toLowerCase().contains(query))
                            list.add(t);
                    }
                    results.values = list;
                    results.count = list.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                list = (List<MainActivity.Teacher>) results.values;
                if(list.size() > 1)
                    prsIdSelected = -1;
                notifyDataSetChanged();
            }
        }
    }
}
