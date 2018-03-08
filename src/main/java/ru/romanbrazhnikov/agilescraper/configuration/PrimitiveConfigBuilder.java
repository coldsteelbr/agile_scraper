package ru.romanbrazhnikov.agilescraper.configuration;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.romanbrazhnikov.agilescraper.requestarguments.Argument;
import ru.romanbrazhnikov.agilescraper.requestarguments.RequestArguments;
import ru.romanbrazhnikov.agilescraper.requestarguments.Values;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpMethods;
import ru.romanbrazhnikov.agilescraper.sourceprovider.cookies.Cookie;
import ru.romanbrazhnikov.agilescraper.sourceprovider.cookies.CookieRules;
import ru.romanbrazhnikov.agilescraper.sourceprovider.cookies.Cookies;
import ru.romanbrazhnikov.agilescraper.utils.FileUtils;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Sample usage:
 * <p>
 * PrimitiveConfigBuilder builder = new PrimitiveConfigBuilder();
 * builder.readFromXmlFile("parser_conf_custom_cookies.prs");
 * Configuration configuration = builder.init();
 * System.out.println(configuration.getDebugInfo());
 */
public class PrimitiveConfigBuilder {

    //
    // xPath constants
    //
    private static final String XPATH_NAME = "/Config/@name";
    private static final String XPATH_BASE_URL = "/Config/BaseUrl/@value";
    private static final String XPATH_BASE_URL_DELIMITER = "/Config/BaseUrl/@delimiter";
    private static final String XPATH_REQUEST_PARAMS = "/Config/RequestParams/@value";
    private static final String XPATH_REQUEST_PARAMS_BODY = "/Config/RequestParams";
    private static final String XPATH_REQUEST_ARGUMENTS = "/Config/RequestArguments/Argument";
    private static final String XPATH_HTTP_HEADERS = "/Config/Headers/Header";
    private static final String XPATH_METHOD = "/Config/Method/@value";
    private static final String XPATH_USE_PROXY = "/Config/UseProxy/@value";
    private static final String XPATH_PROXY_LIST_PATH = "/Config/UseProxy/@path";
    private static final String XPATH_ENCODING = "/Config/Encoding/@value";
    private static final String XPATH_FIRST_PAGE = "/Config/FirstPage/@value";
    private static final String XPATH_STEP = "/Config/mStep/@value";
    private static final String XPATH_DELAY_MS = "/Config/Delay/@ms";
    private static final String XPATH_MAX_PAGE_PATTERN = "/Config/MaxPagePattern";
    private static final String XPATH_MARKERS = "/Config/Markers/Marker";
    private static final String XPATH_DESTINATION = "/Config/Destination/@value";
    private static final String XPATH_COOKIES = "/Config/Cookies";
    private static final String XPATH_CUSTOM_COOKIE_LIST = "/Config/Cookies/Cookie";
    private static final String XPATH_FIRST_LEVEL_PATTERN = "/Config/FirstLevelPattern";
    private static final String XPATH_SECOND_LEVEL_PATTERN = "/Config/SecondLevelPattern";
    private static final String XPATH_FIRST_LEVEL_BINDINGS = "/Config/FirstLevelBindings/Binding";
    private static final String XPATH_SECOND_LEVEL_BINDINGS = "/Config/SecondLevelBindings/Binding";
    private static final String XPATH_SECOND_LEVEL_BASE_URL = "/Config/SecondLevelBaseUrl/@value";
    private static final String XPATH_FIRST_LEVEL_PARSER_TYPE = "/Config/FirstLevelPattern/@parser";
    private static final String XPATH_SECOND_LEVEL_PARSER_TYPE = "/Config/SecondLevelPattern/@parser";


    //
    // System fields
    //
    private Document mDoc;
    private XPath mXPath;
    private String mErrorMessage;

    // CONFIGURATION
    private PrimitiveConfiguration mConfiguration;

    public PrimitiveConfigBuilder() {
        mXPath = XPathFactory.newInstance().newXPath();
    }

    public boolean readFromXmlFile(String xmlPath) {
        String fileData;

        try {
            fileData = FileUtils.readFromFileToString(xmlPath);
        } catch (IOException e) {
            mErrorMessage = e.getMessage();
            return false;
        }

        return readFromXmlString(fileData);
    }

    public boolean readFromXmlString(String xmlString) {
        DocumentBuilderFactory myDocFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder myDocBuilder;
        try {
            myDocBuilder = myDocFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            mErrorMessage = e.getMessage();
            return false;
        }


        try {
            mDoc = myDocBuilder.parse(new InputSource(new StringReader(xmlString)));
        } catch (SAXException e) {
            mErrorMessage = "SAXException: " + e.getMessage();
            return false;
        } catch (IOException e) {
            mErrorMessage = "IOException: " + e.getMessage();
            return false;
        }


        // read successfully
        return true;
    }

    public PrimitiveConfiguration init() {
        int firstLevel = 1;
        int secondLevel = 2;

        initPrimitives();
        initHeaders();
        initRequestArguments();
        initMarkers();
        initCookies();
        initDataFieldBinding(firstLevel);
        // adding second level name to bindings
        if (mConfiguration.secondLevelName != null && !mConfiguration.secondLevelName.isEmpty()) {
            mConfiguration.firstLevelBindings.put(mConfiguration.secondLevelName, PrimitiveConfiguration.FIELD_SUB_URL);
        }
        initDataFieldBinding(secondLevel);

        return mConfiguration;
    }

    private void initHeaders() {
        // TODO: Init headers
        Map<String, String> headers = new HashMap<>();
        NodeList headerNodeList = (NodeList) getByXPath(XPATH_HTTP_HEADERS, XPathConstants.NODESET);

        if (headerNodeList == null) {
            return;
        }


        for (int i = 0; i < headerNodeList.getLength(); i++) {
            Node currentHeader = headerNodeList.item(i);
            String currentName = getByXPath("@name", currentHeader);
            String currentValue = getByXPath("@value", currentHeader);

            headers.put(currentName, currentValue);
        }

        mConfiguration.headers = headers;
    }

    private void initPrimitives() {
        String configName = getParserAttributeAsString(XPATH_NAME);
        String baseUrl = getParserAttributeAsString(XPATH_BASE_URL);
        String baseUrlDelimiter = getParserAttributeAsString(XPATH_BASE_URL_DELIMITER);
        String requestParams = getParserAttributeAsString(XPATH_REQUEST_PARAMS);
        if ("".equals(requestParams) || requestParams == null) {
            requestParams = getByXPath(XPATH_REQUEST_PARAMS_BODY);
        }
        String method = getParserAttributeAsString(XPATH_METHOD);
        String useProxy = getParserAttributeAsString(XPATH_USE_PROXY);
        String proxyPath = getParserAttributeAsString(XPATH_PROXY_LIST_PATH);
        String encoding = getParserAttributeAsString(XPATH_ENCODING);
        String firstPageAsString = getParserAttributeAsString(XPATH_FIRST_PAGE);
        String stepAsString = getParserAttributeAsString(XPATH_STEP);
        String delay = getByXPath(XPATH_DELAY_MS);
        String maxPagePattern = getByXPath(XPATH_MAX_PAGE_PATTERN).trim();
        String destination = getParserAttributeAsString(XPATH_DESTINATION);
        String firstLevelPattern = getByXPath(XPATH_FIRST_LEVEL_PATTERN).trim();
        String secondLevelPattern = getByXPath(XPATH_SECOND_LEVEL_PATTERN).trim();
        String secondLevelName = !secondLevelPattern.equals("") ? PrimitiveConfiguration.SECOND_LEVEL_NAME : "";
        String secondLevelBaseUrl = getParserAttributeAsString(XPATH_SECOND_LEVEL_BASE_URL);

        String firstLevelParserType = getByXPath(XPATH_FIRST_LEVEL_PARSER_TYPE);
        String secondLevelParserType = getByXPath(XPATH_SECOND_LEVEL_PARSER_TYPE);

        HttpMethods httpMethod = HttpMethods.GET;
        switch (method.toLowerCase()) {
            case "get":
                httpMethod = HttpMethods.GET;
                break;
            case "post":
                httpMethod = HttpMethods.POST;
                break;
        }


        int firstPage = Integer.parseInt(firstPageAsString);
        int step = 1;

        if (stepAsString != null && !stepAsString.equals("")) {
            step = Integer.parseInt(stepAsString);
        }

        int delayInMillis = -1;
        if (delay != null && !delay.equals("")) {
            delayInMillis = Integer.parseInt(delay);
        }

        mConfiguration = new PrimitiveConfiguration(
                configName,
                baseUrl,
                baseUrlDelimiter,
                httpMethod,
                requestParams,
                firstPage,
                step,
                maxPagePattern,
                destination,
                firstLevelPattern,
                secondLevelPattern,
                secondLevelName,
                secondLevelBaseUrl,
                encoding,
                delayInMillis
        );

        // Use Proxy
        if (useProxy != null) {
            switch (useProxy.toLowerCase()) {
                case "yes":
                case "true":
                    mConfiguration.useProxy = true;
                    mConfiguration.proxyListPath = proxyPath;
                    break;
                case "no":
                case "false":
                default:
                    mConfiguration.useProxy = false;
            }
        }

        // PARSER TYPES FOR 2 LEVELS
        if (firstLevelParserType != null) {
            switch (firstLevelParserType.toLowerCase()) {
                default:
                case "regex":
                case "regexp":
                    mConfiguration.firstLevelParserType = PrimitiveConfiguration.Parsers.REGEX;
                    break;
                case "xpath":
                    mConfiguration.firstLevelParserType = PrimitiveConfiguration.Parsers.XPATH;
                    break;
            }
        }

        if (secondLevelParserType != null) {
            switch (secondLevelParserType.toLowerCase()) {
                default:
                case "regex":
                case "regexp":
                    mConfiguration.secondLevelParserType = PrimitiveConfiguration.Parsers.REGEX;
                    break;
                case "xpath":
                    mConfiguration.secondLevelParserType = PrimitiveConfiguration.Parsers.XPATH;
                    break;
            }
        }
    }

    private void initRequestArguments() {
        RequestArguments requestArguments = new RequestArguments();
        NodeList formatParamNodeList = (NodeList) getByXPath(XPATH_REQUEST_ARGUMENTS, XPathConstants.NODESET);
        if (formatParamNodeList != null) {
            requestArguments.mArgumentList = new ArrayList<>();
            // for each RequestArgument
            for (int i = 0; i < formatParamNodeList.getLength(); i++) {
                Argument currentArgument = new Argument();

                // getting format param attribute
                currentArgument.name = "{[" + getByXPath("@name", formatParamNodeList.item(i)) + "]}";
                currentArgument.field = getByXPath("@field", formatParamNodeList.item(i));

                // getting format param value bindings...
                // ...getting child nodes,
                NodeList CurrentValuePairNodeList =
                        (NodeList) getByXPath("Values", formatParamNodeList.item(i), XPathConstants.NODESET);
                currentArgument.mValues = new ArrayList<>();
                // ...for each value pair.
                for (int j = 0; j < CurrentValuePairNodeList.getLength(); j++) {
                    Values currentValuePair = new Values();

                    // getting attrs of the current RequestArgumentValues
                    currentValuePair.argumentValue = getByXPath("@argumentValue", CurrentValuePairNodeList.item(j));
                    currentValuePair.fieldValue = getByXPath("@fieldValue", CurrentValuePairNodeList.item(j));

                    // adding current pair to the list
                    currentArgument.mValues.add(currentValuePair);
                }
                // adding current fixed param to the configuration
                requestArguments.mArgumentList.add(currentArgument);
            }
            mConfiguration.requestArguments = requestArguments;
            mConfiguration.requestArguments.initProvider(mConfiguration.requestParams);
        }

    }

    private void initMarkers() {
        NodeList markerNodeList = (NodeList) getByXPath(XPATH_MARKERS, XPathConstants.NODESET);
        if (markerNodeList != null) {

            for (int nodeNum = 0; nodeNum < markerNodeList.getLength(); nodeNum++) {
                // getting necessary attributes
                String field = getByXPath("@field", markerNodeList.item(nodeNum));
                String value = getByXPath("@value", markerNodeList.item(nodeNum));

                // adding marker to the configuration
                mConfiguration.markers.put(field, value);
            }

        }
    }

    private void initCookies() {
        Node CookiesRequestNode = (Node) getByXPath(XPATH_COOKIES, XPathConstants.NODE);
        if (CookiesRequestNode != null) {
            CookieRules cookieRules = new CookieRules();
            // setting cookie request
            String CookieRequestAddress = getByXPath("@address", CookiesRequestNode);
            String CookieRequestParams = getByXPath("@params", CookiesRequestNode);
            String CookieRequestMethod = getByXPath("@method", CookiesRequestNode);

            if (!CookieRequestAddress.isEmpty() &&
                    !CookieRequestParams.isEmpty() &&
                    !CookieRequestMethod.isEmpty()) {
                cookieRules.mRequestCookiesAddress = CookieRequestAddress;
                cookieRules.mRequestCookiesParamString = CookieRequestParams;
                cookieRules.mRequestCookiesMethod = CookieRequestMethod;

                Cookies cookies = new Cookies();
                cookies.mCookieRules = cookieRules;
                mConfiguration.cookies = cookies;
            } else {

                // trying to set custom cookies
                NodeList CookieNodeList = (NodeList) getByXPath(XPATH_CUSTOM_COOKIE_LIST, XPathConstants.NODESET);
                if (CookieNodeList != null) {
                    Cookies cookies = new Cookies();
                    cookies.mCookieList = new ArrayList<>();

                    for (int i = 0; i < CookieNodeList.getLength(); i++) {

                        String Name = getByXPath("@name", CookieNodeList.item(i));
                        String Value = getByXPath("@value", CookieNodeList.item(i));
                        String Domain = getByXPath("@domain", CookieNodeList.item(i));
                        Cookie currentCookie = new Cookie(Name, Value, Domain);

                        cookies.mCookieList.add(currentCookie);

                    }
                    mConfiguration.cookies = cookies;
                }

            }
        }
    }

    private void initDataFieldBinding(int lvl) {
        String xpathLevelBindings;
        switch (lvl) {
            case 1:
                xpathLevelBindings = XPATH_FIRST_LEVEL_BINDINGS;
                break;
            case 2:
                xpathLevelBindings = XPATH_SECOND_LEVEL_BINDINGS;
                break;
            default:
                mErrorMessage = "Unknown level code for binding";
                return;
        }

        NodeList bindingsNodeList = (NodeList) getByXPath(xpathLevelBindings, XPathConstants.NODESET);
        if (bindingsNodeList != null) {

            Map<String, String> bindings = new HashMap<>();
            String curDataName;
            String curFieldName;
            for (int i = 0; i < bindingsNodeList.getLength(); i++) {

                curDataName = getByXPath("@dataName", bindingsNodeList.item(i));
                curFieldName = getByXPath("@fieldName", bindingsNodeList.item(i));

                bindings.put(curDataName, curFieldName);
            }

            switch (lvl) {
                case 1:
                    mConfiguration.firstLevelBindings = bindings;
                    break;
                case 2:
                    mConfiguration.secondLevelBindings = bindings;
                    break;
                default:
                    mErrorMessage = "Unknown level code for binding";
                    return;
            }
        }
    }

    //
    // XPATH METHODS
    //

    private String getParserAttributeAsString(String p_xPath) {
        String resultString = getByXPath(p_xPath);
        if (resultString == null || resultString.equals("")) {
            return null;
        } else {
            return resultString;
        }

    }

    /**
     * Gets a string value by XPath from root
     */
    private String getByXPath(String p_xPathString) {
        return (String) getByXPath(p_xPathString, null, null);
    }

    /**
     * Gets a string value by XPath from an item
     */
    private String getByXPath(String p_xPathString, Object p_item) {
        return (String) getByXPath(p_xPathString, p_item, null);
    }

    /**
     * Gets a specified type value by XPath from root
     */
    private Object getByXPath(String p_xPathString, QName p_xPathReturnType) {
        return getByXPath(p_xPathString, null, p_xPathReturnType);
    }

    /**
     * Gets a specified type value by XPath from mentioned item
     */
    private Object getByXPath(String p_xPathString, Object p_item, QName p_xPathReturnType) {

        Object item;
        // checking if the item is set
        if (p_item == null) {
            // ... not set - use root document
            item = mDoc;
        } else {
            // ... else use the item
            item = p_item;
        }

        QName xPathReturnType;
        if (p_xPathReturnType == null) {
            xPathReturnType = XPathConstants.STRING;
        } else {
            xPathReturnType = p_xPathReturnType;
        }

        try {
            // applying xPath expression to the item
            return mXPath.compile(p_xPathString).evaluate(item, xPathReturnType);
        } catch (XPathExpressionException e) {
            mErrorMessage = "[XPathExpressionException]: " + e.getMessage();
            return null;
        }
    }

}
