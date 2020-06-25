package com.anders.SMarker.adapter.message;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anders.SMarker.R;
import com.anders.SMarker.http.NetworkTask;
import com.anders.SMarker.model.MessageList;
import com.anders.SMarker.utils.AppVariables;
import com.anders.SMarker.widget.LineItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class MessageReceiveFragment extends Fragment {
    RecyclerView messageRecyclerView;
    AdapterMessageList adapterMessageList = null;
    private String receiveString;
    public TextView message_cnt,non_message_cnt;
    private CheckBox non_read_chk;
    public static ArrayList<MessageList> messageLists,messageListsAll= null;

    public static final String ARG_SECTION_NUMBER = "section_number";

    public static MessageReceiveFragment newInstance(int position) {
        MessageReceiveFragment fragment = new MessageReceiveFragment();

        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        recevieListView();
        View view = inflater.inflate(R.layout.messagelist_receive, container, false);
        messageLists = new ArrayList<>();
        messageListsAll = new ArrayList<>();
        messageRecyclerView = (RecyclerView)view.findViewById(R.id.receive_list);
        message_cnt = (TextView)view.findViewById(R.id.message_cnt);
        non_read_chk = (CheckBox)view.findViewById(R.id.non_read_chk);
        non_message_cnt= (TextView)view.findViewById(R.id.non_message_cnt);

        messageRecyclerView.addItemDecoration(new LineItemDecoration(getContext(), LinearLayout.VERTICAL));
        messageRecyclerView.setHasFixedSize(true);

        LinearLayoutManager Im = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        messageRecyclerView.setLayoutManager(Im);

        int non_read_cnt=0;

        if (receiveString != null && !"".equals(receiveString)) {
            try {
                JSONArray jsonArray = new JSONArray(receiveString);
                int arraycnt = jsonArray.length();

                message_cnt.setText(Integer.toString(arraycnt));

                for (int i = 0; i < arraycnt; i++) {

                    JSONObject item = jsonArray.getJSONObject(i);

                    String user_nm = item.getString("user_nm");
                    // String ut_nm = item.getString("ut_nm");
                    String gubn = item.getString("gubn");
                    String content = item.getString("content");
                    String reg_dt = item.getString("reg_dt");
                    String co_idx = item.getString("co_idx");
                    String read_chk = item.getString("read_chk");
                    String action_content = item.getString("action_content");
                    if (read_chk.equals("N")) {
                        non_read_cnt++;
                    }
                    MessageList messageData = new MessageList();

                    messageData.setSend_receive_chk("R");
                    messageData.setMessage_co_idx(co_idx);
                    messageData.setMessage_user_nm(user_nm);
                    //  messageData.setMessage_ut_nm(ut_nm);
                    messageData.setMessage_gubn(gubn);
                    messageData.setMessage_content(content);
                    messageData.setReg_dt(reg_dt);
                    messageData.setRead_chk(read_chk);
                    messageData.setAction_content(action_content);


                    messageLists.add(messageData);

                    messageListsAll.add(messageData);


                }

                non_message_cnt.setText(Integer.toString(non_read_cnt));
                non_read_chk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (adapterMessageList != null) {
                            for (Iterator<MessageList> it = messageLists.iterator(); it.hasNext(); ) {
                                MessageList value = it.next();
                                if (value.getRead_chk().equals("Y")) {
                                    it.remove();
                                }
                            }
                        }


                        if (non_read_chk.isChecked()) {

                            adapterMessageList = new AdapterMessageList(getActivity(), messageLists);
                            adapterMessageList.notifyDataSetChanged();
                            messageRecyclerView.setAdapter(adapterMessageList);
                        } else {
                            adapterMessageList = new AdapterMessageList(getActivity(), messageListsAll);
                            adapterMessageList.notifyDataSetChanged();
                            messageRecyclerView.setAdapter(adapterMessageList);


                        }
                    }
                });

                if (!non_read_chk.isChecked()) {
                    if (adapterMessageList != null) {

                        // messageLists = adapterMessageList.getItems();
                    }
                    adapterMessageList = new AdapterMessageList(getActivity(), messageLists);
                    adapterMessageList.notifyDataSetChanged();
                    messageRecyclerView.setAdapter(adapterMessageList);
                }


            } catch (JSONException e) {

                //Log.d(TAG, "showResult : ", e);
            }
        }

        //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        // textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        return view;
    }

    private void recevieListView()
    {
        String[] resultBuilder = null;
        ContentValues addData = new ContentValues();
        // 이 부분 핸드폰번호 장치에서 가져온 것으로 수정 hwang
        addData.put("phoneNB", AppVariables.User_Phone_Number);

        NetworkTask networkTask = new NetworkTask(NetworkTask.API_MESSAGE_RECEIVE_LIST, addData);

        try {
            String result =  networkTask.execute().get();
            if(!result.isEmpty()){
                receiveString = result;

            }else{

            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
