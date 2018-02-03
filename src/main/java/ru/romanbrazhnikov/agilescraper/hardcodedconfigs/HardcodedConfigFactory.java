package ru.romanbrazhnikov.agilescraper.hardcodedconfigs;

import ru.romanbrazhnikov.agilescraper.configuration.PrimitiveConfiguration;
import ru.romanbrazhnikov.agilescraper.requestarguments.Argument;
import ru.romanbrazhnikov.agilescraper.requestarguments.Values;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpMethods;
import ru.romanbrazhnikov.agilescraper.sourceprovider.cookies.Cookie;
import ru.romanbrazhnikov.agilescraper.sourceprovider.cookies.CookieRules;
import ru.romanbrazhnikov.agilescraper.sourceprovider.cookies.Cookies;

import java.util.ArrayList;
import java.util.HashMap;

public class HardcodedConfigFactory {
    public static final String PARAM_PAGE = "{[PAGE]}";
    public static final String PARAM_DISTRICT = "{[DISTRICT]}";
    public static final String FIELD_DISTRICT = "district";

    public static final String paramString = "areas%5B0%5D=" + PARAM_DISTRICT + "&currency=1&costMode=1&cities%5B0%5D=21&page=" + PARAM_PAGE;

    public PrimitiveConfiguration getSpranCommSell() {
        PrimitiveConfiguration configuration = new PrimitiveConfiguration();

        configuration.destinationName = "db_spran";
        configuration.baseUrl = "http://spran.ru/sell/comm.html";
        configuration.requestParams = paramString;
        configuration.firstLevelPattern =
                "<tr[^>]*>\\s*\n" +
                        "<td[^>]*>\\s*(?<TYPE>.*?)\\s*</td>\\s*\n" +
                        "<td[^>]*>\\s*(?:.*?)</td>\\s*\n" +
                        "<td[^>]*>\\s*<a(?:.*?)href\\s*=\\s*\"(?<SECONDLEVEL>.*?)\"[^>]*>\\s*(?<ADDRESS>.*?)\\s*</a>\\s*</td>\\s*\n" +
                        "<td[^>]*>\\s*<span[^>]*>\\s*<span[^>]*>\\s*(?<SQUARE>.*?)\\s*</span>\\s*</span>\\s*</td>\\s*\n" +
                        "<td[^>]*>\\s*<span[^>]*>\\s*(?<TOTALPRICE>.*?)\\s*</span>\\s*(?:.*?)</td>\\s*\n" +
                        "<td[^>]*>\\s*(?<CONTACT>.*?)\\s*</td>\\s*";
        configuration.secondLevelPattern =
                "комментарий\\s*продавца</h[1-6]+>\\s*\n" +
                        "<p[^>]*>\\s*(?<NOTES>.*?)\\s*</p>";

        configuration.firstLevelBindings = new HashMap<>();
        configuration.firstLevelBindings.put("TYPE", "type");
        configuration.firstLevelBindings.put("ADDRESS", "address");
        configuration.firstLevelBindings.put("SQUARE", "square");
        configuration.firstLevelBindings.put("TOTALPRICE", "price");
        configuration.firstLevelBindings.put("CONTACT", "contact_info");


        //
        //  Second Level
        //
        configuration.secondLevelName = PrimitiveConfiguration.SECOND_LEVEL_NAME;
        configuration.firstLevelBindings.put(configuration.secondLevelName, configuration.secondLevelName);
        configuration.secondLevelBindings = new HashMap<>();
        configuration.secondLevelBindings.put("NOTES", "notes");

        configuration.secondLevelBaseUrl = "http://spran.ru";

        // Request arguments
        configuration.requestArguments.mArgumentList = new ArrayList<>();
        Argument argumentDistrict = new Argument();
        argumentDistrict.name = PARAM_DISTRICT;
        argumentDistrict.field = FIELD_DISTRICT;
        argumentDistrict.mValues = new ArrayList<>();
        argumentDistrict.mValues.add(new Values("22", "Дзержинский"));
        argumentDistrict.mValues.add(new Values("23", "Железнодорожный"));
        argumentDistrict.mValues.add(new Values("24", "Заельцовский"));
        argumentDistrict.mValues.add(new Values("25", "Килининский"));
        argumentDistrict.mValues.add(new Values("26", "Кировский"));
        argumentDistrict.mValues.add(new Values("27", "Ленинский"));
        argumentDistrict.mValues.add(new Values("29", "Октябрьский"));
        argumentDistrict.mValues.add(new Values("30", "Первомайский"));
        argumentDistrict.mValues.add(new Values("31", "Советский"));
        argumentDistrict.mValues.add(new Values("32", "Центральный"));
        configuration.requestArguments.mArgumentList.add(argumentDistrict);
        configuration.requestArguments.initProvider(configuration.requestParams);

        // Markers
        configuration.markers.put("city", "Новосибирск");
        configuration.markers.put("market", "ком. продажа");

        return configuration;
    }

    public PrimitiveConfiguration getSpranFlatSell() {
        PrimitiveConfiguration configuration = new PrimitiveConfiguration();

        configuration.destinationName = "db_spran_flat_sale";
        configuration.baseUrl = "http://spran.ru/sell/flat.html";
        configuration.requestParams = "currency=1&costMode=1&cities[0]=21&areas[0]=" + PARAM_DISTRICT + "&page=" + PARAM_PAGE;
        configuration.firstLevelPattern =
                "<tr\\s*class[^>]*>\\s*\n" +
                        "<td[^>]*>\\s*(?<FLATS>.*?)\\s*</td>\\s*\n" +
                        "<td[^>]*>\\s*(?:.*?)</td>\\s*\n" +
                        "<td[^>]*>\\s*<a[^h]*\\s*href\\s*=\\s*\"(?<SECONDLEVEL>.*?)\"[^>]*>\\s*(?<ADDRESS>.*?)\\s*</a>\\s*</td>\\s*\n" +
                        "<td[^>]*>\\s*<span[^>]*>\\s*<span[^>]*>\\s*(?<SQUARE>.*?)\\s*</span>\\s*</span>\\s*</td>\\s*\n" +
                        "<td[^>]*>\\s*<span[^>]*>\\s*(?<FLOOR>.*?)\\s*</span>\\s*</td>\\s*\n" +
                        "<td[^>]*>\\s*<span[^>]*>\\s*(?<TOTALPRICE>.*?)\\s*</span>\\s*</td>\\s*\n" +
                        "<td[^>]*>\\s*(?<CONTACT>.*?)\\s*</td>\\s*";
        configuration.secondLevelPattern =
                "комментарий\\s*продавца</h[1-6]+>\\s*\n" +
                        "<p[^>]*>\\s*(?<NOTES>.*?)\\s*</p>";

        configuration.firstLevelBindings = new HashMap<>();
        configuration.firstLevelBindings.put("FLATS", "flats");
        configuration.firstLevelBindings.put("ADDRESS", "address");
        configuration.firstLevelBindings.put("CONTACT", "agency");
        configuration.firstLevelBindings.put("FLOOR", "floor");
        configuration.firstLevelBindings.put("SQUARE", "square");
        configuration.firstLevelBindings.put("TOTALPRICE", "price");


        //
        //  Second Level
        //
        configuration.secondLevelName = PrimitiveConfiguration.SECOND_LEVEL_NAME;
        configuration.firstLevelBindings.put(configuration.secondLevelName, configuration.secondLevelName);
        configuration.secondLevelBindings = new HashMap<>();
        configuration.secondLevelBindings.put("NOTES", "notes");

        configuration.secondLevelBaseUrl = "http://spran.ru";

        // Request arguments
        configuration.requestArguments.mArgumentList = new ArrayList<>();
        Argument argumentDistrict = new Argument();
        argumentDistrict.name = PARAM_DISTRICT;
        argumentDistrict.field = FIELD_DISTRICT;
        argumentDistrict.mValues = new ArrayList<>();
        argumentDistrict.mValues.add(new Values("22", "Дзержинский"));
        argumentDistrict.mValues.add(new Values("23", "Железнодорожный"));
        argumentDistrict.mValues.add(new Values("24", "Заельцовский"));
        argumentDistrict.mValues.add(new Values("25", "Килининский"));
        argumentDistrict.mValues.add(new Values("26", "Кировский"));
        argumentDistrict.mValues.add(new Values("27", "Ленинский"));
        argumentDistrict.mValues.add(new Values("29", "Октябрьский"));
        argumentDistrict.mValues.add(new Values("30", "Первомайский"));
        argumentDistrict.mValues.add(new Values("31", "Советский"));
        argumentDistrict.mValues.add(new Values("32", "Центральный"));
        configuration.requestArguments.mArgumentList.add(argumentDistrict);
        configuration.requestArguments.initProvider(configuration.requestParams);

        // Markers
        configuration.markers.put("city", "Новосибирск");
        configuration.markers.put("market", "кв. продажа");

        return configuration;
    }

    public PrimitiveConfiguration getProstoTomskCommSell() {
        PrimitiveConfiguration configuration = new PrimitiveConfiguration();
        configuration.method = HttpMethods.GET;
        configuration.baseUrl = "http://prosto.tomsk.ru";
        configuration.requestParams = "rm=prosto_offers_list&l_page=" + PARAM_PAGE;
        configuration.requestArguments.initProvider(configuration.requestParams);
        configuration.firstPageNum = 1; // default
        configuration.pageStep = 1; // default
        configuration.maxPagePattern = "page\\s*=\\s*[0-9]+\"><span[^>]*>(?<PAGENUM>.*?)\\s*</span>\\s*</a>";

        configuration.cookies = new Cookies();
        configuration.cookies.mCookieList = new ArrayList<>();
        configuration.cookies.mCookieList.add(
                new Cookie("PHPSESSID", "h85fc5nah05lm0qa44ag4qrnl1", "prosto.tomsk.ru"));

        configuration.destinationName = "prosto_tomsk";
        configuration.firstLevelPattern = "<tr\\s*id\\s*=\\s*\"offer[^>]*>\\s*\n" +
                "<td[^>]*>\\s*(?:.*?)</td>\\s*\n" +
                "<td[^>]*>\\s*<div[^>]*>\\s*(?<TYPE>.*?)\\s*</div>\\s*</td>\\s*\n" +
                "<td[^>]*>\\s*<div[^>]*>\\s*(?<ADDRESS>.*?)\\s*</div>\\s*\n" +
                "(?:\n" +
                "   (?:.*?)\n" +
                "   (?:<div[^>]*>\\s*(?<DISTRICT>.*?)\\s*</div>)\n" +
                ")?\n" +
                "\\s*</td>\\s*\n" +
                "<td[^>]*>\\s*(?<TOTALSQUARE>.*?)\\s*</td>\\s*\n" +
                "<td[^>]*>\\s*<div[^>]*>\\s*(?<TOTALPRICE>.*?)\\s*</div>\\s*\n" +
                "<div[^>]*>\\s*(?:.*?)</div>\\s*</td>\\s*";
        configuration.firstLevelBindings = new HashMap<>();
        configuration.firstLevelBindings.put("TYPE", "type");
        configuration.firstLevelBindings.put("ADDRESS", "address");
        configuration.firstLevelBindings.put("DISTRICT", "district");
        configuration.firstLevelBindings.put("TOTALSQUARE", "total_square");
        configuration.firstLevelBindings.put("TOTALPRICE", "total_price");


        return configuration;
    }

    public PrimitiveConfiguration getProstoTomskCommRent() {
        PrimitiveConfiguration configuration = new PrimitiveConfiguration();
        configuration.method = HttpMethods.GET;
        configuration.baseUrl = "http://prosto.tomsk.ru";
        configuration.requestParams = "rm=prosto_offers_list&l_page=" + PARAM_PAGE;
        configuration.requestArguments.initProvider(configuration.requestParams);
        configuration.firstPageNum = 1; // default
        configuration.pageStep = 1; // default
        configuration.maxPagePattern = "page\\s*=\\s*[0-9]+\"><span[^>]*>(?<PAGENUM>.*?)\\s*</span>\\s*</a>";

        configuration.cookies = new Cookies();
        configuration.cookies.mCookieRules = new CookieRules();
        configuration.cookies.mCookieRules.mRequestCookiesAddress = "http://prosto.tomsk.ru/index.php?rm=prosto_offers_list";
        configuration.cookies.mCookieRules.mRequestCookiesParamString =
                "_prosto_ctx_type=commercial&" +
                        "_prosto_mode_type=prosto_active&" +
                        "search_params[cost_th_from]=" +
                        "&search_params[cost_th_to]=" +
                        "&search_params[prosto_realty_type_id][garage]=&" +
                        "search_params[prosto_realty_type_id][office]=" +
                        "&search_params[prosto_realty_type_id][commercial]=" +
                        "&search_params[prosto_realty_type_id][warehouse]=" +
                        "&search_params[prosto_realty_type_id][production]=" +
                        "&_search=Найти" +
                        "&search_params[prosto_legal][sale]=0" +
                        "&search_params[prosto_legal][rent]=1" +
                        "&search_params[prosto_legal][exchange]=" +
                        "&search_params[prosto_region_id][1]=" +
                        "&search_params[prosto_region_id][2]=" +
                        "&search_params[prosto_region_id][3]=" +
                        "&search_params[prosto_region_id][4]=" +
                        "&search_params[prosto_region_id][5]=" +
                        "&search_params[prosto_image][no_matter]=1" +
                        "&search_params[prosto_image][has_image]=" +
                        "&search_params[additional_payment]=" +
                        "&search_params[area_from]=" +
                        "&search_params[area_to]=" +
                        "&search_params[commerce_object_id]=" +
                        "&search_params[realty_state_id]=" +
                        "&search_params[prosto_street_id]=0" +
                        "&search_params[foreign_region_orientir_id]=0";
        configuration.cookies.mCookieRules.mRequestCookiesMethod = "POST";

        configuration.destinationName = "prosto_tomsk_rent";
        configuration.firstLevelPattern = "<tr\\s*id\\s*=\\s*\"offer[^>]*>\\s*\n" +
                "<td[^>]*>\\s*(?:.*?)</td>\\s*\n" +
                "<td[^>]*>\\s*<div[^>]*>\\s*(?<TYPE>.*?)\\s*</div>\\s*</td>\\s*\n" +
                "<td[^>]*>\\s*<div[^>]*>\\s*(?<ADDRESS>.*?)\\s*</div>\\s*\n" +
                "(?:\n" +
                "   (?:.*?)\n" +
                "   (?:<div[^>]*>\\s*(?<DISTRICT>.*?)\\s*</div>)\n" +
                ")?\n" +
                "\\s*</td>\\s*\n" +
                "<td[^>]*>\\s*(?<TOTALSQUARE>.*?)\\s*</td>\\s*\n" +
                "<td[^>]*>\\s*<div[^>]*>\\s*(?<TOTALPRICE>.*?)\\s*</div>\\s*\n" +
                "<div[^>]*>\\s*(?:.*?)</div>\\s*</td>\\s*";
        configuration.firstLevelBindings = new HashMap<>();
        configuration.firstLevelBindings.put("TYPE", "type");
        configuration.firstLevelBindings.put("ADDRESS", "address");
        configuration.firstLevelBindings.put("DISTRICT", "district");
        configuration.firstLevelBindings.put("TOTALSQUARE", "total_square");
        configuration.firstLevelBindings.put("TOTALPRICE", "total_price");


        return configuration;
    }

    public PrimitiveConfiguration getUpWorkDataScrapingJobs() {
        PrimitiveConfiguration configuration = new PrimitiveConfiguration();

        configuration.baseUrl = "https://www.upwork.com/o/jobs/browse/url";
        configuration.requestParams = "page=" + PARAM_PAGE + "&q=Data+Scraping&sort=renew_time_int%2Bdesc";
        configuration.requestArguments.initProvider(configuration.requestParams);

        // headers
        configuration.headers = new HashMap<>();
        configuration.headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
        configuration.headers.put("Host", "www.upwork.com");
        configuration.headers.put("Connection", "keep-alive");
        configuration.headers.put("Accept", "application/json, text/plain, */*");
        configuration.headers.put("X-NewRelic-ID", "VQIBUF5RGwYDVFRVAQA=");
        configuration.headers.put("X-Odesk-User-Agent", "oDesk LM");
        configuration.headers.put("X-Requested-With", "XMLHttpRequest");
        configuration.headers.put("X-Odesk-Csrf-Token", "a8d6ec3fa61059222343fc2e43991a53");
        configuration.headers.put("Referer", "https://www.upwork.com/o/jobs/browse/?from_recent_search=true&q=Data%20Scraping&sort=renew_time_int%2Bdesc");
        configuration.headers.put("Accept-Encoding", "utf-8");
        configuration.headers.put("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4,es;q=0.2,fr;q=0.2");
        configuration.headers.put("Cookies", "__cfduid=d8c18199319398fd7f6cd0e1a467749751512477687; device_view=full; recognized=1; console_user=romanfromrussia; master_access_token=eb8cce68.oauth2v1_1de1a62cbbf2c60af422a2f12cf200bb; oauth2_global_js_token=oauth2v1_df7f79fdf7bcddf04f7cf7b91ac01003; _ga=GA1.2.553355131.1512477691; _gid=GA1.2.123935093.1512477691; visitor_id=5.130.30.2.1512477688599860; current_organization_uid=889396507270238210; qt_visitor_id=5.44.168.89.1500834103535912; session_id=a3c18c487e73470ae521b08c19b811f8; company_last_accessed=d16763747; XSRF-TOKEN=a8d6ec3fa61059222343fc2e43991a53; sc.ASP.NET_SESSIONID=rtgfai31e00a3uhi0grrneu2; _px3=d3adefec6f26be42e371ef5d4b35a4e1fadeb652debfb2f233c7db61c7184a3b:d2a7cXm0wUeLKu2Ceatf1zFPGxzo0j6T74Cjos24w8FvwrbY8EMo32nCYBXwIPHW5QLEwbhdLnBluTFhTaaRng==:1000:rNBC7/PU+kfiGK/EUjJK/UgH+OMvYamTDOY4vfx0kbRlThaKUITVGoahGvS/JAR2t30NgmCGlFsyCA1ggr72hpfsuwu13NXi1VwUWgNxYtOKti6J5azybr7441Ib8B6gG+vBLYL7SdBqS2NLBaS+YSH+zh9Y7wYStlA3nPtKqA4=");

        /*
        configuration.cookies = new Cookies();
        configuration.cookies.mCookieList = new ArrayList<>();
        configuration.cookies.mCookieList.add(new Cookie("__cfduid","d8c18199319398fd7f6cd0e1a467749751512477687", ""));
        configuration.cookies.mCookieList.add(new Cookie("device_view","full", ""));
        configuration.cookies.mCookieList.add(new Cookie("recognized","1", ""));
        configuration.cookies.mCookieList.add(new Cookie("console_user","romanfromrussia", ""));
        configuration.cookies.mCookieList.add(new Cookie("master_access_token","eb8cce68.oauth2v1_1de1a62cbbf2c60af422a2f12cf200bb", ""));
        configuration.cookies.mCookieList.add(new Cookie("oauth2_global_js_token","oauth2v1_df7f79fdf7bcddf04f7cf7b91ac01003", ""));
        configuration.cookies.mCookieList.add(new Cookie("_ga","GA1.2.553355131.1512477691", ""));
        configuration.cookies.mCookieList.add(new Cookie("_gid","GA1.2.123935093.1512477691", ""));
        configuration.cookies.mCookieList.add(new Cookie("visitor_id","5.130.30.2.1512477688599860", ""));
        configuration.cookies.mCookieList.add(new Cookie("current_organization_uid","889396507270238210", ""));
        configuration.cookies.mCookieList.add(new Cookie("qt_visitor_id","5.44.168.89.1500834103535912", ""));
        configuration.cookies.mCookieList.add(new Cookie("session_id","a3c18c487e73470ae521b08c19b811f8", ""));
        configuration.cookies.mCookieList.add(new Cookie("company_last_accessed","d16763747", ""));
        configuration.cookies.mCookieList.add(new Cookie("XSRF-TOKEN","a8d6ec3fa61059222343fc2e43991a53", ""));
        configuration.cookies.mCookieList.add(new Cookie("sc.ASP.NET_SESSIONID","rtgfai31e00a3uhi0grrneu2", ""));
        configuration.cookies.mCookieList.add(new Cookie("_px3", "d3adefec6f26be42e371ef5d4b35a4e1fadeb652debfb2f233c7db61c7184a3b:d2a7cXm0wUeLKu2Ceatf1zFPGxzo0j6T74Cjos24w8FvwrbY8EMo32nCYBXwIPHW5QLEwbhdLnBluTFhTaaRng==:1000:rNBC7/PU+kfiGK/EUjJK/UgH+OMvYamTDOY4vfx0kbRlThaKUITVGoahGvS/JAR2t30NgmCGlFsyCA1ggr72hpfsuwu13NXi1VwUWgNxYtOKti6J5azybr7441Ib8B6gG+vBLYL7SdBqS2NLBaS+YSH+zh9Y7wYStlA3nPtKqA4=", ""));
*/
        configuration.destinationName = "upwork_data_scraping";
        configuration.firstLevelPattern = "\\{\"title\":\"(?<TITLE>.*?)\"";
        configuration.firstLevelBindings = new HashMap<>();
        configuration.firstLevelBindings.put("TITLE", "title");

        return configuration;
    }
}
