package com.example.zyfypt613lsl.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zyfypt613lsl.R;
import com.example.zyfypt613lsl.activities.DetailActivity;
import com.example.zyfypt613lsl.activities.ViewArticleActivity;
import com.example.zyfypt613lsl.bean.ResBean;
import com.example.zyfypt613lsl.common.Common;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private final Context context;
    private List<ResBean> list;
    private final LayoutInflater inflater;
    private String moduleName;
    private String sessionId;

    public MyAdapter(Context context, String moduleName) {
        this.context = context;
        this.moduleName = moduleName;
        this.inflater = LayoutInflater.from(context);
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public void setList(List<ResBean> list) {
        this.list = list;
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
        if (list == null || position < 0 || position >= list.size()) {
            return;
        }

        ResBean bean = list.get(position);
        if (bean == null) return;

        try {
            // 绑定数据
            holder.tvName.setText(bean.getName() != null ? bean.getName() : "无标题");
            holder.tvAchievement.setText(bean.getAuthor() != null ? bean.getAuthor() : "未知作者");
            holder.tvDescription.setText(bean.getUpdate_time() != null ? bean.getUpdate_time() : "未知时间");

            // 加载图片
            if (bean.getThumb() != null && !bean.getThumb().isEmpty()) {
                String imageUrl = Common.buildUploadUrl(bean.getThumb());
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .into(holder.imageView);
            } else {
                holder.imageView.setImageResource(R.mipmap.ic_launcher);
            }

            // 点击事件
            holder.itemView.setOnClickListener(v -> {
                try {
                    int intId = Integer.parseInt(bean.getId() != null ? bean.getId() : "-1");
                    if (intId <= 0) {
                        Toast.makeText(context, "无效的资源ID", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // video/tware/tcase/project 都使用 DetailActivity
                    if ("video".equalsIgnoreCase(moduleName) || 
                        "tware".equalsIgnoreCase(moduleName) ||
                        "tcase".equalsIgnoreCase(moduleName) ||
                        "project".equalsIgnoreCase(moduleName)) {
                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra("mod", moduleName);
                        intent.putExtra("id", intId);
                        intent.putExtra("sessionID", sessionId);
                        context.startActivity(intent);
                        return;
                    }

                    // article 使用 ViewArticleActivity
                    Intent intent = new Intent(context, ViewArticleActivity.class);
                    intent.putExtra("resid", bean.getId());
                    String userId = bean.getUserId() != null ? bean.getUserId() : "";
                    intent.putExtra("userid", userId);
                    context.startActivity(intent);
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "无效的资源ID", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "跳转失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvName, tvAchievement, tvDescription;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 确保这些ID与你的item_layout.xml中的ID一致
            imageView = itemView.findViewById(R.id.iv_image);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAchievement = itemView.findViewById(R.id.tv_author);
            tvDescription = itemView.findViewById(R.id.tv_time);
        }
    }
}