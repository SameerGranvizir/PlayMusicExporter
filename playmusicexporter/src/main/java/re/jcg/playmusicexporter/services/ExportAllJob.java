package re.jcg.playmusicexporter.services;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import re.jcg.playmusicexporter.settings.PlayMusicExporterPreferences;

public class ExportAllJob extends JobService {
    public static final String TAG = "AutoGPME_ExportJob";


    public static void scheduleExport(final Context pContext) {
        PlayMusicExporterPreferences.init(pContext);
        PlayMusicExporterPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.contains("auto")) {
                    scheduleExport(pContext);
                    Log.i(TAG, "Preference changed: " + key);
                }
            }
        });
        if (PlayMusicExporterPreferences.getAutoExportEnabled()) {
            long lInterval = PlayMusicExporterPreferences.getAutoExportFrequency();
            boolean lRequireUnmeteredNetwork = PlayMusicExporterPreferences.getAutoExportRequireUnmetered();
            boolean lRequireCharging = PlayMusicExporterPreferences.getAutoExportRequireCharging();

            JobScheduler lJobScheduler = (JobScheduler) pContext.getSystemService(JOB_SCHEDULER_SERVICE);
            ComponentName lComponentName = new ComponentName(pContext, ExportAllJob.class);
            JobInfo.Builder lBuilder = new JobInfo.Builder(42, lComponentName);
            lBuilder.setPeriodic(lInterval);
            lBuilder.setPersisted(true);
            if (lRequireUnmeteredNetwork)
                lBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
            lBuilder.setRequiresCharging(lRequireCharging);
            lJobScheduler.schedule(lBuilder.build());
        }
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "Started Job: " + params.toString());
        ExportAllService.startExport(this);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "Stopped Job: " + params.toString());
        return true;
    }
}