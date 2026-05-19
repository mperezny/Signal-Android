package org.thoughtcrime.securesms.util;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.signal.core.util.logging.Log;

public class PlayServicesUtil {

  private static final String TAG = Log.tag(PlayServicesUtil.class);

  public enum PlayServicesStatus {
    SUCCESS,
    MISSING,
    NEEDS_UPDATE,
    TRANSIENT_ERROR
  }

  private static boolean isGmsPackageEnabled(Context context) {
    try {
      ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo("com.google.android.gms", 0);

      return applicationInfo != null && applicationInfo.enabled;
    } catch (PackageManager.NameNotFoundException e) {
      Log.w(TAG, e);
      return false;
    }
  }

  public static PlayServicesStatus getPlayServicesStatus(Context context) {
    int gcmStatus = 0;

    try {
      gcmStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
    } catch (Throwable t) {
      Log.w(TAG, t);
      return PlayServicesStatus.MISSING;
    }

    Log.i(TAG, "Play Services: " + gcmStatus);

    if (gcmStatus != ConnectionResult.SUCCESS && isGmsPackageEnabled(context)) {
      Log.w(TAG, "Personal build: GoogleApiAvailability returned " + gcmStatus +
                 ", but com.google.android.gms is installed/enabled. Treating as SUCCESS.");
      return PlayServicesStatus.SUCCESS;
    }

    switch (gcmStatus) {
      case ConnectionResult.SUCCESS:
        return PlayServicesStatus.SUCCESS;
      case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
        try {
          ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo("com.google.android.gms", 0);

          if (applicationInfo != null && !applicationInfo.enabled) {
            return PlayServicesStatus.MISSING;
          }
        } catch (PackageManager.NameNotFoundException e) {
          Log.w(TAG, e);
        }

        return PlayServicesStatus.NEEDS_UPDATE;
      case ConnectionResult.SERVICE_DISABLED:
      case ConnectionResult.SERVICE_MISSING:
      case ConnectionResult.SERVICE_INVALID:
      case ConnectionResult.API_UNAVAILABLE:
      case ConnectionResult.SERVICE_MISSING_PERMISSION:
        return PlayServicesStatus.MISSING;
      default:
        return PlayServicesStatus.TRANSIENT_ERROR;
    }
  }

}
