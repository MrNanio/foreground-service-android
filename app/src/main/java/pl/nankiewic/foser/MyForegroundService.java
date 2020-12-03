package pl.nankiewic.foser;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.Timer;
import java.util.TimerTask;

public class MyForegroundService extends Service {

    //1. Kanał notyfikacji
    public static final String CHANNEL_ID = "MyForegroundServiceChannel";
    public static final String CHANNEL_NAME = "FoSer service channel";
    //2. Odczyt danych zapisanych w Intent
    public static final String MESSAGE = "message";
    public static final String TIME = "time";
    public static final String WORK = "work";
    public static final String WORK_DOUBLE = "work_double";
    public static final String TIME_INCREMENT = "time_increment";
    public static final String START_OVER = "start_over";
    //3. Wartości ustawień
    private String message, time;
    private Boolean show_time, do_work, double_speed, start_over;
    private long period = 2000; //2s
    //4.
    private Context ctx;
    private Intent notificationIntent;
    private PendingIntent pendingIntent;
    //5.
    private int counter;
    private Timer timer;
    private TimerTask timerTask;
    final Handler handler = new Handler();
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Notification notification = new Notification.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_my_icon)
                    .setContentTitle(getString(R.string.ser_title))
                    .setShowWhen(show_time)
                    .setContentText("Licznik: " + String.valueOf(counter))
                    .setLargeIcon(BitmapFactory.decodeResource (getResources() , R.drawable.circle ))
                    .setContentIntent(pendingIntent)
                    .build();

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.notify(1,notification);
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        ctx = this;
        notificationIntent = new Intent(ctx, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);

        //counter = 0;

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                counter++;
                handler.post(runnable);
            }
        };
    }

    @Override
    public void onDestroy() {
        SharedPreferences sharedPreferences = getSharedPreferences("counter_value", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("counter_now_value", counter);
        editor.apply();
        handler.removeCallbacks(runnable);
        timer.cancel();
        timer.purge();
        timer = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);

        message = intent.getStringExtra(MESSAGE);
        show_time = intent.getBooleanExtra(TIME,false);
        do_work = intent.getBooleanExtra(WORK,false);
        double_speed = intent.getBooleanExtra(WORK_DOUBLE,false);
        start_over = intent.getBooleanExtra(START_OVER, true);
        time = intent.getStringExtra(TIME_INCREMENT);
        time = time + "000";
        period = Integer.parseInt(time);


        createNotificationChannel();

       // Intent notificationIntent = new Intent(this,MainActivity.class);
       // PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);


        Notification notification = new Notification.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_my_icon)
                .setContentTitle(getString(R.string.ser_title))
                .setShowWhen(show_time)
                .setContentText(message)
                .setLargeIcon(BitmapFactory.decodeResource (getResources() , R.drawable.circle ))
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1,notification);

        doWork();

        return START_NOT_STICKY;
    }

    private void doWork() {
        if(do_work) {
            timer.schedule(timerTask, 0L, double_speed ? period / 2L : period);
        }
        if(!start_over) {
            SharedPreferences sharedPreferences = getSharedPreferences("counter_value", 0);
            counter = sharedPreferences.getInt("counter_now_value", 0);
        }
        else counter = 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }
}
