package app.editors.manager.mvp.models.list;

import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.ui.adapters.holders.factory.TypeFactory;

public class Footer implements Entity {

    @Override
    public int getType(TypeFactory factory) {
        return factory.type(this);
    }
}
