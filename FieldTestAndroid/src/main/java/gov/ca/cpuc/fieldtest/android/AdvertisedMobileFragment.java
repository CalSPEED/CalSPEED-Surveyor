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

import java.util.ArrayList;
import java.util.TreeSet;


// import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class AdvertisedMobileFragment extends Fragment{

	AdvertisedData datas= new AdvertisedData();
	String address;
	
	View view;
	ListView listView;
	TextView address_display;
	LayoutInflater inflaterT;
	ImageView legendImage;

	public AdvertisedMobileFragment(AdvertisedData datas, String address)
	{
		this.datas=datas;
		this.address = address;
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		Log.w("Viewer", "********************* AdvertisedMobileFragment.java - onCreateView() invoked");
		
		view = inflater.inflate(R.layout.adver_mobile_frag, container, false);
        inflaterT = inflater;
        listView = (ListView) view.findViewById(R.id.list);
        
		address_display = (TextView)view.findViewById(R.id.address);
		address_display.setText(address);
		
		//Add legend image here
		legendImage = (ImageView)view.findViewById(R.id.legend);
		legendImage.setImageResource(R.drawable.legend);

        TreeSet<DisplayResults> data_list =
				new TreeSet<DisplayResults>(new DisplayResultsComparator());
		for(int i=0; i < datas.features.size(); i++)
		{
			if(datas.features.get(i).attributes.ServiceTyp.equalsIgnoreCase("Mobile"))
			{
				String dbaName = datas.features.get(i).attributes.dbANAME;
				String maxAdUp = datas.features.get(i).attributes.interpMBUp;
				String maxAdDn = datas.features.get(i).attributes.interpMBDN;
				String techCode = datas.features.get(i).attributes.techCode;

				// For Debugging
				Log.v("Viewer", dbaName + "/(UP)" + maxAdUp + "/(DOWN)" + maxAdDn + "/Tech Code "
						+ techCode);

				DisplayResults temp= new DisplayResults();
				temp.setName(dbaName);
				temp.setImageNumber(Key.upKey(maxAdUp));
				temp.setImageNumber2(Key.downKey(maxAdDn));
				temp.convertTechCodeDetail(techCode);
				data_list.add(temp);
			}
		}
		
		// If no carrier exists, display a message.
		if (data_list.size() == 0)
		{
			address_display.setText("No advertised mobile data in this address.");
		}
        listView.setAdapter(
				new MyCustomBaseAdapter(getActivity(), new ArrayList<DisplayResults>(data_list)));
        return view;
	}
	
}