package stellarnear.mystory.SettingsFraments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.view.Surface;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import stellarnear.mystory.Activities.LibraryLoader;
import stellarnear.mystory.Activities.MainActivity;
import stellarnear.mystory.Activities.ObservatoryActivity;
import stellarnear.mystory.Activities.SaveSharedPreferencesActivity;
import stellarnear.mystory.Activities.ShelfActivity;
import stellarnear.mystory.BooksLibs.Book;
import stellarnear.mystory.DailyChecker.DailyChecker;
import stellarnear.mystory.R;
import stellarnear.mystory.Tools;
import stellarnear.mystory.UITools.MyLottieDialog;

public class SettingsFragment extends CustomPreferenceFragment {
    private Activity mA;
    private Context mC;
    private final List<String> histoPrefKeys = new ArrayList<>();
    private final List<String> histoTitle = new ArrayList<>();

    private String currentPageKey;
    private String currentPageTitle;

    private final Tools tools = Tools.getTools();
    private PrefInfoScreenFragment prefInfoScreenFragment;

/*
    private OnSharedPreferenceChangeListener listener =
            new OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    if (key.contains("they_key_that_triggersomething")) {
                        do nomething
                    }
                }
            };

 */

    @Override
    protected void onCreateFragment() {
        //settings.registerOnSharedPreferenceChangeListener(listener);  if we need to catch something
        this.mA = getActivity();
        this.mC = getContext();

        addPreferencesFromResource(R.xml.pref);
        this.histoPrefKeys.add("pref");

        this.histoTitle.add("Paramètres");

        this.prefInfoScreenFragment = new PrefInfoScreenFragment(mA, mC);
    }

    @Override
    protected void onPreferenceTreeClickFragment(android.preference.PreferenceScreen preferenceScreen, android.preference.Preference preference) throws Exception {
        if (preference.getKey().contains("pref_")) {
            histoPrefKeys.add(preference.getKey());
            histoTitle.add(preference.getTitle().toString());
            this.currentPageKey = preference.getKey();
            this.currentPageTitle = preference.getTitle().toString();
            navigate();
        } else {
            action(preference);
        }
    }

    // will be called by SettingsActivity (Host Activity)
    public void onUpButton() {
        if (histoPrefKeys.get(histoPrefKeys.size() - 1).equalsIgnoreCase("pref") || histoPrefKeys.size() <= 1) // in top-level
        {
            Intent intent;
            if (getActivity().getWindowManager().getDefaultDisplay().getRotation() == Surface.ROTATION_0) {
                intent = new Intent(mA, MainActivity.class);
            } else if (getActivity().getWindowManager().getDefaultDisplay().getRotation() == Surface.ROTATION_180) {
                intent = new Intent(mA, ObservatoryActivity.class);
            } else {
                intent = new Intent(mA, ShelfActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            mA.startActivity(intent);
        } else // in sub-level
        {
            currentPageKey = histoPrefKeys.get(histoPrefKeys.size() - 2);
            currentPageTitle = histoTitle.get(histoTitle.size() - 2);
            navigate();
            histoPrefKeys.remove(histoPrefKeys.size() - 1);
            histoTitle.remove(histoTitle.size() - 1);
        }
    }

    @Override
    protected void onDestroyFragment() {
        //settings.unregisterOnSharedPreferenceChangeListener(listener);
    }


    private void navigate() {
        if (currentPageKey.equalsIgnoreCase("pref")) {
            displayMainPage();
        } else if (currentPageKey.contains("pref_")) {
            loadPage();
            if (currentPageKey.equalsIgnoreCase("pref_gift")) {
                PreferenceCategory otherList = (PreferenceCategory) findPreference("pref_gift_list_cat");

                if (LibraryLoader.getAccessStats().getGiftsUnclaimed().size() > 0) {
                    //we pack gift by types
                    Map<String, Integer> giftNumber = new HashMap<>();
                    for (String gift : LibraryLoader.getAccessStats().getGiftsUnclaimed()) {
                        giftNumber.putIfAbsent(gift, 0);
                        giftNumber.put(gift, giftNumber.get(gift) + 1);
                    }
                    int nthgift = 0;
                    for (Map.Entry<String, Integer> entry : giftNumber.entrySet()) {
                        Preference pref = new Preference(mC);
                        pref.setKey("gift_" + nthgift);
                        pref.setTitle(entry.getValue() + " x " + entry.getKey());
                        //pref.setSummary(txt);
                        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                new AlertDialog.Builder(mC)
                                        .setIcon(R.drawable.ic_warning_24dp)
                                        .setTitle("Réclamer ce cadeau ?")
                                        .setMessage("Confirmes tu obtenir le cadeau :\n\n" + entry.getKey())
                                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                LibraryLoader.getAccessStats().claimGift(entry.getKey());
                                                LibraryLoader.saveAccessStats();
                                                navigate();
                                            }
                                        })
                                        .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .show();
                                return false;
                            }
                        });
                        otherList.addPreference(pref);

                    }
                } else {
                    Preference pref = new Preference(mC);
                    pref.setKey("nogift");
                    pref.setTitle("Pas de cadeaux à réclamer");
                    otherList.addPreference(pref);

                }
            }
        }
    }

    private void displayMainPage() {
        getPreferenceScreen().removeAll();
        int xmlID = R.xml.pref;
        addPreferencesFromResource(xmlID);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(currentPageTitle);
    }

    private void loadPage() {
        try {
            getPreferenceScreen().removeAll();
            int xmlID = getResources().getIdentifier(currentPageKey, "xml", getContext().getPackageName());
            addPreferencesFromResource(xmlID);
        } catch (Exception e) {
            e.printStackTrace();
            log.err("Error during loadPage in settings", e);
            displayMainPage();
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(currentPageTitle);
    }

    private void action(Preference preference) throws Exception {
        switch (preference.getKey()) {
            case "infos":
                prefInfoScreenFragment.showInfo();
                break;

            case "export_save":
                Intent intentSave = new Intent(mC, SaveSharedPreferencesActivity.class);
                intentSave.putExtra("ACTION_TYPE", "SAVE");
                intentSave.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                mC.startActivity(intentSave);
                break;

            case "import_save":
                Intent intentLoad = new Intent(mC, SaveSharedPreferencesActivity.class);
                intentLoad.putExtra("ACTION_TYPE", "LOAD");
                intentLoad.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                mC.startActivity(intentLoad);
                break;

            case "send_report":
                log.sendReport(getActivity());
                break;

            case "optimize_all_images":

                new AlertDialog.Builder(mC)
                        .setIcon(R.drawable.ic_warning_24dp)
                        .setTitle("Réparer et optimiser les images")
                        .setMessage("Veux tu réparer les images manquantes et optimiser toutes les images ?")
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Button cancelButton = new Button(getContext());
                                cancelButton.setBackground(getContext().getDrawable(R.drawable.button_cancel_gradient));

                                cancelButton.setText("Fermer");
                                cancelButton.setTextColor(getContext().getColor(R.color.end_gradient_button_cancel));

                                MyLottieDialog fixingAlert = new MyLottieDialog(getActivity())
                                        .setTitle("Réparation en cours")
                                        .setAnimation(R.raw.fixing)
                                        .setMessage("-/-")
                                        .setAnimationRepeatCount(-1)
                                        .addActionButton(cancelButton)
                                        .setAutoPlayAnimation(true)
                                        .setCancelable(false)
                                        .setOnShowListener(dialogInterface -> {
                                        })
                                        .setOnDismissListener(dialogInterface -> {
                                        })
                                        .setOnCancelListener(dialogInterface -> {
                                        });

                                cancelButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        fixingAlert.dismiss();
                                    }
                                });
                                fixingAlert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialogInterface) {
                                        tools.customToast(mC, "Réparation finie");
                                    }
                                });
                                fixingAlert.show();
                                final AtomicInteger nFix = new AtomicInteger();
                                final AtomicInteger totalFix = new AtomicInteger();
                                totalFix.addAndGet(LibraryLoader.getDownloadList().size());
                                totalFix.addAndGet(LibraryLoader.getShelf().size());
                                totalFix.addAndGet(LibraryLoader.getWishList().size());
                                if (LibraryLoader.getCurrentBook() != null) {
                                    totalFix.getAndIncrement();
                                }


                                Tools.OnFixedImageEventListener fixListener = new Tools.OnFixedImageEventListener() {
                                    @Override
                                    public void onEvent() {
                                        nFix.incrementAndGet();
                                        fixingAlert.setMessage(nFix.get() + " / " + totalFix.get() + " images de livres réparées");
                                        if (nFix.get() == totalFix.get()) {
                                            fixingAlert.dismiss();
                                        }
                                    }
                                };

                                List<Callable<Void>> allSync = new ArrayList<>();

                                if (LibraryLoader.getCurrentBook() != null) {
                                    addCallable(allSync, LibraryLoader.getCurrentBook(), fixListener);
                                }
                                for (Book book : LibraryLoader.getDownloadList()) {
                                    addCallable(allSync, book, fixListener);
                                }
                                for (Book book : LibraryLoader.getShelf()) {
                                    addCallable(allSync, book, fixListener);
                                }
                                for (Book book : LibraryLoader.getWishList()) {
                                    addCallable(allSync, book, fixListener);
                                }
                                if (allSync.size() > 0) {
                                    int availableProcessors = Runtime.getRuntime().availableProcessors();
                                    ExecutorService executor = Executors.newFixedThreadPool(availableProcessors * 10);
                                    log.info(
                                            allSync.size() + " image fixing in " + availableProcessors * 4
                                                    + " parralel thread  !");
                                    try {
                                        executor.invokeAll(allSync);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                        log.err("Error during parallel refresh of images in settings", e);
                                    }
                                }
                            }
                        })
                        .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();

                break;

            case "delete_files":
                new AlertDialog.Builder(mC)
                        .setIcon(R.drawable.ic_warning_24dp)
                        .setTitle("Suppression des fichiers locaux")
                        .setMessage("Es-tu sûre de vouloir supprimer tout les fichiers locaux de l'application ?")
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (File file : LibraryLoader.getInternalStorageDir().listFiles()) {
                                    file.delete();
                                }
                                tools.customToast(mC, "Suppression finie");
                            }
                        })
                        .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();

                break;
            case "test_button_notif":
                DailyChecker.sendNotification(mC);
                break;


        }
    }

    private void addCallable(List<Callable<Void>> allSync, Book book, Tools.OnFixedImageEventListener fixListener) {
        allSync.add(new Callable<Void>() {
            @Override
            public Void call() {
                try {
                    fixBookImage(book, fixListener);
                } catch (Exception e) {
                    log.warn("Could fix image for book : " + book.getName(), e);
                    if (fixListener != null) {
                        fixListener.onEvent();
                    }
                }
                return null;
            }
        });
    }

    private void fixBookImage(Book book, Tools.OnFixedImageEventListener fixListener) {
        if (book == null) {
            if (fixListener != null) {
                fixListener.onEvent();
            }
            return;
        }
        if (book.getImagePath() != null && new File(book.getImagePath()).exists()) {
            new File(book.getImagePath()).delete();
        }
        book.setImagePath(null);
        book.discardBytes();

        if (book.getCover_url() == null) { //custom book
            try {
                // Load the drawable resource as a Bitmap
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.custom_book);

                // Convert the Bitmap to a byte array
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, buffer);
                byte[] imageBytes = buffer.toByteArray();

                // Create the file to save the image
                File imageFile = new File(LibraryLoader.getInternalStorageDir(), book.getUuid().toString() + ".jpg");
                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                    // Write the byte array to the file
                    fos.write(imageBytes);
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    log.err("Error during fixImage in settings", e);
                }

                // Set the image path and perform additional operations
                book.setImagePath(imageFile.getAbsolutePath());
                tools.convertByteToStoredFile(book);
                LibraryLoader.saveBook(book);
                if (fixListener != null) {
                    fixListener.onEvent();
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.err("Error during fixImage in settings", e);
                if (fixListener != null) {
                    fixListener.onEvent();
                }
            }
        } else {
            tools.fixMissingImage(book, fixListener);
        }
    }
}