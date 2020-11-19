package gov.ca.cpuc.fieldtest.android;

/**
 * Created by joshuaahn on 3/3/17.
 */
public class ResultsItem {

    private Integer dataImage;
    private String dataText;

    public ResultsItem(Integer image, String text){
        this.dataImage = image;
        this.dataText = text;
    }

    String getDataText(){
        return(this.dataText);
    }

    Integer getDataImage(){
        return(this.dataImage);
    }

    void setDataImage(Integer newDataImage) {
        this.dataImage = newDataImage;
    }

    void setText(String text) {
        this.dataText = text;
    }
}