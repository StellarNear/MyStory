package stellarnear.mystory.DailyChecker;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.time.Instant;
import java.util.Random;

import stellarnear.mystory.Activities.MainActivity;
import stellarnear.mystory.Constants;
import stellarnear.mystory.R;

public class DailyCheckWorker extends Worker {

    public DailyCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (shouldRemindToConnect(getApplicationContext())) {
            // Trigger notification
            sendNotification();
        }
        return Result.success();
    }


    private boolean shouldRemindToConnect(Context mC) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mC);
        String lastConnect = sharedPreferences.getString("daily_checker_MS_last_connect", "");
        if (!lastConnect.equalsIgnoreCase("")) {

            // Getting the current log time
            String now = Constants.DATE_FORMATTER.format(Instant.now());
            return !now.equalsIgnoreCase(lastConnect);
        } else {
            return false;  //something went wrong
        }
    }

    private void sendNotification() {
        String[] notificationMessages = {
                "C'est quoi cette histoire, tu lis plus là ?",
                "N'oublie pas de te connecter aujourd'hui !",
                "Hey, il est temps de lire un peu !",
                "Tu n'aimes plus ton application ? :(",
                "Ne perds pas ta streak, connecte-toi vite !"
        };

// Create a Random instance
        Random random = new Random();
// Select a random message
        String randomMessage = notificationMessages[random.nextInt(notificationMessages.length)];

// Create an intent to open your app's main activity
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);  // Replace MainActivity with your activity name
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Ensures the app opens in a fresh task

// Wrap the intent in a PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE required for Android 12+
        );

// Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "daily_reminder_channel")
                .setSmallIcon(R.drawable.ic_notification_book_24)  // Use your app icon
                .setContentTitle("Rappel de connexion")
                .setContentText(randomMessage)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)  // The notification disappears when clicked
                .setContentIntent(pendingIntent);  // Attach the pending intent to the notification

// Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(1, builder.build());


    }
}
