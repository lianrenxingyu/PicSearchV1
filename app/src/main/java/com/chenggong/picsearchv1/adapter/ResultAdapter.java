package com.chenggong.picsearchv1.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chenggong.picsearchv1.R;
import com.chenggong.picsearchv1.aty.WebPageActivity;
import com.chenggong.picsearchv1.bean.Result;
import com.chenggong.picsearchv1.net.HttpUtil;
import com.chenggong.picsearchv1.net.ImageUtil;
import com.chenggong.picsearchv1.utils.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by chenggong on 18-3-27.
 * 搜索结果的界面中的RecyclerView的adapter
 */

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private Context context;
    private static final String TAG = "ResultAdapter";
    private List<Result> resultList;

    private Handler uiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(context, "保存到Download,下载完成", Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * ViewHolder 内部类
     */
     class ViewHolder extends RecyclerView.ViewHolder {
        View view;
//        TextView tv_title;  //暂时为搜索物品+作者
//        ImageView model_image;
//        TextView tv_timePost;
//        TextView tv_description;
//        TextView tv_webpageURL;

        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            image = itemView.findViewById(R.id.image);
//            tv_title = itemView.findViewById(R.id.tv_title);
//            model_image = itemView.findViewById(R.id.model_image);
//            tv_timePost = itemView.findViewById(R.id.tv_timePost);
//            tv_description = itemView.findViewById(R.id.tv_description);
//            tv_webpageURL = itemView.findViewById(R.id.tv_webpageURL);
        }

    }

    public ResultAdapter(Context context, List<Result> resultList) {
        this.context = context;
        this.resultList = resultList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_pic_item, parent, false);

        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Result result = resultList.get(position);

        ImageUtil.loadImage(context, result.getImgURL(), holder.image);

        final Dialog dialog = new Dialog(context, R.style.Theme_AppCompat_Dialog);
        dialog.setContentView(R.layout.dialog_image_view);
        ImageView bigImage = dialog.findViewById(R.id.big_imageView);
        TextView title = dialog.findViewById(R.id.tv_title);
        title.setText(result.getName() +  "-" + result.getSource());
        title.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebPageActivity.start(context, result.getWebpageURL());
                dialog.dismiss();
            }
        });

        ImageUtil.loadImage(dialog.getContext(), result.getImgURL(), bigImage);
        dialog.setCanceledOnTouchOutside(true);

        bigImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });


        final AlertDialog isDownloadDialog = new AlertDialog.Builder(context)
                .setMessage("要下载"+result.getName()+"吗?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        uiHandler.sendEmptyMessage(0);
                        //TODO 下载文件操作
                        HttpUtil.download(result.getImgURL(), new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                                Logger.d(TAG, "下载失败");
                                uiHandler.sendEmptyMessage(2);

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {

                                byte[] bytes = new byte[2048];
                                int len;
                                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download";
                                File file = new File(filePath, result.getName()+".jpg");
                                FileOutputStream fos = new FileOutputStream(file);

                                InputStream inputStream = response.body().byteStream();
                                while ((len = inputStream.read(bytes)) != -1) {
                                    fos.write(bytes, 0, len);

                                }
                                fos.flush();
                                fos.close();
                                inputStream.close();
                                uiHandler.sendEmptyMessage(1);
                            }
                        });
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        holder.image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isDownloadDialog.show();
                return true;
            }
        });
        bigImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isDownloadDialog.show();
                return true;
            }
        });
    }


    @Override
    public int getItemCount() {
        return resultList.size();
    }

}
