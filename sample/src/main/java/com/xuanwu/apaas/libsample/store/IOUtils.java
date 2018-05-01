package com.xuanwu.apaas.libsample.store;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Amberllo on 2016/12/22.
 */
public class IOUtils {


    /**
     * Stream转化为String
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String stream2String(InputStream inputStream) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
        StringBuffer sb = new StringBuffer();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        reader.close();
        inputStream.close();
        return sb.toString();
    }
}
