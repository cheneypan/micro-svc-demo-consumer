package com.micro.svc.demo.consumer.web.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by cheney on 2017/8/27.
 */
@Service
public class ProductClient extends BaseClient{

    @Value("${micro.svc.product.server:unknown}")
    private String productServer;

    public String call(String msg) throws Exception {
        logger.info("productServer: " + productServer);
        logger.info("send message: " + msg);

        String url = "http://" + this.productServer + "/call?msg=" + msg;
        logger.info("url: " + url);

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(url);
        HttpResponse httpResponse = client.execute(get);
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new Exception(
                    "Failed : HTTP error core : " + httpResponse.getStatusLine().getStatusCode());
        }
        String result = EntityUtils.toString(httpResponse.getEntity());
        return result;
    }
}
