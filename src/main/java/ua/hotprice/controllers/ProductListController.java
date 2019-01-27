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

import javax.servlet.ServletRequest;
import java.util.List;

@Controller
public class ProductListController {

    @Autowired
    private ProductService productService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private PageHotlineService pageHotlineService;

    @Autowired
    private DocumentHotlineService documentHotline;

    @RequestMapping(value = {"/auto/*/*/", "/bt/*/*/", "/dom/*/*/", "/dacha_sad/*/*/", "/deti/*/*/", "/zootovary/*/*/", "/tools/*/*/",
            "/knigi/*/*/", "/computer/*/*/", "/musical_instruments/*/*/", "/office/*/*/", "/fashion/*/*/", "/krasota/*/*/",
            "/remont/*/*/", "/mobile/*/*/", "/sport/*/*/", "/av/*/*/", "/health/*/*/", "/tourism/*/*/", "/us/*/*/"}, method = RequestMethod.GET)
    public String getProductList(@RequestParam(required = false, defaultValue = "1") String page,
                                 ServletRequest request, Model model) {

        Document docHotline = documentHotline.getDocumentProductListPage(request, page);
        List<Product> productList = productService.getProductListPage(docHotline);
        Page pageInfo = pageHotlineService.getInfoFromProductListPage(docHotline);

        model.addAttribute("menu", menuService.getMenu());
        model.addAttribute("productList", productList);
        model.addAttribute("pageInfo", pageInfo);

        return "productList";
    }

}
