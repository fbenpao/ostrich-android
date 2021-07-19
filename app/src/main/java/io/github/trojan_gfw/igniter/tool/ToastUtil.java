package io.github.trojan_gfw.igniter.tool;

import android.content.Context;
import android.widget.Toast;

/***************
 ***** Created by fan on 2020/6/27.
 ***************/
public class ToastUtil {
    private static Toast toast;
    public static void showToast(Context context, int code, String content)
    {
        //code=1时Toast显示的时间长，code=0时显示的时间短。
        if (toast==null)
        {
            if (code ==0)
                toast=Toast.makeText(context,content,Toast.LENGTH_SHORT);
            if (code==1)
                toast=Toast.makeText(context,content,Toast.LENGTH_LONG);
        }
        else
        {
            toast.setText(content);
        }
        toast.show();
    }
}

