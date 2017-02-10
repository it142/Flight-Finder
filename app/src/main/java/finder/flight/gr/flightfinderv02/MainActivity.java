package finder.flight.gr.flightfinderv02;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    FlightFragment flightFragment;

    // Animation stuff
    int cur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null) {
            flightFragment = new FlightFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.container, flightFragment).commit();
        }

        if(!FlightData.animation_loaded) initAnimation();
        else FlightData.animation.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void searchFlight(View v) {
        flightFragment.searchFlight(v);
    }

    private void initAnimation() {
        FlightData.animation = new AnimationDrawable();
        FlightData.animation.setOneShot(false);
        Resources res = getResources();

        for(int i = 1; i <= 30; i++) {
            String name = "frame" + String.format("%01d", i);
            int resId = res.getIdentifier(name, "drawable", getPackageName());
            MainActivity.BitmapWorkerTask task = new MainActivity.BitmapWorkerTask();
            task.execute(resId, 200, 200);
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }


    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 2;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private int data = 0;

        @Override
        protected Bitmap doInBackground(Integer... params) {
            data = params[0];
            return decodeSampledBitmapFromResource(getResources(), data, params[1], params[2]);
        }
        protected void onPostExecute(Bitmap bitmap) {
            cur++;
            FlightData.animation.addFrame(new BitmapDrawable(getResources(), bitmap), 60);
            if(cur == 30) {
                FlightData.animation_loaded = true;
                Log.i("Animation", "Done");
            }
        }

    }


}
