package ecnu.modana.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import redis.clients.jedis.Jedis;

public class File2Redis {

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
            byte[] buffer = new byte[10240];//数组大小应适当大于文件
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
    public Boolean setFile(String key, String path, Jedis jedis)
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
    public static InputStream getInputStream(String key,Jedis jedis)
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


}

