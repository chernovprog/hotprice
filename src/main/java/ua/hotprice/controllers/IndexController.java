package ua.hotprice.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import ua.hotprice.model.menu.Group;
import ua.hotprice.services.MenuService;

import java.util.List;

@Controller
@RequestMapping("/")
public class IndexController {

    @Autowired
    private MenuService menuService;

    @RequestMapping("/")
    public String index(Model model) {

        model.addAttribute("menu", menuService.getMenu());
        model.addAttribute("popularGroupsList", menuService.getPopularGroupsList());

        return "index";
    }

}

