package com.brentvatne.exoplayer;

import android.util.Log;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.network.CookieJarContainer;
import com.facebook.react.modules.network.ForwardingCookieHandler;
import com.facebook.react.modules.network.OkHttpClientProvider;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import okhttp3.Call;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DataSourceUtil {

    private DataSourceUtil() {
    }

    private static DataSource.Factory rawDataSourceFactory = null;
    private static DataSource.Factory defaultDataSourceFactory = null;
    private static HttpDataSource.Factory defaultHttpDataSourceFactory = null;
    private static String userAgent = null;

    public static void setUserAgent(String userAgent) {
        DataSourceUtil.userAgent = userAgent;
    }

    public static String getUserAgent(ReactContext context) {
        if (userAgent == null) {
            userAgent = Util.getUserAgent(context, "ReactNativeVideo");
        }
        return userAgent;
    }

    public static DataSource.Factory getRawDataSourceFactory(ReactContext context) {
        if (rawDataSourceFactory == null) {
            rawDataSourceFactory = buildRawDataSourceFactory(context);
        }
        return rawDataSourceFactory;
    }

    public static void setRawDataSourceFactory(DataSource.Factory factory) {
        DataSourceUtil.rawDataSourceFactory = factory;
    }


    public static DataSource.Factory getDefaultDataSourceFactory(ReactContext context, DefaultBandwidthMeter bandwidthMeter, Map<String, String> requestHeaders) {
        if (defaultDataSourceFactory == null || (requestHeaders != null && !requestHeaders.isEmpty())) {
            defaultDataSourceFactory = buildDataSourceFactory(context, bandwidthMeter, requestHeaders);
        }
        return defaultDataSourceFactory;
    }

    public static void setDefaultDataSourceFactory(DataSource.Factory factory) {
        DataSourceUtil.defaultDataSourceFactory = factory;
    }

    public static HttpDataSource.Factory getDefaultHttpDataSourceFactory(ReactContext context, DefaultBandwidthMeter bandwidthMeter, Map<String, String> requestHeaders) {
        if (defaultHttpDataSourceFactory == null || (requestHeaders != null && !requestHeaders.isEmpty())) {
            defaultHttpDataSourceFactory = buildHttpDataSourceFactory(context, bandwidthMeter, requestHeaders);
        }
        return defaultHttpDataSourceFactory;
    }

    public static void setDefaultHttpDataSourceFactory(HttpDataSource.Factory factory) {
        DataSourceUtil.defaultHttpDataSourceFactory = factory;
    }

    private static DataSource.Factory buildRawDataSourceFactory(ReactContext context) {
        return new RawResourceDataSourceFactory(context.getApplicationContext());
    }

    private static DataSource.Factory buildDataSourceFactory(ReactContext context, DefaultBandwidthMeter bandwidthMeter, Map<String, String> requestHeaders) {
        return new DefaultDataSource.Factory(context,
                buildHttpDataSourceFactory(context, bandwidthMeter, requestHeaders));
    }

    private static HttpDataSource.Factory buildHttpDataSourceFactory(ReactContext context, DefaultBandwidthMeter bandwidthMeter, Map<String, String> requestHeaders) {
        OkHttpClient client = OkHttpClientProvider.getOkHttpClient()
                .newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        CookieJarContainer container = (CookieJarContainer) client.cookieJar();
        ForwardingCookieHandler handler = new ForwardingCookieHandler(context);
        container.setCookieJar(new JavaNetCookieJar(handler));
        OkHttpDataSource.Factory okHttpDataSourceFactory = new OkHttpDataSource.Factory((Call.Factory) client)
                .setUserAgent(getUserAgent(context))
                .setTransferListener(bandwidthMeter);

        if (requestHeaders != null)
            okHttpDataSourceFactory.setDefaultRequestProperties(requestHeaders);

        return okHttpDataSourceFactory;
    }
}