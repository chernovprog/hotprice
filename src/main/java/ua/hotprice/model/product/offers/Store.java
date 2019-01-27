package ua.hotprice.model.product.offers;

import javax.persistence.*;

@Entity
@Table(name = "stores")
public class Store {    // StoreNamesMatching
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String storeNameHotlineVersion;
    @Column
    private String storeNamePriceuaVersion;

    public Store() {
    }

    public Store(String storeNameHotlineVersion, String storeNamePriceuaVersion) {
        this.storeNameHotlineVersion = storeNameHotlineVersion;
        this.storeNamePriceuaVersion = storeNamePriceuaVersion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStoreNameHotlineVersion() {
        return storeNameHotlineVersion;
    }

    public void setStoreNameHotlineVersion(String storeNameHotlineVersion) {
        this.storeNameHotlineVersion = storeNameHotlineVersion;
    }

    public String getStoreNamePriceuaVersion() {
        return storeNamePriceuaVersion;
    }

    public void setStoreNamePriceuaVersion(String storeNamePriceuaVersion) {
        this.storeNamePriceuaVersion = storeNamePriceuaVersion;
    }
}
