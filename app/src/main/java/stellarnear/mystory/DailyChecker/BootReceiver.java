package stellarnear.mystory.DailyChecker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import stellarnear.mystory.Activities.MainActivity;

public class BootReceiver extends BroadcastReceiver {
   @Override
   public void onReceive(Context context, Intent intent) {
      if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
         MainActivity.setUpDailyChecker(context);  // Reschedule the daily alarm
      }
   }
}