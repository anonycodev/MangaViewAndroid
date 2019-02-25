package ml.melun.mangaview.customViews;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.AbstractDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;

import static ml.melun.mangaview.Utils.convertUri;

public class MangaDraweeView extends SimpleDraweeView{
    private int type = -1;
    public MangaDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
    }

    private final ControllerListener listener = new BaseControllerListener<ImageInfo>() {
        @Override
        public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
            updateViewSize(imageInfo);
        }

        @Override
        public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
            updateViewSize(imageInfo);
        }
    };

    public MangaDraweeView(Context context) {
        super(context);
    }

    public MangaDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MangaDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MangaDraweeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        DraweeController controller = ((PipelineDraweeControllerBuilder)getControllerBuilder())
                .setControllerListener(listener)
                .setUri(convertUri(uri.getPath()))
                .setOldController(getController())
                .build();
        setController(controller);
    }

    @Override
    public void setImageURI(@Nullable String uriString) {
        DraweeController controller = ((PipelineDraweeControllerBuilder)getControllerBuilder())
                .setControllerListener(listener)
                .setUri(convertUri(uriString))
                .setOldController(getController())
                .build();
        setController(controller);
    }

    @Override
    public void setImageURI(@Nullable String uriString, @Nullable Object callerContext) {
        DraweeController controller = ((PipelineDraweeControllerBuilder)getControllerBuilder())
                .setControllerListener(listener)
                .setCallerContext(callerContext)
                .setUri(convertUri(uriString))
                .setOldController(getController())
                .build();
        setController(controller);
    }

    @Override
    public void setImageURI(Uri uri, Object callerContext) {
        DraweeController controller = ((PipelineDraweeControllerBuilder)getControllerBuilder())
                .setControllerListener(listener)
                .setCallerContext(callerContext)
                .setUri(convertUri(uri.getPath()))
                .setOldController(getController())
                .build();
        setController(controller);

    }

    void updateViewSize(@Nullable ImageInfo info) {
        if(info!=null){
            setAspectRatio(((float)info.getWidth()) / ((float)info.getHeight()));
        }
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
