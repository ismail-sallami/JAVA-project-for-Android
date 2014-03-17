package com.goeuro.http;


public interface HttpListner {
    /**
     * @param http
     * Http handler object. when http request is completed then is call in HttpHandler class
     */
    void notifyHTTPRespons(HttpHandler http);
}
