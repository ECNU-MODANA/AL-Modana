package ecnu.modana.alsmc.ftp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 * 
 * @author JKQ
 *
 * 2016��7��11������8:16:19
 */
public class FtpUtils {

 private FTPClient ftpClient;
 private String fileName, strencoding;
 private String ip = "182.254.215.240";        // ������IP��ַ
 private String userName = "ftpadmin";        // �û���
 private String userPwd = "jiangkaiqiang";        // ����
 private int port = 21;      // �˿ں�
 private String path = "AL-SMC";        // ��ȡ�ļ��Ĵ��Ŀ¼

 /**
  * init ftp servere
  */
 public FtpUtils() {
  this.reSet();
 }

 public void reSet() {
  // �Ե�ǰϵͳʱ��ƴ���ļ���
  fileName = "ftp.txt";
  strencoding = "UTF-8";
  this.connectServer(ip, port, userName, userPwd, path);
 }

 /**
  * @param ip
  * @param port
  * @param userName
  * @param userPwd
  * @param path
  * @throws SocketException
  * @throws IOException function:���ӵ�������
  */
 public void connectServer(String ip, int port, String userName, String userPwd, String path) {
  ftpClient = new FTPClient();
  try {
   // ����
   ftpClient.connect(ip, port);
   // ��¼
   ftpClient.login(userName, userPwd);
   if (path != null && path.length() > 0) {
    // ��ת��ָ��Ŀ¼
    ftpClient.changeWorkingDirectory(path);
   }
  } catch (SocketException e) {
   e.printStackTrace();
  } catch (IOException e) {
   e.printStackTrace();
  }
 }

 /**
  * @throws IOException function:�ر�����
  */
 public void closeServer() {
  if (ftpClient.isConnected()) {
   try {
    ftpClient.logout();
    ftpClient.disconnect();
   } catch (IOException e) {
    e.printStackTrace();
   }
  }
 }

 /**
  * @param path
  * @return function:��ȡָ��Ŀ¼�µ��ļ���
  * @throws IOException
  */
 public List<String> getFileList(String path) {
  List<String> fileLists = new ArrayList<String>();
  // ���ָ��Ŀ¼�������ļ���
  FTPFile[] ftpFiles = null;
  try {
   ftpFiles = ftpClient.listFiles(path);
  } catch (IOException e) {
   e.printStackTrace();
  }
  for (int i = 0; ftpFiles != null && i < ftpFiles.length; i++) {
   FTPFile file = ftpFiles[i];
   if (file.isFile()) {
    fileLists.add(file.getName());
   }
  }
  return fileLists;
 }

 /**
  * @param fileName
  * @return function:�ӷ������϶�ȡָ�����ļ�
  * @throws ParseException
  * @throws IOException
  */
 public String readFile() throws ParseException {
  InputStream ins = null;
  StringBuilder builder = null;
  try {
   // �ӷ������϶�ȡָ�����ļ�
   ins = ftpClient.retrieveFileStream(fileName);
   BufferedReader reader = new BufferedReader(new InputStreamReader(ins, strencoding));
   String line;
   builder = new StringBuilder(150);
   while ((line = reader.readLine()) != null) {
    builder.append(line);
   }
   reader.close();
   if (ins != null) {
    ins.close();
   }
   // ��������һ��getReply()�ѽ�������226���ѵ�. �������ǿ��Խ���������null����
   ftpClient.getReply();
  } catch (IOException e) {
   e.printStackTrace();
  }
  return builder.toString();
 }

 /**
  * @param fileName function:ɾ���ļ�
  */
 public void deleteFile(String fileName) {
  try {
   ftpClient.deleteFile(fileName);
  } catch (IOException e) {
   e.printStackTrace();
  }
 }
 
/**
 * writeFile
 * @param fileContent
 * @param path
 */
 public void writeFile(String fileContent){
	 InputStream is = null;  
     // 1.������  
     is = new ByteArrayInputStream(fileContent.getBytes());  
     // 4.ָ��д���Ŀ¼  
     try {
		//ftpClient.changeWorkingDirectory(path);
		ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);  
		ftpClient.storeFile(fileName, is);  
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}  
     // 5.д����  
 }
 /**
  * @param args
  * @throws ParseException
  */
 public static void main(String[] args) throws ParseException {
  FtpUtils ftp = new FtpUtils();
  ftp.writeFile("user");
  String str = ftp.readFile();
  System.out.println(str);
 }
}