/*
 * Created by Michael Efremov on 05.10.20 16:35
 */

package app.documents.core.network.manager.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.editors.manager.mvp.models.base.Base;
import app.editors.manager.mvp.models.base.Download;

public class ResponseDownload extends Base {

    @SerializedName(KEY_RESPONSE)
    @Expose
    private List<Download> response;

    public List<Download> getResponse() {
        return response;
    }

    public void setResponse(List<Download> response) {
        this.response = response;
    }

}
