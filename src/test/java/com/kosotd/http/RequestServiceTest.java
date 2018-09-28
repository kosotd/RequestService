package com.kosotd.http;

import org.junit.Test;

public class RequestServiceTest {

    @Test
    public void sendTest(){
        RequestService.Response response = RequestService.build().get(b -> {
            b.setUrl("http://google.com");
        }).send();
        System.out.println(response.getData());
    }
}
