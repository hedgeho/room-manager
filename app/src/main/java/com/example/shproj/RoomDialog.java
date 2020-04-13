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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.shproj.AddActivity.formatSeats;
import static com.example.shproj.MainActivity.rooms;

public class RoomDialog extends DialogFragment {

    EditText et;
    ListView lv;
    Toolbar toolbar;
    int roomSelected = -1;

    static void display(FragmentManager manager) {
        RoomDialog roomDialog = new RoomDialog();
        roomDialog.show(manager, "TAG");
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
        lv = view.findViewById(R.id.lv_room);
        RoomAdapter adapter = new RoomAdapter();
        adapter.list = Arrays.asList(rooms);
        lv.setAdapter(adapter);
        et = view.findViewById(R.id.et_room2);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();
                if(s.length() == 0) {
                    adapter.list = Arrays.asList(rooms);
                } else {
                    ArrayList<MainActivity.Room> list = new ArrayList<>();
                    for (MainActivity.Room room : rooms) {
                        if(room.classNumber.toLowerCase().contains(query)) {
                            list.add(room);
                        }
                    }
                    adapter.list = list;
                }
                adapter.notifyDataSetChanged();
                if(adapter.list.size() == 1 && adapter.list.get(0).classNumber.toLowerCase().equals(query)) {
                    roomSelected = Arrays.binarySearch(rooms, adapter.list.get(0), (o1, o2) -> o1.classNumber.equals(o2.classNumber) ? 0 : 1);
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
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.setTitle("Выбор класса");
        toolbar.inflateMenu(R.menu.dialog_room);
        toolbar.getMenu().getItem(0).setEnabled(false);
        toolbar.setOnMenuItemClickListener(item -> {
            ((AddActivityTest) getActivity()).refreshRoom(roomSelected);
            dismiss();
            return true;
        });
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
            tv.setText(types.toString());
            tv = view.findViewById(R.id.tv_seats);
            tv.setText(room.seats + " " + formatSeats(room.seats));
            tv = view.findViewById(R.id.tv_responsible);
            String fio = room.responsibleFio;
            String[] words = fio.split(" ");
            if(words.length == 3) {
                fio = words[0] + " " + words[1].charAt(0) + ". " + words[2].charAt(0) + ".";
            }
            tv.setText(fio);
            view.setOnClickListener(v -> {
                et.setText(room.classNumber);
                roomSelected = position;
            });
            view.setLongClickable(true);
            view.setOnLongClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
}
