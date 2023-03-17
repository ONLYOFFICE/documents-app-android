package app.documents.core.network.manager.models.base

import java.io.Serializable

open class ItemProperties : Entity, Serializable, Cloneable {
    var isSelected = false
    var isJustCreated = false
    var isReadOnly = false
    var isClicked = false

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): ItemProperties {
        return super.clone() as ItemProperties
    }

}