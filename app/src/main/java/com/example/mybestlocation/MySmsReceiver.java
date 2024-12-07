package com.example.mybestlocation;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MySmsReceiver extends BroadcastReceiver {

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                if (messages.length > 0) {
                    String messageBody = messages[0].getMessageBody();
                    String phoneNumber = messages[0].getDisplayOriginatingAddress();

                    // Check for location request message
                    if (messageBody.contains("FINDFRIENDS: Envoyer moi votre position")) {
                        // Start the location service to send back the position
                        Intent locationServiceIntent = new Intent(context, MyLocationService.class);
                        locationServiceIntent.putExtra("phone", phoneNumber);
                        context.startService(locationServiceIntent);
                    }

                    // Check for received location message
                    if (messageBody.contains("FindFriends: Ma position est:")) {
                        String[] parts = messageBody.split("#");
                        if (parts.length == 3) {
                            String longitude = parts[1];
                            String latitude = parts[2];

                            // Create a notification to display the received location
                            NotificationCompat.Builder notificationBuilder =
                                    new NotificationCompat.Builder(context, "FindFriendsChannel")
                                            .setContentTitle("Position Received")
                                            .setContentText("Tap to view on map")
                                            .setSmallIcon(android.R.drawable.ic_dialog_map)
                                            .setAutoCancel(true);

                            // Intent to open the map activity
                            Intent mapIntent = new Intent(context, MapActivity.class);
                            mapIntent.putExtra("latitude", latitude);
                            mapIntent.putExtra("longitude", longitude);

                            // Use PendingIntent flags for security (use FLAG_IMMUTABLE for newer versions)
                            PendingIntent pendingIntent = PendingIntent.getActivity(
                                    context,
                                    1,
                                    mapIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                            );
                            notificationBuilder.setContentIntent(pendingIntent);

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                            // Create notification channel (required for Android O and above)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                NotificationChannel channel = new NotificationChannel(
                                        "FindFriendsChannel",
                                        "FindFriends Notifications",
                                        NotificationManager.IMPORTANCE_DEFAULT
                                );
                                notificationManager.createNotificationChannel(channel);
                            }

                            notificationManager.notify(1, notificationBuilder.build());

                        }
                    }
                }
            }
        }
    }
}
