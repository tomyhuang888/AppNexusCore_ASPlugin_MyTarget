package com.aerserv.appnexus;

import android.app.Activity;
import android.util.Log;

import com.aerserv.sdk.AerServConfig;
import com.aerserv.sdk.AerServEvent;
import com.aerserv.sdk.AerServEventListener;
import com.aerserv.sdk.AerServInterstitial;
import com.aerserv.sdk.utils.AerServLog;
import com.appnexus.opensdk.MediatedInterstitialAdView;
import com.appnexus.opensdk.MediatedInterstitialAdViewController;
import com.appnexus.opensdk.ResultCode;
import com.appnexus.opensdk.TargetingParameters;

import java.util.List;
import java.util.Map;

/**
 * Aerserv SDK plugin to mediate interstitial to AppNexus.
 * @version 0.1.0
 */
public class AerServCustomEventInterstitial implements MediatedInterstitialAdView {

  private static final String AD_CLICKED_EVENT = "Interstitial ad clicked";
  private static final String AD_DISMISSED_EVENT = "Interstitial ad dismissed";
  private static final String AD_FAILED_EVENT = "Failed to load interstitial ad";
  private static final String AD_IMPRESSION_EVENT = "Interstitial ad made a impression";
  private static final String AD_LOADED_EVENT = "Interstitial ad loaded";
  private static final String AD_LOADING = "Loading interstitial ad";
  private static final String EXCEPTION_NULL_CONTEXT = "activity cannot be null";
  private static final String EXCEPTION_NULL_LISTENER = "mediatedInterstitialAdViewController" +
      "cannot be null";
  private static final String LOG_TAG = AerServCustomEventInterstitial.class.getSimpleName();
  private static final String MISSING_PLC = "Cannot load AerServ ad because placement is missing";
  private static final String PRELOAD_READY_EVENT = "Interstitial ad is ready";
  private static final String PRELOAD_NOT_READY = "show called, but interstitial is not ready";
  private static final String UNMAP_EVENT = "The following AerServ interstitial ad event cannot " +
      "be mapped";

  private AerServInterstitial aerServInterstitial = null;
  private AerServEventListener aerServEventListener = null;

  // When the interstitial is loaded and ready to play.
  private boolean isReady = false;

  @Override
  public void destroy() {
    if (aerServInterstitial != null) {
      aerServInterstitial.kill();
      aerServInterstitial = null;
    }
  }

  @Override
  public boolean isReady() {
    return isReady;
  }

  @Override
  public void onDestroy() {
    destroy();
  }

  @Override
  public void onPause() {
    aerServInterstitial.pause();
  }

  @Override
  public void onResume() {
    aerServInterstitial.play();
  }

  @Override
  public void requestAd(final MediatedInterstitialAdViewController
      mediatedInterstitialAdViewController, final Activity activity, String parameter, String adId,
      TargetingParameters targetingParameters) {

    // Check for nulls.
    if (activity == null) {
      mediatedInterstitialAdViewController.onAdFailed(ResultCode.INVALID_REQUEST);
    }
    if (mediatedInterstitialAdViewController == null) {
      throw new IllegalArgumentException(EXCEPTION_NULL_LISTENER);
    }
      if (adId == null) {
        AerServLog.w(LOG_TAG, MISSING_PLC);
      }

      // Configuration for Interstitial
      AerServLog.v(LOG_TAG, "Placement is: " + adId);
      if (adId == null) {
        mediatedInterstitialAdViewController.onAdFailed(ResultCode.INVALID_REQUEST);
      }

      AerServConfig aerServConfig = new AerServConfig(activity, adId);
      Map<String, String> pubKeys = AerServPluginUtil
              .getHashMap(targetingParameters.getCustomKeywords());
      if(pubKeys != null) {
        aerServConfig.setPubKeys(pubKeys);
      }
      aerServConfig.setPreload(true);
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
                  mediatedInterstitialAdViewController.onAdClicked();
                  AerServLog.d(LOG_TAG, AD_CLICKED_EVENT);
                  break;
                case AD_DISMISSED:
                  AerServLog.d(LOG_TAG, AD_DISMISSED_EVENT);
                  mediatedInterstitialAdViewController.onAdCollapsed();
                  break;
                case AD_FAILED:
                  mediatedInterstitialAdViewController.onAdFailed(ResultCode.UNABLE_TO_FILL);
                  AerServLog.d(LOG_TAG, AD_FAILED_EVENT);
                  break;
                case AD_IMPRESSION:
                  mediatedInterstitialAdViewController.onAdExpanded();
                  AerServLog.d(LOG_TAG, AD_IMPRESSION_EVENT);
                  break;
                case AD_LOADED:
                  mediatedInterstitialAdViewController.onAdLoaded();
                  AerServLog.d(LOG_TAG, AD_LOADED_EVENT);
                  break;
                case PRELOAD_READY:
                  AerServLog.d(LOG_TAG, PRELOAD_READY_EVENT);
                  /*
                   * mediatedInterstitialAdViewController.onAdLoaded needs to be called at preload to let
                   * AppNexus know that the ad is loaded and placed in the queue. If
                   * mediatedInterstitialAdViewController.onAdLoaded isn't called on PRELOAD_READY,
                   * AppNexus will timeout.
                   */
                  mediatedInterstitialAdViewController.onAdLoaded();
                  isReady = true;
                  break;
                default:
                  AerServLog.d(LOG_TAG, UNMAP_EVENT + ": " + aerServEvent.toString());
              }
            }
          });
        }
      });

      aerServEventListener = new AerServEventListener() {
        @Override
        public void onAerServEvent(AerServEvent aerServEvent, List<Object> list) {
          switch (aerServEvent) {
          /*
           * Aerserv event's needs to be mapped to AppNexus event. The events that to be called is
           * onAdClicked, onAdCollapsed, onAdExpanded and onAdLoaded.
           */
            case AD_CLICKED:
              mediatedInterstitialAdViewController.onAdClicked();
              AerServLog.d(LOG_TAG, AD_CLICKED_EVENT);
              break;
            case AD_DISMISSED:
              AerServLog.d(LOG_TAG, AD_DISMISSED_EVENT);
              mediatedInterstitialAdViewController.onAdCollapsed();
              break;
            case AD_FAILED:
              mediatedInterstitialAdViewController.onAdFailed(ResultCode.UNABLE_TO_FILL);
              AerServLog.d(LOG_TAG, AD_FAILED_EVENT);
              break;
            case AD_IMPRESSION:
              mediatedInterstitialAdViewController.onAdExpanded();
              AerServLog.d(LOG_TAG, AD_IMPRESSION_EVENT);
              break;
            case AD_LOADED:
              mediatedInterstitialAdViewController.onAdLoaded();
              AerServLog.d(LOG_TAG, AD_LOADED_EVENT);
              break;
            case PRELOAD_READY:
              AerServLog.d(LOG_TAG, PRELOAD_READY_EVENT);
            /*
             * mediatedInterstitialAdViewController.onAdLoaded needs to be called at preload to let
             * AppNexus know that the ad is loaded and placed in the queue. If
             * mediatedInterstitialAdViewController.onAdLoaded isn't called on PRELOAD_READY,
             * AppNexus will timeout.
             */
              mediatedInterstitialAdViewController.onAdLoaded();
              isReady = true;
              break;
            default:
              AerServLog.d(LOG_TAG, UNMAP_EVENT + ": " + aerServEvent.toString());
          }
        }};
      aerServConfig.setEventListener(aerServEventListener);
      AerServLog.d(LOG_TAG, AD_LOADING);
      aerServInterstitial = new AerServInterstitial(aerServConfig);
  }

  @Override
  public void show() {
    if (isReady()) {
      aerServInterstitial.show();
    } else {
      AerServLog.d(LOG_TAG, PRELOAD_NOT_READY);
    }
  }
}
