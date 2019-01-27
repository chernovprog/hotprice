package ua.hotprice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.hotprice.comparators.GroupPriorityCompare;
import ua.hotprice.dao.GroupDAO;
import ua.hotprice.dao.MarketDAO;
import ua.hotprice.dao.RubricDAO;
import ua.hotprice.model.menu.Group;
import ua.hotprice.model.menu.Market;
import ua.hotprice.model.menu.Rubric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MenuService {
    @Autowired
    private MarketDAO marketDAO;

    @Autowired
    private RubricDAO rubricDAO;

    @Autowired
    private GroupDAO groupDAO;

    @Autowired
    private GroupPriorityCompare groupPriorityCompare;

    /* public void addMarket(Market market) { marketDAO.addMarket(market); }

    public void addRubric(Rubric rubric) { rubricDAO.addRubric(rubric); }

    public void addGroup(Group group) { groupDAO.addGroup(group); } */

    public List<Market> getMenu() {
        List<Market> marketList =  marketDAO.marketList();

        return marketList;
    }

    public List<Group> getPopularGroupsList() {
        List<Group> groupList = groupDAO.popularGroupsList();
        Collections.sort(groupList, groupPriorityCompare);
        return groupList;
    }
}
