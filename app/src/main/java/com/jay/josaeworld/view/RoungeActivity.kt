package com.jay.josaeworld.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.jay.josaeworld.databinding.ActivityRoungeBinding

class RoungeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoungeBinding
    var mAdView: AdView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        binding = ActivityRoungeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        MobileAds.initialize(this) {}
        mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView!!.loadAd(adRequest)

        val webView = binding.mainWebview

        webView.settings.javaScriptEnabled = true
        webView.settings.builtInZoomControls = true
        webView.settings.setSupportZoom(true)
        webView.settings.displayZoomControls = false

        webView.loadUrl("https://namu.wiki/w/%EC%8B%9C%EC%A1%B0%EC%83%88(%EC%9D%B8%ED%84%B0%EB%84%B7%20%EB%B0%A9%EC%86%A1%EC%9D%B8)")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webView.loadUrl("javascript:(function(){" + "document.querySelector(\"meta[name=viewport]\").setAttribute('content', 'width=device-width, initial-scale=1.0, maximum-scale=3.0, minimum-scale=1.0, user-scalable=yes,target-densitydpi=medium-dpi');" + "})();")
            }
        }
    }
}
