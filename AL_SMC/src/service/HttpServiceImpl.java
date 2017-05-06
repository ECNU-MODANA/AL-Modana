package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class HttpServiceImpl implements HttpService {

	@Override
	public String sendGet(String url) {

		return sendGet(url, 10 * 1000);
	}

	@Override
	public String sendGet(String url, int timeout) {
		String result = "";
		BufferedReader in = null;
		try {
			String urlNameString = url;
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连�?
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属�?
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.setConnectTimeout(2000);
			connection.setReadTimeout(timeout);
			// 建立实际的连�?
			connection.connect();
			// 获取�?有响应头字段
			Map<String, List<String>> map = connection.getHeaderFields();
			// 遍历�?有的响应头字�?
			for (String key : map.keySet()) {
				System.out.println(key + "--->" + map.get(key));
			}
			// 定义 BufferedReader输入流来读取URL的响�?
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 使用finally块来关闭输入�?
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

		return result;
	}

	@Override
	public String sendPost(String url, String params) {
		return sendPost(url, params, 10);
	}

	public String sendPost(String url, String params, int secTimeout) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连�?
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属�?
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");

			// 发�?�POST请求必须设置如下两行
			conn.setConnectTimeout(2000);
			conn.setReadTimeout(secTimeout * 1000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发�?�请求参�?

			// String encodedParams = URLEncoder.encode(params,"utf-8");
			out.print(params);
			// flush输出流的缓冲
			out.flush();

			// 定义BufferedReader输入流来读取URL的响�?
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;

			boolean isFirst = true;
			while ((line = in.readLine()) != null) {
				if (isFirst) {
					result += line;
					isFirst = false;
				} else {
					result += "\n" + line;
				}
			}

		} catch (Exception e) {
			System.out.println("发�?? POST 请求出现异常�?" + e);
			e.printStackTrace();
		} finally {
			// 关闭输出流�?�输入流
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}

		return result;
	}

}
