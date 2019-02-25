package ml.melun.mangaview.customViews;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.facebook.drawee.controller.AbstractDraweeControllerBuilder;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;

import static ml.melun.mangaview.Utils.convertUri;

public class UriDraweeView extends SimpleDraweeView {
    public UriDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
    }

    public UriDraweeView(Context context) {
        super(context);
    }

    public UriDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UriDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public UriDraweeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected AbstractDraweeControllerBuilder getControllerBuilder() {
        return super.getControllerBuilder();
    }

    @Override
    public void setImageRequest(ImageRequest request) {
        super.setImageRequest(request);
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(convertUri(uri.getPath()));
    }

    @Override
    public void setImageURI(@Nullable String uriString) {
        super.setImageURI(convertUri(uriString));
    }

    @Override
    public void setImageURI(Uri uri, @Nullable Object callerContext) {
        super.setImageURI(convertUri(uri.getPath()), callerContext);
    }

    @Override
    public void setImageURI(@Nullable String uriString, @Nullable Object callerContext) {
        super.setImageURI(convertUri(uriString), callerContext);
    }

    @Override
    public void setActualImageResource(int resourceId) {
        super.setActualImageResource(resourceId);
    }

    @Override
    public void setActualImageResource(int resourceId, @Nullable Object callerContext) {
        super.setActualImageResource(resourceId, callerContext);
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
    }
}
