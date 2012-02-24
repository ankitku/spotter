package com.ankit.spotter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;

import com.facebook.android.*;
import com.facebook.android.Facebook.*;

public class SpotterActivity extends Activity {
	//facebook appid
	private static final String APP_ID = "170372316380836";
	//face.com keys
	private static final String API_KEY = "7b08bfb327d0230711241f359f762288";
	private static final String API_SECRET = "1127306488f90daba70ad107b5b5e580";
	
	private static final int REQ_CODE_PICK_IMAGE = 0;
    private static final String[] PERMISSIONS =
        new String[] {"publish_stream", "read_stream", "offline_access"};

    private Facebook mFacebook;
    private String access_token;
    

    private String filePath;
    

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mFacebook = new Facebook(APP_ID);
        
        Log.d("app_id",APP_ID);
        
        mFacebook.authorize(this, new DialogListener() {
            @Override
            public void onComplete(Bundle values) {
            	access_token = mFacebook.getAccessToken();
            	
                
                //after successful login, getting a pic from gallery
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQ_CODE_PICK_IMAGE); 
            }
                

            @Override
            public void onFacebookError(FacebookError error) {}

            @Override
            public void onError(DialogError e) {}

            @Override
            public void onCancel() {}
        });
        

    }  
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);        
        
        switch(requestCode) { 
        case REQ_CODE_PICK_IMAGE:
            if(resultCode == RESULT_OK){  
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                filePath = cursor.getString(columnIndex);
                cursor.close();
                
                try {
        			postPic();
        		} catch (Exception e1) {
        			// TODO Auto-generated catch block
        			e1.printStackTrace();
        		}

            }
        default:
        	mFacebook.authorizeCallback(requestCode, resultCode, data);
        }
    }
    
    public void postPic() throws Exception
    {
    	HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
        nameValuePairs.add(new BasicNameValuePair("api_key", API_KEY));
        nameValuePairs.add(new BasicNameValuePair("api_secret", API_SECRET));
        nameValuePairs.add(new BasicNameValuePair("uids", "friends@facebook.com"));
        nameValuePairs.add(new BasicNameValuePair("access_token", access_token ));

        HttpPost httppost = new HttpPost("http://api.face.com/faces/recognize.json");
        
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        
        File file = new File(filePath);

        MultipartEntity mpEntity = new MultipartEntity();
        ContentBody cbFile = new FileBody(file, "image/jpeg");
        mpEntity.addPart("userfile", cbFile);
        httppost.setEntity(mpEntity);
       
        
        HttpResponse response = httpclient.execute(httppost);
        

        HttpEntity resEntity = response.getEntity();
        String _response=EntityUtils.toString(resEntity); 

        Log.i("request_",EntityUtils.toString(httppost.getEntity())); 
        Log.i("response_", _response);

        httpclient.getConnectionManager().shutdown();
      }
    }






