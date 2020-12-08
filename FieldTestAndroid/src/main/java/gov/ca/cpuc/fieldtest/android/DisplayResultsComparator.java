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

import java.util.Comparator;

/**
 * Created by joshuaahn on 8/8/16.
 */
public class DisplayResultsComparator implements Comparator<DisplayResults> {

    public DisplayResultsComparator() {
        super();
    }

    @Override
    public int compare(DisplayResults a, DisplayResults b) {
        String aName = a.getName();
        String bName = b.getName();

        if (aName.compareTo(bName) == 0) {
            String aTechCode = a.getTechName();
            String bTechCode = b.getTechName();
            if (aTechCode.compareTo(bTechCode) == 0) {
                int aDownload = a.getImageNumber2();
                int bDownload = b.getImageNumber2();
                if (aDownload < bDownload) {
                    return 1;
                } else if(aDownload > bDownload) {
                    return -1;
                } else {
                    int aUpload = a.getImageNumber();
                    int bUpload = b.getImageNumber();
                    return aUpload > bUpload ? 1 : aUpload < bUpload ? -1 : 0;
                }
            } else {
                return aTechCode.compareTo(bTechCode);
            }
        } else {
            return aName.compareTo(bName);
        }
    }


}
