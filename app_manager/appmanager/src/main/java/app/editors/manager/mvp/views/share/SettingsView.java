package app.editors.manager.mvp.views.share;

import android.content.Intent;

import androidx.annotation.Nullable;

import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

import java.util.List;

import app.editors.manager.mvp.models.base.Entity;
import app.editors.manager.mvp.models.explorer.Item;
import app.editors.manager.mvp.models.share.Share;
import app.editors.manager.mvp.views.base.BaseViewExt;
import app.editors.manager.ui.views.custom.PlaceholderViews;


@StateStrategyType(OneExecutionStateStrategy.class)
public interface SettingsView extends BaseViewExt {
    void onGetShare(List<Entity> list, int accessCode);
    void onRemove(Share share, int sharePosition);
    void onGetShareItem(Entity entity, int mSharePosition, int access);
    void onExternalAccess(int accessCode, boolean isMessage);
    void onInternalLink(@Nullable String internalLink);
    void onItemType(boolean isFolder);
    void onAddShare(Item item);
    void onPlaceholderState(PlaceholderViews.Type type);
    void onActionButtonState(boolean isVisible);
    void onResultState(boolean isShared);
    void onSendLink(Intent intent);
    void onButtonState(boolean state);
    void onPopupState(boolean state);
    void onShowPopup(int mSharePosition);
}