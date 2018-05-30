package com.chenggong.picsearchv1.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chenggong.picsearchv1.R;
import com.chenggong.picsearchv1.aty.ResultActivity;
import com.chenggong.picsearchv1.utils.Configure;
import com.chenggong.picsearchv1.utils.Logger;

import java.util.List;

/**
 * Created by chenggong on 18-5-30.
 *
 * @author chenggong
 */

public class PicRecordAdapter extends RecyclerView.Adapter<PicRecordAdapter.ViewHolder> {


    private static final String TAG = "PicRecordAdapter";
    private Context context ;
    private List<Uri> uriList;

    public PicRecordAdapter(Context context, List<Uri> uriList) {
        this.context = context;
        this.uriList = uriList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pic_record, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Uri uri = uriList.get(position);
        Glide.with(context).load(uri).into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 注意: 在系统版本5.0以上和5.0以下，从uri获取图片路径的方法不同，在5.0以上可以直接  {@link uri.getpath()}
                 */
                String[] proj = {MediaStore.Images.Media.DATA};
                //好像是android多媒体数据库的封装接口，具体的看Android文档
                Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
                //获得用户选择的图片的索引值
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                //将光标移至开头 ，这个很重要，不小心很容易引起越界
                cursor.moveToFirst();
                //最后根据索引值获取图片路径   结果类似：/mnt/sdcard/DCIM/Camera/IMG_20151124_013332.jpg
                String path = cursor.getString(column_index);

                Logger.d(TAG,path);
                Logger.d(TAG,uri.getPath());

                ResultActivity.start(context,path, Configure.IMAGE_TYPE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return uriList.size();
    }

     class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView =  itemView.findViewById(R.id.image_record);
        }
    }
}

