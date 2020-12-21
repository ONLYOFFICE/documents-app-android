package app.editors.manager.ui.adapters.holders.factory;

import android.view.View;

import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.models.explorer.UploadFile;
import app.editors.manager.mvp.models.list.Footer;
import app.editors.manager.mvp.models.list.Header;
import app.editors.manager.ui.adapters.ExplorerAdapter;
import app.editors.manager.ui.adapters.holders.BaseViewHolderExplorer;

public interface TypeFactory {

    int type(File file);
    int type(Folder folder);
    int type(Header header);
    int type(Footer header);
    int type(UploadFile uploadFile);

    BaseViewHolderExplorer createViewHolder(View parent, int type, ExplorerAdapter adapter);
}
