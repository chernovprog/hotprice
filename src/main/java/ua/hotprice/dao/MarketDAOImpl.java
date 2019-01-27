package ua.hotprice.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ua.hotprice.model.menu.Market;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MarketDAOImpl implements MarketDAO {
    @PersistenceContext
    private EntityManager em;

    @Override
    public void addMarket(Market market) {
        em.persist(market);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Market> marketList() {
        TypedQuery<Market> query = null;
        List<Market> markets = new ArrayList<>();
        try {
            query = em.createQuery("SELECT m FROM Market m", Market.class);
            if (query != null) markets = query.getResultList();
        }  catch (Exception ex) {
            ex.printStackTrace();
        }
        return markets;
    }

}
