package com.xuanwu.apaas.libsample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xuanwu.apaas.libsample.store.UserBean;

import java.util.List;

/**
 * Created by LYL on 2017/8/10.
 */

public class UserAdapter extends BaseAdapter {
    Context context;
    List<UserBean> data;
    public UserAdapter(Context context, List<UserBean> data){
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public UserBean getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        Holder holder;
        if(view==null){
            view = LayoutInflater.from(context).inflate(R.layout.item_user,null);
            holder = new Holder();
            holder.tv_id = view.findViewById(R.id.item_tv_userid);
            holder.tv_name = view.findViewById(R.id.itemm_tv_userName);
            holder.tv_tel = view.findViewById(R.id.item_tv_userTel);
            view.setTag(holder);
        }else{
            holder = (Holder) view.getTag();
        }
        UserBean user = getItem(position);
        holder.tv_id.setText(user.getUserId());
        holder.tv_name.setText(user.getUserName());
        holder.tv_tel.setText(user.getTelephone());
        return view;
    }

    class Holder{
        TextView tv_name,tv_id,tv_tel;
    }
}
