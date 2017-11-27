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

public class BandListItem extends ArrayAdapter<Band> {
    private static class ViewHolder{
        TextView bandNameView, numGigsView, numSampelesView, decibelsView;
        ImageView bandImgView;
    }


    public BandListItem(ArrayList<Band> data, Context context){
        super(context, R.layout.band_row, data);

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;
        Band band = getItem(position);
        ViewHolder viewHolder;
        final View result;

        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.band_row, parent, false);
            viewHolder.bandNameView = (TextView) convertView.findViewById(R.id.bandName);
            viewHolder.numGigsView = (TextView) convertView.findViewById(R.id.gigs);
            viewHolder.numSampelesView = (TextView) convertView.findViewById(R.id.numSamples);
            viewHolder.decibelsView = (TextView) convertView.findViewById(R.id.decibels);
            viewHolder.bandImgView = (ImageView) convertView.findViewById(R.id.bandImage);

            result = convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }


        Resources res = getContext().getResources();
        viewHolder.bandNameView.setText(band.getName());
        viewHolder.numGigsView.setText(res.getString(R.string.gigs, band.getNumGigs()));
        viewHolder.numSampelesView.setText(res.getString(R.string.samples, band.getNumSamples()));
        viewHolder.decibelsView.setText(res.getString(R.string.decibelsAvg, band.getDecibels()));

        DownloadImage downloadBandImage = new DownloadImage(viewHolder.bandImgView);

        try {
            downloadBandImage.execute(new URL("http", "gavs.work", 8000, "band_image?id="+band.getId()));
        } catch (MalformedURLException e) {
            Toast toasterr = Toast.makeText(getContext(), "Error occurred", Toast.LENGTH_SHORT);
            toasterr.show();
            e.printStackTrace();
        }


        return convertView;
    }
}
