package com.ev.approver;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Map;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * Created by Kumar_Thangaraj on 7/28/2018.
 */

public class ApproverMessagingService extends FirebaseMessagingService {
    private static final String TAG = "ApproverMessagingServ";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            Intent intent = new Intent(this, LaunchActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Map<String,String> data = remoteMessage.getData();
            Log.d(TAG, "onMessageReceived: requestId is "+data.get("requestId"));
            intent.putExtra("requestId",(String)data.get("requestId"));
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.approver)
                    .setContentTitle(data.get("title"))
                    .setContentText(data.get("content"))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(uri);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(0,mBuilder.build());

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

}
