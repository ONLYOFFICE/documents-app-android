package app.editors.manager.mvp.models.list;


import java.io.Serializable;

import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.ui.adapters.holders.factory.TypeFactory;

public class Header implements Serializable, Entity {

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
}
