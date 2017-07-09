package in.codingninjas.todoapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by nsbhasin on 09/07/17.
 */

class AlarmClass {
    private Context context;

    static AlarmClass getInstance(Context context) {
        return new AlarmClass(context);
    }

    private AlarmClass(Context context) {
        this.context = context;
    }
    private AlarmManager getAlarmManager() {
        return (AlarmManager) context.getSystemService(ALARM_SERVICE);
    }

    void createAlarm(Intent i, int requestCode, long timeInMillis) {
        AlarmManager alarmManager = getAlarmManager();
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
    }

    private boolean doesPendingIntentExist(Intent i, int requestCode) {
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    void deleteAlarm(Intent i, int requestCode) {
        if (doesPendingIntentExist(i, requestCode)) {
            PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, i, PendingIntent.FLAG_NO_CREATE);
            pi.cancel();
            getAlarmManager().cancel(pi);
        }
    }
}
