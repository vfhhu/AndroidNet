package xyz.vfhhu.lib.net.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by leo on 2015/7/10.
 */
public class Http {

    public static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }


    public static byte[] GetByte( String strURL ) {
        String RetStr="";
        byte[] retBuffer=new byte[0];
        try  {
            URL pageUrl = new URL(strURL );
            byte[] buffer = new byte[4096];
            // 讀入網頁(位元串流)
            BufferedInputStream bis = new BufferedInputStream(pageUrl.openStream());
            for (;;) {
                int data = bis.read(buffer);
                // Check for EOF
                if (data == -1)
                    break;
                byte[] tmpBuffer = new byte[data+retBuffer.length];
                System.arraycopy(retBuffer, 0, tmpBuffer, 0, retBuffer.length);
                System.arraycopy(buffer, 0, tmpBuffer, retBuffer.length, data);
                retBuffer=tmpBuffer;
                //RetStr+=(char) data;
            }
            bis.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return retBuffer;
    }
    public static String Get( String strURL ) {
        String RetStr="";
        try  {
            URL pageUrl = new URL(strURL );

            // 讀入網頁(位元串流)
            BufferedInputStream bis = new BufferedInputStream(pageUrl.openStream());
            for (;;) {
                int data = bis.read();
                // Check for EOF
                if (data == -1)
                    break;
                RetStr+=(char) data;
            }
            bis.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return RetStr;
    }
    public static String Post(String strURL,String para) throws Exception {

        String url = strURL;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        //con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "data="+para;

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());
        return response.toString();
    }





    //====================upload file

    final public static int UPLOAD_CODE_NOTFILE=1;
    final public static int UPLOAD_CODE_URL_EXCEPTION=2;
    final public static int UPLOAD_CODE_EXCEPTION=3;
    public static String uploadFile(String upLoadServerUri ,String sourceFileUri, Map<String, String> parmas) throws Exception {
        return uploadFile(upLoadServerUri ,sourceFileUri, parmas,"uploaded_file");
    }
    public static String uploadFile(String upLoadServerUri ,String sourceFileUri, Map<String, String> parmas,String InputFileName) throws Exception {
        StringBuffer response = new StringBuffer();
        int serverResponseCode = 0;


        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        String[] q = sourceFileUri.split("/");
        int idx = q.length - 1;
        fileName=q[idx];



        if (!sourceFile.isFile()) {
            throw new Exception("Failed to upload code:" + UPLOAD_CODE_NOTFILE + " ,is not file" );
        }else {
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
                //conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);



//                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name='"+InputFileName+"';filename='"+ fileName + "'" + lineEnd);
                dos.writeBytes("Content-Type: image/jpeg"  + lineEnd);
                dos.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
                dos.writeBytes(lineEnd);


                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);



                Iterator<String> keys = parmas.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = parmas.get(key);

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                    dos.writeBytes("Content-Type: text/plain" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(value);
                    dos.writeBytes(lineEnd);
                }
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                fileInputStream.close();
                dos.flush();
                dos.close();




                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
//                String serverResponseMessage = conn.getResponseMessage();
//                Log.i("uploadFile", "HTTP Response is : "
//                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){

                }

                //close the streams //
                ;

                BufferedReader in = new BufferedReader( new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String inputLine;


                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println(response.toString());
                //MLog.i("uploadFile", "HTTP Response is : "  + serverResponseCode + ": " + response.toString());



            } catch (MalformedURLException ex) {
                throw ex;
            } catch (Exception e) {
                throw e;
            }
            return response.toString();

        } // End else block
    }


    public static String uploadFileImage(String upLoadServerUri ,String sourceFileUri, Map<String, String> parmas) throws Exception {
        return uploadFileImage(upLoadServerUri ,sourceFileUri, parmas,"uploaded_file");
    }
    public static String uploadFileImage(String upLoadServerUri ,String sourceFileUri, Map<String, String> parmas,String InputFileName) throws Exception {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(sourceFileUri, options);
        int width = options.outWidth;
        int height = options.outHeight;
        String type = options.outMimeType;



        int inSampleSize=1;
        int reqW=width;
        if(width>height){
            reqW=height;
        }
        reqW=reqW/2;
//        MLog.d(TAG,"-----------------------------reqW:"+reqW);
        if (reqW >= 480) {
            while ((reqW / inSampleSize) >= 480) {
                inSampleSize ++;
            }
        }
        options.inSampleSize=inSampleSize;
        options.inJustDecodeBounds = false;
        return uploadFileImage(upLoadServerUri ,sourceFileUri, parmas,InputFileName,options);
    }
    private static String uploadFileImage(String upLoadServerUri ,String sourceFileUri, Map<String, String> parmas,String InputFileName,final BitmapFactory.Options options) throws Exception {
        StringBuffer response = new StringBuffer();
        int serverResponseCode = 0;


        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        String[] q = sourceFileUri.split("/");
        int idx = q.length - 1;
        fileName=q[idx];



        if (!sourceFile.isFile()) {
            throw new Exception("Failed to upload code:" + UPLOAD_CODE_NOTFILE + " ,is not file" );
        }else {
            try {
                URL url = new URL(upLoadServerUri);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
                //conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name='"+InputFileName+"';filename='"+ fileName + "'" + lineEnd);
                dos.writeBytes("Content-Type: image/jpeg"  + lineEnd);
                dos.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
                dos.writeBytes(lineEnd);

                // open a URL connection to the Servlet
//                MLog.d(TAG,"-----------------------------sourceFileUri:"+sourceFileUri);
//                MLog.d(TAG,"-----------------------------options.inSampleSize:"+options.inSampleSize+","+options.outWidth);

                Bitmap bmp = BitmapFactory.decodeFile(sourceFileUri,options);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                InputStream in = new ByteArrayInputStream(bos.toByteArray());

                bytesAvailable = in.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = in.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = in.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = in.read(buffer, 0, bufferSize);
                }
                in.close();
                //FileInputStream fileInputStream=new FileInputStream(bos.toByteArray());
                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                Iterator<String> keys = parmas.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = parmas.get(key);

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                    dos.writeBytes("Content-Type: text/plain" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(value);
                    dos.writeBytes(lineEnd);
                }
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                dos.flush();
                dos.close();

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
//                String serverResponseMessage = conn.getResponseMessage();
//                Log.i("uploadFile", "HTTP Response is : "
//                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){
                }
                BufferedReader inBufferedReader = new BufferedReader( new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String inputLine;


                while ((inputLine = inBufferedReader.readLine()) != null) {
                    response.append(inputLine);
                }
                inBufferedReader.close();
                System.out.println(response.toString());
                //MLog.i("uploadFile", "HTTP Response is : "  + serverResponseCode + ": " + response.toString());



            } catch (MalformedURLException ex) {
                throw ex;
            } catch (Exception e) {
                throw e;
            }
            return response.toString();

        } // End else block
    }
}
