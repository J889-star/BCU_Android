package com.mandarin.bcu.androidutil.asynchs;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mandarin.bcu.DownloadScreen;
import com.mandarin.bcu.MainActivity;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.DefineItf;
import com.mandarin.bcu.androidutil.StaticStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import common.CommonStatic;
import main.MainBCU;

public class CheckUpdates extends AsyncTask<Void,Integer,Void> {
    private final String path;
    private final boolean cando;

    private final WeakReference<Activity> weakReference;

    private boolean lang;
    private String [] lan = {"/en/","/jp/","/kr/","/zh/"};
    private String [] langfile = {"EnemyName.txt","StageName.txt","UnitName.txt","UnitExplanation.txt","EnemyExplanation.txt","CatFruitExplanation.txt"};
    private String source;
    private ArrayList<String> fileneed;
    private ArrayList<String> filenum;
    private boolean contin = true;

    private JSONObject ans;

    CheckUpdates(String path, boolean lang, ArrayList<String> fileneed, ArrayList<String> filenum, Activity context, boolean cando) {
        this.weakReference = new WeakReference<>(context);
        this.path = path;
        this.lang = lang;
        this.fileneed = fileneed;
        this.filenum = filenum;
        this.cando = cando;

        source = path+"/lang";
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();
        TextView checkstate = activity.findViewById(R.id.mainstup);
        checkstate.setText(R.string.main_check_up);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        File output;
        try {
            JSONObject inp = new JSONObject();
            inp.put("bcuver", MainBCU.ver);

            String assetlink = "http://battlecatsultimate.cf/api/java/getAssets.php";
            URL asseturl = new URL(assetlink);
            HttpURLConnection connection = (HttpURLConnection) asseturl.openConnection();
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");

            OutputStream os = connection.getOutputStream();
            os.write(inp.toString().getBytes(StandardCharsets.UTF_8));
            os.close();

            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
            String result = readAll(new BufferedReader(isr));

            ans = new JSONObject(result);
            is.close();
            connection.disconnect();

            for (String s1 : lan) {
                for (String s : langfile) {
                    String url = "http://battlecatsultimate.cf/api/resources/lang";
                    String langurl = url + s1 + s;
                    URL link = new URL(langurl);
                    HttpURLConnection c = (HttpURLConnection) link.openConnection();
                    c.setRequestMethod("GET");
                    c.connect();

                    InputStream urlis = c.getInputStream();

                    byte[] buf = new byte[1024];
                    int len1;
                    int size = 0;
                    while ((len1 = urlis.read(buf)) != -1) {
                        size += len1;
                    }


                    output = new File(source + s1, s);

                    if (output.exists()) {
                        if (output.length() != size) {
                            lang = true;
                            break;
                        }
                    } else {
                        lang = true;
                        break;
                    }

                    c.disconnect();
                }

                if (lang) {
                    break;
                }
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
            contin = false;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            contin = false;
        } catch (IOException e) {
            e.printStackTrace();
            contin = false;
        } catch (JSONException e) {
            e.printStackTrace();
            contin = false;
        }


        publishProgress(1);

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Activity activity = weakReference.get();
        TextView checkstate = activity.findViewById(R.id.mainstup);
        System.out.println(lang);
        if (values[0] == 1) {
            checkstate.setText(R.string.main_check_file);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        System.out.println("Continue : "+contin);
        if(contin) {
            checkFiles(ans);

            Activity activity = weakReference.get();

            if (fileneed.isEmpty() && filenum.isEmpty()) {
                new AddPathes(activity).execute();
            }
        } else {
            new CheckUpdates(path,lang,fileneed,filenum,weakReference.get(),cando).execute();
        }
    }

    private String readAll(Reader rd) {
        try {
            StringBuilder sb = new StringBuilder();
            int chara;
            while ((chara = rd.read()) != -1) {
                sb.append((char)chara);
            }

            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void checkFiles(JSONObject asset) {
        Activity activity = weakReference.get();

        try {
            Map<String, String> libmap = new TreeMap<>();
            JSONArray ja = asset.getJSONArray("android");

            for(int i=0;i<ja.length();i++) {
                JSONArray ent = ja.getJSONArray(i);
                libmap.put(ent.getString(0),ent.getString(1));
            }

            ArrayList<String> lib = new ArrayList<>(libmap.keySet());

            AlertDialog.Builder donloader = new AlertDialog.Builder(activity);
            final Intent intent = new Intent(activity, DownloadScreen.class);
            donloader.setTitle(R.string.main_file_need);
            donloader.setMessage(R.string.main_file_up);
            donloader.setPositiveButton(R.string.main_file_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(lang && !fileneed.contains("Language")) {
                        fileneed.add("Language");
                        filenum.add(String.valueOf(filenum.size()));
                    }
                    System.out.println(fileneed.toString());
                    intent.putExtra("fileneed", fileneed);
                    intent.putExtra("filenum", filenum);
                    activity.startActivity(intent);
                    activity.finish();
                }
            });

            donloader.setNegativeButton(R.string.main_file_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(!cando)
                        activity.finish();
                }
            });

            donloader.setCancelable(false);

            try {
                Set<String> libs = com.mandarin.bcu.io.Reader.getInfo(path);

                if(libs != null && libs.isEmpty()) {
                    for (int i = 0; i < lib.size(); i++) {
                        fileneed.add(lib.get(i));
                        filenum.add(String.valueOf(i));
                    }
                    AlertDialog downloader = donloader.create();
                    downloader.show();
                } else {
                    for (int i = 0; i < lib.size(); i++) {
                        if (!(libs != null && libs.contains(lib.get(i)))) {
                            fileneed.add(lib.get(i));
                            filenum.add(String.valueOf(i));
                        }
                    }

                    if (!filenum.isEmpty()) {
                        donloader.setTitle(R.string.main_file_x);
                        AlertDialog downloader = donloader.create();
                        downloader.show();
                    } else if (lang) {
                        fileneed.add("Language");
                        filenum.add(String.valueOf(filenum.size()));
                        donloader.setTitle(R.string.main_file_x);
                        AlertDialog downloader = donloader.create();
                        downloader.show();
                    }
                }
            } catch (Exception e) {
                for (int i = 0; i < lib.size(); i++) {
                    fileneed.add(lib.get(i));
                    filenum.add(String.valueOf(i));
                }
                donloader.setTitle(R.string.main_info_corr);
                donloader.setMessage(R.string.main_info_cont);
                AlertDialog downloader = donloader.create();
                downloader.show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

class AddPathes extends AsyncTask<Void,Integer,Void> {
    private final WeakReference<Activity> weakReference;

    AddPathes(Activity activity) {
        this.weakReference = new WeakReference<>(activity);
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();
        TextView checkstate = activity.findViewById(R.id.mainstup);
        checkstate.setText(R.string.main_file_read);
    }


    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();
        SharedPreferences shared = activity.getSharedPreferences("configuration", Context.MODE_PRIVATE);
        com.mandarin.bcu.decode.ZipLib.init();
        com.mandarin.bcu.decode.ZipLib.read();

        StaticStore.getUnitnumber();
        StaticStore.getEnemynumber();
        StaticStore.root = 1;

        new DefineItf().init();

        StaticStore.getLang(shared.getInt("Language",0));

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakReference.get();

        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }
}