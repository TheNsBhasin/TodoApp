package in.codingninjas.todoapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Todo todo = (Todo) intent.getSerializableExtra(IntentConstants.TODO_ITEM);
        Log.i("Notification","ID: " + todo.getId());
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_done_white_24dp)
                .setContentTitle(todo.getTitle())
                .setAutoCancel(true)
                .setContentText("Your task is due today")
                .setDefaults(Notification.DEFAULT_ALL);

        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) todo.getId(), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify((int) todo.getId(), mBuilder.build());
    }
}
