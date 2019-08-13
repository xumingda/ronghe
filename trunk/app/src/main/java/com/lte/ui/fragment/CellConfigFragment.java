package com.lte.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.App;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.lte.R;
import com.lte.data.CellConfig;
import com.lte.data.DataManager;
import com.lte.data.ScanSet;
import com.lte.data.StationInfo;
import com.lte.data.table.BlackListTable;
import com.lte.tcpserver.TcpManager;
import com.lte.ui.event.CellUpgradeEvent;
import com.lte.ui.event.MessageEvent;
import com.lte.ui.listener.OnBackPressedListener;
import com.lte.ui.widget.CommonToast;
import com.lte.ui.widget.DialogManager;
import com.lte.ui.widget.SweetAlertDialog;
import com.lte.utils.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * Created by chenxiaojun on 2017/9/15.
 */

public class CellConfigFragment extends SupportFragment implements View.OnClickListener{
    private StationInfo stationInfo;
    private Button save;
    private Button cancel;
    private OnBackPressedListener mActivityListener;
    private EditText downlink_frequency_point;
    private EditText cell_pci;
    private EditText plmn__list;
    private EditText tac_cycle;
    private EditText tac;
    private EditText cell_pci_list;
    private EditText pilot_frequency_list;
    private EditText uplink_frequency_point;
    private EditText transmitted_power;
    private EditText measure;

    private CellConfig cellConfig;
    private SweetAlertDialog mAddDialog;
    private EditText mEt;
    private ArrayList<Integer> pciList;
    private ArrayList<Integer> pilotFrequencyList;
    private ArrayList<Integer> plmnList;
    private Button upgrade;

    private AtomicReference<String> localProgressFlag;
    private MaterialSpinner configmode;
    private List<String> mConfigModeList = new ArrayList<>();

    public static CellConfigFragment newInstance(StationInfo stationInfo, ArrayList<Integer> pciList,ArrayList<Integer> plmn,ArrayList<Integer> pilotFrequencyList) {
        CellConfigFragment fragment = new CellConfigFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.STATION,stationInfo);
        bundle.putIntegerArrayList(Constants.PCI,pciList);
        bundle.putIntegerArrayList(Constants.PLMN,plmn);
        bundle.putIntegerArrayList(Constants.PILOT,pilotFrequencyList);
        fragment.setArguments(bundle);
        return fragment;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mActivityListener = (OnBackPressedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement IConnectionFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivityListener = null;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cellconfigfragment, container, false);
        init(view);
        return view;
    }
    private void init(View view) {
        EventBus.getDefault().register(this);
        Bundle bundle = getArguments();
        stationInfo = bundle.getParcelable(Constants.STATION);
        pciList = bundle.getIntegerArrayList(Constants.PCI);
        pilotFrequencyList = bundle.getIntegerArrayList(Constants.PILOT);
        plmnList = bundle.getIntegerArrayList(Constants.PLMN);
        downlink_frequency_point = (EditText) view.findViewById(R.id.downlink_frequency_point);//下行频点
        cell_pci = (EditText) view.findViewById(R.id.cell_pci_et);//小区pci
        plmn__list = (EditText) view.findViewById(R.id.plmn__list);
        tac_cycle = (EditText) view.findViewById(R.id.tac_cycle_et);
        tac = (EditText) view.findViewById(R.id.tac);
        cell_pci_list = (EditText) view.findViewById(R.id.cell_pci_list);
        pilot_frequency_list = (EditText) view.findViewById(R.id.pilot_frequency_list);
        uplink_frequency_point = (EditText) view.findViewById(R.id.uplink_frequency_point_et);
        transmitted_power = (EditText) view.findViewById(R.id.transmitted_power_et);
        measure = (EditText) view.findViewById(R.id.measure_et);
        save = (Button)view.findViewById(R.id.save);
        cancel = (Button)view.findViewById(R.id.cancel);
        configmode = (MaterialSpinner) view.findViewById(R.id.configmode);
        mConfigModeList.add("自动配置");
        mConfigModeList.add("手动配置");
        configmode.setItems(mConfigModeList);
        if(stationInfo.getTDDtype() == 1){
            configmode.setVisibility(View.GONE);
        }else {
            configmode.setVisibility(View.VISIBLE);
        }
        upgrade = (Button)view.findViewById(R.id.upgrade);

        upgrade.setOnClickListener(this);

        save.setOnClickListener(this);
        cancel.setOnClickListener(this);
        if(stationInfo == null){
            for (StationInfo info : App.get().getMList()) {
                if(TextUtils.equals(info.getIp(),App.get().Ip)){
                    stationInfo = info;
                }
            }
        } else {
            for (StationInfo info : App.get().getMList()) {
                if(TextUtils.equals(info.getIp(),stationInfo.getIp())){
                    stationInfo = info;
                }
            }
        }
        if(stationInfo != null){
            cellConfig = stationInfo.getCellConfig();
            if(cellConfig != null){
                cellConfig.setId(stationInfo.getId());
                downlink_frequency_point.setText(cellConfig.getDownlink_frequency_point()+"");
                cell_pci.setText(cellConfig.getCell_pci()+"");
                for (Integer integer : cellConfig.getPlmn()) {
                    String s = Integer.toHexString(integer);
                    plmn__list.append(s.substring(0,s.length()-1));
                }
                tac_cycle.setText(cellConfig.getTac_cycle()+"");
                tac.setText(cellConfig.getTac()+"");
                for (Integer integer : cellConfig.getPciList()) {
                    cell_pci_list.append(integer+",");
                }
                for (Integer integer : cellConfig.getPilot_frequency_list()) {
                    pilot_frequency_list.append(integer+",");
                }
                uplink_frequency_point.setText(cellConfig.getUplink_frequency_point()+"");
                transmitted_power.setText(cellConfig.getTransmitted_power()+"");
                measure.setText(cellConfig.getMeasure()+"");
                configmode.setSelectedIndex(cellConfig.getConfigmode());
            }else {
                cellConfig = new CellConfig();
                cellConfig.setId(stationInfo.getId());
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.save:
                if(!TextUtils.isEmpty(plmn__list.getText().toString())){
                    if(plmn__list.getText().toString().length()!= 0  ){
                        ArrayList<Integer> list = new ArrayList<>();
                        if(plmn__list.getText().toString().contains(",")){
                            String[] strings = plmn__list.getText().toString().split(",");
                            for (String string : strings) {
                                if(string.length()>0){
                                    try {
                                        list.add(Integer.parseInt(string+"f",16));
                                    }catch (Exception e){
                                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }else {
                            try {
                                list.add(Integer.parseInt(plmn__list.getText().toString()+"f",16));
                            }catch (Exception e){
                                Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                            }
                        }
                        cellConfig.setPlmn(list);
                    }
                }
                if(!TextUtils.isEmpty(cell_pci_list.getText().toString())){
                    if(cell_pci_list.getText().toString().length()!= 0  ){
                        ArrayList<Integer> list = new ArrayList<>();
                        if(cell_pci_list.getText().toString().contains(",")){
                            String[] strings = cell_pci_list.getText().toString().split(",");
                            for (String string : strings) {
                                if(string.length()>0){
                                    try {
                                        list.add(Integer.valueOf(string));
                                    }catch (Exception e){
                                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }else {
                            try {
                                list.add(Integer.valueOf(cell_pci_list.getText().toString()));
                            }catch (Exception e){
                                Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                            }
                        }
                        cellConfig.setPciList(list);
                    }
                }
                if(!TextUtils.isEmpty(pilot_frequency_list.getText().toString())){
                    if(pilot_frequency_list.getText().toString().length()!= 0  ){
                        ArrayList<Integer> list = new ArrayList<>();
                        if(pilot_frequency_list.getText().toString().contains(",")){
                            String[] strings = pilot_frequency_list.getText().toString().split(",");
                            for (String string : strings) {
                                if(string.length()>0){
                                    try {
                                        list.add(Integer.valueOf(string));
                                    }catch (Exception e){
                                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }else {
                            try {
                                list.add(Integer.valueOf(pilot_frequency_list.getText().toString()));
                            }catch (Exception e){
                                Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                            }
                        }
                        cellConfig.setPilot_frequency_list(list);
                    }
                }
                if(!TextUtils.isEmpty(downlink_frequency_point.getText().toString())){
                    try {
                        int downlink_frequency_point_data = Integer.parseInt(downlink_frequency_point.getText().toString());
                        cellConfig.setDownlink_frequency_point(downlink_frequency_point_data);
                        if (stationInfo.getIp() != null) {
                            if (TextUtils.equals(stationInfo.getIp().substring(13, 16), "211") ||
                                    TextUtils.equals(stationInfo.getIp().substring(13, 16), "212") ||
                                    TextUtils.equals(stationInfo.getIp().substring(13, 16), "213")) {
                                cellConfig.setUplink_frequency_point(downlink_frequency_point_data);
                            } else if (TextUtils.equals(stationInfo.getIp().substring(13, 16), "216") ||
                                    TextUtils.equals(stationInfo.getIp().substring(13, 16), "217")) {
                                cellConfig.setUplink_frequency_point((downlink_frequency_point_data + 18000));

                            }
                        }
                    }catch (Exception e){
                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                    }
                }
                if(!TextUtils.isEmpty(cell_pci.getText().toString())){
                    try {
                        cellConfig.setCell_pci(Integer.parseInt(cell_pci.getText().toString()));
                    }catch (Exception e){
                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                    }
                }
                if(!TextUtils.isEmpty(tac_cycle.getText().toString())){
                    try {
                        cellConfig.setTac_cycle(Integer.parseInt(tac_cycle.getText().toString()));
                    }catch (Exception e){
                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                    }
                }else {
                    cellConfig.setTac_cycle(-1);
                }
                if(!TextUtils.isEmpty(tac.getText().toString())){
                    try {
                        cellConfig.setTac(Integer.parseInt(tac.getText().toString()));
                    }catch (Exception e){
                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                    }
                }
//                if(!TextUtils.isEmpty(uplink_frequency_point.getText().toString())){
//                    try {
//                        cellConfig.setUplink_frequency_point(Integer.parseInt(uplink_frequency_point.getText().toString()));
//                    }catch (Exception e){
//                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
//                    }
//                }
                if(!TextUtils.isEmpty(transmitted_power.getText().toString())){
                    try {
                        cellConfig.setTransmitted_power(Integer.parseInt(transmitted_power.getText().toString()));
                    }catch (Exception e){
                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                    }
                }else {
                    cellConfig.setTransmitted_power(-1);
                }
                if(!TextUtils.isEmpty(measure.getText().toString())){
                    try {
                        cellConfig.setMeasure(Integer.parseInt(measure.getText().toString()));
                    }catch (Exception e){
                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                    }
                }
                cellConfig.setConfigmode(configmode.getSelectedIndex());
                stationInfo.setCellConfig(cellConfig);
                for (StationInfo info : App.get().getMList()) {
                    if(TextUtils.equals(info.getIp(),stationInfo.getIp())){
                        info.setCellConfig(cellConfig);
                    }
                }
                DataManager.getInstance().createOrUpdateStation(stationInfo);
                mActivityListener.onBack();
                break;
            case R.id.cancel:
                mActivityListener.onBack();
                break;
            case R.id.upgrade:
                if(!TextUtils.isEmpty(plmn__list.getText().toString())){
                    if(plmn__list.getText().toString().length()!= 0  ){
                        ArrayList<Integer> list = new ArrayList<>();
                        if(plmn__list.getText().toString().contains(",")){
                            String[] strings = plmn__list.getText().toString().split(",");
                            for (String string : strings) {
                                if(string.length()>0){
                                    try {
                                        list.add(Integer.parseInt(string+"f",16));
                                    }catch (Exception e){
                                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }else {
                            try {
                                list.add(Integer.parseInt(plmn__list.getText().toString()+"f",16));
                            }catch (Exception e){
                                Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                            }
                        }
                        cellConfig.setPlmn(list);
                    }
                }
                if(!TextUtils.isEmpty(cell_pci_list.getText().toString())){
                    if(cell_pci_list.getText().toString().length()!= 0  ){
                        ArrayList<Integer> list = new ArrayList<>();
                        if(cell_pci_list.getText().toString().contains(",")){
                            String[] strings = cell_pci_list.getText().toString().split(",");
                            for (String string : strings) {
                                if(string.length()>0){
                                    try {
                                        list.add(Integer.valueOf(string));
                                    }catch (Exception e){
                                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }else {
                            try {
                                list.add(Integer.valueOf(cell_pci_list.getText().toString()));
                            }catch (Exception e){
                                Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                            }
                        }
                        cellConfig.setPciList(list);
                    }
                }
                if(!TextUtils.isEmpty(pilot_frequency_list.getText().toString())){
                    if(pilot_frequency_list.getText().toString().length()!= 0  ){
                        ArrayList<Integer> list = new ArrayList<>();
                        if(pilot_frequency_list.getText().toString().contains(",")){
                            String[] strings = pilot_frequency_list.getText().toString().split(",");
                            for (String string : strings) {
                                if(string.length()>0){
                                    try {
                                        list.add(Integer.valueOf(string));
                                    }catch (Exception e){
                                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }else {
                            try {
                                list.add(Integer.valueOf(pilot_frequency_list.getText().toString()));
                            }catch (Exception e){
                                Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                            }
                        }
                        cellConfig.setPilot_frequency_list(list);
                    }
                }
                if(!TextUtils.isEmpty(downlink_frequency_point.getText().toString())){
                    try {
                        int downlink_frequency_point_data = Integer.parseInt(downlink_frequency_point.getText().toString());
                        cellConfig.setDownlink_frequency_point(downlink_frequency_point_data);
                        if (stationInfo.getIp() != null) {

                            //211配置39下行频点或212配置38下行频点时
                            //提示：该基带板不能配置该频点
                            //band38/41 37750~28249 或40936
                            //band39 38250~38649
                            if (TextUtils.equals(stationInfo.getIp().substring(13, 16), "211")){
                                if(38250<=downlink_frequency_point_data && downlink_frequency_point_data<=38649){
                                    //band39
                                    Toast.makeText(getContext(),"该基带板不能配置该频点",Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            if(TextUtils.equals(stationInfo.getIp().substring(13, 16), "212")){
                                if(( 37750<=downlink_frequency_point_data && downlink_frequency_point_data<=38249||downlink_frequency_point_data==40936)){
                                    //band38/41
                                    Toast.makeText(getContext(),"该基带板不能配置该频点",Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }



                            if (TextUtils.equals(stationInfo.getIp().substring(13, 16), "211") ||
                                    TextUtils.equals(stationInfo.getIp().substring(13, 16), "212") ||
                                    TextUtils.equals(stationInfo.getIp().substring(13, 16), "213")) {
                                cellConfig.setUplink_frequency_point(downlink_frequency_point_data);
                            } else if (TextUtils.equals(stationInfo.getIp().substring(13, 16), "216") ||
                                    TextUtils.equals(stationInfo.getIp().substring(13, 16), "217")) {
                                cellConfig.setUplink_frequency_point((downlink_frequency_point_data + 18000));

                            }
                        }
                    }catch (Exception e){
                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                    }
                }
                if(!TextUtils.isEmpty(cell_pci.getText().toString())){
                    try {
                        cellConfig.setCell_pci(Integer.parseInt(cell_pci.getText().toString()));
                    }catch (Exception e){
                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                    }
                }
                if(!TextUtils.isEmpty(tac_cycle.getText().toString())){
                    try {
                        cellConfig.setTac_cycle(Integer.parseInt(tac_cycle.getText().toString()));
                    }catch (Exception e){
                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                    }
                }else {
                    cellConfig.setTac_cycle(-1);
                }
                if(!TextUtils.isEmpty(tac.getText().toString())){
                    try {
                        cellConfig.setTac(Integer.parseInt(tac.getText().toString()));
                    }catch (Exception e){
                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                    }
                }
//                if(!TextUtils.isEmpty(uplink_frequency_point.getText().toString())){
//                    try {
//                        cellConfig.setUplink_frequency_point(Integer.parseInt(uplink_frequency_point.getText().toString()));
//                    }catch (Exception e){
//                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
//                    }
//                }
                if(!TextUtils.isEmpty(transmitted_power.getText().toString())){
                    try {
                        cellConfig.setTransmitted_power(Integer.parseInt(transmitted_power.getText().toString()));
                    }catch (Exception e){
                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                    }
                }else {
                    cellConfig.setTransmitted_power(-1);
                }
                if(!TextUtils.isEmpty(measure.getText().toString())){
                    try {
                        cellConfig.setMeasure(Integer.parseInt(measure.getText().toString()));
                    }catch (Exception e){
                        Toast.makeText(getActivity(),getString(R.string.add_number),Toast.LENGTH_LONG).show();
                    }
                }
                stationInfo.setCellConfig(cellConfig);
                TcpManager.getInstance().setCellUpGrade(stationInfo);
                for (StationInfo info : App.get().getMList()) {
                    if(TextUtils.equals(info.getIp(),stationInfo.getIp())){
                        info.setCellConfig(cellConfig);
                    }
                }
                DataManager.getInstance().createOrUpdateStation(stationInfo);
                localProgressFlag = new AtomicReference<>();
                localProgressFlag.set(DialogManager.showProgressDialog(_mActivity, "小区更新中，请稍候.."));
                break;
        }
    }
    /**
     * 小区更新完成
     */
    @Subscribe
    public void onUpdateSuccess(CellUpgradeEvent event) {
        if (event != null) {
            try{
                if (localProgressFlag != null) {
                    DialogManager.closeDialog(localProgressFlag.get());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            CommonToast.show(_mActivity,R.string.upgrade_success);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public static CellConfigFragment newInstance() {
        return new CellConfigFragment();
    }
//    private void showModifyNameDialog(String title, final boolean isAddPci) {
//
//        mAddDialog = new SweetAlertDialog.Builder(getActivity())
//                .setMessage(title)
//                .setHasTwoBtn(true)
//                .setNegativeButton(R.string.cancel)
//                .setPositiveButton(R.string.add, new SweetAlertDialog.OnDialogClickListener() {
//                    @Override
//                    public void onClick(Dialog dialog, int which) {
//
//                        String temp = mEt.getText().toString().trim();
//                        checkModifiedNameAndSave(isAddPci,temp);
//                    }
//                }).create();
//
//        mAddDialog.addContentView(R.layout.input_pci);
//        mEt = (EditText) mAddDialog.findView(R.id.et_pci);
//        mAddDialog.show();
//
//    }


//    private boolean checkModifiedNameAndSave(boolean isAddPci,String tempNumber) {
//        if (TextUtils.isEmpty(tempNumber)) {
//            Toast.makeText(getActivity(), getString(R.string.not_be_null), Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        if(isAddPci){
//            scanSet.getPciList().add(Integer.valueOf(tempNumber));
//            pci_list.append(tempNumber);
//
//        }else {
//            scanSet.getEarfchList().add(Integer.valueOf(tempNumber));
//            earfcn_range_list.append(tempNumber);
//        }
//        return true;
//    }
}
