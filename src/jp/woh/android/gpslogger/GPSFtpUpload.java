package jp.woh.android.gpslogger;

import java.io.ByteArrayInputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.io.IOException;
import android.util.Log;

public class GPSFtpUpload {
	String ftpHost;
	String ftpRemoteDir;
	int ftpPort;
	String ftpUserId;
	String ftpPassword;
	int ftpTimeout;
	boolean ftpPassive;
	
	public GPSFtpUpload(GPSSetting config){
		this.ftpHost = config.ftpHost;
		this.ftpPort = config.ftpPort;
		this.ftpUserId = config.ftpUserId;
		this.ftpPassword = config.ftpPassword;
		this.ftpRemoteDir = config.ftpRemoteDir;
		this.ftpTimeout = config.ftpTimeout;
		this.ftpPassive = config.ftpPassive;
	}

	public String upload(String filename,String data){
		return upload(filename,data,ftpTimeout);
	}
	public String upload(String filename,String data,int timeout){
		
		Log.d("GPSLogger",
				"ftpHost: " + ftpHost +
				",ftpPort: " + String.valueOf(ftpPort) + 
				",ftpRemoteDir: " + ftpRemoteDir +
				",ftpTimeout: " + String.valueOf(ftpTimeout)  +
				",ftpPassive: " + String.valueOf(ftpPassive) 
		);

		FTPClient ftPClient = new FTPClient();
		ByteArrayInputStream is = null;
		timeout *= 1000;
		try{
			ftPClient.setDefaultTimeout(timeout);
			Log.d("GPSLogger","FTP connect");
			ftPClient.connect(ftpHost, ftpPort);
			if (FTPReply.isPositiveCompletion(ftPClient.getReplyCode()) == false){
				return "FTP connect error";
			}
			Log.d("GPSLogger","FTP setSoTimeout");
			ftPClient.setSoTimeout(timeout);
			Log.d("GPSLogger","FTP login");
			if (ftPClient.login(ftpUserId, ftpPassword) == false)
				return "FTP login error";
			if (ftpPassive == true){
				Log.d("GPSLogger","FTP enterLocalPassiveMode");
				ftPClient.enterLocalPassiveMode();
			}
				
			Log.d("GPSLogger","FTP changeWorkingDirectory");
			ftPClient.setFileType(FTP.ASCII_FILE_TYPE);
			if (ftPClient.changeWorkingDirectory(ftpRemoteDir) == false)
				return "FTP chdir error";

			org.apache.commons.net.ftp.FTPFile[] arrFiles = ftPClient.listFiles();
			int nTotal = arrFiles.length;
			for (int i = 0; i < nTotal; i++) {
				Log.d("GPSLogger",arrFiles[i].getName());
			}

			Log.d("GPSLogger","ftPClient.list" + ftPClient.listFiles(filename).length);
			if(ftPClient.listFiles(filename).length == 1){
				Log.d("GPSLogger","FTP deleteFile");
				ftPClient.deleteFile(filename);
			}
			
			Log.d("GPSLogger","FTP storeFile");
			is = new ByteArrayInputStream(data.getBytes());
			if(!ftPClient.storeFile(filename, is))
				return "FTP upload error";
/*
			Log.d("GPSLogger","FTP completePendingCommand");
			if(!ftPClient.completePendingCommand())
				return "FTP complete error";
*/			
			Log.d("GPSLogger","FTP logout");
			if(!ftPClient.logout())
				return "FTP logout error";
			
			return "success";
		}catch(SocketException e){
			e.printStackTrace();
			return "SocketException";
		}catch(SocketTimeoutException e){
			e.printStackTrace();
			return "SocketTimeoutException";
		}catch(IOException e){
			e.printStackTrace();
			return "IOException";
		}finally{
			if(is != null)
				try{is.close();} catch(Exception e){e.printStackTrace();}
			if(ftPClient.isConnected())
				try{ftPClient.disconnect();} catch(Exception e){e.printStackTrace();}
		}
	}
	
	public String test(){
		return upload("test.txt","test data");
	}

	public void uploadTest() throws Exception{   

	    String ftpHost = "[Host]";
	    String ftpRemoteDir = "[RemoteDir]";
	    int ftpPort = 21;
	    String ftpUserId = "[UserId]";
	    String ftpPassword = "[Password]";
	    int ftpTimeout = 10;
	    boolean ftpPassive = true;
	    String filename = "[Remote File Name]";
	    String data = "[Remote File Data]";
	    int ftpFileType = FTP.ASCII_FILE_TYPE;  // BINARY_FILE_TYPE

	    FTPClient ftPClient = new FTPClient();
	    ByteArrayInputStream is = null;
	    try{
	        ftPClient.setDefaultTimeout(ftpTimeout * 1000);

	        ftPClient.connect(ftpHost, ftpPort);
	        if (FTPReply.isPositiveCompletion(ftPClient.getReplyCode()) == false){
	            throw new Exception("FTP connect error");
	        }

	        ftPClient.setSoTimeout(ftpTimeout * 1000);

	        if (ftPClient.login(ftpUserId, ftpPassword) == false)
	            throw new Exception("FTP login error");

	        if (ftpPassive == true){
	            ftPClient.enterLocalPassiveMode();
	        }

	        ftPClient.setFileType(ftpFileType);
	        if (ftPClient.changeWorkingDirectory(ftpRemoteDir) == false)
	            throw new Exception("FTP chdir error");

	        Log.d("uploadTest","FTP storeFile");
	        is = new ByteArrayInputStream(data.getBytes());
	        if(!ftPClient.storeFile(filename, is))
	            throw new Exception("FTP upload error");

	        if(!ftPClient.completePendingCommand())
	            throw new Exception("FTP complete error");

	        if(!ftPClient.logout())
	            throw new Exception("FTP logout error");

	    }catch(SocketException e){
	        e.printStackTrace();
	        throw e;
	    }catch(SocketTimeoutException e){
	        e.printStackTrace();
	        throw e;
	    }catch(IOException e){
	        e.printStackTrace();
	        throw e;
	    }finally{
	        if(is != null)
	            try{is.close();} catch(Exception e){}
	        if(ftPClient.isConnected())
	            try{ftPClient.disconnect();}catch(Exception e){}
	    }
	}
	
}
