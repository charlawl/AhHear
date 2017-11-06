package ViewComponents;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by charlottehearne on 05/11/2017.
 */

public class DownloadVenueImage extends AsyncTask<URL, Integer, Bitmap> {
    private ImageView imageView;

    public DownloadVenueImage(ImageView imageView){
        this.imageView = imageView;
    }
    protected Bitmap doInBackground(URL... urls) {
        int count = urls.length;
        Bitmap result = null;

        for (URL url : urls) {
            try {
                InputStream stream = url.openStream();
                result = BitmapFactory.decodeStream(stream);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return result;

    }

    protected void onPostExecute(Bitmap bitmap){
        imageView.setImageBitmap(bitmap);
    }

}