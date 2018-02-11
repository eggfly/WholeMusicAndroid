package wholemusic.android;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import wholemusic.core.api.framework.model.Music;

/**
 * Created by haohua on 2018/2/11.
 */

public class MusicListAdapter extends RecyclerView.Adapter<MusicViewHolder> implements View.OnClickListener {

    private final Context mContext;
    private List<? extends Music> mData = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;

    public MusicListAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<? extends Music> musics) {
        mData = musics;
    }

    public List<? extends Music> getData() {
        return mData;
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
        itemView.setOnClickListener(this);
        return new MusicViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MusicViewHolder holder, int position) {
        holder.itemView.setTag(position);
        TextView text = (TextView) holder.itemView.findViewById(android.R.id.text1);
        final Music item = mData.get(position);
        text.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            final int pos = (int) v.getTag();
            mOnItemClickListener.onItemClick(v, pos);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
