package ua.hotprice.services.hotline;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import ua.hotprice.exceptions.JsoupDocumentException;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Service
public class DocumentHotlineService {
    private static final String domain = "https://hotline.ua";
    private static final Logger logger = Logger.getLogger(DocumentHotlineService.class);

    public Document getDocumentProductListPage(ServletRequest request, String page) {
        String path = getPath(request);
        page = correctNumberPage(page);
        String url = page.equals("0") ? domain + path : domain + path + "?p=" + page;

        Document doc = null;

        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException ex) {
            ex.printStackTrace();
            logger.error("HTTP error fetching URL. Status=404. Path=" + path);
            throw new JsoupDocumentException("HTTP error fetching URL. Status=404");
        }

        return doc;
    }

    // In current page does not exist lint with offers. Create this one.
    public Document getDocumentProductWithOffersPage(ServletRequest request) {
        String path = getPath(request);
        String linkWithOffers = domain + path + "prices/";

        Document doc = null;
        try {
            doc = Jsoup.connect(linkWithOffers).get();
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("HTTP error fetching URL. Status=404. Path=" + path);
            throw new JsoupDocumentException("HTTP error fetching URL. Status=404");
        }

        return doc;
    }

    public Document getDocumentBySearchQuery(String query, String page) {
        page = correctNumberPage(page);
        String url = page.equals("0") ? domain + "/sr/?q=" + query : domain + "/sr/?q=" + query + "&p=" + page;

        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("HTTP error fetching URL. Status=404. URL=" + url);
            throw new JsoupDocumentException("HTTP error fetching URL. Status=404");
        }

        return doc;
    }

    private String correctNumberPage(String page) {
        if (!NumberUtils.isDigits(page)) {
            return "0";
        } else {
            int pageInt = Integer.parseInt(page);
            if (pageInt < 1) {
                return "0";
            } else {
                return "" + (--pageInt);
            }
        }
    }

    private String getPath(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        return httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
    }

}