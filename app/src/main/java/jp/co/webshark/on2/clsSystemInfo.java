package jp.co.webshark.on2;

/**
 * Created by takashi on 2015/09/17.
 */
public class clsSystemInfo {
    String newestVersion;
    String forceUpdateVersion;
    String storeUrl;
    String d064BaseUrl;
    String d064UrlParams;

    public int getNewestVersion() {
        return Integer.parseInt(newestVersion);
    }

    public void setNewestVersion(String newestVersion) {
        this.newestVersion = newestVersion;
    }

    public int getForceUpdateVersion() {
        return Integer.parseInt(forceUpdateVersion);
    }
    public void setForceUpdateVersion(String forceUpdateVersion) {
        this.forceUpdateVersion = forceUpdateVersion;
    }

    public String getStoreUrl() {
        return storeUrl;
    }
    public void setStoreUrl(String storeUrl) {
        this.storeUrl = storeUrl;
    }

    public String getD064BaseUrl() {
        return d064BaseUrl;
    }
    public void setD064BaseUrl(String d064BaseUrl) {
        this.d064BaseUrl = d064BaseUrl;
    }

    public String getD064UrlParams() {
        return d064UrlParams;
    }
    public void setD064UrlParams(String d064UrlParams) {
        this.d064UrlParams = d064UrlParams;
    }

}
