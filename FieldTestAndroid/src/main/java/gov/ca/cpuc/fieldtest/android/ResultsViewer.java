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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


class ResultsViewer extends ArrayAdapter<ResultsItem> {
	private ArrayList<ResultsItem> result = new ArrayList<ResultsItem>();
    private Context context;

    private static class ViewHolder {
		private TextView text;
		private ImageView image;
	}

	ResultsViewer(Context context, ArrayList<ResultsItem> result) {
		super(context, R.layout.testresultview, result);
		this.result = result;
		this.context = context;

	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context
    				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.testresultview, null);
            
            ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) v.findViewById(R.id.resultsValue);
			viewHolder.image = (ImageView) v.findViewById(R.id.resultsIcon);
			v.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) v.getTag();
		holder.text.setText(result.get(position).getDataText());
		holder.image.setImageResource(result.get(position).getDataImage());
		return (v);
	}

}
