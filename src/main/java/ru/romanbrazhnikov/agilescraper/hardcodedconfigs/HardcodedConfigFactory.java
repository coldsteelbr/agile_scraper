package ru.romanbrazhnikov.agilescraper.hardcodedconfigs;

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
    public static final String paramDistrict = "{[DISTRICT]}";
    public static final String fieldDistrict = "district";

    public static final String paramString = "areas%5B0%5D=" + paramDistrict + "&currency=1&costMode=1&cities%5B0%5D=21&page=" + PARAM_PAGE;

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
        argumentDistrict.name = paramDistrict;
        argumentDistrict.field = fieldDistrict;
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
}
