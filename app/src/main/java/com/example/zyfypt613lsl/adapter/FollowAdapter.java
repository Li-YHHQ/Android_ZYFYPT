package com.example.zyfypt613lsl.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zyfypt613lsl.R;
import com.example.zyfypt613lsl.bean.FollowResultBean;
import com.example.zyfypt613lsl.common.Common;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * "我的关注"列表适配器 - 美化版。
 */
public class FollowAdapter extends RecyclerView.Adapter<FollowAdapter.ViewHolder> {

    private final Context context;
    private final LayoutInflater inflater;
    private final List<FollowResultBean> data = new ArrayList<>();

    public FollowAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }
    
    /**
     * 获取会话ID
     * @return 会话ID字符串，如果未登录则返回null
     */
    private String getSessionID() {
        // 使用与其他地方一致的 SharedPreferences 名称
        android.content.SharedPreferences prefs = context.getSharedPreferences(
                "login", android.content.Context.MODE_PRIVATE);
        return prefs.getString(Common.SESSION_HEADER, null);
    }

    public void setData(List<FollowResultBean> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_follow, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 0 || position >= data.size()) return;
        FollowResultBean bean = data.get(position);
        if (bean == null) return;

        // 调试日志 - 打印所有字段
        android.util.Log.d("FollowAdapter", "=== 绑定数据 [" + position + "] ===");
        android.util.Log.d("FollowAdapter", "id=" + bean.getId());
        android.util.Log.d("FollowAdapter", "idolid=" + bean.getIdolid());
        android.util.Log.d("FollowAdapter", "idolname=" + bean.getIdolname());
        android.util.Log.d("FollowAdapter", "realname=" + bean.getRealname());
        android.util.Log.d("FollowAdapter", "username=" + bean.getUsername());
        android.util.Log.d("FollowAdapter", "author=" + bean.getAuthor());
        android.util.Log.d("FollowAdapter", "account=" + bean.getAccount());
        android.util.Log.d("FollowAdapter", "avatar=" + bean.getAvatar());
        android.util.Log.d("FollowAdapter", "rolename=" + bean.getRolename());
        android.util.Log.d("FollowAdapter", "email=" + bean.getEmail());
        android.util.Log.d("FollowAdapter", "phone=" + bean.getPhone());
        android.util.Log.d("FollowAdapter", "getDisplayName()=" + bean.getDisplayName());

        // 设置用户名 - 使用getDisplayName()获取最佳显示名称
        String displayName = bean.getDisplayName();
        // 最后才显示"未知用户"
        if (TextUtils.isEmpty(displayName)) {
            displayName = "未知用户";
        }
        android.util.Log.d("FollowAdapter", "最终显示名称=" + displayName);
        holder.tvName.setText(displayName);
        
        // 设置角色标签
        String role = bean.getRolename();
        if (!TextUtils.isEmpty(role)) {
            holder.tvRole.setText(role);
            holder.tvRole.setVisibility(View.VISIBLE);
        } else {
            holder.tvRole.setVisibility(View.GONE);
        }
        
        // 设置描述
        String desc = bean.getDescription();
        if (!TextUtils.isEmpty(desc)) {
            holder.tvDescription.setText(desc);
        } else {
            // 如果没有描述，显示默认文字
            holder.tvDescription.setText("这个人很懒，什么都没写~");
        }
        holder.tvDescription.setVisibility(View.VISIBLE);

        // 加载头像 - 使用avatar字段
        String avatar = bean.getAvatar();
        if (!TextUtils.isEmpty(avatar)) {
            String avatarUrl = Common.buildUploadUrl(avatar);
            Picasso.get()
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_person);
        }

        // 点击整个item跳转到用户详情页
        holder.itemView.setOnClickListener(v -> {
            try {
                int userId = Integer.parseInt(bean.getIdolid());
                android.content.Intent intent = new android.content.Intent(context, 
                        com.example.zyfypt613lsl.activities.UserDetailActivity.class);
                intent.putExtra(com.example.zyfypt613lsl.activities.UserDetailActivity.EXTRA_USER_ID, userId);
                // 使用getDisplayName()获取最佳显示名称
                intent.putExtra(com.example.zyfypt613lsl.activities.UserDetailActivity.EXTRA_USER_NAME, bean.getDisplayName());
                intent.putExtra(com.example.zyfypt613lsl.activities.UserDetailActivity.EXTRA_USER_ROLE, bean.getRolename());
                intent.putExtra(com.example.zyfypt613lsl.activities.UserDetailActivity.EXTRA_USER_DESC, bean.getDescription());
                intent.putExtra(com.example.zyfypt613lsl.activities.UserDetailActivity.EXTRA_USER_AVATAR, bean.getAvatar());
                context.startActivity(intent);
            } catch (NumberFormatException e) {
                android.widget.Toast.makeText(context, "用户ID无效", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // 取消关注按钮点击事件
        holder.btnUnfollow.setOnClickListener(v -> {
            try {
                String sessionId = getSessionID();
                if (sessionId == null || sessionId.isEmpty()) {
                    android.widget.Toast.makeText(context, "请先登录", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // 使用全局网络客户端（FocusService.unfocus返回String，需要ScalarsConverter）
                com.example.zyfypt613lsl.service.FocusService service =
                        com.example.zyfypt613lsl.utils.NetworkClient.getInstance()
                                .createScalarsService(com.example.zyfypt613lsl.service.FocusService.class);
                retrofit2.Call<String> call = service.unfocus(
                        Integer.parseInt(bean.getIdolid()), sessionId);
                
                call.enqueue(new retrofit2.Callback<String>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<String> call, @NonNull retrofit2.Response<String> response) {
                        if (response.isSuccessful()) {
                            android.widget.Toast.makeText(context, "已取消关注", android.widget.Toast.LENGTH_SHORT).show();
                            // 从数据列表中移除该项
                            int pos = holder.getAdapterPosition();
                            if (pos != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                                data.remove(pos);
                                notifyItemRemoved(pos);
                                notifyItemRangeChanged(pos, data.size());
                            }
                        } else {
                            android.widget.Toast.makeText(context, "取消关注失败", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onFailure(@NonNull retrofit2.Call<String> call, @NonNull Throwable t) {
                        android.widget.Toast.makeText(context, "网络错误: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                android.widget.Toast.makeText(context, "用户ID无效", android.widget.Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                android.widget.Toast.makeText(context, "操作失败: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivAvatar;
        final TextView tvName;
        final TextView tvRole;
        final TextView tvDescription;
        final View btnUnfollow;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvRole = itemView.findViewById(R.id.tv_user_type);
            tvDescription = itemView.findViewById(R.id.tv_description);
            btnUnfollow = itemView.findViewById(R.id.btn_follow);
        }
    }
}
