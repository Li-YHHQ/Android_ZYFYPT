package com.example.zyfypt613lsl.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zyfypt613lsl.R;
import com.example.zyfypt613lsl.activities.DetailActivity;
import com.example.zyfypt613lsl.activities.ViewArticleActivity;
import com.example.zyfypt613lsl.bean.CollectResultBean;
import com.example.zyfypt613lsl.common.Common;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * "我的收藏"列表适配器 - 显示完整资源信息
 */
public class CollectAdapter extends RecyclerView.Adapter<CollectAdapter.ViewHolder> {

    private static final String TAG = "CollectAdapter";
    private final Context context;
    private final LayoutInflater inflater;
    private final List<CollectResultBean> data = new ArrayList<>();
    private final String moduleName;

    public CollectAdapter(Context context, String moduleName) {
        this.context = context;
        this.moduleName = moduleName;
        this.inflater = LayoutInflater.from(context);
    }

    public void setData(List<CollectResultBean> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        Log.d(TAG, "setData: " + data.size() + " items");
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 0 || position >= data.size()) return;
        CollectResultBean bean = data.get(position);
        if (bean == null) return;

        Log.d(TAG, "绑定数据[" + position + "]: name=" + bean.getName() + ", author=" + bean.getAuthor() + ", thumb=" + bean.getThumb());

        // 设置标题 - 显示真实标题
        String title = bean.getName();
        if (TextUtils.isEmpty(title)) {
            // 如果没有标题，显示资源ID（备用）
            String resid = bean.getResid();
            if (!TextUtils.isEmpty(resid)) {
                title = "资源 #" + resid;
            } else {
                title = "未知资源";
            }
        }
        holder.tvTitle.setText(title);
        
        // 设置作者 - 显示真实作者名
        String authorName = bean.getAuthor();
        if (TextUtils.isEmpty(authorName)) {
            authorName = bean.getUsername();
        }
        if (TextUtils.isEmpty(authorName)) {
            authorName = bean.getDisplayAuthor();
        }
        // 如果还是没有作者，不显示"未知作者"，保持空白
        holder.tvAuthor.setText(!TextUtils.isEmpty(authorName) ? authorName : "");
        
        // 设置时间
        String time = bean.getUpdatetime();
        holder.tvTime.setText(!TextUtils.isEmpty(time) ? time : "");

        // 加载封面图
        String thumb = bean.getThumb();
        if (!TextUtils.isEmpty(thumb)) {
            String imageUrl = Common.buildUploadUrl(thumb);
            Log.d(TAG, "加载图片: " + imageUrl);
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(getDefaultIcon())
                    .error(getDefaultIcon())
                    .into(holder.ivThumb);
        } else {
            holder.ivThumb.setImageResource(getDefaultIcon());
        }

        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            String resid = bean.getResid();
            if (TextUtils.isEmpty(resid)) {
                return;
            }
            
            try {
                int id = Integer.parseInt(resid);
                if ("article".equals(moduleName)) {
                    Intent intent = new Intent(context, ViewArticleActivity.class);
                    intent.putExtra("resid", resid);
                    intent.putExtra("userid", bean.getUserid() != null ? bean.getUserid() : "");
                    context.startActivity(intent);
                } else {
                    String sessionId = context.getSharedPreferences("login", Context.MODE_PRIVATE)
                            .getString(Common.SESSION_HEADER, "");
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("mod", moduleName);
                    intent.putExtra("sessionID", sessionId);
                    context.startActivity(intent);
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid resid: " + resid);
            }
        });
    }

    private int getDefaultIcon() {
        switch (moduleName) {
            case "article": return R.drawable.ic_blog;
            case "video": return R.drawable.ic_video;
            case "tware": return R.drawable.ic_keynote;
            case "tcase": return R.drawable.ic_sample;
            case "project": return R.drawable.ic_id;
            default: return R.mipmap.ic_launcher;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivThumb;
        final TextView tvTitle;
        final TextView tvAuthor;
        final TextView tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_image);
            tvTitle = itemView.findViewById(R.id.tv_name);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
