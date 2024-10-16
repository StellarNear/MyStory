package stellarnear.mystory.DailyChecker;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import java.time.Instant;
import java.util.Random;

import stellarnear.mystory.Activities.SplashActivity;
import stellarnear.mystory.Constants;
import stellarnear.mystory.R;

public class DailyChecker extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Directly check if the user launched the app today
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastConnect = sharedPreferences.getString("daily_checker_MS_last_connect", "");

        boolean forced = sharedPreferences.getBoolean("switch_fake_forced_notif", context.getResources().getBoolean(R.bool.switch_fake_forced_notif_def));
        if (forced) {
            sendNotification(context);
        } else if (!lastConnect.equalsIgnoreCase("")) {
            String now = Constants.DATE_FORMATTER.format(Instant.now());
            if (!now.equalsIgnoreCase(lastConnect)) {
                // User hasn't launched the app today, trigger the notification
                sendNotification(context);
            }
        }
    }

    public static void sendNotification(Context context) {
        String[] notificationMessages = {
                "C'est quoi cette histoire, tu lis plus là ?",
                "N'oublie pas de te connecter aujourd'hui !",
                "Hey, il est temps de lire un peu !",
                "Tu n'aimes plus ton application ? :(",
                "Ne perds pas ta streak, connecte-toi vite !"
        };

        Random random = new Random();
        String randomMessage = notificationMessages[random.nextInt(notificationMessages.length)];

        Intent intent = new Intent(context, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder_channel_MS")
                .setSmallIcon(R.drawable.ic_notification_book_24)
                .setContentTitle("Rappel de connexion")
                .setContentText(randomMessage)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }

}