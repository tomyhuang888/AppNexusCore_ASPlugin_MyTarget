package com.aerserv.appnexus;

import android.app.Activity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.aerserv.sdk.AerServBanner;
import com.aerserv.sdk.AerServConfig;
import com.aerserv.sdk.AerServEvent;
import com.aerserv.sdk.AerServEventListener;
import com.aerserv.sdk.utils.AerServLog;
import com.appnexus.opensdk.MediatedBannerAdView;
import com.appnexus.opensdk.MediatedBannerAdViewController;
import com.appnexus.opensdk.ResultCode;
import com.appnexus.opensdk.TargetingParameters;

import java.util.List;
import java.util.Map;

/**
 * Aerserv SDK plugin to mediate interstitial to AppNexus.
 * @version 0.1.0
 */
public class AerServCustomEventBanner implements MediatedBannerAdView {

  private static final String AD_CLICKED_EVENT = "Banner ad clicked";
  private static final String AD_DISMISSED_EVENT = "Banner ad dismissed";
  private static final String AD_FAILED_EVENT = "Failed to load banner ad";
  private static final String AD_IMPRESSION_EVENT = "Banner ad made a impression";
  private static final String AD_LOADED_EVENT = "Banner ad loaded";
  private static final String AD_LOADING = "Loading banner ad";
  private static final String EXCEPTION_NULL_CONTEXT = "activity cannot be null";
  private static final String EXCEPTION_NULL_LISTENER = "mediatedBannerAdViewController" +
      "cannot be null";
  private static final String LOG_TAG = AerServCustomEventBanner.class.getSimpleName();
  private static final String MISSING_PLC = "Cannot load AerServ ad because placement is missing";
  private static final String UNMAP_EVENT = "The following AerServ banner ad event cannot be" +
      " mapped";

  private AerServBanner aerServBanner;

  @Override
  public void destroy() {
    if (aerServBanner != null) {
      aerServBanner.kill();
      aerServBanner = null;
    }
  }

  @Override
  public void onDestroy() {
    destroy();
  }

  @Override
  public void onPause() {
    aerServBanner.pause();
  }

  @Override
  public void onResume() {
    aerServBanner.play();
  }

  @Override
  public View requestAd(final MediatedBannerAdViewController mediatedBannerAdViewController,
                        final Activity activity, String parameter, String adId, int width, int height,
                        TargetingParameters targetingParameters) {

    // Check for null
    if (activity == null) {
      mediatedBannerAdViewController.onAdFailed(ResultCode.INVALID_REQUEST);
    }
    if (mediatedBannerAdViewController == null) {
      throw new IllegalArgumentException(EXCEPTION_NULL_LISTENER);
    }

    try {
      // Configuration for banner
      AerServLog.d(LOG_TAG, "Placement is: " + adId);
      if (adId == null) {
        AerServLog.w(LOG_TAG, MISSING_PLC);
        mediatedBannerAdViewController.onAdFailed(ResultCode.INVALID_REQUEST);
      }
      AerServConfig aerServConfig = new AerServConfig(activity, adId);
      aerServConfig.setRefreshInterval(0);
      Map<String, String> pubKeys = AerServPluginUtil
              .getHashMap(targetingParameters.getCustomKeywords());
      if(pubKeys != null) {
        aerServConfig.setPubKeys(pubKeys);
      }
      aerServConfig.setEventListener(new AerServEventListener() {
        @Override
        public void onAerServEvent(final AerServEvent aerServEvent, List<Object> list) {
          activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              /*
               * Aerserv event's needs to be mapped to AppNexus event. The events that to be called is
               * onAdClicked, onAdCollapsed, onAdExpanded and onAdLoaded.
               */
              switch (aerServEvent) {
                case AD_CLICKED:
                  AerServLog.d(LOG_TAG, AD_CLICKED_EVENT);
                  mediatedBannerAdViewController.onAdClicked();
                  break;
                case AD_DISMISSED:
                  AerServLog.d(LOG_TAG, AD_DISMISSED_EVENT);
                  mediatedBannerAdViewController.onAdCollapsed();
                  break;
                case AD_FAILED:
                  AerServLog.d(LOG_TAG, AD_FAILED_EVENT);
                  mediatedBannerAdViewController.onAdFailed(ResultCode.UNABLE_TO_FILL);
                  break;
                case AD_IMPRESSION:
                  AerServLog.d(LOG_TAG, AD_IMPRESSION_EVENT);
                  mediatedBannerAdViewController.onAdExpanded();
                case AD_LOADED:
                  AerServLog.d(LOG_TAG, AD_LOADED_EVENT);
                  mediatedBannerAdViewController.onAdLoaded();
                  break;
                default:
                  AerServLog.d(LOG_TAG, UNMAP_EVENT + ": " + aerServEvent.toString());
              }
            }
          });
        }
      });
      AerServLog.d(LOG_TAG, AD_LOADING);

      aerServBanner = new AerServBanner(activity);
      if(width != -1 && height != -1) {
        int widthDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width,
                activity.getResources().getDisplayMetrics());
        int heightDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height,
                activity.getResources().getDisplayMetrics());

        aerServBanner.setLayoutParams(new FrameLayout.LayoutParams(widthDP,
                heightDP, Gravity.CENTER));
      } else {
        aerServBanner.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
      }
      aerServBanner.configure(aerServConfig);
      aerServBanner.show();
      return aerServBanner;
    } catch (Exception e) {
      mediatedBannerAdViewController.onAdFailed(ResultCode.INVALID_REQUEST);
    }
    return null;
  }
}
