package com.mjapps.mjfilebrowse.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.util.List;

/**
 * Created by kishan on 29/04/15.
 * Cache cleaner implementation
 */
public class CacheCleaner {
    private final String PACKAGE_NAME = "browser.xtreme.com.xtremefbrowser";

    //trims the cache
    public void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //kill cached processes
    public void killCachedProcesses(Context context) {
        List<ApplicationInfo> packages;
        ActivityManager manager;
        PackageManager pm;
        pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);

        manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ApplicationInfo packageInfo : packages) {
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                continue;
            }
            if (packageInfo.packageName.equals(PACKAGE_NAME)) {
                continue;
            }
            manager.killBackgroundProcesses(packageInfo.packageName);
        }
    }

    //delete cache directory
    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
}
