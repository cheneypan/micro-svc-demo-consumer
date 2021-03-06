package com.micro.svc.demo.consumer.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.micro.svc.demo.consumer.web.client.ProductClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cheney on 2017/8/27.
 */
@RestController
public class IndexController {

    private Logger logger = Logger.getLogger(getClass());

    @Value("${spring.aplication.name}")
    private String applicationName;

    @Value("${micro_svc_product_server:unknown1}")
    private String productServer;

    @Autowired
    private ProductClient productClient;

    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

    @RequestMapping(value = "", method = {RequestMethod.GET, RequestMethod.POST})
    public Object index() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("spring.aplication.name", applicationName);
        jsonObject.put("micro.svc.product.server", productServer);
        jsonObject.put("micro.svc.product.server.port", 8080);
        jsonObject.put("random.num", RandomStringUtils.randomNumeric(8));
        jsonObject.put("random.string", RandomStringUtils.randomAlphabetic(8));
        jsonObject.put("now", df.format(new Date()));

        JSONObject replyJSON = new JSONObject();
        if (StringUtils.isNotBlank(productServer) && !"unknown".equalsIgnoreCase(productServer)) {
            String msg = RandomStringUtils.randomAlphabetic(8);
            String url = "http://" + this.productServer + ":8080/product/call?msg=" + msg;
            replyJSON.put("micro.svc.product.server.url", url);

            replyJSON.put("send.message", msg);
            try {
                String reply = productClient.call(url);
                replyJSON.put("reply.message", reply);
            } catch (Exception e) {
                replyJSON.put("reply.error", e.getMessage());
                logger.error(e.getMessage(), e);
            }
        } else {
            replyJSON.put("reply.info", "not config server");
        }
        jsonObject.put("reply", replyJSON);
        logger.info("result: " + jsonObject.toJSONString());
        return jsonObject;
    }

    @RequestMapping(value = "call", method = {RequestMethod.GET, RequestMethod.POST})
    public String call(String msg) {
        String result = RandomStringUtils.randomAlphabetic(6);
        String message = String.format("this is %s. i get a message[%s] and gen a random string[%s].",
                this.applicationName, msg, result
        );
        logger.info(message);
        return result;
    }
}
