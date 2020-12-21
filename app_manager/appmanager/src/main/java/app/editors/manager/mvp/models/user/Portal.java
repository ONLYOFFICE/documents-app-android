package app.editors.manager.mvp.models.user;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import app.editors.manager.mvp.models.base.ItemProperties;

public class Portal extends ItemProperties implements Serializable {

    @SerializedName("tenantId")
    @Expose
    private Integer tenantId;
    @SerializedName("tenantAlias")
    @Expose
    private String tenantAlias;
    @SerializedName("mappedDomain")
    @Expose
    private Object mappedDomain;
    @SerializedName("version")
    @Expose
    private Integer version;
    @SerializedName("versionChanged")
    @Expose
    private String versionChanged;
    @SerializedName("tenantDomain")
    @Expose
    private String tenantDomain;
    @SerializedName("hostedRegion")
    @Expose
    private Object hostedRegion;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("language")
    @Expose
    private String language;
    @SerializedName("trustedDomains")
    @Expose
    private List<Object> trustedDomains = null;
    @SerializedName("trustedDomainsType")
    @Expose
    private Integer trustedDomainsType;
    @SerializedName("ownerId")
    @Expose
    private String ownerId;
    @SerializedName("createdDateTime")
    @Expose
    private String createdDateTime;
    @SerializedName("lastModified")
    @Expose
    private String lastModified;
    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("statusChangeDate")
    @Expose
    private String statusChangeDate;
    @SerializedName("partnerId")
    @Expose
    private Object partnerId;
    @SerializedName("affiliateId")
    @Expose
    private Object affiliateId;
    @SerializedName("paymentId")
    @Expose
    private Object paymentId;
    @SerializedName("industry")
    @Expose
    private Integer industry;
    @SerializedName("spam")
    @Expose
    private Boolean spam;
    @SerializedName("calls")
    @Expose
    private Boolean calls;

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantAlias() {
        return tenantAlias;
    }

    public void setTenantAlias(String tenantAlias) {
        this.tenantAlias = tenantAlias;
    }

    public Object getMappedDomain() {
        return mappedDomain;
    }

    public void setMappedDomain(Object mappedDomain) {
        this.mappedDomain = mappedDomain;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getVersionChanged() {
        return versionChanged;
    }

    public void setVersionChanged(String versionChanged) {
        this.versionChanged = versionChanged;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public Object getHostedRegion() {
        return hostedRegion;
    }

    public void setHostedRegion(Object hostedRegion) {
        this.hostedRegion = hostedRegion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<Object> getTrustedDomains() {
        return trustedDomains;
    }

    public void setTrustedDomains(List<Object> trustedDomains) {
        this.trustedDomains = trustedDomains;
    }

    public Integer getTrustedDomainsType() {
        return trustedDomainsType;
    }

    public void setTrustedDomainsType(Integer trustedDomainsType) {
        this.trustedDomainsType = trustedDomainsType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(String createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getStatusChangeDate() {
        return statusChangeDate;
    }

    public void setStatusChangeDate(String statusChangeDate) {
        this.statusChangeDate = statusChangeDate;
    }

    public Object getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Object partnerId) {
        this.partnerId = partnerId;
    }

    public Object getAffiliateId() {
        return affiliateId;
    }

    public void setAffiliateId(Object affiliateId) {
        this.affiliateId = affiliateId;
    }

    public Object getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Object paymentId) {
        this.paymentId = paymentId;
    }

    public Integer getIndustry() {
        return industry;
    }

    public void setIndustry(Integer industry) {
        this.industry = industry;
    }

    public Boolean getSpam() {
        return spam;
    }

    public void setSpam(Boolean spam) {
        this.spam = spam;
    }

    public Boolean getCalls() {
        return calls;
    }

    public void setCalls(Boolean calls) {
        this.calls = calls;
    }
}
