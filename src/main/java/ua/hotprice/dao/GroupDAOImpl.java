package ua.hotprice.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ua.hotprice.model.menu.Group;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GroupDAOImpl implements GroupDAO {
    @PersistenceContext
    private EntityManager em;

    @Override
    public void addGroup(Group group) {
        em.persist(group);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> groupList() {
        TypedQuery<Group> query = null;
        List<Group> groups = new ArrayList<>();
        try {
            query = em.createQuery("SELECT g FROM Group AS g", Group.class);
            if (query != null) groups = query.getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return groups;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> popularGroupsList() {
        TypedQuery<Group> query = null;
        List<Group> groups = new ArrayList<>();
        try {
            query = em.createQuery("SELECT g FROM Group AS g WHERE popular = 1", Group.class);
            if (query != null) groups = query.getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return groups;
    }

}
