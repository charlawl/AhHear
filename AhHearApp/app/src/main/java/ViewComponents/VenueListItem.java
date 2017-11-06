package ViewComponents;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ahhear.ahhearapp.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by charlottehearne on 05/11/2017.
 */

public class VenueListItem extends ArrayAdapter<Venue> {
    private static class ViewHolder{
        TextView venueNameView, numGigsView, numSampelesView, decibelsView;
        ImageView venueImgView;
    }


    public VenueListItem(ArrayList<Venue> data, Context context){
        super(context, R.layout.venue_row, data);

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;
        Venue venue = getItem(position);
        ViewHolder viewHolder;
        final View result;

        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.venue_row, parent, false);
            viewHolder.venueNameView = (TextView) convertView.findViewById(R.id.venueName);
            viewHolder.numGigsView = (TextView) convertView.findViewById(R.id.gigs);
            viewHolder.numSampelesView = (TextView) convertView.findViewById(R.id.numSamples);
            viewHolder.decibelsView = (TextView) convertView.findViewById(R.id.decibels);
            viewHolder.venueImgView = (ImageView) convertView.findViewById(R.id.venueImage);

            result = convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }


        Resources res = getContext().getResources();
        viewHolder.venueNameView.setText(venue.getVenueName());
        viewHolder.numGigsView.setText(res.getString(R.string.gigs, venue.getNumGigs()));
        viewHolder.numSampelesView.setText(res.getString(R.string.samples, venue.getNumSamples()));
        viewHolder.decibelsView.setText(res.getString(R.string.decibelsAvg, venue.getDecibels()));

        DownloadVenueImage downloadVenueImage = new DownloadVenueImage(viewHolder.venueImgView);

        try {
            downloadVenueImage.execute(new URL("http", "10.0.2.2", 8000, "images?id="+venue.getId()));
        } catch (MalformedURLException e) {
            Toast toasterr = Toast.makeText(getContext(), "Error occurred", Toast.LENGTH_SHORT);
            toasterr.show();
            e.printStackTrace();
        }


        return convertView;
    }
}
