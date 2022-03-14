package com.app.jussfun.model;

import com.app.jussfun.base.BaseViewHolder;

import java.util.ArrayList;

public class HolderModel {
    ArrayList<BaseViewHolder> listHolder =new ArrayList<>();

    public ArrayList<BaseViewHolder> getListHolder() {
        return listHolder;
    }

    public void setListHolder(ArrayList<BaseViewHolder> listHolder) {
        this.listHolder = listHolder;
    }
}