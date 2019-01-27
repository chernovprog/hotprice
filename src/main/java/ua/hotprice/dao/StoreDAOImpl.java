package ua.hotprice.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ua.hotprice.model.product.offers.Store;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class StoreDAOImpl implements StoreDAO {
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void addStores(Store store) {
        em.persist(store);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Store> getStoresList() {
        TypedQuery<Store> query = null;
        List<Store> stores = null;
        try {
            query = em.createQuery("SELECT s FROM Store AS s", Store.class);
            if (query != null) stores = query.getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return stores;
    }
}
