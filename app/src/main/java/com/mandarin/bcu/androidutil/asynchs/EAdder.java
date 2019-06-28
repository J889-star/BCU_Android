package com.mandarin.bcu.androidutil.asynchs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EdgeEffect;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.mandarin.bcu.EnemyInfo;
import com.mandarin.bcu.R;
import com.mandarin.bcu.androidutil.EDefiner;
import com.mandarin.bcu.androidutil.StaticStore;
import com.mandarin.bcu.androidutil.adapters.EnemyListAdapter;
import com.mandarin.bcu.androidutil.adapters.SingleClick;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

import common.system.MultiLangCont;
import common.system.P;
import common.system.files.VFile;

public class EAdder extends AsyncTask<Void,Integer,Void> {
    private final WeakReference<Activity> weakReference;

    public EAdder(Activity activity) {
        this.weakReference = new WeakReference<>(activity);
    }

    @Override
    protected void onPreExecute() {
        Activity activity = weakReference.get();

        ListView listView = activity.findViewById(R.id.enlist);
        listView.setVisibility(View.GONE);

        ImageButton back = activity.findViewById(R.id.enlistbck);
        back.setOnClickListener(new SingleClick() {
            @Override
            public void onSingleClick(View v) {
                activity.finish();
            }
        });
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Activity activity = weakReference.get();

        new EDefiner().define(activity);

        if(StaticStore.ebitmaps == null) {
            StaticStore.ebitmaps = new Bitmap[StaticStore.emnumber];

            for(int i = 0;i < StaticStore.emnumber;i++) {
                String shortPath = "./org/enemy/"+number(i)+"/edi_"+number(i)+".png";

                try {
                    float ratio = 32f/32f;
                    StaticStore.ebitmaps[i] = StaticStore.getResizeb((Bitmap) Objects.requireNonNull(VFile.getFile(shortPath)).getData().getImg().bimg(), activity, 85f*ratio, 32f*ratio);
                } catch(NullPointerException e) {
                    float ratio = 32f/32f;
                    StaticStore.ebitmaps[i] = StaticStore.empty(activity, 85f*ratio, 32f*ratio);
                }
            }
        }

        if(StaticStore.enames == null) {
            StaticStore.enames = new String[StaticStore.emnumber];

            for(int i = 0;i<StaticStore.emnumber;i++) {
                StaticStore.enames[i] = withID(i, MultiLangCont.ENAME.getCont(StaticStore.enemies.get(i)));
            }
        }

        publishProgress(0);

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... results) {
        Activity activity = weakReference.get();

        ListView list = activity.findViewById(R.id.enlist);
        ArrayList<Integer> location = new ArrayList<>();
        for(int i = 0;i<StaticStore.emnumber;i++) {
            location.add(i);
        }
        EnemyListAdapter enemy = new EnemyListAdapter(activity,StaticStore.enames,StaticStore.ebitmaps,location);
        list.setAdapter(enemy);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(SystemClock.elapsedRealtime() - StaticStore.enemyinflistClick < StaticStore.INTERVAL)
                    return;

                StaticStore.enemyinflistClick = SystemClock.elapsedRealtime();

                Intent result = new Intent(activity, EnemyInfo.class);
                result.putExtra("ID",location.get(position));
                activity.startActivity(result);
            }
        });
    }

    @Override
    protected void onPostExecute(Void result) {
        Activity activity = weakReference.get();

        super.onPostExecute(result);
        ListView list = activity.findViewById(R.id.enlist);
        list.setVisibility(View.VISIBLE);
        ProgressBar prog = activity.findViewById(R.id.enlistprog);
        prog.setVisibility(View.GONE);
    }

    private String number(int num) {
        if (0 <= num && num < 10) {
            return "00" + num;
        } else if (10 <= num && num <= 99) {
            return "0" + num;
        } else {
            return String.valueOf(num);
        }
    }

    private String withID(int id, String name) {
        String result;
        String names = name;

        if(names == null)
            names = "";

        if(names.equals("")) {
            result = number(id);
        } else {
            result = number(id)+" - "+names;
        }

        return result;
    }
}
