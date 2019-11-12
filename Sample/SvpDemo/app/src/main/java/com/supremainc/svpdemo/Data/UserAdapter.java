package com.supremainc.svpdemo.Data;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.supremainc.svpdemo.R;

import java.util.ArrayList;

public class UserAdapter extends ArrayAdapter<User> implements Filterable {

    public class UserViewHolder{
        public TextView userId;
        public TextView numOfFingerprint;
        public TextView numOfCard;
    }

    private ArrayList<User> mUserList;
    private ArrayList<User> mFilteredUserList;
    private Activity        mActivity;
    private Filter          mListFilter;

    public UserAdapter(Activity activity, ArrayList<User> users){
        super(activity,R.layout.user_item,users);
        this.mUserList = users;
        this.mFilteredUserList = users;
        this.mActivity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        UserViewHolder userViewHolder;
        View itemView = convertView;

        if(itemView == null){
            userViewHolder = new UserViewHolder();
            itemView = mActivity.getLayoutInflater().inflate(R.layout.user_item, parent, false);

            userViewHolder.userId = (TextView)itemView.findViewById(R.id.userId);
            userViewHolder.numOfFingerprint = (TextView) itemView.findViewById(R.id.numOfFingerprint);
            userViewHolder.numOfCard = (TextView) itemView.findViewById(R.id.numOfCard);

            itemView.setTag(userViewHolder);
        }
        else {
            userViewHolder =(UserViewHolder)itemView.getTag();
        }

        User user = mFilteredUserList.get(position);
        String userID = user.getUserID();
        if (!user.getUserName().isEmpty())
            userID = userID + "(" + user.getUserName() + ")";

        userViewHolder.userId.setText(userID);
        userViewHolder.numOfFingerprint.setText(String.valueOf(user.getNumOfFinger()));

        if(!user.getCardNumber().equals(""))
            userViewHolder.numOfCard.setText("1");

        return itemView;
    }

    @Override
    public Filter getFilter() {
        if (mListFilter == null) {
            mListFilter = new ListFilter() ;
        }
        return mListFilter ;
    }

    @Override
    public int getCount() {
        return mFilteredUserList.size() ;
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public User getItem(int position) {
        return mFilteredUserList.get(position) ;
    }

    private class ListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults() ;

            if (constraint == null || constraint.length() == 0) {
                results.values = mUserList;
                results.count = mUserList.size();
            }
            else {
                ArrayList<User> itemList = new ArrayList<>();
                for (User item : mUserList) {
                    if (item.getUserID().toUpperCase().contains(constraint.toString().toUpperCase()) ||
                        item.getUserName().toUpperCase().contains(constraint.toString().toUpperCase()) ) {
                        itemList.add(item) ;
                    }
                }
                results.values = itemList ;
                results.count = itemList.size() ;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredUserList = (ArrayList<User>) results.values;

            if (results.count > 0) {
                notifyDataSetChanged() ;
            }
            else {
                notifyDataSetInvalidated() ;
            }
        }
    }
}
