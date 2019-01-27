package ua.hotprice.services.priceua;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ua.hotprice.model.product.Product;
import ua.hotprice.model.product.offers.Offer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductPriceuaService {
    private static final String domain = "https://price.ua";
    private static final String resourceSource = "https://price.ua/favicon.ico";
    private static final Logger logger = Logger.getLogger(ProductPriceuaService.class);
    @Autowired
    private ApplicationContext context;

    public Product parseProductPageWithOffers(String productName) {
        Document doc = this.getDocumentByProductName(productName);
        Product product = null;

        if (doc != null) {
            Element firstProduct = this.getFirstProductFromSearchResult(doc);
            if (firstProduct != null) {
                String linkFirstProduct = this.getLinkFirstProduct(firstProduct);

                String linkOffers = "";
                if (linkFirstProduct != null && !"".equals(linkFirstProduct)) {
                    linkOffers = this.getDirectLinkProductOffers(linkFirstProduct);

                    List<Offer> listOffers = new ArrayList<>();
                    if (!"".equals(linkOffers)) {
                        listOffers = this.getListOffers(linkOffers);
                    }
                    if (!listOffers.isEmpty()) {
                        product = context.getBean(Product.class);
                        product.setName(this.getNameFirstProduct(firstProduct));
                        product.setOffers(listOffers);
                    }
                }
            }
        }

        return product;
    }

    private Document getDocumentByProductName(String productName) {
        String source = "https://price.ua/search/?q=" + productName;
        Document doc = null;
        try {
            doc = Jsoup.connect(source).get();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("HTTP error fetching URL. Status=404. Search by keyword in Price.ua. Path=" + source);
        }
        return doc;
    }

    private Element getFirstProductFromSearchResult(Document doc) {
        return doc.selectFirst("div.product-block");
    }

    private String getLinkFirstProduct(Element firstProductBlock) {
        return firstProductBlock.select("a.model-name.ga_card_mdl_title").attr("href");
    }

    // Get a direct link to the product offers
    private String getDirectLinkProductOffers(String link) {
        if (link != null && !"".equals(link) && link.contains("/")) {
            String[] split = link.split("/");
            String lastPart = split[split.length - 1];
            if (lastPart.contains("t1m")) {
                String id = lastPart.substring(lastPart.indexOf("t1m") + 3, lastPart.length());
                return "https://price.ua/" + split[4] + "/prices/" + id;
            }
        }
        return "";
    }

    private String getNameFirstProduct(Element firstProduct) {
        return firstProduct.select("a.model-name.ga_card_mdl_title").text();
    }

    /* Parsing a list of offers */
    private List<Offer> getListOffers(String linkWithOffers) {
        Document doc = this.getDocumentWithOffers(linkWithOffers);
        List<Offer> offerList = new ArrayList<>();

        if (doc != null) {
            Elements blocksOffers = this.getBlocksOffers(doc);

            for (Element block : blocksOffers) {
                Offer offer = context.getBean(Offer.class);

                offer.setNameStore(this.getNameStore(block));
                offer.setImgLogoStore(this.getImgLogoStore(block));
                offer.setImgProduct(this.getImgProductStore(block));
                offer.setLink(this.getLinkStore(block));
                offer.setDesc(this.getDescription(block, doc));
                offer.setPrice(this.getPriceFromSite(block));
                offer.setCurrency(this.getCurrencyStore(block));
                offer.setResourceSrc(resourceSource);

                offerList.add(offer);
            }
        }

        return offerList;
    }

    private Document getDocumentWithOffers(String linkWithOffers) {
        Document doc = null;
        try {
            doc = Jsoup.connect(linkWithOffers).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    private Elements getBlocksOffers(Document doc) {
        return doc.select("div.priceline-item");
    }

    private String getNameStore(Element el) {
        return el.select("span.logo img").attr("alt");
    }

    private String getImgLogoStore(Element el) {
        String res = el.select("span.logo img").attr("src");
        return "".equals(res) ? "" : "https:" + res;
    }

    private String getImgProductStore(Element el) {
        String res = el.select("div.photo-wrap img").attr("src");
        return "".equals(res) ? "" : "https:" + res;
    }

    private String getLinkStore(Element el) {
        String res = el.select("div.stores-block a").attr("href");
        return "".equals(res) ? "" : domain + res;
    }


    private String getDescription(Element el, Document doc) {
        String desc = el.select("span.descr-text").text();
        if ("".equals(desc)) {  /*  add a name if the field is empty */
            String productName = doc.select("[itemprop='name']").text();
            return productName;
        }

        String res = "";
        if (desc != null && !"".equals(desc)) {
            if (desc.contains("Подробнее")) {
                res = desc.substring(0, desc.indexOf("Подробнее"));
            }
            if (desc.length() > 200) {
                res = desc.substring(0, 200) + "...";
            }
        }

        return res;
    }

    private Integer getPriceFromSite(Element el) {
        String priceStr = el.select("span.price").text();

        if (priceStr != null && !"".equals(priceStr)) {
            if (priceStr.contains(" ")) {
                priceStr = priceStr.replace(" ", "");
            }

            if (priceStr.contains("грн.")) {
                priceStr = priceStr.replace("грн.", "");
            }

            if (NumberUtils.isDigits(priceStr)) {
                return Integer.valueOf(priceStr);
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    private String getCurrencyStore(Element el) {
        return el.select("span.price span").text();
    }
}

