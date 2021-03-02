package uk.gov.hmcts.ccd.domain.model.definition;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;


public class BannersResult implements Serializable {

    private List<Banner> banners;

    public BannersResult() {

    }

    public BannersResult(List<Banner> banners) {
        this.banners = banners;
    }


    @JsonProperty("banners")
    public List<Banner> getBanners() {
        return banners;
    }

    public void setBanners(List<Banner> banners) {
        this.banners = banners;
    }
}
