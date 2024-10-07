package com.rootwatchparty.railwayapi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrainFetchService extends Service {

    private static final String TAG = "TrainFetchService";
    private static final String CHANNEL_ID = "ticket_channel";
    private static final String CHANNEL_ID2 = "ticket_channel2";
    private static final long COUNTDOWN_INTERVAL = 10000; // 10 seconds

    private boolean isAvailable = false;
    private CountDownTimer countDownTimer;
    private Retrofit retrofit;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
        initRetrofit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String searchUrl = intent.getStringExtra("search_url");
        String tripId = intent.getStringExtra("trip_id");

        if (searchUrl != null && tripId != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, createNotification("Fetching train data..."), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            }

            startCountdown(searchUrl, tripId);
        }

        return START_STICKY;
    }

    private void startCountdown(String searchUrl, String tripNumber) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(COUNTDOWN_INTERVAL, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateNotification((millisUntilFinished / 1000) + " seconds remaining");
            }

            @Override
            public void onFinish() {
                fetchTrainData(searchUrl, tripNumber);
            }
        };
        countDownTimer.start();
    }

    private void fetchTrainData(String searchUrl, String tripNumber) {
        updateNotification("Fetching train data...");

        Uri uri = Uri.parse(searchUrl);
        String fromCity = uri.getQueryParameter("fromcity");
        String toCity = uri.getQueryParameter("tocity");
        String dateOfJourney = uri.getQueryParameter("doj");
        String seatClass = uri.getQueryParameter("class");

        if (fromCity == null || toCity == null || dateOfJourney == null || seatClass == null) {
            return;
        }

        ApiService apiService = retrofit.create(ApiService.class);
        apiService.searchTrips(fromCity, toCity, dateOfJourney, seatClass).enqueue(new Callback<TrainResponse>() {
            @Override
            public void onResponse(@NonNull Call<TrainResponse> call, @NonNull Response<TrainResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Train> trains = response.body().getData().getTrains();
                    checkAvailability(trains, tripNumber, searchUrl);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TrainResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Error fetching train data", t);
                updateNotification("Request failed.");
                startCountdown(searchUrl, tripNumber); // Retry after failure
            }
        });
    }

    private void checkAvailability(List<Train> trains, String tripNumber, String searchUrl) {
        List<String> toCheckList = Arrays.stream(tripNumber.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        boolean anyAvailable = false; // Flag to check if any tickets are available

        for (String ticket : toCheckList) {
            for (Train train : trains) {
                String extractedNumber = train.getTripNumber().replaceAll(".*\\((\\d+)\\).*", "$1");
                if (ticket.equals(extractedNumber)) {
                    boolean isAvailable = false;
                    for (SeatType seatType : train.getSeatTypes()) {
                        if (seatType.getSeatCounts().getOnline() > 0) {
                            isAvailable = true;
                            break;
                        }
                    }

                    if (isAvailable) {
                        notifyTicketAvailable(train);
                        anyAvailable = true; // Mark that at least one ticket is available
                        break; // Break the inner loop to avoid notifying multiple times for the same ticket
                    }
                }
            }
        }

        if (!anyAvailable) {
            startCountdown(searchUrl, tripNumber); // Only start countdown if no tickets are available for any trip number
        }
    }


    private void notifyTicketAvailable(Train train) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString;
        try {
            jsonString = objectMapper.writeValueAsString(train.getSeatTypes());
        } catch (JsonProcessingException e) {
            jsonString = "Ticket Available";
        }
        showAvailableTicketNotification(jsonString);
    }

    private void updateNotification(String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, createNotification(message));
        }
    }

    private Notification createNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Train Fetch Service")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_rail)
                .setOngoing(true);
        return builder.build();
    }

    private void showAvailableTicketNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID2)
                .setContentTitle("Ticket Available")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(R.drawable.ic_rail)
                .setVibrate(new long[]{0, 1000}) // Vibration pattern
                .setSound(android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION));

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(2, builder.build());

        // Stop the service after showing the notification
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not binding to any activity
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_ID, "Train Fetch Service", NotificationManager.IMPORTANCE_DEFAULT);
            channel1.setVibrationPattern(null);
            channel1.setSound(null, null);

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_ID2, "Train Ticket Availability", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel1);
                manager.createNotificationChannel(channel2);
            }
        }
    }

    private void initRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://railspaapi.shohoz.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public boolean checkTrain(String trainNumber, String toCheck) {
        String extractedNumber = trainNumber.replaceAll(".*\\((\\d+)\\).*", "$1");

        System.out.println("extractedNumber : "+extractedNumber);

        boolean contains = false;
        for (String number : toCheck.split(",")) {
            if (number.trim().equals(extractedNumber)) {
                contains = true;
                return  contains;
            }
        }

        return contains;
    }

}
