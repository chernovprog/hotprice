package ua.hotprice.dao;

import ua.hotprice.model.menu.Rubric;

import java.util.List;

public interface RubricDAO {
    void addRubric(Rubric rubric);
    List<Rubric> rubricList();
}
