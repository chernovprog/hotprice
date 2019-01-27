package ua.hotprice.comparators;

import org.springframework.stereotype.Component;
import ua.hotprice.model.menu.Group;

import java.util.Comparator;

@Component
public class GroupPriorityCompare implements Comparator<Group> {

    @Override
    public int compare(Group g1, Group g2) {
        if (g1.getPriority() < g2.getPriority()) return -1;
        if (g1.getPriority() > g2.getPriority()) return 1;
        else return 0;
    }
}
