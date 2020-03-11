package com.daya.earthquake.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.daya.earthquake.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {
    private Context context;
    private View view;
    private LayoutInflater layoutInflater;
    public CustomInfoWindow(Context context) {
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.custom_info_window,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {

        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        TextView title = view.findViewById(R.id.winTitle);
        TextView magnitude = view.findViewById(R.id.magnitude);
        title.setText(marker.getTitle());
        magnitude.setText(marker.getSnippet());
        return view;
    }
}
