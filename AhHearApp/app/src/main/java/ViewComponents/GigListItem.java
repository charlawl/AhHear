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

public class GigListItem extends ArrayAdapter<Band> {
    private static class ViewHolder{
        TextView gigNameView, gigDateView;
        ImageView gigImgView;
    }


    public GigListItem(ArrayList<Band> data, Context context){
        super(context, R.layout.gig_row, data);
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
            convertView = inflater.inflate(R.layout.gig_row, parent, false);
            viewHolder.gigNameView = (TextView) convertView.findViewById(R.id.gigName);
            viewHolder.gigDateView = (TextView) convertView.findViewById(R.id.gigDate);
            viewHolder.gigImgView = (ImageView) convertView.findViewById(R.id.gigImage);

            result = convertView;
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }


        Resources res = getContext().getResources();
        viewHolder.gigNameView.setText(band.getName());
        viewHolder.gigDateView.setText(res.getString(R.string.gigs, band.getNumGigs()));

        DownloadImage downloadBandImage = new DownloadImage(viewHolder.gigImgView);

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
