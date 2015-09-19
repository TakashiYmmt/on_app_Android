package jp.co.webshark.on2;

/**
 * Created by takashi on 2015/09/17.
 */
public class clsSystemInfo {
    String newestVersion;
    String forceUpdateVersion;
    String storeUrl;

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

}
