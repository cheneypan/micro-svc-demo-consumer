package com.micro.svc.demo.consumer.web.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by cheney on 2017/8/27.
 */
public class BaseClient {

    protected Logger logger = Logger.getLogger(getClass());

    /**
     * 返回JSON对象
     * @param request
     * @param url
     * @return
     */
    public JSONObject transmitJSONObject(HttpServletRequest request, String url) {
        return transmitJSONObject(request, url, null);
    }

    public JSONObject transmitJSONObject(HttpServletRequest request, String url, Map<String, String> extraMap) {
        String result = transmit(request, url, extraMap);
        if (StringUtils.isNotBlank(result)) {
            return JSON.parseObject(result);
        }
        return null;
    }

    public JSONArray transmitJSONArray(HttpServletRequest request, String url) {
        return transmitJSONArray(request, url, null);
    }

    public JSONArray transmitJSONArray(HttpServletRequest request, String url, Map<String, String> extraMap) {
        String result = transmit(request, url, extraMap);
        if (StringUtils.isNotBlank(result)) {
            return JSON.parseArray(result);
        }
        return null;
    }

    /**
     * 参考：
     * <a href="http://www.2cto.com/kf/201206/134418.html">
     * http转发实现之一：转发代码的实现
     * </a>
     * @param request
     * @param url
     * @return
     */
    @SuppressWarnings("unchecked")
    public String transmit(HttpServletRequest request, String url, Map<String, String> extraMap) {
        logger.info("url: " + url);

        try {
//			HttpServletRequest request = (HttpServletRequest) wrapper.getRequest();

            HttpClient client = HttpClientBuilder.create().build();

            HttpRequestBase method = null;
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                StringBuffer buffer = new StringBuffer();
                Enumeration<String> names = request.getParameterNames();
                if (null != names) {
                    while (names.hasMoreElements()) {
                        String name = (String) names.nextElement();
                        String value = request.getParameter(name);
                        value = URLEncoder.encode(value, "UTF-8");
                        buffer.append(String.format("&%s=%s", name, value));
                    }
                }
                if (null != extraMap) {
                    Set<Map.Entry<String, String>> set = extraMap.entrySet();
                    for (Map.Entry<String, String> entry : set) {
                        buffer.append(String.format("&%s=%s", entry.getKey(), entry.getValue()));
                    }
                }
                String paras = buffer.toString();
                if (StringUtils.isNotBlank(paras)) {
                    paras = paras.substring(1);
                    url = url + "?" + paras;
                }
                logger.info("get url: " + url);

                HttpGet get = new HttpGet(url);
                method = get;
            } else if ("POST".equalsIgnoreCase(request.getMethod())) {
//				InputStreamEntity entity = new InputStreamEntity(request.getInputStream());
                Enumeration<String> names = request.getParameterNames();
                List<NameValuePair> params= new ArrayList<>();
                if(null != names){
                    while (names.hasMoreElements()) {
                        String name = (String) names.nextElement();
                        String value = request.getParameter(name);
                        value = URLEncoder.encode(value, "UTF-8");
                        params.add(new BasicNameValuePair(name, value));
                    }
                }
                if (null != extraMap) {
                    Set<Entry<String, String>> set = extraMap.entrySet();
                    for (Entry<String, String> entry : set) {
                        params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                    }
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, StandardCharsets.UTF_8);
                entity.setContentType("application/x-www-form-urlencoded;charset=utf-8");


                HttpPost post = new HttpPost(url);
                post.setEntity(entity);

                method = post;
            } else {
                throw new Exception("not support request method: " + request.getMethod());
            }

            // header
//			Enumeration<String> headerNames = request.getHeaderNames();
//			if (null != headerNames) {
//				while (headerNames.hasMoreElements()) {
//					String name = (String) headerNames.nextElement();
//					method.setHeader(name, request.getHeader(name));
//				}
//			}
            // TODO 可再添加认证头

            HttpResponse httpResponse = client.execute(method);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new Exception(
                        "Failed : HTTP error core : " + httpResponse.getStatusLine().getStatusCode());
            }
            String result = EntityUtils.toString(httpResponse.getEntity());
            logger.debug("postRequest 返回信息 : " + result);
            return result;
        } catch (Exception e) {
            logger.error("postRequest 请求信息异常", e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public String transmit2(HttpServletRequest request, String url) {
        logger.info("url: " + url);

        try {
            HttpClient client = HttpClientBuilder.create().build();

            HttpRequestBase method = null;
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                HttpGet get = new HttpGet(url);
                method = get;
            } else if ("POST".equalsIgnoreCase(request.getMethod())) {
                InputStreamEntity entity = new InputStreamEntity(request.getInputStream());
//				entity.setContentType("application/x-www-form-urlencoded;charset=utf-8");

                HttpPost post = new HttpPost(url);
                post.setEntity(entity);

                method = post;
            } else {
                throw new Exception("not support request method: " + request.getMethod());
            }

            // header
            Enumeration<String> headerNames = request.getHeaderNames();
            if (null != headerNames) {
                while (headerNames.hasMoreElements()) {
                    String name = (String) headerNames.nextElement();
                    method.addHeader(name, request.getHeader(name));
                }
            }
            // TODO 可再添加认证头

            HttpResponse httpResponse = client.execute(method);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new Exception(
                        "Failed : HTTP error core : " + httpResponse.getStatusLine().getStatusCode());
            }
            String result = EntityUtils.toString(httpResponse.getEntity());
            logger.debug("postRequest 返回信息 : " + result);
            return result;
        } catch (Exception e) {
            logger.error("postRequest 请求信息异常", e);
        }
        return null;
    }
}
