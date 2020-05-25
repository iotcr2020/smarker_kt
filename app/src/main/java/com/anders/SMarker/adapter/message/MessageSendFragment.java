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
import android.widget.LinearLayout;

import com.anders.SMarker.R;
import com.anders.SMarker.http.NetworkTask;
import com.anders.SMarker.model.MessageList;
import com.anders.SMarker.utils.AppVariables;
import com.anders.SMarker.widget.LineItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MessageSendFragment extends Fragment {
    RecyclerView messageRecyclerView;
    AdapterMessageList adapterMessageList;
    private String sendString;

    public static MessageSendFragment newInstance(int position) {
        MessageSendFragment fragment = new MessageSendFragment();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        sendListView();
        View view = inflater.inflate(R.layout.messagelist_send, container, false);
        ArrayList<MessageList> messageLists = new ArrayList<>();
        messageRecyclerView = (RecyclerView)view.findViewById(R.id.send_list);
        messageRecyclerView.addItemDecoration(new LineItemDecoration(getContext(), LinearLayout.VERTICAL));
        messageRecyclerView.setHasFixedSize(true);
        LinearLayoutManager Im = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        messageRecyclerView.setLayoutManager(Im);
        try {
            JSONArray jsonArray = new JSONArray(sendString);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String receive_nm = item.getString("receive_nm");
               // String ut_nm = item.getString("ut_nm");
                String gubn = item.getString("gubn");
                String content = item.getString("content");
                String reg_dt = item.getString("reg_dt");
                String co_idx = item.getString("co_idx");

                MessageList messageData = new MessageList();

                messageData.setSend_receive_chk("S");
                messageData.setMessage_user_nm(receive_nm);
               // messageData.setMessage_ut_nm(ut_nm);
                messageData.setMessage_gubn(gubn);
                messageData.setMessage_content(content);
                messageData.setReg_dt(reg_dt);
                messageData.setMessage_co_idx(co_idx);

                messageLists.add(messageData);

            }

            adapterMessageList = new AdapterMessageList(getActivity(), messageLists);
            messageRecyclerView.setAdapter(adapterMessageList);


        } catch (JSONException e) {

            //Log.d(TAG, "showResult : ", e);
        }

        //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        // textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void sendListView()
    {
        String[] resultBuilder = null;
        ContentValues addData = new ContentValues();
        // 이 부분 핸드폰번호 장치에서 가져온 것으로 수정 hwang
        addData.put("phoneNB", AppVariables.User_Phone_Number);


        NetworkTask networkTask = new NetworkTask(NetworkTask.API_MESSAGE_SEND_LIST, addData);

        try {
            String result =  networkTask.execute().get();
            if(!result.isEmpty()){
                sendString = result;
                Log.d("send result", result);

            }else{

            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
