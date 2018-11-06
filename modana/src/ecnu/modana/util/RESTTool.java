package ecnu.modana.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ecnu.modana.FmiDriver.bean.Trace;

public class RESTTool {
	
	//配置服务的ip与端口号
	private static String redisAddress = "47.101.58.16";
	private static int redisPort = 6379;
	//http://localhost:8080/psrma
	public static Trace generateOneTraceWithPSRMA(Map<String, Object> params,String url){
		

//    		Jedis jedis = new Jedis("localhost", 6379);
//    		jedis.set("test", "test");
//		System.out.println(jedis.get("test"));
    	    params.put("redisAddress", redisAddress);
    	    params.put("redisPort", redisPort);    		
    		try {
				URL targetUrl = new URL(url);
				HttpURLConnection httpConnection = (HttpURLConnection)targetUrl.openConnection();
				httpConnection.setDoOutput(true);
                httpConnection.setRequestMethod("POST");
                httpConnection.setRequestProperty("Content-Type", "application/json");
                Gson gson = new Gson();
                String input = gson.toJson(params);
               
                OutputStream outputStream = httpConnection.getOutputStream();
                outputStream.write(input.getBytes());
                outputStream.flush();

                if (httpConnection.getResponseCode() != 200) {
                       throw new RuntimeException("Failed : HTTP error code : "
                              + httpConnection.getResponseCode());
                }

                BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
                              (httpConnection.getInputStream())));

                String output;
                System.out.println("Output from Server:\n");
                StringBuilder resultString = new StringBuilder();
                while ((output = responseBuffer.readLine()) != null) {
                       resultString.append(output);
                }
                httpConnection.disconnect();
                return new Gson().fromJson(resultString.toString(), new TypeToken<Trace>() {}.getType());

           } catch (MalformedURLException e) {

                e.printStackTrace();
                return new Trace(null,null);

           } catch (IOException e) {

                e.printStackTrace();
                return new Trace(null,null);
          }
    		
    }

}
