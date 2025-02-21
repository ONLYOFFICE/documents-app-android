package app.documents.core.model.login

data class Email(override val id: String) : Member {

    override val shared: Boolean
        get() = false
}