package com.devin.test.mercury;

import com.devin.mercury.annotation.Post;
import com.devin.model.mercury.MercuryRequest;

/**
 * @ClassName TestMainRequest
 * @Description TODO
 * @Author shanhouwang
 * @Date 2020/9/14 6:54 PM
 * @Version 1.0
 **/
@Post(url = "user")
public class TestMainRequest extends MercuryRequest<CommonResponse<Person>> {

    public String id;

    public String name;

    public TestMainRequest(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
