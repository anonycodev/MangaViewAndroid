package ml.melun.mangaview;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;

import androidx.appcompat.app.AlertDialog;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Display;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import ml.melun.mangaview.activity.CaptchaActivity;
import ml.melun.mangaview.activity.EpisodeActivity;
import ml.melun.mangaview.activity.ViewerActivity2;
import ml.melun.mangaview.mangaview.CustomHttpClient;
import ml.melun.mangaview.mangaview.Login;
import ml.melun.mangaview.mangaview.Manga;
import ml.melun.mangaview.mangaview.Title;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Utils {

    private static int captchaCount = 0;

    public static final String ReservedChars = "|\\?*<\":>+[]/'";

    public static Boolean deleteRecursive(File fileOrDirectory) {
        if(!checkWriteable(fileOrDirectory)) return false;
        try {
            if (fileOrDirectory.isDirectory())
                for (File child : fileOrDirectory.listFiles())
                    if(!deleteRecursive(child)) return false;
            fileOrDirectory.delete();
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public static boolean checkWriteable(File targetDir) {
        if(targetDir.isDirectory()) {
            File tmp = new File(targetDir, "mangaViewTestFile");
            try {
                if (tmp.createNewFile()) tmp.delete();
                else return false;
            } catch (Exception e) {
                return false;
            }
            return true;
        }else{
            File tmp = new File(targetDir.getParent(), "mangaViewTestFile");
            try {
                if (tmp.createNewFile()) tmp.delete();
                else return false;
            } catch (Exception e) {
                return false;
            }
            return true;
        }
    }

//    public static String httpsGet(String urlin, String cookie){
//        BufferedReader reader = null;
//        try {
//            InputStream stream = null;
//            URL url = new URL(urlin);
//            if(url.getProtocol().equals("http")){
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//                connection.setRequestProperty("Accept-Encoding", "*");
//                connection.setRequestProperty("Accept", "*");
//                connection.setRequestProperty("Cookie",cookie);
//                connection.connect();
//                stream = connection.getInputStream();
//            }else if(url.getProtocol().equals("https")){
//                HttpsURLConnection connections = (HttpsURLConnection) url.openConnection();
//                connections.setInstanceFollowRedirects(false);
//                connections.setRequestMethod("GET");
//                connections.setRequestProperty("Accept-Encoding", "*");
//                connections.setRequestProperty("Accept", "*");
//                connections.setRequestProperty("Cookie",cookie);
//                connections.connect();
//                stream = connections.getInputStream();
//            }
//            reader = new BufferedReader(new InputStreamReader(stream));
//            StringBuffer buffer = new StringBuffer();
//            String line = "";
//            while ((line = reader.readLine()) != null) {
//                buffer.append(line);
//            }
//            return buffer.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (reader != null) {
//                    reader.close();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }
//
//    public static String httpsGet(String urlin){
//        return httpsGet(urlin, "");
//    }
    public static Intent episodeIntent(Context context,Title title){
        Intent episodeView = new Intent(context, EpisodeActivity.class);
        episodeView.putExtra("title", new Gson().toJson(title));
        return episodeView;
    }

    public static Intent viewerIntent(Context context, Manga manga){
        Intent viewer = new Intent(context, ViewerActivity2.class);

        viewer.putExtra("manga",new Gson().toJson(manga));
        return viewer;
    }
    public static void showPopup(Context context, String title, String content, DialogInterface.OnClickListener clickListener, DialogInterface.OnCancelListener cancelListener){
        AlertDialog.Builder builder;
        if (new Preference(context).getDarkTheme()) builder = new AlertDialog.Builder(context, R.style.darkDialog);
        else builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(content)
                .setPositiveButton("확인", clickListener)
                .setOnCancelListener(cancelListener)
                .show();
    }

    public static void showErrorPopup(Context context, String message, Exception e, boolean force_close){
        AlertDialog.Builder builder;
        String title = "오류";
        if (new Preference(context).getDarkTheme()) builder = new AlertDialog.Builder(context, R.style.darkDialog);
        else builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(force_close) ((Activity)context).finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        if(force_close) ((Activity)context).finish();
                    }
                });
        if(e != null) {
            builder.setNeutralButton("자세히", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showStackTrace(context, e);
                }
            });
        }
        builder.show();
    }

    public static boolean checkConnection(Context context){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) ((Activity)context).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public static void showCaptchaPopup(Context context, int code, Exception e, boolean force_close){
        if(!checkConnection(context)){
            //no internet
            //showErrorPopup(context, "네트워크 연결이 없습니다.", e, force_close);
            Toast.makeText(context, "네트워크 연결이 없습니다.", Toast.LENGTH_LONG).show();
            if(force_close) ((Activity) context).finish();
        }else if(captchaCount == 0){
            startCaptchaActivity(context, code);
        }else {
            AlertDialog.Builder builder;
            String title = "오류";
            String content = "정보를 불러오는데 실패하였습니다. CAPTCHA를 재인증 하겠습니까?";
            if (new Preference(context).getDarkTheme())
                builder = new AlertDialog.Builder(context, R.style.darkDialog);
            else builder = new AlertDialog.Builder(context);
            builder.setTitle(title)
                    .setMessage(content)
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (force_close) ((Activity) context).finish();
                        }
                    })
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startCaptchaActivity(context, code);
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            if (force_close) ((Activity) context).finish();
                        }
                    });
            if (e != null) {
                builder.setNeutralButton("자세히", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showStackTrace(context, e);
                    }
                });
            }
            try {
                builder.show();
            }catch (Exception e2){
                e2.printStackTrace();
            }
        }
        captchaCount++;
    }

    static void startCaptchaActivity(Context context, int code){
        Intent captchaIntent = new Intent(context, CaptchaActivity.class);
        ((Activity)context).startActivityForResult(captchaIntent, code);
    }

    public static void showCaptchaPopup(Context context, Exception e) {
        // viewer call
        showCaptchaPopup(context, 0, e, true);
    }

    public static void showCaptchaPopup(Context context, int code){
        // menu call
        showCaptchaPopup(context, code, null, false);
    }

    public static void showCaptchaPopup(Context context){
        // viewer call
        showCaptchaPopup(context, 0, null, true);
    }

    private static void showStackTrace(Context context, Exception e){
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append(e.getMessage()+"\n");
        for(StackTraceElement s : e.getStackTrace()){
            sbuilder.append(s+"\n");
        }
        final String error = sbuilder.toString();
        AlertDialog.Builder builder;
        String title = "STACK TRACE";
        if (new Preference(context).getDarkTheme()) builder = new AlertDialog.Builder(context, R.style.darkDialog);
        else builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(error)
                .setNeutralButton("복사", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("stack_trace", error);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context,"클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show();
                        ((Activity)context).finish();
                    }
                })
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((Activity)context).finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        ((Activity)context).finish();
                    }
                })
                .show();
    }


    public static void showPopup(Context context, String title, String content){
        AlertDialog.Builder builder;
        if (new Preference(context).getDarkTheme()) builder = new AlertDialog.Builder(context, R.style.darkDialog);
        else builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(content)
                .setPositiveButton("확인", null)
                .show();
    }

    static char[] filter = {'/','?','*',':','|','<','>','\\'};
    static public String filterFolder(String input){
        for(int i=0; i<filter.length;i++) {
            int index = input.indexOf(filter[i]);
            while(index>=0) {
                char tmp[] = input.toCharArray();
                tmp[index] = ' ';
                input = String.valueOf(tmp);
                index = input.indexOf(filter[i]);
            }
        }
        return input;
    }

    static public String readFileToString(File data){
        StringBuilder raw = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(data));
            String line;
            while ((line = br.readLine()) != null) {
                raw.append(line);
            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return raw.toString();
    }

    public static Bitmap getSample(Bitmap input, int width){
        //scale down bitmap to avoid outofmem exception
        if(input.getWidth()<=width) return input;
        else{
            //ratio
            float ratio = (float)input.getHeight()/(float)input.getWidth();
            int height = Math.round(ratio*width);
            return Bitmap.createScaledBitmap(input, width, height,false);
        }
    }

    public static int getScreenSize(Display display){
        Point size = new Point();
        display.getSize(size);
        int width = size.x>size.y ? size.x : size.y;
        //max pixels : 3000 ?
        return width>3000 ? 3000 : width ;
    }

    public static Boolean writeComment(CustomHttpClient client, Login login, int id, String content, String baseUrl){
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Cookie", login.getCookie(true));
//            headers.put("Content-Type","application/x-www-form-urlencoded");
//            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
//            headers.put("Accept-Encoding", "gzip, deflate, br");
//            headers.put("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");


            Response tokenResponse = client.mget("/bbs/ajax.comment_token.php?_="+ System.currentTimeMillis());
            String token = new JSONObject(tokenResponse.body().string()).getString("token");
            tokenResponse.close();
//
//            String param = "token="+token
//                    +"&w=c&bo_table=manga&wr_id="+id
//                    +"&comment_id=&pim=&sca=&sfl=&stx=&spt=&page=&is_good=0&wr_content="+URLEncoder.encode(content, "UTF-8");
            RequestBody requestBody = new FormBody.Builder()
                    .addEncoded("token",token)
                    .addEncoded("w","c")
                    .addEncoded("bo_table","manga")
                    .addEncoded("wr_id",String.valueOf(id))
                    .addEncoded("comment_id","")
                    .addEncoded("pim","")
                    .addEncoded("sca","")
                    .addEncoded("sfl","")
                    .addEncoded("stx","")
                    .addEncoded("spt","")
                    .addEncoded("page","")
                    .addEncoded("is_good","0")
                    .addEncoded("wr_content",content)
                    .build();



            Response commentResponse = client.post(baseUrl + "/bbs/write_comment_update.php", requestBody, headers);
            int responseCode = commentResponse.code();
            commentResponse.close();
            if(responseCode == 302)
                return true;
        }catch (Exception e){

        }
        return false;
    }

    public static void hideSpinnerDropDown(Spinner spinner) {
        try {
            Method method = Spinner.class.getDeclaredMethod("onDetachedFromWindow");
            method.setAccessible(true);
            method.invoke(spinner);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean writePreferenceToFile(Context c, File f){
        try {
            FileOutputStream stream = new FileOutputStream(f);
            stream.write(readPref(c).getBytes());
            stream.flush();
            stream.close();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public static boolean readPreferenceFromFile(Preference p, Context c, File f){
        try {
            JSONObject data = new JSONObject(readFileToString(f));

            SharedPreferences.Editor editor = c.getSharedPreferences("mangaView", Context.MODE_PRIVATE).edit();

            editor.putString("recent",data.getJSONArray("recent").toString());
            editor.putString("favorite",data.getJSONArray("favorite").toString());
            editor.putString("homeDir",data.getString("homeDir"));
            editor.putBoolean("darkTheme",data.getBoolean("darkTheme"));
            editor.putBoolean("volumeControl",data.getBoolean("volumeControl"));
            editor.putString("bookmark",data.getJSONObject("bookmark").toString());
            editor.putString("bookmark2",data.getJSONObject("bookmark2").toString());
            editor.putInt("viewerType",data.getInt("viewerType"));
            editor.putBoolean("pageReverse",data.getBoolean("pageReverse"));
            editor.putBoolean("dataSave",data.getBoolean("dataSave"));
            editor.putBoolean("stretch",data.getBoolean("stretch"));
            editor.putInt("startTab",data.getInt("startTab"));
            editor.putString("url",data.getString("url"));
            editor.putString("notice",data.getJSONArray("notice").toString());
            editor.putLong("lastUpdateTime", data.getLong("lastUpdateTime"));
            editor.putLong("lastNoticeTime", data.getLong("lastNoticeTime"));
            editor.putBoolean("leftRight", data.getBoolean("leftRight"));
            editor.putBoolean("autoUrl", data.getBoolean("autoUrl"));

            editor.commit();
            p.init(c);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String readPref(Context context){
        SharedPreferences sharedPref = ((Activity)context).getSharedPreferences("mangaView", Context.MODE_PRIVATE);
        JSONObject data = new JSONObject();
        try {
            data.put("recent",new JSONArray(sharedPref.getString("recent", "[]")));
            data.put("favorite",new JSONArray(sharedPref.getString("favorite", "[]")));
            data.put("homeDir",sharedPref.getString("homeDir","/sdcard/MangaView/saved"));
            data.put("darkTheme",sharedPref.getBoolean("darkTheme", false));
            data.put("volumeControl",sharedPref.getBoolean("volumeControl",false));
            data.put("bookmark",new JSONObject(sharedPref.getString("bookmark", "{}")));
            data.put("bookmark2",new JSONObject(sharedPref.getString("bookmark2", "{}")));
            data.put("viewerType", sharedPref.getInt("viewerType",0));
            data.put("pageReverse",sharedPref.getBoolean("pageReverse",false));
            data.put("dataSave",sharedPref.getBoolean("dataSave", false));
            data.put("stretch",sharedPref.getBoolean("stretch", false));
            data.put("leftRight", sharedPref.getBoolean("leftRight", false));
            data.put("startTab",sharedPref.getInt("startTab", 0));
            data.put("url",sharedPref.getString("url", "http://188.214.128.5"));
            data.put("notice",new JSONArray(sharedPref.getString("notice", "[]")));
            data.put("lastNoticeTime",sharedPref.getLong("lastNoticeTime",0));
            data.put("lastUpdateTime",sharedPref.getLong("lastUpdateTime",0));
            data.put("autoUrl", sharedPref.getBoolean("autoUrl", true));
        }catch(Exception e){
            e.printStackTrace();
        }
        return (prefFilter(data.toString()));
    }

    public static String prefFilter(String input){
        // keep newline and filter everything else
        return input.replace("\\n", "/n")
                .replace("\\","")
                .replace("/n", "\\n");
    }

}
