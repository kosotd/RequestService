package com.kosotd.http;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.omg.CORBA.StringHolder;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Сервис для отправки com.kosotd.http запросов
 */
public class RequestService {
    private static Logger logger = Logger.getLogger(RequestService.class.getName());

    /**
     * начало построения запроса
     * @return класс содержащий методы для построения разных типов com.kosotd.http запросов (get и post)
     */
    public static RequestBuilder build(){
        return new RequestBuilder(30_000);
    }

    /**
     * начало построения запроса
     * @param httpRequestTimeout таймаут запроса
     * @return класс содержащий методы для построения разных типов com.kosotd.http запросов (get и post)
     */
    public static RequestBuilder build(int httpRequestTimeout){
        return new RequestBuilder(httpRequestTimeout);
    }

    /**
     * класс содержащий методы для построения разных типов com.kosotd.http запросов (get и post)
     */
    public static class RequestBuilder {

        private int httpRequestTimeout;

        private RequestBuilder(int httpRequestTimeout) {
            this.httpRequestTimeout = httpRequestTimeout;
        }

        /**
         * создать GET запрос используя builder
         * @param builder для установки параметров запроса
         * @return класс содержащий методы для отправки запросов
         */
        public RequestSender get(Consumer<GetBuilder> builder) {
            GetBuilder getBuilder = new GetBuilder(this);
            builder.accept(getBuilder);
            return getBuilder.getRequestSender();
        }

        /**
         * создать get запрос по url
         * @param url url запроса
         * @return класс содержащий методы для отправки запросов
         */
        public RequestSender get(String url) {
            return get(url, new HashMap<>(), new HashMap<>());
        }

        /**
         * создать GET запрос с указанным url и хидерами
         * @param url url запроса
         * @param headers хидеры
         * @return класс содержащий методы для отправки запросов
         */
        public RequestSender getWithHeaders(String url, Map<String, String> headers) {
            return get(url, headers, new HashMap<>());
        }

        /**
         * создать GET запрос с указанным url и параметрами
         * @param url url запроса
         * @param params параметры
         * @return класс содержащий методы для отправки запросов
         */
        public RequestSender getWithParams(String url, Map<String, String> params) {
            return get(url, new HashMap<>(), params);
        }

        /**
         * создать GET запрос с указанным url, параметрами и хидерами
         * @param url url запроса
         * @param headers хидеры
         * @param params url параметры запроса ?name=value&name1=value1
         * @return класс содержащий методы для отправки запросов
         */
        public RequestSender get(String url, Map<String, String> headers, Map<String, String> params) {
            StringBuilder urlWithParams = new StringBuilder(url);
            if (params.size() > 0) urlWithParams.append("?");
            StringHolder delim = new StringHolder("");
            params.forEach((param, value) -> {
                try {
                    urlWithParams.append(delim.value).append(param).append("=").append(URLEncoder.encode(value, "UTF-8"));
                } catch (Exception e){
                    throw new RuntimeException(e.getMessage());
                }
                delim.value = "&";
            });
            HttpGet get = new HttpGet(urlWithParams.toString());
            headers.forEach(get::setHeader);
            return new RequestSender(httpRequestTimeout, get);
        }

        /**
         * создать POST запрос используя builder
         * @param builder для установки параметров запроса
         * @return класс содержащий методы для отправки запросов
         */
        public RequestSender post(Consumer<PostBuilder> builder) {
            PostBuilder postBuilder = new PostBuilder(this);
            builder.accept(postBuilder);
            return postBuilder.getRequestSender();
        }

        /**
         * создать POST запрос с указанным url
         * @param url url запроса
         * @return класс содержащий методы для отправки запросов
         */
        public RequestSender post(String url) {
            return post(url, new HashMap<>(), new HashMap<>());
        }

        /**
         * создать POST запрос с указанным url и телом
         * @param url url запроса
         * @param body тело запроса
         * @return класс содержащий методы для отправки запросов
         */
        public RequestSender post(String url, String body) {
            return post(url, new HashMap<>(), body);
        }

        /**
         * создать POST запрос с указанным url и хидерами
         * @param url url запроса
         * @param headers хидеры
         * @return класс содержащий методы для отправки запросов
         */
        public RequestSender postWithHeaders(String url, Map<String, String> headers) {
            return post(url, headers, new HashMap<>());
        }

        /**
         * создать POST запрос с указанным url, телом и хидерами
         * @param url url запроса
         * @param headers хидеры
         * @param body тело запроса
         * @return класс содержащий методы для отправки запросов
         */
        public RequestSender post(String url, Map<String, String> headers, String body) {
            HttpPost post = new HttpPost(url);
            headers.forEach(post::setHeader);
            post.setEntity(new ByteArrayEntity(body.getBytes()));
            return new RequestSender(httpRequestTimeout, post);
        }

        /**
         * создать POST запрос с указанным url и параметрами
         * @param url url запроса
         * @param params UrlEncoded параметры
         * @return класс содержащий методы для отправки запросов
         */
        public RequestSender postWithParams(String url, Map<String, String> params) {
            return post(url, new HashMap<>(), params);
        }

        /**
         * создать POST запрос с указанным url, параметрами и хидерами
         * @param url url запроса
         * @param params UrlEncoded параметры
         * @param headers хидеры
         * @return класс содержащий методы для отправки запросов
         */
        public RequestSender post(String url, Map<String, String> headers, Map<String, String> params) {
            try {
                HttpPost post = new HttpPost(url);
                headers.forEach(post::setHeader);
                List<NameValuePair> paramsList = new ArrayList<>();
                params.forEach((s, s2) -> paramsList.add(new BasicNameValuePair(s, s2)));
                post.setEntity(new UrlEncodedFormEntity(paramsList));
                return new RequestSender(httpRequestTimeout, post);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * класс содержащий методы для отправки запросов
     */
    public static class RequestSender {

        private HttpRequestBase request;
        private int httpRequestTimeout;

        private RequestSender(int httpRequestTimeout, HttpRequestBase request) {
            this.httpRequestTimeout = httpRequestTimeout;
            this.request = request;
        }

        /**
         * отправить запрос и вернуть результат как строку
         * ожидаемый статус ответа 200
         * @return ответ на запрос
         */
        public Response send() {
            return send(HttpStatus.OK, false);
        }

        /**
         * отправить указынный запрос, выдает ошибку в случае, если статус ответа не совпадает с expectedStatus,
         * возвращает текст ответа как строку
         * @param expectedStatus ожидаемый статус ответа
         * @return ответ на запрос
         */
        public Response send(HttpStatus expectedStatus) {
            return send(expectedStatus, true);
        }

        private Response send(HttpStatus expectedStatus, boolean expectStatus) {
            Response result = new Response("", new Header[0], 0);

            try {
                RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(httpRequestTimeout).build();
                try (CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build()) {

                    try (CloseableHttpResponse response = client.execute(request)) {
                        result.status = response.getStatusLine().getStatusCode();
                        result.setHeaders(response.getAllHeaders());

                        try (StringWriter writer = new StringWriter()) {
                            try (InputStream stream = response.getEntity().getContent()) {
                                IOUtils.copy(stream, writer);
                                result.setData(writer.toString());
                            }
                        }
                    }
                }
            } catch(Exception e){
                logger.info(e.getMessage());
                throw new RuntimeException("Error while executing the query: " + e.getMessage());
            }

            if (expectStatus && (result.status != expectedStatus.value())) {
                logger.info(result.getData());
                throw new RuntimeException("Expected status is " + expectedStatus.value() + ", but actual " + result.status);
            }

            return result;
        }
    }

    public static class GetBuilder {

        private RequestBuilder requestBuilder;
        private String url;
        private Map<String, String> params = new HashMap<>();
        private Map<String, String> headers = new HashMap<>();

        private GetBuilder(RequestBuilder requestBuilder) {
            this.requestBuilder = requestBuilder;
        }

        public GetBuilder setUrl(String url) {
            this.url = url;
            return this;
        }

        public GetBuilder addParam(String name, String value) {
            params.put(name, value);
            return this;
        }

        public GetBuilder setParams(Map<String, String> params) {
            this.params.clear();
            this.params.putAll(params);
            return this;
        }

        public GetBuilder addHeader(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public GetBuilder setHeaders(Map<String, String> headers) {
            this.headers.clear();
            this.headers.putAll(headers);
            return this;
        }

        private RequestSender getRequestSender() {
            if (url == null)
                throw new RuntimeException("URL not defined");
            return requestBuilder.get(url, headers, params);
        }
    }

    public static class PostBuilder {

        private RequestBuilder requestBuilder;
        private String url;
        private String body;
        private Map<String, String> params = new HashMap<>();
        private Map<String, String> headers = new HashMap<>();

        private PostBuilder(RequestBuilder requestBuilder) {
            this.requestBuilder = requestBuilder;
        }

        public PostBuilder setUrl(String url) {
            this.url = url;
            return this;
        }

        public PostBuilder setBody(String body) {
            this.body = body;
            return this;
        }

        public PostBuilder addParam(String name, String value) {
            params.put(name, value);
            return this;
        }

        public PostBuilder setParams(Map<String, String> params) {
            this.params.clear();
            this.params.putAll(params);
            return this;
        }

        public PostBuilder addHeader(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public PostBuilder setHeaders(Map<String, String> headers) {
            this.headers.clear();
            this.headers.putAll(headers);
            return this;
        }

        private RequestSender getRequestSender() {
            if (url == null)
                throw new RuntimeException("URL not defined");
            if (body == null)
                return requestBuilder.post(url, headers, params);
            else
                return requestBuilder.post(url, headers, body);
        }
    }

    public static class Response {
        private String data;
        private Header[] headers;
        private int status;

        private Response(String data, Header[] headers, int status) {
            this.data = data;
            this.headers = headers;
            this.status = status;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public Header[] getHeaders() {
            return headers;
        }

        public void setHeaders(Header[] headers) {
            this.headers = headers;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String component1(){
            return data;
        }

        public Header[] component2(){
            return headers;
        }

        public int component3(){
            return status;
        }
    }
}
