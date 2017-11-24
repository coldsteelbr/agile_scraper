package ru.romanbrazhnikov.agilescraper.sourceprovider;

import io.reactivex.Single;
import ru.romanbrazhnikov.agilescraper.cookies.Cookie;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class HttpSourceProvider {
    // TODO: Add encoding conversion
    // TODO: from server: ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(myString)
    private String mBaseUrl = "";

    private List<String> mCookiesToRequest;
    private List<String> mCookiesFromResponse;

    private String mClientCharset;
    private String mServerEncoding = "utf-8";
    private HttpMethods mHttpMethod = HttpMethods.GET;
    private String mQueryParamString;

    public void setBaseUrl(String baseUrl) {
        mBaseUrl = baseUrl;
    }

    public void setClientCharset(String clientCharset) {
        mClientCharset = clientCharset;
    }

    public void setHttpMethod(HttpMethods httpMethod) {
        mHttpMethod = httpMethod;
    }

    public void setQueryParamString(String queryParamString) {
        mQueryParamString = queryParamString;
    }

    public Single<String> requestSource() {
        return Single.create(emitter -> {
            try {
                // opening connection
                URL myURL;// = new URL(mBaseUrl);
                HttpURLConnection httpConnection = null;// = (HttpURLConnection) myURL.openConnection();


                // METHOD
                switch (mHttpMethod) {

                    case GET:
                        myURL = new URL(mBaseUrl + "?" + mQueryParamString);
                        httpConnection = (HttpURLConnection) myURL.openConnection();
                        addCookiesIfAny(httpConnection);
                        break;
                    case POST:
                        myURL = new URL(mBaseUrl);
                        httpConnection = (HttpURLConnection) myURL.openConnection();
                        httpConnection.setDoOutput(true); // Triggers POST.
                        httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + mClientCharset);
                        addCookiesIfAny(httpConnection);

                        // Sending POST form
                        try (OutputStream output = httpConnection.getOutputStream()) {
                            output.write(mQueryParamString.getBytes(mClientCharset));
                        }

                        break;
                }
                // setting encoding
                // TODO: TBD setting encoding from server

                // opening input stream from the connection
                InputStream httpResponse = httpConnection.getInputStream();

                // getting headers
                Set httpHeaders = httpConnection.getHeaderFields().keySet();

                // getting cookie headers
                if (httpHeaders.contains("Set-Cookie")) {
                    mCookiesFromResponse = httpConnection.getHeaderFields().get("Set-Cookie");
                }

                // result html response
                StringBuilder responseHtmlBuilder = new StringBuilder();

                // trying to read from the input stream
                try (Scanner responseScanner = new Scanner(httpResponse)) {
                    // while there's something to read...
                    while (responseScanner.hasNextLine()) {
                        // reading current line
                        responseHtmlBuilder.append(responseScanner.nextLine()).append("\n");
                    }
                    // closing the scanner after reading
                    responseScanner.close();

                    // returning result TODO: encoding conversion
                    //ByteBuffer byteBuffer = (ByteBuffer)Charset.forName(mServerEncoding).encode(responseHtmlBuilder.toString()).limit(Integer.MAX_VALUE);
                    emitter.onSuccess(responseHtmlBuilder.toString());
                } catch (Exception e) {
                    Exception exception = new Exception("HttpSourceProvider (Reading input stream): " + e.getMessage());
                    exception.setStackTrace(e.getStackTrace());
                    emitter.onError(exception);
                } finally {
                    // closing input stream
                    httpResponse.close();
                }

            } catch (Exception ex) {

                Exception exception = new Exception("HttpSourceProvider (requestSource): " + ex.getMessage());
                exception.setStackTrace(ex.getStackTrace());
                emitter.onError(exception);
            }
        });
    }

    private void addCookiesIfAny(HttpURLConnection httpConnection){
        // Adding cookies if any
        if (mCookiesToRequest != null) {
            for (String currentCookie : mCookiesToRequest) {
                httpConnection.setRequestProperty("Cookie", currentCookie);
            }
        }
    }

    public void setCookiesHeadersToRequest(List<String> cookiesToRequest) {
        mCookiesToRequest = cookiesToRequest;
    }

    public void setCustomCookies(List<Cookie> cookieList) {
        // lazy initialization
        if (mCookiesToRequest == null) {
            mCookiesToRequest = new ArrayList<>();
        }

        // setting cookies
        for (Cookie currentCookie : cookieList) {
            mCookiesToRequest.add(currentCookie.getHeader());
        }

    }

    public List<String> getCookieHeadersFromResponse() {
        return mCookiesFromResponse;
    }
}
