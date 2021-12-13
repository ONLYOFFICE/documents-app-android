package app.editors.manager.mvp.models.list;


import java.io.Serializable;

import app.editors.manager.R;
import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.ui.adapters.holders.factory.TypeFactory;
import lib.toolkit.base.ui.adapters.holder.ViewType;

public class Header implements Serializable, Entity, ViewType {

    private String title = "";

    public Header() {}

    public Header(final String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int getType(TypeFactory factory) {
        return factory.type(this);
    }

    @Override
    public int getViewType() {
        return R.layout.list_explorer_header;
    }
}
