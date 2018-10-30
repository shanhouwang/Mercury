package com.devin.test.mercury;

import com.devin.mercury.model.MercurySuccessCallback;

public class Test {

    public static void testRequest() {
        new JavaNullRequest().request(BaseResponse.class, new MercurySuccessCallback<BaseResponse>() {
            @Override
            public void callback(BaseResponse result) {

            }
        });

        new MainRequest("10086", "devin").request(BaseResponse.class, new MercurySuccessCallback<BaseResponse>() {
            @Override
            public void callback(BaseResponse result) {

            }
        });

        new JavaNullRequest().request(BaseResponse.class, new MercurySuccessCallback<BaseResponse>() {
            @Override
            public void callback(BaseResponse result) {

            }
        });
    }

}
