package app.editors.manager.mvp.models.base;


import java.io.Serializable;

import app.editors.manager.ui.adapters.holders.factory.TypeFactory;

public class ItemProperties implements Serializable, Entity, Cloneable {

    private boolean isSelected = false;

    private boolean isJustCreated = false;

    private boolean isReadOnly = false;

    private boolean isClicked = false;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isJustCreated() {
        return isJustCreated;
    }

    public void setJustCreated(boolean justCreated) {
        isJustCreated = justCreated;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }

    public boolean isClicked() {
        return isClicked;
    }

    public void setClicked(boolean clicked) {
        isClicked = clicked;
    }

    @Override
    public ItemProperties clone() throws CloneNotSupportedException {
        try {
            return (ItemProperties) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public int getType(TypeFactory factory) {
        return 0;
    }
}
