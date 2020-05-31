package com.devin.test.mercury;

import com.devin.mercury.model.MercurySuccessCallback;

public class Test {

    public static void testRequest() {
        new JavaNullRequest().request(new MercurySuccessCallback<BaseResponse>() {
            @Override
            public void callback(BaseResponse result) {

            }
        });

        new JavaNullRequest().request(new MercurySuccessCallback<BaseResponse>() {
            @Override
            public void callback(BaseResponse result) {

            }
        });
    }

}
