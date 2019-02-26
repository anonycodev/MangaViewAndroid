package ml.melun.mangaview.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import ml.melun.mangaview.Preference;
import ml.melun.mangaview.R;
import ml.melun.mangaview.adapter.StripAdapter;
import ml.melun.mangaview.customViews.WrapContentDraweeView;
import ml.melun.mangaview.mangaview.Decoder;
import ml.melun.mangaview.mangaview.Manga;
import ml.melun.mangaview.mangaview.Title;

import static ml.melun.mangaview.Utils.convertUri;
import static ml.melun.mangaview.Utils.cutBitmap;
import static ml.melun.mangaview.Utils.getSample;
import static ml.melun.mangaview.Utils.getScreenSize;

public class ViewerActivity2 extends AppCompatActivity {
    Preference p;
    Boolean dark, volumeControl, toolbarshow=true, reverse, touch=true, online, stretch;
    Context context = this;
    String name;
    int id;
    Manga manga;
    ImageButton next, prev, commentBtn;
    android.support.v7.widget.Toolbar toolbar;
    Button pageBtn, nextPageBtn, prevPageBtn, touchToggleBtn;
    AppBarLayout appbar, appbarBottom;
    TextView toolbarTitle;
    int viewerBookmark = -1;
    List<String> imgs;
    ProgressDialog pd;
    List<Manga> eps;
    int index;
    Title title;
    SimpleDraweeView frame;
    int type=-1;
    Intent result;
    AlertDialog.Builder alert;
    Spinner spinner;
    int screenWidth = 0;
    ControllerListener listener;

    Decoder d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        p = new Preference(this);
        dark = p.getDarkTheme();
        if(dark) setTheme(R.style.AppThemeDarkNoTitle);
        else setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer2);

        next = this.findViewById(R.id.toolbar_next);
        prev = this.findViewById(R.id.toolbar_previous);
        toolbar = this.findViewById(R.id.viewerToolbar);
        appbar = this.findViewById(R.id.viewerAppbar);
        toolbarTitle = this.findViewById(R.id.toolbar_title);
        appbarBottom = this.findViewById(R.id.viewerAppbarBottom);
        volumeControl = p.getVolumeControl();
        reverse = p.getReverse();
        frame = this.findViewById(R.id.viewer_image);
        pageBtn = this.findViewById(R.id.viewerBtn1);
        pageBtn.setText("-/-");
        nextPageBtn = this.findViewById(R.id.nextPageBtn);
        prevPageBtn = this.findViewById(R.id.prevPageBtn);
        touchToggleBtn = this.findViewById(R.id.viewerBtn2);
        touchToggleBtn.setText("입력 제한");
        commentBtn = this.findViewById(R.id.commentButton);
        spinner = this.findViewById(R.id.toolbar_spinner);
        stretch = p.getStretch();
        if(stretch) frame.setScaleType(ImageView.ScaleType.FIT_XY);
        screenWidth = getScreenSize(getWindowManager().getDefaultDisplay());

        Intent intent = getIntent();

        manga = new Gson().fromJson(intent.getStringExtra("manga"),new TypeToken<Manga>(){}.getType());
        title = new Gson().fromJson(intent.getStringExtra("title"),new TypeToken<Title>(){}.getType());

        online = intent.getBooleanExtra("online",true);
        name = manga.getName();
        id = manga.getId();

        toolbarTitle.setText(name);
        viewerBookmark = p.getViewerBookmark(id);


        if(intent.getBooleanExtra("recent",false)){
            Intent resultIntent = new Intent();
            setResult(RESULT_OK,resultIntent);
        }
        if(!online) {
            //load local imgs
            //appbarBottom.setVisibility(View.GONE);
            next.setVisibility(View.GONE);
            prev.setVisibility(View.GONE);
            if(id>-1){
                //if manga has id = manga has title = update bookmark and add to recent
                p.addRecent(title);
                p.setBookmark(title.getName(),id);
            }
            imgs = manga.getImgs();
            commentBtn.setVisibility(View.GONE);
            d = new Decoder(manga.getSeed(), manga.getId());
            refreshImage();
        }else{
            //if online
            //fetch imgs
            loadImages l = new loadImages();
            l.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        nextPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(touch) nextPage();
            }
        });
        prevPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(touch) prevPage();
            }
        });
        touchToggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(touch) {
                    touch = false;
                    touchToggleBtn.setBackgroundResource(R.drawable.button_bg_on);
                }
                else{
                    touch = true;
                    touchToggleBtn.setBackgroundResource(R.drawable.button_bg);
                }
            }
        });

        pageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dark) alert = new AlertDialog.Builder(context,R.style.darkDialog);
                else alert = new AlertDialog.Builder(context);

                alert.setTitle("페이지 선택\n(1~"+imgs.size()+")");
                final EditText input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setRawInputType(Configuration.KEYBOARD_12KEY);
                alert.setView(input);
                alert.setPositiveButton("이동", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int button) {
                        //이동 시
                        if(input.getText().length()>0) {
                            int page = Integer.parseInt(input.getText().toString());
                            if (page < 1) page = 1;
                            if (page > imgs.size()) page = imgs.size();
                            viewerBookmark = page - 1;
                            refreshImage();
                        }
                    }
                });
                alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int button) {
                        //취소 시
                    }
                });
                alert.show();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(index>0) {
                    index--;
                    manga = eps.get(index);
                    id = manga.getId();
                    loadImages l = new loadImages();
                    l.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(index<eps.size()-1) {
                    index++;
                    manga = eps.get(index);
                    id = manga.getId();
                    loadImages l = new loadImages();
                    l.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });

        View.OnLongClickListener tbToggle = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //touched = true;
                toggleToolbar();
                return true;
            }
        };
        nextPageBtn.setOnLongClickListener(tbToggle);
        prevPageBtn.setOnLongClickListener(tbToggle);

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentActivity = new Intent(context, CommentsActivity.class);
                //create gson and put extra
                Gson gson = new Gson();
                commentActivity.putExtra("comments", gson.toJson(manga.getComments()));
                commentActivity.putExtra("bestComments", gson.toJson(manga.getBestComments()));
                startActivity(commentActivity);
            }
        });

        listener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
                updateFrameSize(imageInfo);
            }

            @Override
            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                updateFrameSize(imageInfo);
            }
        };

    }

    void nextPage(){
        if(viewerBookmark==imgs.size()-1 && ( type==-1 || type==1)){
            //end of manga
        }else if(type==0){
            //is two page, current pos: right
            //dont add page
            //only change type
            type = 1;
            final String image = imgs.get(viewerBookmark);
            //placeholder
            //frame.setImageResource(R.drawable.placeholder);
            Postprocessor postprocessor = new BasePostprocessor() {
                @Override
                public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
                    int width = sourceBitmap.getWidth();
                    int height = sourceBitmap.getHeight();
                    CloseableReference<Bitmap> bitref =  bitmapFactory.createBitmap(cutBitmap(getSample(d.decode(sourceBitmap), screenWidth),type));
                    bitmapFactory.createBitmap(d.decode(sourceBitmap));
                    return CloseableReference.cloneOrNull(bitref);
                }
            };

            ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(convertUri(image))
                    .setPostprocessor(postprocessor)
                    .build();

            frame.setController(Fresco.newDraweeControllerBuilder()
                    .setImageRequest(imageRequest)
                    .setOldController(frame.getController())
                    .setControllerListener(listener)
                    .build());
        }else{
            //is single page OR unidentified
            //add page
            //has to check if twopage
            viewerBookmark++;
            final String image = imgs.get(viewerBookmark);
            //placeholder
            //frame.setImageResource(R.drawable.placeholder);

            Postprocessor postprocessor = new BasePostprocessor() {
                @Override
                public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
                    int width = sourceBitmap.getWidth();
                    int height = sourceBitmap.getHeight();
                    if(width>height) type = 0;
                    else type = -1;
                    CloseableReference<Bitmap> bitref =  bitmapFactory.createBitmap(cutBitmap(getSample(d.decode(sourceBitmap), screenWidth),type));
                    bitmapFactory.createBitmap(getSample(d.decode(sourceBitmap), screenWidth));
                    return CloseableReference.cloneOrNull(bitref);
                }
            };
            ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(convertUri(image))
                    .setPostprocessor(postprocessor)
                    .build();

            DataSource<CloseableReference<CloseableImage>> dataSource =
                    Fresco.getImagePipeline().fetchImageFromBitmapCache(imageRequest, context);
            try {
                CloseableReference<CloseableImage> imageReference = dataSource.getResult();
                if (imageReference != null) {
                    try {
                        // Do something with the image, but do not keep the reference to it!
                        // The image may get recycled as soon as the reference gets closed below.
                        // If you need to keep a reference to the image, read the following sections.
                    } finally {
                        CloseableReference.closeSafely(imageReference);
                    }
                } else {
                    // cache miss

                }
            } finally {
                dataSource.close();
            }

            frame.setController(Fresco.newDraweeControllerBuilder()
                    .setImageRequest(imageRequest)
                    .setOldController(frame.getController())
                    .setControllerListener(listener)
                    .build());


//            Glide.with(context)
//                    .asBitmap()
//                    .load(image)
//                    .into(new CustomTarget<Bitmap>() {
//                        @Override
//                        public void onLoadCleared(@Nullable Drawable placeholder) {
//                            //
//                        }
//
//                        @Override
//                        public void onResourceReady(Bitmap bitmap,
//                                                    Transition<? super Bitmap> transition) {
//                            Bitmap sample = getSample(d.decode(bitmap),screenWidth);
//                            int screenWidth = sample.getWidth();
//                            int height = sample.getHeight();
//                            if(screenWidth>height){
//                                imgCache = sample;
//                                type=0;
//                                if(reverse) frame.setImageBitmap(Bitmap.createBitmap(imgCache,0,0,screenWidth/2,height));
//                                else frame.setImageBitmap(Bitmap.createBitmap(imgCache,screenWidth/2,0,screenWidth/2,height));
//                            }else{
//                                type=-1;
//                                frame.setImageBitmap(sample);
//                            }
//                            preload();
//                        }
//                    });
        }
        updatePageIndex();
        preload();
    }
    void prevPage(){
        if(viewerBookmark==0 && (type==-1 || type==0)){
            //start of manga
        } else if(type==1){
            //is two page, current pos: left
            type = 0;
            final String image = imgs.get(viewerBookmark);
            //placeholder
            //frame.setImageResource(R.drawable.placeholder);
            Postprocessor postprocessor = new BasePostprocessor() {
                @Override
                public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
                    int width = sourceBitmap.getWidth();
                    int height = sourceBitmap.getHeight();
                    CloseableReference<Bitmap> bitref =  bitmapFactory.createBitmap(cutBitmap(getSample(d.decode(sourceBitmap), screenWidth),type));
                    bitmapFactory.createBitmap(getSample(d.decode(sourceBitmap), screenWidth));
                    return CloseableReference.cloneOrNull(bitref);
                }
            };
            ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(convertUri(image))
                    .setPostprocessor(postprocessor)
                    .build();

            frame.setController(Fresco.newDraweeControllerBuilder()
                    .setImageRequest(imageRequest)
                    .setOldController(frame.getController())
                    .setControllerListener(listener)
                    .build());
        }else{
            //is single page OR unidentified
            //decrease page
            //has to check if twopage
            viewerBookmark--;
            final String image = imgs.get(viewerBookmark);
            //placeholder
            //frame.setImageResource(R.drawable.placeholder);
            Postprocessor postprocessor = new BasePostprocessor() {
                @Override
                public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
                    int width = sourceBitmap.getWidth();
                    int height = sourceBitmap.getHeight();
                    if(width>height) type = 1;
                    else type = -1;
                    CloseableReference<Bitmap> bitref =  bitmapFactory.createBitmap(cutBitmap(getSample(d.decode(sourceBitmap), screenWidth),type));
                    bitmapFactory.createBitmap(getSample(d.decode(sourceBitmap), screenWidth));
                    return CloseableReference.cloneOrNull(bitref);
                }
            };
            ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(convertUri(image))
                    .setPostprocessor(postprocessor)
                    .build();

            frame.setController(Fresco.newDraweeControllerBuilder()
                    .setImageRequest(imageRequest)
                    .setOldController(frame.getController())
                    .setControllerListener(listener)
                    .build());

//            Glide.with(context)
//                    .asBitmap()
//                    .load(image)
//                    .into(new CustomTarget<Bitmap>() {
//                        @Override
//                        public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
//                            Bitmap sample = getSample(d.decode(bitmap), screenWidth);
//                            int screenWidth = sample.getWidth();
//                            int height = sample.getHeight();
//                            if(screenWidth>height){
//                                imgCache = sample;
//                                type=1;
//                                if(reverse) frame.setImageBitmap(Bitmap.createBitmap(imgCache, screenWidth/2, 0, screenWidth / 2, height));
//                                else frame.setImageBitmap(Bitmap.createBitmap(imgCache,0,0,screenWidth/2,height));
//                            }else{
//                                type=-1;
//                                frame.setImageBitmap(sample);
//                            }
//                        }
//
//                        @Override
//                        public void onLoadCleared(@Nullable Drawable placeholder) {
//
//                        }
//                    });
        }
        updatePageIndex();
        preload();
    }

    void refreshImage(){
        final String image = imgs.get(viewerBookmark);
        type = -1;
        //placeholder
        //frame.setImageResource(R.drawable.placeholder);
//        Glide.with(context)
//                .asBitmap()
//                .load(image)
//                .into(new CustomTarget<Bitmap>() {
//                    @Override
//                    public void onLoadCleared(@Nullable Drawable placeholder) {
//
//                    }
//
//                    @Override
//                    public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
//                        Bitmap sample = getSample(d.decode(bitmap),screenWidth);
//                        int screenWidth = sample.getWidth();
//                        int height = sample.getHeight();
//                        if(screenWidth>height){
//                            imgCache = sample;
//                            type=0;
//                            if(reverse) frame.setImageBitmap(Bitmap.createBitmap(imgCache, 0, 0, screenWidth / 2, height));
//                            else frame.setImageBitmap(Bitmap.createBitmap(imgCache,screenWidth/2,0,screenWidth/2,height));
//                        }else{
//                            type=-1;
//                            frame.setImageBitmap(sample);
//                        }
//                        preload();
//                    }
//                });
        Postprocessor postprocessor = new BasePostprocessor() {
            @Override
            public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
                int width = sourceBitmap.getWidth();
                int height = sourceBitmap.getHeight();
                if(width>height) type = 0;
                else type = -1;
                CloseableReference<Bitmap> bitref =  bitmapFactory.createBitmap(cutBitmap(getSample(d.decode(sourceBitmap), screenWidth),type));
                bitmapFactory.createBitmap(getSample(d.decode(sourceBitmap), screenWidth));
                return CloseableReference.cloneOrNull(bitref);
            }
        };
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(convertUri(image))
                .setPostprocessor(postprocessor)
                .build();

        frame.setController(Fresco.newDraweeControllerBuilder()
                .setImageRequest(imageRequest)
                .setOldController(frame.getController())
                .setControllerListener(listener)
                .build());

        updatePageIndex();
        preload();
    }

    void preload(){
//        if(viewerBookmark<imgs.size()-1)
//            Glide.with(context)
//                    .asBitmap()
//                    .load(imgs.get(viewerBookmark+1))
//                    .preload();
        if(viewerBookmark<imgs.size()-1) {
            String img = imgs.get(viewerBookmark+1);
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(convertUri(img))
                    .build();
            DataSource<CloseableReference<CloseableImage>>
                    dataSource = imagePipeline.fetchDecodedImage(imageRequest, context);

        }
    }
    void updatePageIndex(){
        if(id>0) {
            p.setViewerBookmark(id, viewerBookmark);
            if (0 == viewerBookmark || imgs.size()-1==viewerBookmark) p.removeViewerBookmark(id);
        }
        pageBtn.setText(viewerBookmark+1+"/"+imgs.size());
        if(viewerBookmark==imgs.size()-1 && !toolbarshow) toggleToolbar();
    }

    public void toggleToolbar(){
        //attrs = getWindow().getAttributes();
        if(toolbarshow){
            appbar.animate().translationY(-appbar.getHeight());
            appbarBottom.animate().translationY(+appbarBottom.getHeight());
            toolbarshow=false;
        }
        else {
            appbar.animate().translationY(0);
            appbarBottom.animate().translationY(0);
            toolbarshow=true;
        }
        //getWindow().setAttributes(attrs);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(volumeControl && (keyCode==KeyEvent.KEYCODE_VOLUME_DOWN ||keyCode==KeyEvent.KEYCODE_VOLUME_UP)) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ) {
                nextPage();
            } else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                prevPage();
            }
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    private class loadImages extends AsyncTask<Void,Void,Integer> {
        protected void onPreExecute() {
            super.onPreExecute();
            if(dark) pd = new ProgressDialog(context, R.style.darkDialog);
            else pd = new ProgressDialog(context);
            pd.setMessage("로드중");
            pd.setCancelable(false);
            pd.show();
        }

        protected Integer doInBackground(Void... params) {
            manga.fetch(p.getUrl());
            imgs = manga.getImgs();
            d = new Decoder(manga.getSeed(), manga.getId());
            return null;
        }

        @Override
        protected void onPostExecute(Integer res) {
            super.onPostExecute(res);
            eps = manga.getEps();
            List<String> epsName = new ArrayList<>();
            for(int i=0; i<eps.size(); i++){
                if(eps.get(i).getId()==id){
                    index = i;
                }
                epsName.add(eps.get(i).getName());
            }
            toolbarTitle.setText(manga.getName());
            toolbarTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            toolbarTitle.setMarqueeRepeatLimit(-1);
            toolbarTitle.setSingleLine(true);
            toolbarTitle.setSelected(true);

            if(index==0) next.setEnabled(false);
            else next.setEnabled(true);
            if(index==eps.size()-1) prev.setEnabled(false);
            else prev.setEnabled(true);
            result = new Intent();
            result.putExtra("id",id);
            setResult(RESULT_OK, result);

            //refresh spinner
            spinner.setAdapter(new ArrayAdapter(context, R.layout.spinner_item, epsName));
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long idt) {
                    ((TextView)parent.getChildAt(0)).setTextColor(Color.rgb(249, 249, 249));
                    if(index!= position) {
                        index = position;
                        manga = eps.get(index);
                        id = manga.getId();
                        loadImages l = new loadImages();
                        l.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spinner.setSelection(index);

            if(title == null) title = manga.getTitle();
            p.addRecent(title);
            if(id>0) p.setBookmark(title.getName(),id);
            viewerBookmark = p.getViewerBookmark(id);
            refreshImage();
            if (pd.isShowing()) {
                pd.dismiss();
            }
        }
    }
    private void updateFrameSize(ImageInfo info){
        if(info!=null){
            frame.setAspectRatio(((float)info.getWidth()) / ((float)info.getHeight()));
        }
    }
}