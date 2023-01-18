package com.brentvatne.exoplayer;

import android.util.Log;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.network.CookieJarContainer;
import com.facebook.react.modules.network.ForwardingCookieHandler;
import com.facebook.react.modules.network.OkHttpClientProvider;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
        return new DefaultDataSourceFactory(context, bandwidthMeter,
                buildHttpDataSourceFactory(context, bandwidthMeter, requestHeaders));
    }

    private static HttpDataSource.Factory buildHttpDataSourceFactory(ReactContext context, DefaultBandwidthMeter bandwidthMeter, Map<String, String> requestHeaders) {
        OkHttpClient client = OkHttpClientProvider.getOkHttpClient()
                .newBuilder()
                .addInterceptor(new LoggingInterceptor())
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();
        CookieJarContainer container = (CookieJarContainer) client.cookieJar();
        ForwardingCookieHandler handler = new ForwardingCookieHandler(context);
        container.setCookieJar(new JavaNetCookieJar(handler));
        OkHttpDataSourceFactory okHttpDataSourceFactory = new OkHttpDataSourceFactory((Call.Factory) client, getUserAgent(context), bandwidthMeter);

        if (requestHeaders != null)
            okHttpDataSourceFactory.getDefaultRequestProperties().set(requestHeaders);

        return okHttpDataSourceFactory;
    }
}

class LoggingInterceptor implements Interceptor {
    public static ArrayList<Integer> segmentsToSkip = new ArrayList<Integer>();

    public int segmentNumberIndex = 0;
    public boolean isHLS = false;
    public static int maximumRequests = 0;
    public static int segmentLength = 6;


    ArrayList<Integer> errorsList = new ArrayList<Integer>() {
        {
            add(404);
            add(403);
            add(503);
            add(504);
        }
    };

    @Override public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        Response firstRespone = response;
        boolean isDrm = false;
        String url =request.url().toString();
        int segmentNumber = 0;

        if(url.contains(".ts")) {
            isHLS = true;

            for (int i = url.length() - 1; i >= 0; i--) {
                if (url.charAt(i) == '_') {
                    String segmentString = url.substring(i + 1, url.indexOf(".ts"));
                    segmentNumber = Integer.parseInt(segmentString);
                    break;
                }
            }
        } else if(url.contains("video") && url.contains("mp4")) {
            if(!isDrm) {
                isDrm = true;
            }

            if(url.contains("init")) {
                segmentNumberIndex = url.indexOf("init");
            } else {
                String segmentURL = url.substring( segmentNumberIndex, url.indexOf("mp4"));
                segmentNumber = new Scanner(String.valueOf(segmentURL)).useDelimiter("\\D+").nextInt() ;
            }
        }

        if(errorsList.contains(response.code())) {

            String newUrl = request.url().toString();
            if(!segmentsToSkip.contains((segmentNumber * segmentLength) - 1) && segmentsToSkip.size() <= 5) {
                if(isDrm) {
                    segmentsToSkip.add(((segmentNumber - 1) * segmentLength) - 1);
                } else {
                    segmentsToSkip.add((segmentNumber * segmentLength) - 1);
                }
            }

            while ( errorsList.contains(response.code()) && maximumRequests < 5 ) {
                maximumRequests++;

                if(maximumRequests == 5) {
                    return firstRespone;
                }

                newUrl = newUrl.replace("_" + segmentNumber + ".", "_" + ++segmentNumber + ".");
                response.close();
                HttpUrl newURL = HttpUrl.parse(newUrl);
                Request newRequest = request.newBuilder().url(newURL).build();
                response = chain.proceed(newRequest);

                if(errorsList.contains(response.code())) {
                    if(!segmentsToSkip.contains((segmentNumber * segmentLength) - 1) && segmentsToSkip.size() <= 5) {
                        if(isDrm) {
                            segmentsToSkip.add(((segmentNumber - 1) * segmentLength) - 1);
                        } else {
                            segmentsToSkip.add((segmentNumber * segmentLength) - 1);
                        }
                    }
                }

            }
            return null;

        } else {
            return response;
        }

    }
}