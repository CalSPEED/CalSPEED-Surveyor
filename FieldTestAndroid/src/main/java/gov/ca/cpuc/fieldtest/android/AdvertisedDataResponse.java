/*
Copyright (c) 2020, California State University Monterey Bay (CSUMB).
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above
       copyright notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. Neither the name of the CPUC, CSU Monterey Bay, nor the names of
       its contributors may be used to endorse or promote products derived from
       this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package gov.ca.cpuc.fieldtest.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

public class AdvertisedDataResponse extends AsyncTask<String, Integer, AdvertisedData> {
	public interface Callback {
		void onComplete(AdvertisedData datas);
		void onFailure();
	}

	Context myContext;
	ProgressDialog dialog;

	private Set<Callback> callbacks = new HashSet<Callback>();

	public void addObserver(Callback cb) {
		callbacks.add(cb);
	}

	public AdvertisedDataResponse(Activity activity, ProgressDialog dialog) {
		myContext = activity;
		this.dialog = dialog;
	}

	public AdvertisedDataResponse() {
	}

	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(myContext);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCancelable(false);
		dialog.setMessage("Downloading Data...");
		dialog.show();
	};

	
	@Override
	protected AdvertisedData doInBackground(String... params) {
		String advertisedInfoURL = params[0];
		Log.d("AdvertisedDataResponse", "Got the advertised info url: " + advertisedInfoURL);
		AdvertisedData data = new AdvertisedData();
		
		Reader reader = null;
		BufferedReader buffer = null;

		try {
			InputStream source = retrieveStream(advertisedInfoURL);
			//if the source is null, need to exit
			if(source == null){
				cancel(true); AdvertisedDataResponse.this.cancel(true);
				//return null;
			}
			else{
				reader = new InputStreamReader(source);
			}
		} catch (Exception e) {
			Log.e("AdvertisedDataResponse", "Got an unexpected exception, cancelling.");
			e.printStackTrace();
		}

		Gson gson = new Gson();
		try {
//			buffer.close();
			data = gson.fromJson(reader, AdvertisedData.class);
			if (data == null) {
				Log.e("AdvertisedDataResponse", "Data is null");
			} else {
				Log.d("AdvertisedDataResponse", "Data contents are: " + data.toString());
			}
		}
		catch(JsonSyntaxException e){
			Log.e("AdvertisedDataResponse", "Got an unexpected JSON Syntax exception, cancelling. "
					+ e.getMessage());

			AdvertisedDataResponse.this.cancel(true);
//		} catch (IOException e) {
//			e.printStackTrace();
//			Log.e("AdvertisedDataResponse", "Got an unexpected IOException, cancelling. "
//					+ e.getMessage());
		}
		if((!this.isCancelled())) {
			Log.d("AdvertisedDataResponse", "Got data and converted it.");
			return data;
		} else {
			Log.d("AdvertisedDataResponse", "Data is null");
			return null;
		}
	}

	public void closeDownloadDialog() {
		if (dialog.isShowing()) {
			dialog.dismiss();
		}
	}
	
	@Override
	protected void onPostExecute(AdvertisedData data) {
		if (dialog.isShowing()) {
			dialog.dismiss();
		}
		for (Callback cb : callbacks) {
			cb.onComplete(data);
		}
	}
//	@Override
//	protected void onCancelled() {
//		if (dialog.isShowing()) {
//			dialog.dismiss();
//		}
//	}
	
	private InputStream retrieveStream(String url) {

		DefaultHttpClient client = new DefaultHttpClient();

		HttpGet getRequest = new HttpGet(url);

		try {

			HttpResponse getResponse = client.execute(getRequest);
			final int statusCode = getResponse.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK) {
				Log.w(getClass().getSimpleName(), "Error " + statusCode
						+ " for URL " + url);
				return null;
			}

			HttpEntity getResponseEntity = getResponse.getEntity();
			return getResponseEntity.getContent();

		} catch (IOException e) {
			getRequest.abort();
			Log.w(getClass().getSimpleName(), "Error for URL " + url, e);
		}

		return null;

	}

}