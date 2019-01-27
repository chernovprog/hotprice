package ua.hotprice.controllers;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import ua.hotprice.model.product.Product;
import ua.hotprice.model.product.page.Page;
import ua.hotprice.services.MenuService;
import ua.hotprice.services.ProductService;
import ua.hotprice.services.hotline.DocumentHotlineService;
import ua.hotprice.services.hotline.PageHotlineService;

import java.util.List;

@Controller
public class SearchController {

    @Autowired
    private ProductService productService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private PageHotlineService pageHotlineService;

    @Autowired
    private DocumentHotlineService documentHotline;

    @RequestMapping(value = "/search/", method = RequestMethod.GET)
    public String search(@RequestParam(name = "query") String query, @RequestParam(required = false, defaultValue = "1") String page,
                         Model model) {
        model.addAttribute("menu", menuService.getMenu());
        model.addAttribute("query", query);

        Document docHotline = documentHotline.getDocumentBySearchQuery(query, page);
        List<Product> productList = productService.getProductListPageBySearch(docHotline);

        if (productList.isEmpty()) { return "errors/productNotFound"; }

        Page pageInfo = pageHotlineService.getInfoFromProductListPage(docHotline);

        model.addAttribute("productList", productList);
        model.addAttribute("pageInfo", pageInfo);
        return "search";
    }
}
