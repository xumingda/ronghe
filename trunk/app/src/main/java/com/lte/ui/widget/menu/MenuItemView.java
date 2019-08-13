package com.lte.ui.widget.menu;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;


import com.App;
import com.lte.R;
import com.lte.data.DataManager;
import com.lte.https.MobileQuery;
import com.lte.ui.activity.HttpSetActivity;
import com.lte.ui.listener.QueryListener;
import com.lte.ui.widget.CommonToast;
import com.lte.ui.widget.DialogManager;
import com.lte.utils.AppUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 */
public abstract class MenuItemView extends MenuBuild {
    private MenuAttribute attribute;
    private final String TAG = "MenuItem";
    private AtomicReference<String> localProgressFlag;
    private boolean isSucces;

    public MenuItemView(Context context) {
        super(context);
    }

    @Override
    protected List<MenuDataItem> onCreateItems() {
        return getMenuData(attribute.type);
    }

    @Override
    protected MenuAttribute getMenuAttribute() {
        return attribute;
    }

    /**
     * 显示菜单
     *
     * @param showTitle 是否显示title
     * @param view      插入的自定义View
     */
    public void showMenu(boolean showTitle, View view) {
        attribute = initAttribute();
        if (attribute == null) {
            Log.e(TAG, "showMenu is ERROR, attribute is NULL");
            return;
        }
        String title = null;
        if (showTitle) title = attribute.musicName;
        super.showMenu(title, view);
    }

    /**
     * 显示菜单
     *
     * @param showTitle     是否显示title
     * @param view          插入的自定义View
     * @param onKeylistener
     */
    public void showMenu(boolean showTitle, View view, DialogInterface.OnKeyListener onKeylistener) {
        attribute = initAttribute();
        if (attribute == null) {
            Log.e(TAG, "showMenu is ERROR, attribute is NULL");
            return;
        }
        String title = null;
        if (showTitle) title = attribute.musicName;
        super.showMenu(title, view, onKeylistener);
    }

    private List<MenuDataItem> getMenuData(int type) {
        List<MenuDataItem> data = new ArrayList<MenuDataItem>();
//        if (type == 2) {//自建歌单
//            data.add(new MenuDataItem(R.drawable.menu_delete_selected, "删除", 9));
//            return data;
         if (type == 1) {
             if(attribute.imsiDataTable.getMobile() == null){
                 data.add(new MenuDataItem(R.drawable.icon_arrow_down_sign, "解析手机号", 6));
             }
            data.add(new MenuDataItem(R.drawable.icon_arrow_down_sign, "加入黑名单", 1));
            data.add(new MenuDataItem(R.drawable.icon_arrow_down_sign, "加入白名单", 2));
         } else if(type == 2){
             data.add(new MenuDataItem(R.drawable.icon_arrow_down_sign, "解除黑名单", 3));
         } else if (type == 3){
             data.add(new MenuDataItem(R.drawable.icon_arrow_down_sign, "解除白名单", 4));
         }else if (type == 4){
             if(attribute.imsiDataTable.getMobile() == null){
                 data.add(new MenuDataItem(R.drawable.icon_arrow_down_sign, "解析手机号", 6));
             }
             data.add(new MenuDataItem(R.drawable.icon_arrow_down_sign, "解除黑名单", 5));
         }else if (type == 5){
             if(attribute.imsiDataTable.getMobile() == null){
                 data.add(new MenuDataItem(R.drawable.icon_arrow_down_sign, "解析手机号", 6));
             }
             data.add(new MenuDataItem(R.drawable.icon_arrow_down_sign, "解除白名单", 7));
         }
        return data;
    }

    @Override
    protected void onItemClick(int itemId) {
        switch (itemId) {
            case 1:
                if(context!=null && attribute.imsiDataTable!=null){
                    DataManager.getInstance().addBlack(attribute.imsiDataTable);
                }
                break;
            case 2:
                if(attribute.imsiDataTable!=null){
                    DataManager.getInstance().addWhite(attribute.imsiDataTable);
                }
                break;
            case 3:
//                AppUtils.showToast(context, "解除黑名单");
                if(attribute.blackListTable!=null){
                    DataManager.getInstance().deleteBlack(attribute.blackListTable);
                }
                break;
            case 4:
//                AppUtils.showToast(context, "解除白名单");
                if(attribute.whiteListTable!=null){
                    DataManager.getInstance().deleteWhite(attribute.whiteListTable);
                }
                break;
            case 5:
//                AppUtils.showToast(context, "解除黑名单");
                if(attribute.imsiDataTable!=null){
                    DataManager.getInstance().deleteBlack(attribute.imsiDataTable);
                }
                break;
            case 6:
                if(!attribute.imsiDataTable.getImsi().startsWith("460") ||attribute.imsiDataTable.getImsi().startsWith("46003")||attribute.imsiDataTable.getImsi().startsWith("46005")
                        ||attribute.imsiDataTable.getImsi().startsWith("46011")){
                    CommonToast.show(context,R.string.error_tips);
                    return;
                }
                isSucces = false;
                MobileQuery.getInstance().queryMobile(attribute.imsiDataTable, new QueryListener() {
                    @Override
                    public void onSuccess() {
                        try{
                            DialogManager.closeDialog(localProgressFlag.get());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        CommonToast.show(context,R.string.query_succes);
                        isSucces = true;
                    }

                    @Override
                    public void onFail() {
                        if(!isSucces){
                            try{
                                DialogManager.closeDialog(localProgressFlag.get());
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            CommonToast.show(context,R.string.query_fail);
                        }
                    }
                });
                localProgressFlag = new AtomicReference<>();
                localProgressFlag.set(DialogManager.showProgressDialog(context, "查询中，请稍候..",true));
                break;
            case 7:
//                AppUtils.showToast(context, "解除白名单");
                if(attribute.imsiDataTable!=null){
                    DataManager.getInstance().deleteWhite(attribute.imsiDataTable);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 删除功能
     */
    protected void onDelItem() {
    }

    /**
     * 执行其他操作(可在MenuAttribute的otherTag中自行定义类型
     */
    protected void onOtherItem(int otherType) {
    }

    ;

    protected abstract MenuAttribute initAttribute();

    @Override
    protected void onItemEnable(MenuDataItem item, MenuItem itemView) {
    }

}
