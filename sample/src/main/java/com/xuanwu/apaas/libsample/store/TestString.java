package com.xuanwu.apaas.libsample.store;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by LYL on 2017/9/21.
 */

public class TestString {
    private static final String url = "file:///android_asset/test.json";
    public String createJson(Context context){
        try {
            InputStream stream = context.getAssets().open("test.json");
            return IOUtils.stream2String(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public List<BizModelTable> get(Context context){
        Gson gson = new Gson();
        Type type = new TypeToken<List<BizModelTable>>(){}.getType();
        return gson.fromJson(createJson(context),type);
    }
}
