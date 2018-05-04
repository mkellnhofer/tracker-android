package com.kellnhofer.tracker.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import com.kellnhofer.tracker.R;
import com.kellnhofer.tracker.model.Location;
import com.kellnhofer.tracker.util.DateUtils;

public class LocationsAdapter extends BaseAdapter {

    private static final DecimalFormat df = new DecimalFormat("#.000000");

    public interface LocationItemListener {
        void onLocationClick(Location location);
    }

    private List<Location> mLocations;
    private LocationItemListener mItemListener;

    public LocationsAdapter(List<Location> locations) {
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

        TextView nameView = (TextView) rowView.findViewById(R.id.name);
        TextView dateView = (TextView) rowView.findViewById(R.id.info);

        nameView.setText(location.getName());
        String date = DateUtils.toUiFormat(location.getDate());
        String pos = df.format(location.getLatitude()) + "/" + df.format(location.getLongitude());
        dateView.setText(date + " | " + pos);

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
