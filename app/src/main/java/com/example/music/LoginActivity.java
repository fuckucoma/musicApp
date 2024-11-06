package com.example.music;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Загружаем библиотеку Hedgehog через CDN
                loadHedgehogScript();

                // Выполняем JavaScript для получения токена через Hedgehog после загрузки страницы
                if (url.contains("audius.co/feed")) {
                    extractTokenUsingHedgehog();
                }
            }
        });

        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        // Загружаем страницу аутентификации Audius
        webView.loadUrl("https://audius.co/signin");
    }

    // Метод для загрузки Hedgehog через CDN, если она не подключена
    private void loadHedgehogScript() {
        String hedgehogScript = "javascript:(function() {" +
                "  if (typeof window.Hedgehog === 'undefined') {" +
                "    var script = document.createElement('script');" +
                "    script.src = 'https://cdn.jsdelivr.net/npm/@audius/hedgehog';" +
                "    script.onload = function() { console.log('Hedgehog загружена'); };" +
                "    document.head.appendChild(script);" +
                "  } else {" +
                "    console.log('Hedgehog уже подключена');" +
                "  }" +
                "})();";

        webView.evaluateJavascript(hedgehogScript, null);
    }

    // Интерфейс для получения данных из JavaScript
    private class WebAppInterface {
        @JavascriptInterface
        public void onTokenReceived(String accessToken) {
            Log.d(TAG, "Access Token: " + accessToken);
            saveToken(accessToken);
        }
    }

    // Метод для извлечения токена через Hedgehog после входа
    private void extractTokenUsingHedgehog() {
        String jsCode = "javascript:(function() {" +
                "  const hedgehog = new window.Hedgehog();" +
                "  hedgehog.login({ username: 'velkosasa21@gmail.com', password: 'jutluv20062122_' }).then(wallet => {" +
                "    wallet.getToken().then(token => {" +
                "      Android.onTokenReceived(token);" +
                "    }).catch(error => console.error('Error getting token:', error));" +
                "  }).catch(error => console.error('Login error:', error));" +
                "})();";

        webView.evaluateJavascript(jsCode, null);
    }

    private void saveToken(String token) {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putString("access_token", token)
                .apply();
        Log.d(TAG, "Token saved: " + token);
    }
}
