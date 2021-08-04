package app.documents.core.webdav

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Path
import org.simpleframework.xml.Root
import java.util.*

data class WebDavModel constructor(
    @field:ElementList(name = "multistatus", entry = "response", inline = true, required = false)
    var list: List<ResponseBean>? = null
)

@Root(name = "response", strict = false)
data class ResponseBean constructor(
    @field:Element(required = false)
    var href: String? = null,
    @Path("propstat")
    @field:Element(required = false)
    var status: String? = null,
    @Path("propstat/prop")
    @field:Element(required = false, name = "getlastmodified")
    var lastModified: String? = null,
    @Path("propstat/prop")
    @field:Element(required = false, name = "getcontentlength")
    var contentLength: String? = null,
    @Path("propstat/prop")
    @field:Element(required = false)
    var owner: String? = null,
    @Path("propstat/prop")
    @field:Element(required = false, name = "getcontenttype")
    var contentType: String? = null,
    @Path("propstat/prop")
    @field:Element(required = false, name = "displayname")
    var displayName: String? = null,
    @Path("propstat/prop")
    @field:Element(required = false, name = "getetag")
    var etag: String? = null,
    @Path("propstat/prop")
    @field:Element(required = false, name = "resourcetype")
    var resourceType: String? = null
) {

    val lastModifiedDate: Date
        get() = if (lastModified == null) {
            Date()
        } else {
            Date(lastModified)
        }

    val isDir: Boolean
        get() = "httpd/unix-directory" == contentType || contentType == null
}