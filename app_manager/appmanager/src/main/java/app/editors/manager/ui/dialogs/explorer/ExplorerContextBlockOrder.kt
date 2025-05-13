package app.editors.manager.ui.dialogs.explorer

sealed interface ExplorerContextBlockOrder {
    val order: Int

    interface Header : ExplorerContextBlockOrder {
        override val order: Int get() = 0
    }

    interface Open : ExplorerContextBlockOrder {
        override val order: Int get() = 1
    }

    interface Common : ExplorerContextBlockOrder {
        override val order: Int get() = 2
    }

    interface Operation : ExplorerContextBlockOrder {
        override val order: Int get() = 3
    }

    interface Remove : ExplorerContextBlockOrder {
        override val order: Int get() = 4
    }
}