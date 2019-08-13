package com.lte.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.App;
import com.communication.utils.Constant;
import com.lte.R;
import com.lte.ui.adapter.VerAdapter;
import com.lte.ui.event.SystemOutEvent;
import com.lte.ui.fragment.BaseFragment;
import com.lte.ui.fragment.SystemFragment;
import com.lte.ui.widget.TitleBar;
import com.lte.utils.CHexConver;
import com.lte.utils.SendCommendHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

import me.yokeyword.fragmentation.SupportActivity;

import static com.lte.utils.DateUtils.getVersionName;

/**
 * Created by chenxiaojun on 2017/11/6.
 */

public class VerActivity extends BaseActivity {
    private TitleBar titleBar;
    private RecyclerView recyclerView;
    private VerAdapter mAdapter;
    private TextView textView;
    private TextView mcu_version_tv;
    private TextView fpga_version_tv;
    private ReceiveDataThread mReceiveDataThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ver_activity);
        titleBar = (TitleBar) findViewById(R.id.titlebar);
        fpga_version_tv = (TextView) findViewById(R.id.fpga_version_tv);
        mcu_version_tv = (TextView)findViewById(R.id.mcu_version_tv);
        titleBar.setTitle(R.string.verSoft);
        titleBar.setLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VerActivity.this.finish();
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.ver_list);

//        textView = (TextView) findViewById(R.id.text);
//
//        String temp = getResources().getString(R.string.version);
//        String tabCurrentUser = String.format(temp, getVersionName(this));
//        textView.setText(tabCurrentUser);

        mAdapter = new VerAdapter(this, App.get().getOnLineList());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(mAdapter);
        EventBus.getDefault().register(this);
        mcu_version_tv.setText(Constant.mcu_version);
        fpga_version_tv.setText(Constant.fpga_version);

    }



    @Subscribe
    public void onSystemOut(SystemOutEvent outEvent){
        if(outEvent.isOut()){
            this.finish();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(getMcuVersion);
        EventBus.getDefault().unregister(this);
    }
    Runnable getMcuVersion = new Runnable() {
        @Override
        public void run() {
            sendCommand(SendCommendHelper.getMcuVer());
        }
    };



    private void sendCommand(String str)
    {
        String cmd = CHexConver.str2HexStr(str)+"0D0A";
        if (BaseFragment.mbConectOk){
            int i;
            if (cmd.length() <= 0)
                return; /* Loop/switch isn't completed */
            if (BaseFragment.mOutputMode != 0)
            {
                byte byte0 = BaseFragment.mOutputMode;
                i = 0;
                if (1 == byte0)
                {
                    if (CHexConver.checkHexStr(cmd))
                    {
                        i = SendData(CHexConver.hexStringToBytes(cmd.toUpperCase()));
                    }
                    else
                    {
                        i = 0;
                    }
                }
            }
            else
                i = SendData(CHexConver.hexStringToBytes(cmd.toUpperCase()));
            if (i < 0) {
            }
        }else{
            mHandler.sendEmptyMessage(100);

        }
    }
    @Override
    public void onResume() {
        super.onResume();
        //接收蓝牙数据
        startReceiveData();
        mHandler.postDelayed(getMcuVersion, 600);
//        mHandler.postDelayed(getFpgaVersion, 3500);
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mReceiveDataThread != null) {
            mReceiveDataThread.stopRunnable();
            mReceiveDataThread = null;
        }
    }
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String str = getString(R.string.msg_connect_ok) + "\r\n";
                    break;
                case 2:
                    String data = msg.getData().getString("str");
                    if (data.length() >= 6) {
                        if (data.substring(0, 4).equals("0A01")) {
                            int data1 = Integer.parseInt(data.substring(6,8), 16);
                            int high1 = getHeight3((byte)data1);
                            int low1 = getLow5((byte)data1);
                            int data2 = Integer.parseInt(data.substring(4,6), 16);
                            int high2 = getHeight4((byte)data2);
                            int low2 = getLow4((byte)data2);
                            mcu_version_tv.setText((low2+2011)+String.format("%02d", high2)+String.format("%02d", low1));
                            Constant.mcu_version = (low2+2011)+String.format("%02d", high2)+String.format("%02d", low1);
                        } else if (data.substring(1, 5).equals("0A02")) {
                            int data1 = Integer.parseInt(data.substring(7,9), 16);
                            int high1 = getHeight3((byte)data1);
                            int low1 = getLow5((byte)data1);
                            int data2 = Integer.parseInt(data.substring(5,7), 16);
                            int high2 = getHeight4((byte)data2);
                            int low2 = getLow4((byte)data2);
                            fpga_version_tv.setText((low2+2011)+String.format("%02d", high2)+String.format("%02d", low1));
                            Constant.fpga_version = (low2+2011)+String.format("%02d", high2)+String.format("%02d", low1);
                        }else if (data.substring(0, 4).equals("0A02")&&data.length()>8){
                            int data1 = Integer.parseInt(data.substring(6,8), 16);
                            int high1 = getHeight3((byte)data1);
                            int low1 = getLow5((byte)data1);
                            int data2 = Integer.parseInt(data.substring(4,6), 16);
                            int high2 = getHeight4((byte)data2);
                            int low2 = getLow4((byte)data2);
                            fpga_version_tv.setText((low2+2011)+String.format("%02d", high2)+String.format("%02d", low1));
                            Constant.fpga_version = (low2+2011)+String.format("%02d", high2)+String.format("%02d", low1);
                        }
                    }


                    break;
                case 3:
                    mcu_version_tv.setText(msg.getData().getString("str"));
                    break;
                case 100:
//                    Toast.makeText(getActivity(), getString(R.string.msg_Bluetooth_conn_lost), Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }
    };

    private void startReceiveData() {
        if (mReceiveDataThread == null) {
            mReceiveDataThread = new ReceiveDataThread();
            mReceiveDataThread.startRunable();
            mReceiveDataThread.start();
        }
    }

    private class ReceiveDataThread extends Thread {
        boolean isStopRunnable = false;

        public ReceiveDataThread() {
        }

        public void startRunable() {
            isStopRunnable = false;
        }

        public void stopRunnable() {
            isStopRunnable = true;
        }

        @Override
        public void run() {
            if (BaseFragment.mbConectOk) {
                int i;
                byte bytebuf[] = new byte[200];
                String str;
                while (!isStopRunnable) {
                    i = ReceiveData(bytebuf);
                    if (i <= 0)
                        break;
                    if (BaseFragment.mInputMode == 0) {
                        str = new String(bytebuf, 0, i);
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("str", str);
                        message.setData(bundle);
                        message.what = 2;
                        mHandler.sendMessage(message);

                    } else if (1 == BaseFragment.mInputMode) {
                        str = (new StringBuilder(String.valueOf(CHexConver.byte2HexStr(bytebuf, i)))).append(" ").toString();
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("str", str);
                        message.setData(bundle);//bundle传值
                        message.what = 3;
                        mHandler.sendMessage(message);

                    }
                }
            }

        }
    }


    public static int getHeight4(byte data){//获取高四位
        int height;
        height = ((data & 0xf0) >> 4);
        return height;
    }

    public static int getLow4(byte data){//获取低四位
        int low;
        low = (data & 0x0f);
        return low;
    }

    public static int getHeight3(byte data){//获取高三位
        int height;
        height = ((data & 0xe0) >> 5);
        return height;
    }

    public static int getLow5(byte data){//获取低五位
        int low;
        low = (data & 0x1f);
        return low;
    }

    protected int ReceiveData(byte bytebuf[])
    {
        if (!BaseFragment.mbConectOk)
            return -2;
        try
        {
            return BaseFragment.misIn.read(bytebuf);
        }
        catch (IOException ioexception)
        {
            terminateConnect();
            return -3;
        }
    }

    protected int SendData(byte bytebuf[])
    {
        int bytelength;
        if (BaseFragment.mbConectOk)
        {
            try
            {
                BaseFragment.mosOut.write(bytebuf);
                bytelength = bytebuf.length;
            }
            catch (IOException ioexception)
            {
                terminateConnect();
                bytelength = -3;
            }
        }
        else
            bytelength = -2;
        return bytelength;
    }
    protected void terminateConnect()
    {
        if (BaseFragment.mbConectOk)
        {
            try
            {
                BaseFragment.mbConectOk = false;
                BaseFragment.mbsSocket.close();
                BaseFragment.misIn.close();
                BaseFragment. mosOut.close();
            }
            catch (IOException localIOException)
            {
                BaseFragment. misIn = null;
                BaseFragment. mosOut = null;
                BaseFragment. mbsSocket = null;
            }
        }
    }
}
