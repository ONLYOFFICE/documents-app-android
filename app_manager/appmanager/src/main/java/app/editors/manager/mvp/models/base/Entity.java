package app.editors.manager.mvp.models.base;


import app.editors.manager.ui.adapters.holders.factory.TypeFactory;

/*
* Marker
* */
public interface Entity {
    int getType(TypeFactory factory);
}
