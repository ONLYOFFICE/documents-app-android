
package app.documents.core.network.webdav.models;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.util.Date;
import java.util.List;

public class WebDavModel {

    @ElementList(name = "multistatus", entry = "response", inline = true, required = false)
    private List<ResponseBean> list;

    public List<ResponseBean> getList() {
        return list;
    }

    public void setList(List<ResponseBean> list) {
        this.list = list;
    }

    @Root(name = "response", strict = false)
    public static class ResponseBean {
        @Element(required = false)
        private String href;

        @Path("propstat")
        @Element(required = false)
        private String status;

        @Path("propstat/prop")
        @Element(required = false, name = "getlastmodified")
        private String lastModified;

        @Path("propstat/prop")
        @Element(required = false, name = "getcontentlength")
        private String contentLength;

        @Path("propstat/prop")
        @Element(required = false)
        private String owner;

        @Path("propstat/prop")
        @Element(required = false, name = "getcontenttype")
        private String contentType;

        @Path("propstat/prop")
        @Element(required = false, name = "displayname")
        private String displayName;

        @Path("propstat/prop")
        @Element(required = false, name = "getetag")
        private String etag;


        public Date getLastModifiedDate() {
            if (lastModified == null) {
                return new Date();
            } else {
                return new Date(lastModified);
            }
        }


        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getLastModified() {
            return lastModified;
        }

        public void setLastModified(String lastModified) {
            this.lastModified = lastModified;
        }

        public String getContentLength() {
            return contentLength;
        }

        public void setContentLength(String contentLength) {
            this.contentLength = contentLength;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getEtag() {
            return etag;
        }

        public void setEtag(String etag) {
            this.etag = etag;
        }

        public boolean isDir() {
            return contentLength == null || "httpd/unix-directory".equals(contentType) || href.endsWith("/");
        }
    }
}
