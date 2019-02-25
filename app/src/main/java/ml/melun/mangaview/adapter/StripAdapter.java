package ml.melun.mangaview.adapter;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;

import java.util.List;

import ml.melun.mangaview.Preference;
import ml.melun.mangaview.R;
import ml.melun.mangaview.customViews.UriDraweeView;
import ml.melun.mangaview.mangaview.Decoder;
import ml.melun.mangaview.mangaview.MSMPostProcessor;

import static ml.melun.mangaview.Utils.convertUri;

//todo: autocut 부드럽게 (ex: insert image whenever image is splitted)
//todo: viewer2에서 이미지 로드시 애니메이션 없애기


public class StripAdapter extends RecyclerView.Adapter<StripAdapter.ViewHolder> {

    private List<String> imgs;
    private LayoutInflater mInflater;
    private Context mainContext;
    private StripAdapter.ItemClickListener mClickListener;
    Boolean autoCut = false;
    Boolean reverse;
    int __seed;
    Decoder d;
    int width;


    // data is passed into the constructor
    public StripAdapter(Context context, List<String> data, Boolean cut, int seed, int id, int width) {
        this.mInflater = LayoutInflater.from(context);
        mainContext = context;
        this.imgs = data;
        autoCut = cut;
        reverse = new Preference(context).getReverse();
        __seed = seed;
        d = new Decoder(seed, id);
        this.width = width;
    }

    public void removeAll(){
        imgs.clear();
        notifyDataSetChanged();
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_strip, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int pos) {
        int type = -1;
        int position = pos;
        if(autoCut){
            //type : 0/1
            type = pos%2;
            position = pos/2;
        }
        Postprocessor postprocessor = new MSMPostProcessor(d,type,width);
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(convertUri(imgs.get(position)))
                .setPostprocessor(postprocessor)
                .build();
        ControllerListener listener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
                holder.refresh.setVisibility(View.GONE);
                if(imageInfo.getHeight()<2){
                    holder.frame.setVisibility(View.GONE);
                }else{
                    holder.frame.setVisibility(View.VISIBLE);
                    //change imageview layoutparams
                    updateFrameSize(imageInfo, holder);
                }
            }

            @Override
            public void onSubmit(String id, Object callerContext) {
                super.onSubmit(id, callerContext);
                holder.refresh.setVisibility(View.GONE);
            }

            @Override
            public void onIntermediateImageFailed(String id, Throwable throwable) {
                super.onIntermediateImageFailed(id, throwable);
                holder.refresh.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                super.onFailure(id, throwable);
                holder.refresh.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                if(imageInfo.getHeight()<2){
                    holder.frame.setVisibility(View.GONE);
                    holder.refresh.setVisibility(View.GONE);
                }else{
                    holder.frame.setVisibility(View.VISIBLE);
                    holder.refresh.setVisibility(View.GONE);
                    //change imageview layoutparams
                    updateFrameSize(imageInfo, holder);
                }
            }
        };

        holder.frame.setController(Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest)
                .setOldController(holder.frame.getController())
                .setControllerListener(listener)
                .build());

        preload(position);
    }


    // total number of rows
    @Override
    public int getItemCount() {
        if(autoCut) return imgs.size()*2;
        else return imgs.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        SimpleDraweeView frame;
        ImageButton refresh;
        ViewHolder(View itemView) {
            super(itemView);
            frame = itemView.findViewById(R.id.frame);
            refresh = itemView.findViewById(R.id.refreshButton);
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //refresh image
                    notifyItemChanged(getAdapterPosition());
                }
            });
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick();
        }
    }

    // allows clicks events to be caught
    public void setClickListener(StripAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick();
    }

    private void updateFrameSize(ImageInfo info, ViewHolder holder){
        if(info!=null){
            holder.frame.setAspectRatio(((float)info.getWidth()) / ((float)info.getHeight()));
        }
    }

    void preload(int pos){
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        if(pos<imgs.size()-1) {
            String img = imgs.get(pos+1);
            ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(convertUri(img))
                    .build();
            imagePipeline.prefetchToDiskCache(imageRequest, mainContext);
        }
        if(pos>0){
            String img = imgs.get(pos-1);
            ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(convertUri(img))
                    .build();
            imagePipeline.prefetchToDiskCache(imageRequest, mainContext);

        }
    }
}

