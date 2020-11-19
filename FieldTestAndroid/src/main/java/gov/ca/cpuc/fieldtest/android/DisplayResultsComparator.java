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
