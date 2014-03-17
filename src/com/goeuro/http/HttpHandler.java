package com.goeuro.http;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HttpHandler {

	/**
	 * refrence of HttpListener interface
	 */
	HttpListner httpListener;
	/**
	 * url for example http://wwww.google.com
	 */
	String urlstring = "";
	/**
	 * past request parameter items
	 */
	List<NameValuePair> postRequestParameters = null;
	/**
	 * Http unique reqest id
	 */
	int reqId = 0;
	/**
	 * hold the http response
	 */
	String resMessage = "No Response Please check Url or it may be https certificate issue.";
	/**
	 * response code
	 */
	int resCode = -1;
	Hashtable<String, String> header = null;

	/**
	 * @param urlstring
	 *            requested url
	 * @param requestParameters
	 *            list post parameters if get request then null
	 * @param header
	 *            list of header
	 * @param reqId
	 *            url request id
	 */
	public HttpHandler(String urlstring,
			final List<NameValuePair> requestParameters,
			final Hashtable<String, String> header, int reqId) {
		this.urlstring = urlstring;
		this.postRequestParameters = requestParameters;
		this.reqId = reqId;
		this.header = header;
	}

	/**
	 * @return reqest id for request
	 */
	public int getReqId() {
		return reqId;
	}

	/**
	 * Return requested url
	 * 
	 * @return
	 */
	public String getURL() {
		return urlstring;
	}

	/**
	 * @return the response
	 */
	public String getResponse() {

		return resMessage;
	}

	/**
	 * Return Response Code
	 * 
	 * @return
	 */
	public int getResCode() {
		return resCode;
	}

	/**
	 * @param httpListener
	 *            add the listener for notify the response
	 */
	public void addHttpLisner(HttpListner httpListener) {
		this.httpListener = httpListener;
	}

	/**
	 * send the http or https request
	 */

	public void sendRequest() {
		// TODO Auto-generated method stub
		try {
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https",
					new EasySSLSocketFactory(), 443));

			HttpParams params = new BasicHttpParams();
			params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 50000);
			params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
			params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
					new ConnPerRouteBean(30));
			params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			ClientConnectionManager cm = new SingleClientConnManager(params,
					schemeRegistry);
			HttpClient httpclient = new DefaultHttpClient(cm, params);
			// DefaultHttpClient httpclient = null;
			if (postRequestParameters != null) {// //////// for post request
				// POST the envelope
				HttpPost httppost = new HttpPost(urlstring);
				if (header != null) {
					Enumeration enums = header.keys();
					while (enums.hasMoreElements()) {
						String key = (String) enums.nextElement();

						String value = header.get(key);
						httppost.addHeader(key, value);
					}
				}
				httppost.setEntity(new UrlEncodedFormEntity(
						postRequestParameters));
				// Response handler
				ResponseHandler<String> reshandler = new ResponseHandler<String>() {
					// invoked when client receives response
					public String handleResponse(HttpResponse response)
							throws ClientProtocolException, IOException {
						// get response entity
						HttpEntity entity = response.getEntity();
						// get response code
						resCode = response.getStatusLine().getStatusCode();
						// read the response as byte array
						StringBuffer out = new StringBuffer();
						byte[] b = EntityUtils.toByteArray(entity);
						// write the response byte array to a string buffer
						out.append(new String(b, 0, b.length));
						return out.toString();
					}
				};

				resMessage = httpclient.execute(httppost, reshandler);
				// Log.d("", "Response=====" + resMessage);

			} else {// ///////// for get Request

				ResponseHandler<String> responsehandler = new ResponseHandler<String>() {

					@Override
					public String handleResponse(HttpResponse response)
							throws ClientProtocolException, IOException {
						// TODO Auto-generated method stub
						HttpEntity entity = response.getEntity();
						// get response code
						resCode = response.getStatusLine().getStatusCode();
						// read the response as byte array
						StringBuffer out = new StringBuffer();
						byte[] b = EntityUtils.toByteArray(entity);
						// write the response byte array to a string buffer
						out.append(new String(b, 0, b.length));
						return out.toString();
					}
				};
				HttpGet httpget = new HttpGet(urlstring);
				resMessage = httpclient.execute(httpget, responsehandler);

			}
			// close the connection
			httpclient.getConnectionManager().shutdown();
		} catch (Exception e) {
			Log.i("connection Exeception", e.getMessage());
		} finally {

			httpListener.notifyHTTPRespons(this);
		}

	}

}
