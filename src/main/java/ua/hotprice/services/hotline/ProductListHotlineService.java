package ua.hotprice.services.hotline;

import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ua.hotprice.model.product.Product;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductListHotlineService {
    private static final String domain = "https://hotline.ua";
    @Autowired
    private ApplicationContext context;

    //  Getting a list of products of one of the pages
    public List<Product> parseProductListPage(Document doc) {

        List<Product> products = new ArrayList<>();

        if (doc != null) {

            Elements elements = doc.select("li.product-item").not(".product-item-ad");

            for (Element element : elements) {
                Product product = context.getBean(Product.class);
                product.setName(this.getName(element));
                product.setImgS(this.getImgSmall(element));
                product.setImgL(this.getImgLarge(element));
                product.setDesc(this.getDescription(element));
                product.setPriceFrom(this.getPriceFrom(element));
                product.setCurrency(this.getCurrency(element));
                product.setQuantityOffers(this.getQuantityOffers(element));
                product.setUrl(this.getUrl(element));

                products.add(product);
            }

        }

        return products;
    }

    private String getName(Element element) {
        String name = element.select("div.item-info p a").text();
        if (name == null || "".equals(name)) {
            name = element.select(".h4 a").text();
        }
        return name;
    }

    private String getImgSmall(Element element) {
        String imgS = element.select(".img-product.busy").attr("src");
        return imgS.contains("https") ? imgS : !"".equals(imgS) ? domain + imgS : "";
    }

    private String getImgLarge(Element element) {
        String imgL = element.select(".img-product.busy").attr("data-image-tip");
        return imgL.contains("https") ? imgL : !"".equals(imgL) ? domain + imgL : "";
    }

    private String getDescription(Element element) {
        String desc = element.select("div[data-navigation-id] div.text").text();
        if (desc != null && !"".equals(desc)) {
            return desc;
        } else {
            /*desc = element.select(".product-item :nth-child(3)").text();
            if (desc != null && !"".equals(desc)) {
                return desc;
            }*/
            return getName(element);
        }
    }

    private Integer getPriceFrom(Element element) {
        String priceFrom = element.select(".item-price .text-sm").text().replaceAll(" ", "");
        if ("".equals(priceFrom)) {
            priceFrom = element.select(".item-price .price-format .value").text().replaceAll(" ", "");
        }
        if ("".equals(priceFrom)) {
            priceFrom = element.select(".item-price .price .value").text().replaceAll(" ", "");
        }

        if (!"".equals(priceFrom) && priceFrom.indexOf("–") >= 0) {
            String subStr = priceFrom.substring(0, priceFrom.indexOf("–"));
            if (NumberUtils.isDigits(subStr)) return Integer.valueOf(subStr);
            else return 0;
        } else {
            if (NumberUtils.isDigits(priceFrom)) return Integer.valueOf(priceFrom);
            else return 0;
        }
    }

    private String getCurrency(Element element) {
        String curr = element.select(".item-price .text-sm").text();
        if ("".equals(curr)) {
            curr = element.select(".item-price .price-format").text();
        }
        if ("".equals(curr)) {
            curr = element.select(".item-price .price").text();
        }

        if (!"".equals(curr)) {
            int spaceIndex = curr.lastIndexOf(" ");
            if (spaceIndex >= 0 && spaceIndex < curr.length() - 1)
                return curr.substring(curr.lastIndexOf(" ") + 1);
            return "";
        } else {
            return "";
        }
    }

    private Integer getQuantityOffers(Element element) {
        String countOffer = element.select(".item-price a.link").text();
        if (countOffer != null && !countOffer.isEmpty() && countOffer.contains("Все предложения")) {
            if (countOffer.contains("(") && countOffer.contains(")")) {
                String substring = countOffer.substring(countOffer.indexOf("(") + 1, countOffer.indexOf(")"));
                if (NumberUtils.isDigits(substring)) return Integer.valueOf(substring);
            }
        }
        return 1;
    }

    private String getUrl(Element element) {
        String res = element.select(".item-img a").attr("href");
        if ("".equals(res)) res = element.select(".btn-orange").attr("href");// reserve

        return res.contains("/go/price/") ? domain + res : res;
    }
}