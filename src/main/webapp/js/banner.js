/* For banner */
document.addEventListener('DOMContentLoaded', function () {
    startBanner();
})

var bannerImgUrl = [
    "img/banner/banner_1.jpg",
    "img/banner/banner_2.jpg",
    "img/banner/banner_3.jpg",
    "img/banner/banner_4.jpg",
];

function bannerIndex() { return Math.round(Math.random() * (bannerImgUrl.length - 1)); }

function startBanner() {
    var banner = document.getElementById('banner-1');
    banner.style.backgroundImage = "url(" + bannerImgUrl[bannerIndex()] + ")";
    banner.style.backgroundRepeat = "no-repeat no-repeat";
    banner.style.backgroundPosition = "center";
    banner.style.backgroundSize = "60% 60%";
    setTimeout(startBanner, 10000);
}

