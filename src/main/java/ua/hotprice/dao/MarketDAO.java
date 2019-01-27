package ua.hotprice.dao;

import ua.hotprice.model.menu.Market;

import java.util.List;

public interface MarketDAO {
    void addMarket(Market market);
    List<Market> marketList();
}
