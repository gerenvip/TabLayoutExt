package com.suapp.ui.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.suapp.ui.tablayout.HomeActivity;
import com.suapp.ui.tablayout.R;

/**
 * @author wangwei on 2018/4/4.
 *         wangwei@jiandaola.com
 */
public class TestFragment extends Fragment {

    private TextView mText;

    private static final String PAGE_KEY = "PAGE_KEY";

    public static TestFragment instance(String page) {
        TestFragment fragment = new TestFragment();
        Bundle arg = new Bundle();
        arg.putString(PAGE_KEY, page);
        fragment.setArguments(arg);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frg_test, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mText = view.findViewById(R.id.text);
        View msgView = view.findViewById(R.id.msg);

        Bundle arguments = getArguments();
        String page = arguments.getString(PAGE_KEY);
        mText.setText(page);
        msgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentActivity activity = getActivity();
                if (activity != null && activity instanceof HomeActivity) {
                    ((HomeActivity) activity).update();
                }

            }
        });
    }

    public String getPage() {
        Bundle arguments = getArguments();
        String page = arguments.getString(PAGE_KEY);
        return page;
    }

}
