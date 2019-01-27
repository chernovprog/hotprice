package ua.hotprice.dao;

import ua.hotprice.model.menu.Group;

import java.util.List;

public interface GroupDAO {
    void addGroup(Group group);
    List<Group> groupList();
    List<Group> popularGroupsList();
}
