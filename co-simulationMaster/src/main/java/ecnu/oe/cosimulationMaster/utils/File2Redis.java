package ecnu.oe.cosimulationMaster.utils;

import ecnu.oe.cosimulationMaster.model.FMU;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import redis.clients.jedis.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class File2Redis {
    public static ShardedJedisPool pool;
    static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(50);
        config.setMaxWaitMillis(3000);
        JedisShardInfo jedisShardInfo = new JedisShardInfo("47.101.58.16",6379);
        jedisShardInfo.setPassword("31592653");
        List<JedisShardInfo> list = new ArrayList<>();
        list.add(jedisShardInfo);
        pool = new ShardedJedisPool(config, list);
    }
    /*
    * 文件转数组
    * */
    public byte[] toByteArray(String path) throws IOException
    {
        if (path == null)
        {
            return null;
        }
        File f = new File(path);
        if (!f.exists())
        {
            return null;
        }
        BufferedInputStream in = null;//创建一个缓冲处理流
        try
        {
            in = new BufferedInputStream(new FileInputStream(f));
            byte[] buffer = new byte[100240];//数组大小应适当大于文件
            int len = 0;
            int i = 0;
            while (-1 != (len = in.read()))//逐个字节读取
            {
                buffer[i] = (byte) len;//读取到的字节放进数组
                i++;
            }
            byte[] buffer2 = new byte[i];//新建一个与文件大小相同的数组
            System.arraycopy(buffer, 0, buffer2, 0, i);//复制大数组中有效内容
            return buffer2;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /*
    * 将文件转成数组存到redis
    * */
    public Boolean setFile(String key, String path, ShardedJedis jedis)
    {
        if (key == null || key.equals("") || path == null || path.equals(""))
        {
            return false;
        }

        if (jedis != null)
        {
            try
            {
                byte[] b = toByteArray(path);
                if (b != null)
                {
                    String json = jedis.set(key.getBytes(), b);
                    if (json != null && json.equals("OK"))
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    return false;
                }

            }
            catch (Exception ex)
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    /*
    * 将redis中的数组读取出来转成输入流
    * */
    public static InputStream getInputStream(String key,ShardedJedis jedis)
    {
        if (key == null || key.equals(""))
        {
            return null;
        }
        if (jedis != null)
        {
            try
            {
                byte[] json = jedis.get(key.getBytes());//取出数组
                if (json != null && json.length > 0)
                {
                    try
                    {
                        InputStream inputStream = new ByteArrayInputStream(json);//转流
                        return inputStream;
                    }
                    catch (Exception e)
                    {
                        return null;
                    }
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                return null;
            }
        }
        else
        {
            return null;
        }
        return null;
    }

    public static ArrayList<String> storeFMUtoLocal(ArrayList<FMU> fmuList, ShardedJedis jedis){
        OutputStream outputStream = null;
        String localAddressPrefix = System.getProperty("user.dir");
        ArrayList<String> storedList = new ArrayList<>();
        for (FMU fmu : fmuList) {
            String address = fmu.getFMUAddress();
            try{
                String storedFile = localAddressPrefix+address.substring(address.lastIndexOf("/"));
                File localFile = new File(storedFile);
                if(localFile.exists())
                    localFile.delete();
                InputStream inputStream = getInputStream(address, jedis);
                byte[] buffer = new byte[1024];
                while (inputStream.read(buffer)!=-1)
                    FileUtils.writeByteArrayToFile(localFile,buffer,true);

                storedList.add(storedFile);

            }catch (IOException e){
                e.printStackTrace();

            }
        }
        return storedList;


    }
    @Test
    public void testJedis(){

        ShardedJedis jd = pool.getResource();
        jd.set("s1", "v1");
        System.out.println(jd.get("s1"));
    }


}

