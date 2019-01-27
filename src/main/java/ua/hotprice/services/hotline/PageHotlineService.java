package ua.hotprice.services.hotline;

import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.hotprice.model.product.page.Page;
import ua.hotprice.model.product.page.info.Breadcrumbs;
import ua.hotprice.model.product.page.info.Pagination;

import java.util.ArrayList;
import java.util.List;

@Service
public class PageHotlineService {
    @Autowired
    private Page page;
    @Autowired
    private Pagination pagination;

    public Page getInfoFromProductListPage(Document doc) {
        Page page = this.page;
        page.setTitle(this.parseTitle(doc));
        page.setPagination(this.parsePaginationFromPage(doc));
        page.setBreadcrumbs(this.parseBreadcrumbsList(doc));
        page.setCategory(this.parseCategory(doc));
        page.setPath(this.getPath(doc));

        return page;
    }

    public Page getInfoFromProductPage(Document doc) {
        Page page = this.page;
        page.setTitle(this.parseTitle(doc));
        page.setPagination(this.parsePaginationFromPage(doc));
        page.setBreadcrumbs(this.parseBreadcrumbsList(doc));

        return page;
    }

    private Pagination parsePaginationFromPage(Document doc) {
        Pagination pagination = this.pagination;
        List<String> rangPages = this.parseRangPages(doc);
        pagination.setActivePageNumber(this.getActivePageNumber(doc));

        if (!rangPages.isEmpty()) {
            pagination.setRangePages(rangPages);
        }

        if (rangPages.size() >= 7) {
            String lastPageRangStr = rangPages.get(rangPages.size() - 1);
            String lastPageSiteStr = doc.select("div.pages-list :last-child").text();
            if (NumberUtils.isDigits(lastPageRangStr) && NumberUtils.isDigits(lastPageSiteStr)) {
                int lastPageRang = Integer.parseInt(lastPageRangStr);
                int lastPageSite = Integer.parseInt(lastPageSiteStr);
                if (lastPageRang <= (lastPageSite - 2)) {
                    pagination.setNumberLastPage(lastPageSiteStr);
                }
            }
        }

        return pagination;
    }

    private List<Breadcrumbs> parseBreadcrumbsList(Document doc) {
        List<Breadcrumbs> breadcrumbsList = new ArrayList<>();
        breadcrumbsList.add(new Breadcrumbs("/", "Главная"));

        Elements elements = doc.select(".breadcrumbs li");
        Breadcrumbs breadcrumbs;
        for (Element e : elements) {
            if (!"".equals(e.text())) {
                breadcrumbs = new Breadcrumbs(e.text());
                breadcrumbsList.add(breadcrumbs);
            }
        }

        return breadcrumbsList;
    }

    private List<String> parseRangPages(Document doc) {
        List<String> list = doc.select("div.pages-list .pages").eachText();
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.equals("...")) break;
            result.add(s);
        }
        return result;
    }

    private String parseTitle(Document doc) {
        String title = doc.select(".heading h1").text();
        if (title == null || "".equals(title)) {
            title = doc.select(".breadcrumbs li:last-child").text();
        }

        return title;
    }

    private String parseCategory(Document doc) {
        String category = doc.select(".heading h1").text();
        if (category == null || "".equals(category)) {
            category = doc.select(".breadcrumbs li:last-child").text();
        }
        return category;
    }

    private String getActivePageNumber(Document doc) {
        String baseUri = doc.baseUri();

        if (baseUri != null) {
            int indexParam = baseUri.indexOf("p=");
            if (indexParam < 0) {
                return "1";
            } else {
                if (indexParam + 2 < baseUri.length()) {
                    String pageNumberStr = baseUri.substring(indexParam + 2);
                    if (NumberUtils.isDigits(pageNumberStr)) {
                        int pageNumber = Integer.parseInt(pageNumberStr);
                        return "" + ++pageNumber;
                    } else {
                        String[] split = pageNumberStr.split("");
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < split.length; i++) {
                            if (NumberUtils.isDigits(split[i])) sb.append(split[i]);
                            else break;
                        }
                        if (sb.length() > 0) {
                            int pageNumber = Integer.parseInt(sb.toString());
                            return "" + ++pageNumber;
                        }
                    }
                }
            }
        }
        return "";
    }

    private String getPath(Document doc) {
        String baseUri = doc.baseUri().replace("https://hotline.ua", "");
        if (baseUri.contains("/sr/")) baseUri = baseUri.replace("/sr/", "/search/");

        return !baseUri.contains("?") ? baseUri : baseUri.substring(0, baseUri.indexOf("?"));
    }

}