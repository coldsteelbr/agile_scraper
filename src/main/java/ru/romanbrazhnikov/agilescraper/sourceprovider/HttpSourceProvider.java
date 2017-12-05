package ru.romanbrazhnikov.agilescraper.sourceprovider;

import io.reactivex.Single;
import ru.romanbrazhnikov.agilescraper.sourceprovider.cookies.Cookie;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
                URL myURL = null;// = new URL(mBaseUrl);
                HttpsURLConnection httpConnection = null;// = (HttpURLConnection) myURL.openConnection();


                // METHOD
                switch (mHttpMethod) {

                    case GET:
                        myURL = new URL(mBaseUrl + (mQueryParamString != null ? "?" + mQueryParamString : ""));
                        httpConnection = (HttpsURLConnection) myURL.openConnection();
                        //httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
                        //httpConnection.setRequestProperty("Accept","*/*");

                        httpConnection.setRequestProperty("Host", "www.upwork.com");
                        httpConnection.setRequestProperty("Connection", "keep-alive");
                        httpConnection.setRequestProperty("Accept", "application/json, text/plain, */*");
                        httpConnection.setRequestProperty("X-NewRelic-ID", "VQIBUF5RGwYDVFRVAQA=");
                        httpConnection.setRequestProperty("X-Odesk-User-Agent", "oDesk LM");
                        httpConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                        httpConnection.setRequestProperty("X-Odesk-Csrf-Token", "a8d6ec3fa61059222343fc2e43991a53");
                        httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
                        httpConnection.setRequestProperty("Referer", "https://www.upwork.com/o/jobs/browse/?from_recent_search=true&q=Data%20Scraping&sort=renew_time_int%2Bdesc");
                        httpConnection.setRequestProperty("Accept-Encoding", "utf-8");
                        httpConnection.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4,es;q=0.2,fr;q=0.2");
                        httpConnection.setRequestProperty("Cookies", "__cfduid=d8c18199319398fd7f6cd0e1a467749751512477687; device_view=full; recognized=1; console_user=romanfromrussia; master_access_token=eb8cce68.oauth2v1_1de1a62cbbf2c60af422a2f12cf200bb; oauth2_global_js_token=oauth2v1_df7f79fdf7bcddf04f7cf7b91ac01003; _ga=GA1.2.553355131.1512477691; _gid=GA1.2.123935093.1512477691; visitor_id=5.130.30.2.1512477688599860; current_organization_uid=889396507270238210; qt_visitor_id=5.44.168.89.1500834103535912; session_id=a3c18c487e73470ae521b08c19b811f8; company_last_accessed=d16763747; XSRF-TOKEN=a8d6ec3fa61059222343fc2e43991a53; sc.ASP.NET_SESSIONID=rtgfai31e00a3uhi0grrneu2; _px3=d3adefec6f26be42e371ef5d4b35a4e1fadeb652debfb2f233c7db61c7184a3b:d2a7cXm0wUeLKu2Ceatf1zFPGxzo0j6T74Cjos24w8FvwrbY8EMo32nCYBXwIPHW5QLEwbhdLnBluTFhTaaRng==:1000:rNBC7/PU+kfiGK/EUjJK/UgH+OMvYamTDOY4vfx0kbRlThaKUITVGoahGvS/JAR2t30NgmCGlFsyCA1ggr72hpfsuwu13NXi1VwUWgNxYtOKti6J5azybr7441Ib8B6gG+vBLYL7SdBqS2NLBaS+YSH+zh9Y7wYStlA3nPtKqA4=");

                        addCookiesIfAny(httpConnection);
                        break;
                    case POST:
                        myURL = new URL(mBaseUrl);
                        byte[] postData = mQueryParamString.getBytes(StandardCharsets.UTF_8);

                        httpConnection = (HttpsURLConnection) myURL.openConnection();
                        httpConnection.setRequestProperty("User-Agent", "");
                        httpConnection.setRequestProperty("Accept","application/json, text/plain, */*");
                        httpConnection.setInstanceFollowRedirects(false);
                        httpConnection.setDoOutput(true); // Triggers POST.
                        httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + StandardCharsets.UTF_8.displayName());
                        httpConnection.setUseCaches(false);
                        addCookiesIfAny(httpConnection);

                        // Sending POST form
                        try (OutputStream output = httpConnection.getOutputStream()) {
                            output.write(postData);
                        }

                        break;
                }

                System.out.println(myURL.toString());
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

    private void addCookiesIfAny(HttpURLConnection httpConnection) {
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
