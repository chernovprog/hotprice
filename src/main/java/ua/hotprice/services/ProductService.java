package ua.hotprice.services;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.hotprice.dao.StoreDAO;
import ua.hotprice.exceptions.EmptyProductListException;
import ua.hotprice.model.product.Product;
import ua.hotprice.model.product.offers.Offer;
import ua.hotprice.model.product.offers.Store;
import ua.hotprice.services.hotline.ProductHotlineService;
import ua.hotprice.services.hotline.ProductListHotlineService;
import ua.hotprice.services.priceua.ProductPriceuaService;

import java.util.Collections;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductPriceuaService prodPriceuaService;

    @Autowired
    private ProductHotlineService prodHotlineService;

    @Autowired
    private ProductListHotlineService prodListHotlineService;

    @Autowired
    private StoreDAO storeDAO;

    public List<Product> getProductListPage(Document documentHotline) {
        List<Product> productListHotline = prodListHotlineService.parseProductListPage(documentHotline);
        if (productListHotline.isEmpty()) throw new EmptyProductListException("Product list is empty");

        return productListHotline;
    }

    public List<Product> getProductListPageBySearch(Document documentHotline) {
        List<Product> productListHotline = prodListHotlineService.parseProductListPage(documentHotline);

        return productListHotline;
    }

    public Product getProductWithOffersPage(Document documentHotline) {
        Product product = prodHotlineService.parseProductPageWithOffers(documentHotline);
        if (product != null && !isProductNameNullOrEmpty(product)) {
            String keyWord = getKeyWord(product);
            Product productPriceua = prodPriceuaService.parseProductPageWithOffers(keyWord);

            if (productPriceua != null && !isProductNameNullOrEmpty(productPriceua)) {

                Boolean same = this.isItSameProduct(product, productPriceua);
                if (same) {
                    List<Offer> priceuaOffers = productPriceua.getOffers();
                    for (Offer priceuaOffer : priceuaOffers) {
                        product.addOffer(priceuaOffer);
                        product.setQuantityOffers(product.getQuantityOffers() + 1);
                    }
                }

            }

            Collections.sort(product.getOffers());
        }

        return product;
    }

    //product name without prefix in Cyrillic
    private String getKeyWord(Product product) {
        String keyWord = product.getKeyWord();
        if (!"".equals(keyWord)) {
            return keyWord;
        } else {
            return product.getName();
        }
    }

    private boolean isProductNameNullOrEmpty(Product product) {
        String name = product.getName();
        if (name == null || "".equals(name)) {
            return true;
        }
        return false;
    }

    private boolean isItSameProduct(Product prodHotline, Product prodPriceua) {
        String prodNameHotline = getKeyWord(prodHotline).toLowerCase();
        String prodNamePriceua = prodPriceua.getName().toLowerCase();

        if (prodNameHotline.equals(prodNamePriceua)) {
            return true;
        } else if (transformName(prodNameHotline).equals(transformName(prodNamePriceua))) {
            return true;
        } else { /* looking for a price match. Cause the names can be very different (e.g. product code)*/
            List<Store> storesDB = storeDAO.getStoresList();
            List<Offer> hotlineOffers = prodHotline.getOffers();
            List<Offer> priceuaOffers = prodPriceua.getOffers();

            if (storesDB != null) {
                for (Offer hotlineOffer : hotlineOffers) {
                    String storeNamePriceuaDB = getMatchingStoreName(storesDB, hotlineOffer);

                    if (storeNamePriceuaDB != null && !"".equals(storeNamePriceuaDB)) {
                        Offer priceuaOffer = findStoreNameInOffersPriceua(priceuaOffers, storeNamePriceuaDB);

                        if (priceuaOffer != null) {
                            if (comparePrice(hotlineOffer, priceuaOffer)) {
                                // Don't go out immediately. Prices for the same product can be different
                                return true;
                            }
                        }
                    }

                }
            }

            return false;
        }
    }

    private boolean comparePrice(Offer hotlineOffer, Offer priceuaOffer) {
        Integer priceHotline = hotlineOffer.getPrice();
        Integer pricePriceua = priceuaOffer.getPrice();

        if (priceHotline != null && pricePriceua != null) {
            return priceHotline.equals(pricePriceua);
        }

        return false;
    }

    private Offer findStoreNameInOffersPriceua(List<Offer> priceuaOffers, String storeNamePriceuaDB) {
        for (Offer priceuaOffer : priceuaOffers) {
            String storeNameOfferPriceua = priceuaOffer.getNameStore();  // take the name of the store priceua variant
            if (storeNamePriceuaDB.equals(storeNameOfferPriceua)) { // look for this store from the list of offers
                return priceuaOffer;
            }
        }

        return null;
    }

    // store names are different. Looking for an analogue of the name on other resource (Price.ua)
    private String getMatchingStoreName(List<Store> storesDB, Offer hotlineOffer) {
        String storeNameHotlineOffer = hotlineOffer.getNameStore(); // take the name of the store
        if (storeNameHotlineOffer != null && !"".equals(storeNameHotlineOffer)) {
            for (Store storeDB : storesDB) {
                if (storeNameHotlineOffer.equals(storeDB.getStoreNameHotlineVersion())) { // search store name Hotline variant in database
                    return storeDB.getStoreNamePriceuaVersion(); // get the store name Priceua variant in database
                }
            }
        }

        return "";
    }

    private String transformName(String name) {
        StringBuilder nameSB = new StringBuilder(name);

        for (int i = 0; i < nameSB.length(); i++) {
            if (nameSB.charAt(i) == ' ' || nameSB.charAt(i) == '-') {
                nameSB.deleteCharAt(i);
                i--;
            }
        }

        if (nameSB.toString().contains("(") && nameSB.toString().contains(")")) {
            if (nameSB.indexOf("(") < nameSB.indexOf(")")) {
                nameSB.delete(nameSB.indexOf("("), nameSB.indexOf(")") + 1);
            }
        }

        if (nameSB.toString().contains("/")) {
            if (nameSB.indexOf("/") < nameSB.length() - 1) {
                nameSB.delete(nameSB.indexOf("/"), nameSB.length());
            } else {
                nameSB.deleteCharAt(nameSB.length() - 1);
            }
        }

        return nameSB.toString();
    }


}
