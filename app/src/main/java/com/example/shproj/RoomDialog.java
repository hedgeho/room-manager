package com.example.shproj;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;
import static com.example.shproj.AddActivity.formatSeats;
import static com.example.shproj.MainActivity.reservations;
import static com.example.shproj.MainActivity.roomTypes;
import static com.example.shproj.MainActivity.rooms;

public class RoomDialog extends DialogFragment {

    private EditText et, et_seats;
    private Toolbar toolbar;
    private int roomSelected = -1;
    private boolean[] roomTypesFilter, weekdays;
    private RoomAdapter adapter;
    private Calendar calendarFrom, calendarTo, calendarDate;

    private List<MainActivity.Room> filteredList;
    private boolean FILTER = false;

    static void display(FragmentManager manager, Calendar calendarFrom, Calendar calendarTo, Calendar calendarDate, boolean[] weekdays) {
        RoomDialog roomDialog = new RoomDialog();
        roomDialog.show(manager, "TAG");
        roomDialog.calendarFrom = calendarFrom;
        roomDialog.calendarTo = calendarTo;
        roomDialog.calendarDate = calendarDate;
        roomDialog.weekdays = weekdays;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.ThemeOverlay_MaterialComponents_Light);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_room, container, false);
        et = view.findViewById(R.id.et_room2);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();
                if (s.length() == 0) {
                    adapter.list = new ArrayList<>(filteredList);
                } else {
                    ArrayList<MainActivity.Room> list = new ArrayList<>();
                    for (MainActivity.Room room : filteredList) {
                        if (room.classNumber.toLowerCase().contains(query)) {
                            list.add(room);
                        }
                    }
                    adapter.list = list;
                }
                adapter.notifyDataSetChanged();
                if (adapter.list.size() == 1 && adapter.list.get(0).classNumber.toLowerCase().equals(query)) {
//                    roomSelected = Arrays.binarySearch(rooms, adapter.list.get(0), (o1, o2) -> o1.classNumber.equals(o2.classNumber) ? 0 : 1);
                    roomSelected = filteredList.indexOf(adapter.list.get(0));
                    toolbar.getMenu().getItem(0).setEnabled(true);
                } else {
                    toolbar.getMenu().getItem(0).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        toolbar = view.findViewById(R.id.toolbar);

        ChipGroup chips = view.findViewById(R.id.chips_filter);
        roomTypesFilter = new boolean[roomTypes.length];
        Chip chip;
        for (int i = 0; i < roomTypes.length; i++) {
            String description = roomTypes[i].description;
            chip = new Chip(getContext());
            chip.setText(description);
            final int j = i;
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                roomTypesFilter[j] = isChecked;
                performFilter();
            });
            chips.addView(chip);
        }
        TextView tv = view.findViewById(R.id.tv_filter_time);
        if (calendarFrom == null && calendarTo == null)
            tv.setText("Время не задано");
        else if (calendarTo == null) {
            tv.setText(String.format(Locale.getDefault(), "Время: с %02d:%02d", calendarFrom.get(Calendar.HOUR_OF_DAY),
                    calendarFrom.get(Calendar.MINUTE)));
        } else if (calendarFrom == null) {
            tv.setText(String.format(Locale.getDefault(), "Время: до %02d:%02d", calendarTo.get(Calendar.HOUR_OF_DAY),
                    calendarTo.get(Calendar.MINUTE)));
        } else {
            tv.setText(String.format(Locale.getDefault(), "Время: %02d:%02d - %02d:%02d",
                    calendarFrom.get(Calendar.HOUR_OF_DAY), calendarFrom.get(Calendar.MINUTE),
                    calendarTo.get(Calendar.HOUR_OF_DAY), calendarTo.get(Calendar.MINUTE)));
        }
        tv = view.findViewById(R.id.tv_filter_date);
        if (calendarDate != null) {
            tv.setText(String.format(Locale.getDefault(), "%02d.%02d", calendarDate.get(Calendar.DAY_OF_MONTH),
                    calendarDate.get(Calendar.MONTH) + 1));
        } else {
            boolean notEmpty = false;
            for (boolean weekday : weekdays) {
                notEmpty |= weekday;
            }
            if(notEmpty) {
                tv.setText(formatWeekDays(weekdays));
            } else {
                tv.setVisibility(GONE);
            }
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.setTitle("Поиск кабинета");
        toolbar.inflateMenu(R.menu.dialog_room);
        toolbar.getMenu().getItem(0).setEnabled(false);
        toolbar.setOnMenuItemClickListener(item -> {
            ((AddActivity) getActivity()).refreshRoom(roomSelected);
            dismiss();
            return true;
        });
        view.findViewById(R.id.img_filter).setOnClickListener(v -> {
            View scene = view.findViewById(R.id.layout_filter);
            Transition animation = new AutoTransition();
            TransitionManager.beginDelayedTransition((ViewGroup) view, animation);
            FILTER = !FILTER;
            if(scene.getVisibility() == GONE) {
                scene.setVisibility(View.VISIBLE);
                et.setText("");
                et.setEnabled(false);
            } else {
                et.setEnabled(true);
                scene.setVisibility(GONE);
            }
            performFilter();
        });
        et_seats = view.findViewById(R.id.et_filter_seats);
        et_seats.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 0)
                    performFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        filteredList = Arrays.asList(rooms);
        if(calendarFrom != null && calendarTo != null && calendarDate != null) {
            Calendar c1 = Calendar.getInstance(), c2 = Calendar.getInstance();
            c1.setTimeInMillis(calendarDate.getTimeInMillis());
            c2.setTimeInMillis(calendarDate.getTimeInMillis());
            c1.set(Calendar.HOUR_OF_DAY, calendarFrom.get(Calendar.HOUR_OF_DAY));
            c1.set(Calendar.MINUTE, calendarFrom.get(Calendar.MINUTE));
            c2.set(Calendar.HOUR_OF_DAY, calendarTo.get(Calendar.HOUR_OF_DAY));
            c2.set(Calendar.MINUTE, calendarTo.get(Calendar.MINUTE));

            long start = c1.getTimeInMillis(),
                    end = c2.getTimeInMillis();

            HashSet<MainActivity.Room> set = new HashSet<>(filteredList);
            for (MainActivity.Reservation res: reservations) {
                if ((res.startTime < start && res.endTime > start)
                        || (res.startTime > start && res.startTime < end)) {
                    set.remove(res.room);
                }
            }
            filteredList = Arrays.asList(set.toArray(new MainActivity.Room[0]));
        }

        ListView lv = view.findViewById(R.id.lv_room);
        adapter = new RoomAdapter();
        adapter.list = new ArrayList<>(filteredList);
        lv.setAdapter(adapter);
    }

    private void performFilter() {
        if(!FILTER) {
            adapter.list = new ArrayList<>(filteredList);
            adapter.notifyDataSetChanged();
            return;
        }
        et.setText("");
        roomSelected = -1;
        boolean notEmpty = false;
        for (boolean b : roomTypesFilter) {
            notEmpty |= b;
        }
        int seats = Integer.parseInt(et_seats.getText().toString());
        ArrayList<MainActivity.Room> twiceFilteredList = new ArrayList<>();
        for (MainActivity.Room room: filteredList) {
            if(room.seats >= seats)
                twiceFilteredList.add(room);
        }
        if(!notEmpty) {
            adapter.list = new ArrayList<>(twiceFilteredList);
        } else {
            ArrayList<MainActivity.Room> filteredList = new ArrayList<>();
            for (MainActivity.Room room : twiceFilteredList) {
                boolean okay = true;
                for (int j = 0; j < roomTypesFilter.length; j++) {
                    if (!roomTypesFilter[j])
                        continue;
                    boolean flag = false;
                    for (int k = 0; k < room.classTypes.length; k++) {
                        flag = flag || roomTypes[j].typeId == room.classTypes[k];
                    }
                    okay = okay && flag;
                }
                if (okay) {
                    filteredList.add(room);
                }
            }
            adapter.list = filteredList;
        }
        adapter.notifyDataSetChanged();
    }

    private String formatWeekDays(boolean[] array) {
        String[] week = {"ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС"};
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if(array[i] /*&& (i == 0 || !array[i-1])*/)
                result.append(week[i]).append(", ");
//            else if(array[i] && (i <= 1 || ))
        }
        if(result.length() > 0)
            result.delete(result.length()-2, result.length());
        return result.toString();
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
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final MainActivity.Room room = list.get(position);
            View view;
            if(convertView != null)
                view = convertView;
            else
                view = getLayoutInflater().inflate(R.layout.template_room, parent, false);
            TextView tv = view.findViewById(R.id.tv_roomnumber);
            tv.setText(room.classNumber);
            tv = view.findViewById(R.id.tv_roomtype);
            tv.setVisibility(GONE);
//            StringBuilder types = new StringBuilder() ;
//            for (int i = 0; i < room.typeDescriptions.length; i++) {
//                types.append(room.typeDescriptions[i]).append("; ");
//            }
//            if(types.length() > 0) {
//                types.delete(types.length()-2, types.length());
//            }
//            tv.setText(types.toString());
            tv = view.findViewById(R.id.tv_seats);
            tv.setText(room.seats + " " + formatSeats(room.seats));
            tv = view.findViewById(R.id.tv_responsible);
            String fio = room.teacherResponsible.fio;
            String[] words = fio.split(" ");
            if(words.length == 3) {
                fio = words[0] + " " + words[1].charAt(0) + ". " + words[2].charAt(0) + ".";
            }
            tv.setText(fio);
            view.setOnClickListener(v -> {
                et.setText(room.classNumber);
                roomSelected = room.id;
            });
            return view;
        }
    }
}
