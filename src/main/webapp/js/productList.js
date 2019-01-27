/* prev & next buttons pagination */
var $pages = $('a.pages:not(.last-page)');
for (var i = 0; i < $pages.length; i++) {
    if ($($pages[i]).hasClass("active")) {
        if (i == 0) {
            $('.prev').addClass('inactive');
            $('.next').attr('href', $($pages[i + 1]).attr('href'));
        } else if (i == $pages.length - 1) {
            $('.prev').attr('href', $($pages[i - 1]).attr('href'));
            $('.next').addClass('inactive');
        } else {
            $('.prev').attr('href', $($pages[i - 1]).attr('href'));
            $('.next').attr('href', $($pages[i + 1]).attr('href'));
        }
        break;
    }
}

/* turn on spinner (during waiting for a result). It works if there are more than one offer */
$('.product-item').each(function () {
    var countOffersStr = $(this).find('.quantity-offers').text();
    if (countOffersStr !== "(1)") {
        $(this).find('.img-product, .product__name, .button-compare').click(function () {
            $("#spinner").css("display", "block");
        })
    }
});
