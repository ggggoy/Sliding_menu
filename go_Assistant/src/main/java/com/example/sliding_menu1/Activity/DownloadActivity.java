package com.example.sliding_menu1.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.example.sliding_menu1.R;

import java.util.ArrayList;

/**
 * 工程名：Sliding_menu
 * 包名：com.example.sliding_menu1.Activity
 * 作者： win
 * 创建日期：2016/4/10 19:45
 * 实现的主要功能：离线地图
 */
public class DownloadActivity extends Activity implements MKOfflineMapListener, AdapterView.OnItemSelectedListener{

    private Context context;
    private MKOfflineMap mkOfflineMap = null;
    private TextView stateView;
    private ProgressBar progressBar;
    private Spinner province;
    private Spinner city;

    // 为两个Spinner分别创建适配器
    private ArrayAdapter<String> arrayAdapterProvince;
    private ArrayAdapter<String> arrayAdapterCity;

    private String selectedProvince;
    private String selectedCity;

    /**
     * 已下载的离线地图信息列表
     * <p>
     * 把已经下载的城市放入以下ListView中；
     */
    private ArrayList<MKOLUpdateElement> localMapList = null;
    private LocalMapAdapter localMapAdapter = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_download);
        mkOfflineMap = new MKOfflineMap();
        mkOfflineMap.init(this);
        initView();
        initAdapter();
        province.setAdapter(arrayAdapterProvince);

    }

    private void initAdapter() {
        arrayAdapterProvince = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.province));
    }

    private void initView() {
        province = (Spinner) findViewById(R.id.sp_province1);
        city = (Spinner) findViewById(R.id.sp_city1);

        province.setOnItemSelectedListener(this);
        city.setOnItemSelectedListener(this);

        stateView = (TextView) findViewById(R.id.state);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        findViewById(R.id.download_btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //获取已下过的离线地图信息
        localMapList = mkOfflineMap.getAllUpdateInfo();
        if (localMapList == null) {
            localMapList = new ArrayList<MKOLUpdateElement>();
        }

        ListView localMapListView = (ListView) findViewById(R.id.localmaplist);
        localMapAdapter = new LocalMapAdapter();
        localMapListView.setAdapter(localMapAdapter);


    }

    //下载离线城市
    public void search(View view) {
        ArrayList<MKOLSearchRecord> records = mkOfflineMap.searchCity(selectedCity);
        if (records == null || records.size() != 1)
            return;
        mkOfflineMap.start(records.get(0).cityID);
        Toast.makeText(context, "开始下载离线地图. 城市名称: " + records.get(0).cityName,
                Toast.LENGTH_SHORT).show();
        updateView();
    }

    //更新状态显示
    private void updateView() {
        localMapList = mkOfflineMap.getAllUpdateInfo();
        if (localMapList == null) {
            localMapList = new ArrayList<MKOLUpdateElement>();
        }
        localMapAdapter.notifyDataSetChanged();// 更新ListView；
    }

    @Override
    protected void onDestroy() {
        /**
         * 退出时，销毁离线地图模块
         */
        mkOfflineMap.destroy();
        super.onDestroy();
    }

    @Override
    public void onGetOfflineMapState(int type, int state) {
        switch (type){
            case MKOfflineMap.TYPE_DOWNLOAD_UPDATE:
            {
                MKOLUpdateElement update=mkOfflineMap.getUpdateInfo(state);
                //处理下载进度更新提示
                if (update!=null){
                    stateView.setText(String.format("%s : %d%%", update.cityName,
                            update.ratio));
                    updateView();
                    progressBar.setProgress(update.ratio);
                    Log.i("asd", String.valueOf(update.ratio));
                }
                if (update.ratio==100){
                    Toast.makeText(context,"下载完成",Toast.LENGTH_SHORT).show();
                    stateView.setText("下载:--");
                    progressBar.setProgress(0);
                }
            }
            break;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_province1:
                selectedProvince = arrayAdapterProvince.getItem(position);
                Toast.makeText(context, selectedProvince, Toast.LENGTH_SHORT)
                        .show();
                initAdapterCity();
                break;

            case R.id.sp_city1:
                selectedCity = arrayAdapterCity.getItem(position);
                Toast.makeText(context, selectedCity, Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    //离线地图管理列表适配器
    private class LocalMapAdapter extends BaseAdapter{


        @Override
        public int getCount() {
            return localMapList.size();
        }

        @Override
        public Object getItem(int position) {
            return localMapList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            MKOLUpdateElement e = (MKOLUpdateElement) getItem(position);
            view = View.inflate(context, R.layout.offline_localmap_list, null);
            initViewItem(view, e);
            return view;
        }

        private void initViewItem(View view, final MKOLUpdateElement e) {
            Button display = (Button) view.findViewById(R.id.display);
            Button remove = (Button) view.findViewById(R.id.remove);
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView ratio = (TextView) view.findViewById(R.id.ratio);
            ratio.setText(e.ratio + "%");
            title.setText(e.cityName);
            if (e.ratio != 100) {
                display.setEnabled(false);
            } else {
                display.setEnabled(true);
            }
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mkOfflineMap.remove(e.cityID);
                    updateView();
                }
            });
            display.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent();
                    intent.putExtra("x", e.geoPt.longitude);// 获得该城市中心点的经纬度；
                    intent.putExtra("y", e.geoPt.latitude);
                    //显示离线地图的Activity
                    intent.setClass(context, ShowMapActivity.class);
                    startActivity(intent);
                }
            });
        }

    }

    private void initAdapterCity() {
        switch (selectedProvince) {
            case "北京":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.北京));

                break;

            case "天津":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.天津));
                break;

            case "上海":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.上海));
                break;

            case "重庆":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.重庆));
                break;

            case "香港":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.香港));
                break;

            case "澳门":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.澳门));
                break;

            case "河北":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.河北));
                break;

            case "山东":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.山东));
                break;

            case "辽宁":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.辽宁));
                break;

            case "黑龙江":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.黑龙江));
                break;

            case "江苏":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.江苏));
                break;

            case "浙江":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.浙江));
                break;

            case "陕西":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.陕西));
                break;

            case "山西":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.山西));
                break;

            case "福建":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.福建));
                break;

            case "广东":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.广东));
                break;

            case "河南":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.河南));
                break;

            case "四川":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.四川));
                break;

            case "湖南":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.湖南));
                break;

            case "湖北":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.湖北));
                break;

            case "安徽":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.安徽));
                break;

            case "江西":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.江西));
                break;

            case "海南":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.海南));
                break;

            case "云南":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.云南));
                break;

            case "贵州":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.贵州));
                break;

            case "甘肃":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.甘肃));
                break;

            case "青海":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.青海));
                break;

            case "宁夏":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.宁夏));
                break;

            case "内蒙古":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.内蒙古));
                break;

            case "广西":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.广西));
                break;

            case "新疆":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.新疆));
                break;

            case "西藏":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.西藏));
                break;

            case "台湾":
                arrayAdapterCity = new ArrayAdapter<>(context,
                        android.R.layout.simple_spinner_dropdown_item,
                        getResources().getStringArray(R.array.台湾));
                break;

        }
        city.setAdapter(arrayAdapterCity);

    }
}
