package stellarnear.mystory.DailyChecker;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Random;

import stellarnear.mystory.Activities.LibraryLoader;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;

public class DailyCheckWorker extends Worker {
    private Tools tools = Tools.getTools();
    public DailyCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        LibraryLoader.loadLibrary(getApplicationContext());
        if (!LibraryLoader.wasTheApplicationLaunchedToday()) {
            // Trigger notification
            sendNotification();
        }
        return Result.success();
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "daily_reminder_channel")
                .setSmallIcon(R.drawable.ic_notification_book_24)  // Use your app icon
                .setContentTitle("Rappel de connexion")
                .setContentText(randomMessage)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(1, builder.build());

    }
}
