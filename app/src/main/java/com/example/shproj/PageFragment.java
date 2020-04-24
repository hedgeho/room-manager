package com.example.shproj;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.shproj.MainActivity.Reservation;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;

import static com.example.shproj.MainActivity.Room;
import static com.example.shproj.MainActivity.nameToIndex;
import static com.example.shproj.MainActivity.oneDay;
import static com.example.shproj.MainActivity.rooms;
import static com.example.shproj.MainActivity.teachersMap;


public class PageFragment extends Fragment {

    Calendar c;
    Reservation[] list;
    private Reservation[][] roomList; // rooms x reservations
    private Room[] localRooms;

    private int mode = 0;
    private final static int MODE_DEFAULT = 0;
//    private final static int MODE_ROOMS = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_page, container, false);

        if(list == null) {
            list = new Reservation[0];
            roomList = new Reservation[0][];
        }
        makeRoomList();
        ExpandableListView lv = v.findViewById(R.id.lv_page);
        lv.setAdapter(new LVAdapter());
        if(list.length == 0) {
            v.findViewById(R.id.tv_no).setVisibility(View.VISIBLE);
            lv.setVisibility(View.INVISIBLE);
        } else if(list.length == 1) {
            lv.expandGroup(0);
        }

        return v;
    }

    void draw() {
        makeRoomList();
        if(getView() != null) {
            ExpandableListView lv = getView().findViewById(R.id.lv_page);
            lv.setAdapter(new LVAdapter());
            if(list.length == 0) {
                getView().findViewById(R.id.tv_no).setVisibility(View.VISIBLE);
                lv.setVisibility(View.INVISIBLE);
            } else {
                getView().findViewById(R.id.tv_no).setVisibility(View.INVISIBLE);
                lv.setVisibility(View.VISIBLE);
                if(list.length == 1) {
                    lv.expandGroup(0);
                }
            }
        }
    }

    private void makeRoomList() {
        if(rooms == null)
            return;
        LinkedList[] lists = new LinkedList[rooms.length];
//        Arrays.fill(lists, new LinkedList<Reservation>());
        for (int i = 0; i < lists.length; i++) {
            lists[i] = new LinkedList<Reservation>();
        }
        for (Reservation reservation : list) {
            Object index = nameToIndex.get(reservation.classNumber);
            if(index != null) {
                lists[(int) index].add(reservation);
            }
        }

        LinkedList<Room> local = new LinkedList<>();
        for (int i = 0; i < lists.length; i++) {
            LinkedList linkedList = lists[i];
            if (linkedList.size() > 0) {
                local.add(rooms[i]);
            }
        }
        localRooms = local.toArray(new Room[0]);

        roomList = new Reservation[local.size()][];
        int j = 0;
        for (LinkedList linkedList : lists) {
            if (linkedList.size() > 0) {
                roomList[j] = new Reservation[linkedList.size()];
                for (int k = 0; k < linkedList.size(); k++) {
                    roomList[j][k] = (Reservation) linkedList.get(k);
                }
                j++;
            }
        }
//        roomList = new Reservation[lists.length][];
//        for (int i = 0; i < lists.length; i++) {
//            roomList[i] = new Reservation[lists[i].size()];
//            for (int j = 0; j < roomList[i].length; j++) {
//                roomList[i][j] = (Reservation) lists[i].get(j);
//            }
//        }
    }

    class LVAdapter extends BaseExpandableListAdapter {
        @Override
        public int getGroupCount() {
            return roomList.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return roomList[groupPosition].length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return roomList[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return roomList[groupPosition][childPosition];
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;//groupPosition*100 + childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            View v;
            if(convertView != null)
                v = convertView;
            else
                v = getLayoutInflater().inflate(R.layout.template_reservation, parent, false);
            ((TextView) v.findViewById(R.id.tv_subtext)).setText(format(groupPosition));
            v.findViewById(R.id.tv_person).setVisibility(View.GONE);

            boolean live = false;
            for (Reservation reservation: roomList[groupPosition]) {
                if(reservation.startTime <= System.currentTimeMillis() &&
                        System.currentTimeMillis() < reservation.endTime) {
                    live = true;
                    break;
                }
            }
            TextView tv = v.findViewById(R.id.tv_text);//.setText("" + localRooms[groupPosition].classNumber);
            if(live) {
                SpannableStringBuilder ssb = new SpannableStringBuilder(localRooms[groupPosition].classNumber + "   ");
                ssb.setSpan(new ImageSpan(getContext(), R.drawable.live, DynamicDrawableSpan.ALIGN_BASELINE), ssb.length() - 1, ssb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                tv.setText(ssb);
            } else
                tv.setText(localRooms[groupPosition].classNumber);
            return v;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            View view;
            if(convertView == null)
                view = getLayoutInflater().inflate(R.layout.template_reservation, parent, false);
            else
                view = convertView;

            TextView tv = view.findViewById(R.id.tv_text);
            if(mode == MODE_DEFAULT) {
                if(roomList[groupPosition][childPosition].startTime <= System.currentTimeMillis() &&
                    System.currentTimeMillis() < roomList[groupPosition][childPosition].endTime) {
                    SpannableStringBuilder ssb = new SpannableStringBuilder(roomList[groupPosition][childPosition].reason + "   ");
                    ssb.setSpan(new ImageSpan(getContext(), R.drawable.live, DynamicDrawableSpan.ALIGN_BASELINE), ssb.length() - 1, ssb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    tv.setText(ssb);
                } else
                    tv.setText(roomList[groupPosition][childPosition].reason);
                tv = view.findViewById(R.id.tv_subtext);
                String time = "";
                if (roomList[groupPosition][childPosition].startTime >= c.getTimeInMillis())
                    time += millisToTime(roomList[groupPosition][childPosition].startTime);
                time += " - ";
                if (roomList[groupPosition][childPosition].endTime < c.getTimeInMillis() + oneDay)
                    time += millisToTime(roomList[groupPosition][childPosition].endTime);
                if (time.equals(" - "))
                    time = "Весь день";
                tv.setText(time);
                tv = view.findViewById(R.id.tv_person);
                if (roomList[groupPosition][childPosition].customerId == 0) {
                    tv.setText(formatFio(teachersMap.get(roomList[groupPosition][childPosition].teacherId).fio));
                } else
                    tv.setText(formatFio(teachersMap.get(roomList[groupPosition][childPosition].customerId).fio));
            }/* else if(mode == MODE_ROOMS) {
//                tv.setText();
            }*/
            return view;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    private Bitmap paintAndScale(int size) {
        Drawable drawable = getResources().getDrawable(R.drawable.live, getContext().getTheme());
        return Bitmap.createScaledBitmap(drawableToBitmap(drawable), size, size, true);
    }
    private static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private String format(int index) {
        int i = 0;
        String start, end = "";
        StringBuilder result = new StringBuilder();

        Reservation[] array = roomList[index];
        while(i < array.length) {
            i++;
            while(i < array.length && array[i].startTime - array[i-1].endTime > 15*60000) {
                result.append(millisToTime(array[i - 1].startTime)).append("-").append(millisToTime(array[i - 1].endTime)).append(", ");
                i++;
            }
            if(i == array.length) {
                result.append(millisToTime(array[i-1].startTime)).append("-").append(millisToTime(array[i-1].endTime)).append("yo");
                break;
            }
            start = millisToTime(array[i-1].startTime);
            while(i < array.length && array[i].startTime - array[i-1].endTime <= 15*60000) {
                end = millisToTime(array[i].endTime);
                i++;
            }
            result.append(start).append("-").append(end).append(", ");
        }
        return result.subSequence(0, result.length()-2).toString();
    }

    static String formatFio(String fio) {
        String[] words = fio.split(" ");
        if(words.length < 3)
            return fio;
        else
            return words[0] + " " + words[1].charAt(0) + ". " + words[2].charAt(0) + ".";
    }

    private Calendar calendar = Calendar.getInstance();
    private String millisToTime(long millis) {
        calendar.setTimeInMillis(millis);
        if(millis > c.getTimeInMillis() && millis < c.getTimeInMillis() + oneDay)
            return String.format(Locale.getDefault(), "%02d:%02d",
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        else
            return "";
    }
}
