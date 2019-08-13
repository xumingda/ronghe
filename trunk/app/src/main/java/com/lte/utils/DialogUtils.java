package com.lte.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lte.R;


/**
 * 退出对话框(系统类型)
 * 
 * @author C's
 * @date 2014-4-1 下午5:59:10
 * @version V1.0
 */
public class DialogUtils {

	public static Dialog createLoadDialog(Context context, boolean isClickable) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.loading_layout, null);// 得到加载view
		Dialog loadingDialog = new Dialog(context,R.style.loading_dialog);// 创建自定义样式dialog
		loadingDialog.setCancelable(isClickable);// 不可以用“返回键”取消
		loadingDialog.setContentView(v, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
		return loadingDialog;
	}
}
