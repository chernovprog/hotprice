package ua.hotprice.dao;

import ua.hotprice.model.product.offers.Store;

import java.util.List;

public interface StoreDAO {
    void addStores(Store store);
    List<Store> getStoresList();
}
