package com.brentvatne.exoplayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class LoggingInterceptor implements Interceptor {
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
