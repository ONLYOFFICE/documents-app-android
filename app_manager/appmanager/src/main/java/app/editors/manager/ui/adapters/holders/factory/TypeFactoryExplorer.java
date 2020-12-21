package app.editors.manager.ui.adapters.holders.factory;

import android.view.View;

import app.editors.manager.mvp.models.explorer.File;
import app.editors.manager.mvp.models.explorer.Folder;
import app.editors.manager.mvp.models.explorer.UploadFile;
import app.editors.manager.mvp.models.list.Footer;
import app.editors.manager.mvp.models.list.Header;
import app.editors.manager.ui.adapters.ExplorerAdapter;
import app.editors.manager.ui.adapters.holders.BaseViewHolderExplorer;
import app.editors.manager.ui.adapters.holders.FileViewHolder;
import app.editors.manager.ui.adapters.holders.FolderViewHolder;
import app.editors.manager.ui.adapters.holders.FooterViewHolder;
import app.editors.manager.ui.adapters.holders.HeaderViewHolder;
import app.editors.manager.ui.adapters.holders.UploadFileViewHolder;

public class TypeFactoryExplorer implements TypeFactory {

    private static TypeFactoryExplorer mTypeFactoryExplorer;

    private TypeFactoryExplorer(){
    }

    public static TypeFactoryExplorer getFactory(){
        if (mTypeFactoryExplorer == null){
            mTypeFactoryExplorer = new TypeFactoryExplorer();
        }
        return mTypeFactoryExplorer;
    }

    @Override
    public int type(File file) {
        return FileViewHolder.LAYOUT;
    }

    @Override
    public int type(Folder folder) {
        return FolderViewHolder.LAYOUT;
    }

    @Override
    public int type(Header header) {
        return HeaderViewHolder.LAYOUT;
    }

    @Override
    public int type(Footer header) {
        return FooterViewHolder.LAYOUT;
    }

    @Override
    public int type(UploadFile uploadFile) { return UploadFileViewHolder.LAYOUT; }

    @Override
    public BaseViewHolderExplorer createViewHolder(View parent, int type, ExplorerAdapter adapter) {
        BaseViewHolderExplorer viewHolder;
        switch (type) {
            case FileViewHolder.LAYOUT:
                viewHolder = new FileViewHolder(parent, adapter);
                break;
            case FolderViewHolder.LAYOUT:
                viewHolder = new FolderViewHolder(parent, adapter);
                break;
            case HeaderViewHolder.LAYOUT:
                viewHolder = new HeaderViewHolder(parent, adapter);
                break;
            case FooterViewHolder.LAYOUT:
                viewHolder = new FooterViewHolder(parent, adapter);
                break;
            case UploadFileViewHolder.LAYOUT:
                viewHolder = new UploadFileViewHolder(parent, adapter);
                break;
            default:
                throw new RuntimeException("Unknown type is unacceptable: " + type);
        }
        return viewHolder;
    }
}
