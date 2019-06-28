package com.mandarin.bcu.androidutil;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.TypedValue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import common.battle.Treasure;
import common.system.fake.FakeImage;
import common.util.anim.ImgCut;
import common.util.unit.Enemy;
import common.util.unit.Unit;

public class StaticStore {
    public static final String [] LIBREQ = { "000001", "000002", "000003", "080602", "080603","080604","080605","080700"};
    public static final String [] OPTREQS = { "080504" };
    public static final String [] lang = { "", "en", "zh", "ko", "ja", "ru", "de", "fr", "nl", "es" };
    public static final long INTERVAL = 1000;

    public static int root = 0;

    public static Bitmap[] bitmaps = null;
    public static String[] names = null;
    public static List<Unit> units = null;
    public static Treasure t = null;
    public static FakeImage[] img15 = null;
    public static Bitmap[] icons = null;
    public static Bitmap[] picons = null;
    public static Bitmap[] fruit = null;
    public static String [] addition = null;
    public static int unitnumber;
    public static long unitinflistClick = 0;
    public static long enemyinflistClick = 0;

    public static List<Enemy> enemies = null;
    public static String[] enames = null;
    public static Bitmap[] ebitmaps = null;
    public static int emnumber;

    public static void getUnitnumber() {
        String unitpath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files/org/unit/";

        File f = new File(unitpath);
        unitnumber = f.listFiles().length;
    }

    public static void getEnemynumber() {
        String empath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files/org/enemy/";

        File f = new File(empath);
        emnumber = f.listFiles().length;
    }

    public static Bitmap getResize(Drawable drawable, Context context,float dp) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,r.getDisplayMetrics());
        Bitmap b = ((BitmapDrawable)drawable).getBitmap();
        return Bitmap.createScaledBitmap(b,(int)px,(int)px,false);
    }

    public static Bitmap getResizeb(Bitmap b, Context context, float dp) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,r.getDisplayMetrics());
        BitmapDrawable bd = new BitmapDrawable(context.getResources(),Bitmap.createScaledBitmap(b,(int)px,(int)px,true));
        bd.setFilterBitmap(true);
        bd.setAntiAlias(true);
        return bd.getBitmap();
    }

    public static Bitmap getResizeb(Bitmap b, Context context, float w, float h) {
        Resources r = context.getResources();
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,w,r.getDisplayMetrics());
        float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,h,r.getDisplayMetrics());
        BitmapDrawable bd = new BitmapDrawable(context.getResources(),Bitmap.createScaledBitmap(b,(int)width,(int)height,true));
        bd.setFilterBitmap(true);
        bd.setAntiAlias(true);
        return bd.getBitmap();
    }

    public static Bitmap empty(Context context,float w,float h) {
        Resources r = context.getResources();
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,w,r.getDisplayMetrics());
        float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,h,r.getDisplayMetrics());
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        return Bitmap.createBitmap((int)width,(int)height,conf);
    }

    public static int dptopx(float dp,Context context) {
        Resources r = context.getResources();
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,r.getDisplayMetrics());
    }

    public static void readImg() {
        String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.mandarin.BCU/files/org/page/img015.png";
        String imgcut = "./org/page/img015.imgcut";
        File f = new File(path);
        ImgCut img = ImgCut.newIns(imgcut);
        try {
            FakeImage png = FakeImage.read(f);
            img15 = img.cut(png);
        } catch (IOException e) {
            e.printStackTrace();
            img15 = null;
        }
    }
}
