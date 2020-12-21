package app.editors.manager.ui.binders;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;

import app.editors.manager.R;

public class ProfileItemBinder {

    private WeakReference<AppCompatTextView> mTitle;
    private WeakReference<AppCompatTextView> mText;
    private WeakReference<AppCompatImageView> mImage;

    public ProfileItemBinder(@NonNull View view) {
        mTitle = new WeakReference<>(view.findViewById(R.id.title));
        mText = new WeakReference<>(view.findViewById(R.id.text));
        mImage = new WeakReference<>(view.findViewById(R.id.image));
    }

    public ProfileItemBinder setTitle(@StringRes int string) {
        mTitle.get().setText(string);
        return this;
    }

    public ProfileItemBinder setTitle(String string) {
        mTitle.get().setText(string);
        return this;
    }

    public ProfileItemBinder setText(@StringRes int string) {
        mText.get().setText(string);
        return this;
    }

    public ProfileItemBinder setText(String string) {
        mText.get().setText(string);
        return this;
    }

    public ProfileItemBinder setImage(@DrawableRes int drawable) {
        mImage.get().setImageDrawable(ContextCompat.getDrawable(mImage.get().getContext(), drawable));
        return this;
    }

    public ProfileItemBinder setImage(Drawable drawable) {
        mImage.get().setImageDrawable(drawable);
        return this;
    }

    public String getText() {
        return mText.get().getText().toString();
    }
}
