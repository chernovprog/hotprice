package ua.hotprice.services.hotline;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ua.hotprice.model.product.Product;
import ua.hotprice.model.product.offers.Offer;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductHotlineService {
    private static final String domain = "https://hotline.ua";
    private static final String resourceSource = "https://hotline.ua/public/i/favicon/favicon.ico";
    private static final Logger logger = Logger.getLogger(ProductHotlineService.class);
    private static final String phantomjsExeutableFilePath = "/usr/phantomjs-2.1.1-linux-x86_64/bin/phantomjs";
    //private static final String phantomjsExeutableFilePath = "C:\Users\ANDREY\.m2\phantomjs-2.1.1-windows\bin\phantomjs.exe";
    @Autowired
    private ApplicationContext context;

    //  Getting the whole list of offers (information is loaded using JavaScript)
    public Product parseProductPageWithOffers(Document doc) {
        Product product = null;

        if (doc != null) {
            product = context.getBean(Product.class);
            product.setName(this.getName(doc));
            product.setQuantityOffers(this.getQuantityOffers(doc));
            product.setImgS(this.getImgProduct(doc));
            product.setDesc(this.getDescription(doc));
            product.setKeyWord(this.getKeyWord(doc, product));

            String price = getPriceStr(doc);
            if (price != null && !price.isEmpty() && price.contains("–")) {
                product.setPriceFrom(this.getPriceFrom(price));
                product.setPriceTo(this.getPriceTo(price));
            } else if (price != null && !price.isEmpty() && NumberUtils.isDigits(price)) {
                product.setPriceFrom(Integer.valueOf(price));
            }

            product.setCurrency(this.getCurrency(doc));
            product.setOffers(getListOffers(doc, product));
        }

        return product;
    }

    private String getName(Document doc) {
        return doc.select("h1[datatype='card-title']").text();
    }

    private Integer getQuantityOffers(Document doc) {
        Integer quantityOffers = 1;
        String numOffers = doc.select(".resume-item:first-child .h3 .pointer").text();
        if (numOffers != null && !numOffers.isEmpty() && numOffers.contains(":")) {
            String res = numOffers.substring(numOffers.indexOf(":") + 2);
            if (NumberUtils.isDigits(res)) quantityOffers = Integer.valueOf(res);
        }

        return quantityOffers;
    }

    private String getImgProduct(Document doc) {
        String img = doc.select(".img-product").attr("src");

        return img.contains("http") ? img : domain + img;
    }

    private String getDescription(Document doc) {
        String desc = doc.select(".app-nav-scroll .text").text();
        if (desc == null || desc.isEmpty()) {
            return this.getName(doc);
        } else {
            int indexOf = desc.indexOf("... развернуть свернуть");
            if (indexOf > 0) {
                String substring = desc.substring(0, indexOf);
                return substring;
            } else {
                return desc;
            }
        }
    }

    private String getPriceStr(Document doc) {
        String price = doc.select(".resume-item:first-child .price-lg .value").text().replaceAll(" ", "");
        return price;
    }

    private Integer getPriceFrom(String price) {
        String priceFromStr = price.substring(0, price.indexOf("–"));
        if (priceFromStr != null && !priceFromStr.isEmpty() && NumberUtils.isDigits(priceFromStr)) {
            return Integer.valueOf(priceFromStr);
        } else {
            return 0;
        }
    }

    private Integer getPriceTo(String price) {
        String priceToStr = price.substring(price.indexOf("–") + 1);
        if (priceToStr != null && !priceToStr.isEmpty() && NumberUtils.isDigits(priceToStr)) {
            return Integer.valueOf(priceToStr);
        } else {
            return 0;
        }
    }

    private String getCurrency(Document doc) {
        String currency = doc.select(".resume-item:first-child .price-lg").text();
        if (currency != null && !currency.isEmpty() && currency.contains(" ")) {
            String res = currency.substring(currency.lastIndexOf(" ") + 1);
            if (res != null && !res.isEmpty()) return res;
        }
        return "";
    }

    private String getKeyWord(Document doc, Product product) {
        String keywords = doc.select("meta[name='keywords']").attr("content");
        String name = product.getName();
        if (keywords != null && !"".equals(keywords) && keywords.contains(",") && name != null && !"".equals(name)) {
            String keyWord = keywords.substring(0, keywords.indexOf(","));
            if (!"".equals(keyWord) && name.contains(keyWord)) return keyWord;
        }
        return "";
    }

    // Getting product offers information
    private List<Offer> getListOffers(Document doc, Product product) {
        List<Offer> offers = new ArrayList<>();
        WebDriver driver = null;
        String baseUri = doc.baseUri();

        try {
            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setJavascriptEnabled(true);
            caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjsExeutableFilePath);

            driver = new PhantomJSDriver(caps);
            driver.get(baseUri);

            WebElement blockOffer = driver.findElement(By.cssSelector(".viewbox.all-offers"));
            List<WebElement> blocksOffers = blockOffer.findElements(By.cssSelector(".viewbox.all-offers > .offers-item"));

            for (WebElement block : blocksOffers) {
                Offer offer = context.getBean(Offer.class);

                try {
                    offer.setNameStore(getNameStore(block));
                    offer.setImgLogoStore(getLogoStore(block));
                    offer.setLink(getLinkStore(block));
                    offer.setPrice(getProductPriceStore(block));
                    offer.setCurrency(getCurrencyStore(block));
                    String desc = getProductDescriptionStore(block);
                    if ("".equals(desc) || desc == null) desc = product.getName();
                    offer.setDesc(desc);
                    offer.setImgProduct(product.getImgS());
                    offer.setResourceSrc(resourceSource);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                offers.add(offer);
            }

            driver.quit();
        } catch (TimeoutException ex) {
            ex.printStackTrace();
            logger.error("WebDriver TimeoutException. URL=" + baseUri);
        } catch (Exception ex) {    // org.openqa.selenium.remote.service.DriverService.waitUntilAvailable > org.openqa.selenium.net.UrlChecker$TimeoutException > java.util.concurrent.TimeoutException
            ex.printStackTrace();
            logger.error("WebDriver Exception. URL=" + baseUri, ex);
        } finally {
            if (driver != null) driver.quit();
        }

        return offers;
    }

    private String getNameStore(WebElement el) {
        try {
            WebElement nameStore = el.findElement(By.cssSelector("div.ellipsis"));
            return nameStore.getText();
        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private String getLogoStore(WebElement el) {
        try {
            WebElement logo = el.findElement(By.cssSelector("img.img-shop.busy"));
            return logo.getAttribute("src");
        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private String getLinkStore(WebElement el) {
        try {
            WebElement link = el.findElement(By.cssSelector("a[data-tracking-id='goprice-1']"));
            return link.getAttribute("href");
        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private Integer getProductPriceStore(WebElement el) {
        try {
            WebElement price = el.findElement(By.cssSelector("span.price-format span.value"));
            String priceStr = price.getText();
            if (priceStr != null && !"".equals(priceStr)) {
                if (priceStr.contains(" ")) {
                    priceStr = priceStr.replace(" ", "");
                }

                if (NumberUtils.isDigits(priceStr)) {
                    Integer priceInt = Integer.valueOf(priceStr);
                    return priceInt;
                }
            }
        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private String getCurrencyStore(WebElement el) {
        try {
            WebElement currency = el.findElement(By.cssSelector("div.price-box .app-box"));
            return currency.getText() + ".";
        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private String getProductDescriptionStore(WebElement el) {
        try {
            WebElement descWeb = el.findElement(By.cssSelector("[data-selector='descr_line'] a"));
            return descWeb.getText();
        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
