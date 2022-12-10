package com.kellnhofer.tracker.view;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.util.DateUtils;

public class LocationsAdapter extends BaseAdapter {

    private static final int[] WEEK_DAY_TEXT_ID = new int[] {
            R.string.day_monday, R.string.day_tuesday, R.string.day_wednesday, R.string.day_thursday,
            R.string.day_friday, R.string.day_saturday, R.string.day_sunday};

    private static final int[] WEEK_DAY_COLOR_ID = new int[] {
            R.color.day_monday, R.color.day_tuesday, R.color.day_wednesday, R.color.day_thursday,
            R.color.day_friday, R.color.day_saturday, R.color.day_sunday};

    public interface LocationItemListener {
        void onLocationClick(Location location);
    }

    private final Context mContext;

    private List<Location> mLocations;
    private LocationItemListener mItemListener;

    public LocationsAdapter(Context context, List<Location> locations) {
        mContext = context;

        setList(locations);
    }

    public void replaceData(List<Location> locations) {
        setList(locations);
        notifyDataSetChanged();
    }

    public void setLocationItemListener(LocationItemListener itemListener) {
        mItemListener = itemListener;
    }

    private void setList(List<Location> locations) {
        mLocations = locations;
    }

    @Override
    public int getCount() {
        return mLocations.size();
    }

    @Override
    public Location getItem(int i) {
        return mLocations.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = view;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            rowView = inflater.inflate(R.layout.list_item_location, viewGroup, false);
        }

        final Location location = getItem(i);

        TextView labelView = rowView.findViewById(R.id.view_label);
        TextView nameView = rowView.findViewById(R.id.view_name);
        TextView dateView = rowView.findViewById(R.id.view_info);
        ImageView syncStateView = rowView.findViewById(R.id.image_sync_state);

        int weekDay = DateUtils.getWeekDay(location.getDate());
        int weekDayTextId = WEEK_DAY_TEXT_ID[weekDay];
        int weekDayColorId = WEEK_DAY_COLOR_ID[weekDay];
        int weekDayColor = ContextCompat.getColor(mContext, weekDayColorId);
        labelView.setText(weekDayTextId);
        ((GradientDrawable) labelView.getBackground()).setColor(weekDayColor);

        nameView.setText(location.getName());

        String date = DateUtils.toUiFormat(location.getDate());
        dateView.setText(date);

        long remoteId = location.getRemoteId();
        boolean changed = location.isChanged();
        syncStateView.setVisibility(remoteId != 0L && !changed ? View.VISIBLE : View.INVISIBLE);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLocationClicked(location);
            }
        });

        return rowView;
    }

    private void onLocationClicked(Location location) {
        if (mItemListener != null) {
            mItemListener.onLocationClick(location);
        }
    }

}
