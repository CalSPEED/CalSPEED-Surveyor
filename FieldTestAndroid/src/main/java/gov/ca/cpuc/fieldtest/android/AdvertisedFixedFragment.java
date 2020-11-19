/*
Copyright (c) 2013, California State University Monterey Bay (CSUMB).
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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AdvertisedFixedFragment extends Fragment {

	AdvertisedData datas;
	String address;

	View view;
	ListView listView;
	TextView address_display;
	LayoutInflater inflaterT;
	ImageView legendImage;

	public AdvertisedFixedFragment(AdvertisedData data, String address) {
		this.datas = data;
		this.address = address;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		int i, j;
		String carrier;
		boolean exist;
		
		// For Debugging
		Log.w("Viewer", "********************* AdvertisedFixedFragment.java - onCreateView() invoked");
		
		view = inflater.inflate(R.layout.adver_fixed_frag, container, false);
		inflaterT = inflater;
		listView = (ListView) view.findViewById(R.id.list);
		
		address_display = (TextView)view.findViewById(R.id.address);
		address_display.setText(address);
		
		//Add legend image here
		legendImage = (ImageView)view.findViewById(R.id.legend);
		legendImage.setImageResource(R.drawable.legend);
		
		TreeSet<DisplayResults> data_list =
                new TreeSet<DisplayResults>(new DisplayResultsComparator());
		for (i = 0; i < datas.features.size(); i++) {
			if (datas.features.get(i).attributes.ServiceTyp.equalsIgnoreCase("Fixed")) {

                // Locally store the attributes in memory
                String dbaName = datas.features.get(i).attributes.dbANAME;
                String maxAdUp = datas.features.get(i).attributes.maxADUP;
                String maxAdDn = datas.features.get(i).attributes.maxADDOWN;
				String techCode = datas.features.get(i).attributes.techCode;

				// For Debugging
				Log.w("Viewer", dbaName + "/(UP)" + maxAdUp + "/(DOWN)" + maxAdDn + "/Tech Name "
                        + techCode);

				DisplayResults temp = new DisplayResults();
                temp.setName(dbaName);
                if (maxAdUp != null) {
                    temp.setImageNumber(Key.upKey(convertBandwidthToBucket(maxAdUp)));
                }

                if (maxAdDn != null) {
                    temp.setImageNumber2(Key.downKey(convertBandwidthToBucket(maxAdDn)));
                }
                temp.convertTechCodeDetail(techCode);
                data_list.add(temp);
			}
		}
				
		// If no carrier exists, display a message.
		if (data_list.size() == 0) {
            address_display.setText("No advertised fixed data in this address.");
        }
		 // Convert the TreeSet to an ArrayList for the MyCustomBaseAdapter constructor.
        Log.d("FixedFragment", "TreeSet is: " + data_list.toString());
        Log.d("FixedFragment", "ArrayList is: " + (new ArrayList<DisplayResults>(data_list)).toString());
		listView.setAdapter(
                new MyCustomBaseAdapter(getActivity(), new ArrayList<DisplayResults>(data_list)));
		return view;
	}

	private String convertBandwidthToBucket(String bandwidth_as_string) {
        Double bandwidth = Double.parseDouble(bandwidth_as_string);
        if (bandwidth < 0.2) {
            return "1";
        } else if (bandwidth >= 0.2 && bandwidth < 0.75) {
            return "2";
        } else if (bandwidth >= 0.75 && bandwidth < 1.5) {
            return "3";
        } else if (bandwidth >= 1.5 && bandwidth < 3) {
            return "4";
        } else if (bandwidth >= 3 && bandwidth < 6) {
            return "5";
        } else if (bandwidth >= 6 && bandwidth < 10) {
            return "6";
        } else if (bandwidth >= 10 && bandwidth < 25) {
            return "7";
        } else if (bandwidth >= 25 && bandwidth < 50) {
            return "8";
        } else if (bandwidth >= 50 && bandwidth < 100) {
            return "9";
        } else if (bandwidth >= 100 && bandwidth < 1000) {
            return "10";
        } else if (bandwidth >= 1000) {
            return "11";
        } else {
            return "null";
        }
    }
	
	@Override
	public void onPause() {
		super.onPause();
		
		// For Debugging
		Log.w("Viewer", "********************* AdvertisedFixedFragment.java - onPause() invoked");

		System.gc();
	}

}
