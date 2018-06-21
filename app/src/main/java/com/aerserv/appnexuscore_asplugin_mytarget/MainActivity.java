package com.aerserv.appnexuscore_asplugin_mytarget;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.os.Handler;
import android.view.View;

import com.aerserv.sdk.AerServSdk;
import com.appnexus.opensdk.*;

public class MainActivity extends AppCompatActivity {

    private BannerAdView bav = null;
    private InterstitialAdView iav = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AerServSdk.init(MainActivity.this, "380000");
    }

    public void showBanner(View view) {
        if(bav != null){
            bav.destroy();
            bav = null;
        }
        bav = (BannerAdView) findViewById(R.id.banner);
        bav.setPlacementID("1326299");
        bav.setAdSize(300,50);
        bav.setAutoRefreshInterval(60000);
        bav.setShouldServePSAs(true);
        bav.setOpensNativeBrowser(true);
        bav.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded(AdView adView) { System.out.println("MyAppNexusApp onAdLoaded()"); }

            @Override
            public void onAdRequestFailed(AdView adView, ResultCode resultCode) {
                System.out.println("MyAppNexusApp onAdRequestFailed() + result code: " + resultCode.toString());
            }

            @Override
            public void onAdExpanded(AdView adView) { System.out.println("MyAppNexusApp onAdExpanded()"); }

            @Override
            public void onAdCollapsed(AdView adView) { System.out.println("MyAppNexusApp onAdCollapsed()"); }

            @Override
            public void onAdClicked(AdView adView) { System.out.println("MyAppNexusApp onAdClicked()"); }
        });
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                bav.loadAd();
//            }
//        }, 0);
    }

    public void preloadInterstitial(View view) {
        iav = new InterstitialAdView(this);
        iav.setPlacementID("1326299");

        iav.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded(AdView adView) { System.out.println("MyAppNexusApp onAdLoaded()"); }

            @Override
            public void onAdRequestFailed(AdView adView, ResultCode resultCode) {
                System.out.println("MyAppNexusApp onAdRequestFailed() + result code: " + resultCode.toString());
            }

            @Override
            public void onAdExpanded(AdView adView) { System.out.println("MyAppNexusApp onAdExpanded()"); }

            @Override
            public void onAdCollapsed(AdView adView) { System.out.println("MyAppNexusApp onAdCollapsed()"); }

            @Override
            public void onAdClicked(AdView adView) { System.out.println("MyAppNexusApp onAdClicked()"); }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                iav.loadAd();
            }
        }, 0);
    }
    public void showInterstitial(View view) {
        iav.show();
    }
}
