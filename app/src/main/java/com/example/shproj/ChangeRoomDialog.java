package com.example.shproj;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.shproj.AdminActivity.roomAdapter;
import static com.example.shproj.MainActivity.connect;
import static com.example.shproj.MainActivity.log;
import static com.example.shproj.MainActivity.refreshEverything;
import static com.example.shproj.MainActivity.roomTypes;
import static com.example.shproj.MainActivity.rooms;
import static com.example.shproj.MainActivity.teachers;
import static com.example.shproj.PageFragment.formatFio;

public class ChangeRoomDialog extends DialogFragment {

    int id, prsIdSelected;
    Toolbar toolbar;
    boolean[] types;
    boolean[] defaultTypes;
    MainActivity.Room room;

    static void display(FragmentManager manager, int id) {
        ChangeRoomDialog changeRoomDialog = new ChangeRoomDialog();
        changeRoomDialog.id = id;
        changeRoomDialog.show(manager, "TAG");
        if (id == -1) {
            changeRoomDialog.room = new MainActivity.Room();
            changeRoomDialog.room.responsible = -1;
            changeRoomDialog.room.classTypes = new int[0];
        } else
            changeRoomDialog.room = rooms[id];
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.ThemeOverlay_MaterialComponents_Light);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_changeroom, container, false);
        EditText et_num = v.findViewById(R.id.ch_et_num);
        EditText et_seats = v.findViewById(R.id.ch_et_seats);
        AutoCompleteTextView tv = v.findViewById(R.id.actv_responsible);
        if(id != -1) {
            et_num.setText(room.classNumber);
            et_seats.setText(room.seats + "");
            tv.setText(formatFio(room.teacherResponsible.fio));
        }
        tv.setAdapter(new ACTVAdapter(v));
//        tv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                MainActivity.Teacher t = teachersMap.get((int) id);
//                tv.setText(formatFio(t.fio));
//                prsIdSelected = t.personId;
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        tv.setOnItemClickListener((parent1, view1, position1, id1) -> {
//            MainActivity.Teacher t = teachersMap.get((int) id1);
//            tv.setText(formatFio(t.fio));
//            prsIdSelected = t.personId;
//        });
        v.findViewById(R.id.btn_ok).setOnClickListener(v1 -> {
            String num = et_num.getText().toString().trim(),
                    seats = et_seats.getText().toString().trim();
            if(num.length() == 0 || seats.length() == 0 || prsIdSelected == -1) {
                log("prs selected " + prsIdSelected);
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show());
                prsIdSelected = -1;
                return;
            }
            new Thread(() -> {
                String cookie = getContext().getSharedPreferences("pref", 0).getString("cookie", "");
                String response = "aa";
                if(id == -1) {
                    room.classNumber = num;
                    room.seats = Integer.parseInt(seats);
                    room.responsible = prsIdSelected;
                    response = connect("add_room", "classNumber=" + num + "&seats=" + seats
                            + "&responsible=" + prsIdSelected, cookie);
                } else if(!num.equals(room.classNumber) || Integer.parseInt(seats) != room.seats ||
                        prsIdSelected != room.responsible)
                    response = connect("change_class_info", "classNumber=" + num + "&seats=" + seats
                            + "&responsible=" + prsIdSelected, cookie);

                for (int i = 0; i < types.length; i++) {
                    if(types[i] != defaultTypes[i]) {
                        if(types[i]) {
                            response = connect("add_class_type",
                                    "classNumber=" + room.classNumber + "&classType=" + roomTypes[i].typeId,
                                    cookie);
                        } else {
                            response = connect("remove_class_type",
                                    "classNumber=" + room.classNumber + "&classType=" + roomTypes[i].typeId,
                                    cookie);
                        }
                    }
                }
                if(response.equals("aa")) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Нет данных для сохранения", Toast.LENGTH_SHORT).show());
                } else if(response.length() > 1 && response.charAt(0) =='/') {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Что-то пошло не так", Toast.LENGTH_SHORT).show());
                } else {
                    refreshEverything(getActivity());
                    roomAdapter.list = Arrays.asList(rooms);
                    getActivity().runOnUiThread(() -> roomAdapter.notifyDataSetChanged());
                }
                getActivity().runOnUiThread(this::dismiss);
            }).start();
        });
        v.findViewById(R.id.btn_delete).setOnClickListener(v1 -> new Thread(() -> {
            SharedPreferences pref = getContext().getSharedPreferences("pref", 0);
            String response = connect("delete_room", "classNumber=" + room.classNumber,
                    pref.getString("cookie", ""));
            if(response.length() > 1 && response.charAt(0) =='/') {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Что-то пошло не так", Toast.LENGTH_SHORT).show());
            } else {
                refreshEverything(getActivity());
                roomAdapter.list = Arrays.asList(rooms);
                getActivity().runOnUiThread(() -> roomAdapter.notifyDataSetChanged());
            }
        }).start());
        if(id == -1)
            v.findViewById(R.id.btn_delete).setVisibility(View.INVISIBLE);

        types = new boolean[roomTypes.length];
        defaultTypes = new boolean[roomTypes.length];
        for (int i = 0; i < roomTypes.length; i++) {
            for (int j = 0; j < room.classTypes.length; j++) {
                if(roomTypes[i].typeId == room.classTypes[j]) {
                    types[i] = true;
                    defaultTypes[i] = true;
                    break;
                }
            }
        }
        prsIdSelected = room.responsible;
        
        ChipGroup chips = v.findViewById(R.id.chips_types);
        Chip chip;
        for (int i = 0; i < roomTypes.length; i++) {
            chip = new Chip(getContext());
            chip.setText(roomTypes[i].description);
            chip.setChecked(types[i]);
            final int j = i;
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                types[j] = isChecked;
            });
            chips.addView(chip);
        }

        toolbar = v.findViewById(R.id.toolbar_changeroom);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        if(id == -1)
            toolbar.setTitle("Новый кабинет");
        else
            toolbar.setTitle("Кабинет " + room.classNumber);
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
