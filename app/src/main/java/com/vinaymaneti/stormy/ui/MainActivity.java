package com.vinaymaneti.stormy.ui;

import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vinaymaneti.stormy.R;
import com.vinaymaneti.stormy.model.CurrentWeather;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather mCurrentWeather;

    @Bind(R.id.temperatureLabel)
    TextView mTemperatureLabel;

    @Bind(R.id.timeLabel)
    TextView mTimeValue;

    @Bind(R.id.iconImageView)
    ImageView mIconImageView;

    @Bind(R.id.humidityValue)
    TextView mHumidityValue;

    @Bind(R.id.precipValue)
    TextView mPrecipValue;

    @Bind(R.id.summaryLabel)
    TextView mSummaryLabel;

    @Bind(R.id.refreshImageView)
    ImageView mRefreshImageView;

    @Bind(R.id.progressBar)
    ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        final double latitude = 10.793527;
        final double longitude = 106.624942;

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForeCastWeather(latitude, longitude);
            }
        });

        getForeCastWeather(latitude, longitude);
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mRefreshImageView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mRefreshImageView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void getForeCastWeather(double latitude, double longitude) {


        String api_key = "9adfe76743627ad66e0a6ee17c7df062";
        String url = "https://api.forecast.io/forecast/" + api_key + "/" + latitude + "," + longitude + "";

        if (isNetworkAvailable()) {
            toggleRefresh();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserWithAlertDialogError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    try {
                        String jsonString = response.body().string();
                        Log.v(TAG, jsonString);
                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentWeather(jsonString);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            alertUserWithAlertDialogError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception Caught ::--" + e);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONException Caught ::--" + e);
                    }
                }
            });
        } else {
            Toast.makeText(this, R.string.error_network_unavilable, Toast.LENGTH_LONG).show();
        }
    }

    private void updateDisplay() {
        mTemperatureLabel.setText(Integer.toString(mCurrentWeather.getTemperature()));
        mTimeValue.setText(String.format(getString(R.string.time_format), mCurrentWeather.getFormattedTime()));
        mHumidityValue.setText(mCurrentWeather.getHumidity() + "");
        mPrecipValue.setText(mCurrentWeather.getPrecipChange() + "%");
        mSummaryLabel.setText(mCurrentWeather.getSummary());
        Drawable drawable = ContextCompat.getDrawable(this, mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);
    }

    private CurrentWeather getCurrentWeather(String jsonString) throws JSONException {
        JSONObject foreCast = new JSONObject(jsonString);
        String currentTimeZone = foreCast.getString("timezone");
        Log.i(TAG, "Json value:--" + currentTimeZone);

        JSONObject currently = foreCast.getJSONObject("currently");

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setImage(currently.getString("icon"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setPrecipChange(currently.getDouble("precipProbability"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTimeZone(currentTimeZone);
        Log.i(TAG, currentWeather.getFormattedTime());
        return currentWeather;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected())
            isAvailable = true;
        return isAvailable;
    }

    private void alertUserWithAlertDialogError() {
        AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
        alertDialogFragment.show(getFragmentManager(), "error_dialog");
    }
}
