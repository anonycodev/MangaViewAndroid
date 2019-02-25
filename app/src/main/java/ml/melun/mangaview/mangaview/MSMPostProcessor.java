package ml.melun.mangaview.mangaview;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.request.BaseRepeatedPostProcessor;

import static android.graphics.Bitmap.createBitmap;
import static ml.melun.mangaview.Utils.cutBitmap;
import static ml.melun.mangaview.Utils.getSample;

public class MSMPostProcessor extends BaseRepeatedPostProcessor {
    Decoder decoder;
    int maxSize;
    int type;

    public MSMPostProcessor(Decoder d, int type, int maxSize){
        this.decoder = d;
        this.maxSize = maxSize;
        this.type = type;
        /*
        type
        -1  : onepage
        0   : autocut first
        1   : autocut second
         */
    }
    public int getType(){
        return type;
    }

    @Override
    public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
        CloseableReference<Bitmap> bitref =  bitmapFactory.createBitmap(cutBitmap(getSample(decoder.decode(sourceBitmap),maxSize),type));
        bitmapFactory.createBitmap(getSample(decoder.decode(sourceBitmap),maxSize));
        return CloseableReference.cloneOrNull(bitref);
    }
}
