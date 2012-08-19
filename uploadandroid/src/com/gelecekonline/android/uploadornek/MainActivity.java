/*******************************************************************************
 * Copyright 2012 Muharrem Tac
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.gelecekonline.android.uploadornek;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	/**
	 * UPLOAD_IMAGE_URL sabitini kendi sunucunuza gore ayarlayiniz
	 */
	private static final String UPLOAD_IMAGE_URL = "http://10.0.2.2:8080/uploadserver/api/upload";
	private static final int SELECT_IMAGE_INTENT_ID = 1;
	
	ImageView selectedImageView;
	Button selectImageButton;
	Button uploadButton;
	
	ProgressDialog dialog;
	
	Bitmap bitmap;
	String dosyaAdi;
	
	UploadImageTask uploadImageTask;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectedImageView = (ImageView) findViewById(R.id.selectedImageView);
        selectImageButton = (Button) findViewById(R.id.selectImageButton);
        selectImageButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					//Resim secen Intent tanimlamasi
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), SELECT_IMAGE_INTENT_ID);
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), getString(R.string.select_image_problem), Toast.LENGTH_LONG).show();
				}
			}
		});
        
        uploadButton = (Button) findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(selectedImageView.getDrawable()==null){
					Toast.makeText(getApplicationContext(), getString(R.string.please_select_image), Toast.LENGTH_LONG).show();
				}else{
					dialog = ProgressDialog.show(MainActivity.this, getString(R.string.image_uploading), getString(R.string.please_wait), true);
					if (uploadImageTask == null || uploadImageTask.getStatus() == AsyncTask.Status.FINISHED){
						uploadImageTask = new UploadImageTask();
					}
					uploadImageTask.execute();
				}
			}
		});
    }

    /**
     * Activity kapandiginda AsyncTaski sonlandiriyoruz
     */
    @Override
    protected void onDestroy() {
    	if (uploadImageTask != null && uploadImageTask.getStatus() != AsyncTask.Status.FINISHED)
			uploadImageTask.cancel(true);
    	super.onDestroy();
    }
    
    /**
     * Secilen resim bu methoda gelir
     */
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			Uri selectedImageUri = data.getData();
			String filePath = null;

			try {
				String fileManagerString = selectedImageUri.getPath();

				String selectedImagePath = getPath(selectedImageUri);

				if (selectedImagePath != null) {
					filePath = selectedImagePath;
				} else if (fileManagerString != null) {
					filePath = fileManagerString;
				} else {
					Toast.makeText(getApplicationContext(), getString(R.string.unknown_path), Toast.LENGTH_LONG).show();
				}

				if (filePath != null) {
					decodeFile(filePath);
					dosyaAdi = FilenameUtils.getName(filePath);
				} else {
					bitmap = null;
				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), getString(R.string.internal_error), Toast.LENGTH_LONG).show();
				Log.e(e.getClass().getName(), e.getMessage(), e);
			}
		}
	}
    
    /**
     * Content resolverdan data cekmek icin query
     * @param uri
     * @return
     */
    public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}
    
    
    /**
     * Islemi asenkron yapmak icin Threadden daha gelismis bir sinif olan ve Android icin onerilen AsyncTask kullaniyoruz
     * Resim dosyasini bu sinifta sunucuya gonderecegiz. Bu sinif icinde Apache httpmime kutuphanesine ihtiyac duyuyoruz
     * @author mtac
     *
     */
    class UploadImageTask extends AsyncTask <Void, Void, String>{
		
		String sResponse;
		protected String doInBackground(Void... unsued) {
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpContext localContext = new BasicHttpContext();
				HttpPost httpPost = new HttpPost(UPLOAD_IMAGE_URL);

				MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				entity.addPart("dosyaAdi", new StringBody(dosyaAdi,"text/plain", Charset.forName("UTF-8")));
				if(dosyaAdi!=null){
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bitmap.compress(CompressFormat.JPEG, 100, bos);
					byte[] data = bos.toByteArray();
					entity.addPart("dosya", new ByteArrayBody(data, dosyaAdi));
				}
				httpPost.setEntity(entity);
				HttpResponse response = httpClient.execute(httpPost, localContext);
				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
				sResponse = reader.readLine();
				return sResponse;
			} catch (Exception e) {
				if (dialog.isShowing())
					dialog.dismiss();
				Log.e("TAG", e.getMessage());
				return null;
			}
		}

		@Override
		protected void onProgressUpdate(Void... unsued) {

		}

		@Override
		protected void onPostExecute(String response) {
			try {
				if (dialog.isShowing())
					dialog.dismiss();

				if (response!=null) {
					Toast.makeText(MainActivity.this, R.string.image_upload_successfull, Toast.LENGTH_LONG).show();
				}	
			} catch (Exception e) {
				Toast.makeText(MainActivity.this, R.string.image_upload_problem, Toast.LENGTH_LONG).show();
			}
		}
	}
    
    /**
     * Gelen resim dosyasini kodlama ve boyutlandirma islemi icin method
     * @param filePath
     */
    public void decodeFile(String filePath) {
		 try {
	        	if(filePath!=null){
	                BitmapFactory.Options o = new BitmapFactory.Options();
	                o.inJustDecodeBounds = true;
	                BitmapFactory.decodeStream(new FileInputStream(filePath),null,o);
	                final int REQUIRED_SIZE=350;
	                int width_tmp=o.outWidth, height_tmp=o.outHeight;
	                int scale=1;
	                while(true){
	                    if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
	                        break;
	                    width_tmp/=2;
	                    height_tmp/=2;
	                    scale++;
	                }
	                
	                BitmapFactory.Options o2 = new BitmapFactory.Options();
	                o2.inSampleSize=scale;
	                bitmap = BitmapFactory.decodeStream(new FileInputStream(filePath), null, o2);
	        	}
	        } catch (Exception e) {
	        	Log.e("FriendsImageLoader", "error", e);
	        }
		 selectedImageView.setImageBitmap(bitmap);
	}
    
}
