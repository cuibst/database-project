package database.rzotgorz.recordsystem;

import com.alibaba.fastjson.JSONObject;
import database.rzotgorz.filesystem.FileManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.ResourceBundle;

@Slf4j
@Data
public class FileHandler {
    private String filename;
    private int fileId;
    private Boolean opened;
    private JSONObject fileHeader;
    private FileManager fm;
    private Boolean modifyHeader;
    public FileHandler(String filename,int fileId,JSONObject fileHeader,FileManager fm)
    {
        this.filename=filename;
        this.fileId=fileId;
        this.fileHeader=fileHeader;
        this.modifyHeader=false;
        this.opened=true;
        this.fm=fm;
    }
    private static final ResourceBundle bundle = ResourceBundle.getBundle("configurations");
    private static final int HEADER_SIZE=Integer.parseInt(bundle.getString("PAGE_HEADER_SIZE"));
    private static final int PAGE_SIZE=Integer.parseInt(bundle.getString("PAGE_SIZE"));

    public static Logger getLog() {
        return log;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public Boolean getOpened() {
        return opened;
    }

    public void setOpened(Boolean opened) {
        this.opened = opened;
    }

    public JSONObject getFileHeader() {
        return fileHeader;
    }

    public void setFileHeader(JSONObject fileHeader) {
        this.fileHeader = fileHeader;
    }

    public FileManager getFm() {
        return fm;
    }

    public void setFm(FileManager fm) {
        this.fm = fm;
    }

    public Boolean getModifyHeader() {
        return modifyHeader;
    }

    public void setModifyHeader(Boolean modifyHeader) {
        this.modifyHeader = modifyHeader;
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static int getHeaderSize() {
        return HEADER_SIZE;
    }

    public static int getPageSize() {
        return PAGE_SIZE;
    }

    public byte[] getBitmap(byte[] bytes)
    {
        int head=HEADER_SIZE;
        int tail=head+Integer.parseInt(fileHeader.get("bitmapLength").toString());
        return Arrays.copyOfRange(bytes,head,tail);
    }
    public int getNextVacancy(byte[] bytes)
    {
        int offset=Integer.parseInt(bundle.getString("RECORD_NEXT_PAGE"));
        return ByteIntegerConverter.bytesToInt(Arrays.copyOfRange(bytes,offset,offset+4));
    }
    public byte[] setNextVacancy(byte[] bytes,int pageId)
    {
        byte[] vacancyPage=ByteIntegerConverter.intToBytes(pageId);
        for(int i=1;i<5;i++)
            bytes[i]=vacancyPage[i-1];
        return bytes;
    }
    public int getRecordOffset(int slotId)
    {
        return HEADER_SIZE+Integer.parseInt(fileHeader.get("bitmapLength").toString())+Integer.parseInt(fileHeader.get("recordLen").toString())*slotId;
    }
    public byte[] getPage(int pageId)
    {
        return fm.getPageData(fileId,pageId);
    }
    public void putPage(int pageId,byte[] data)
    {
        fm.putPage(fileId,pageId,data);
    }
    public int createPage()
    {
        byte[] bytes=new byte[PAGE_SIZE];
        byte b=0;
        Arrays.fill(bytes,b);
        return fm.createPage(fileId,bytes);
    }
    public byte[] setBitMap(byte[] bitmap,int slotId,Boolean Used)
    {
        for(int bt=0;bt<bitmap.length;bt++)
        {
            for(int bit=0;bit<8;bit++)
            {
                if((bt*8+bit)==slotId)
                {
                    if(Used)
                        bitmap[bt] = (byte)(bitmap[bt] | ((byte)1 << bit));
                    else
                        bitmap[bt] = (byte)(bitmap[bt] & (byte)~( 1<< bit));
                    return bitmap;
                }
            }
        }
        return bitmap;
    }
    public int findVacancySlot(byte[] bitmap)
    {
        int totalLen=Integer.parseInt(fileHeader.get("recordPerPage").toString());
        for(int bt=0;bt<bitmap.length;bt++)
        {
            for(int bit=0;bit<8;bit++)
            {
                if((bt*8+bit)>=totalLen)
                    return -1;
                if(((bitmap[bt]&(1<<bit))>>bit)==0)
                    return (bt*8+bit);
            }
        }
        return -1;
    }
    public Record getRecord(RID rid)
    {
        int pageId=rid.getPageId();
        int slotId=rid.getSlotId();
        byte[] data=fm.readPage(fileId,pageId);
        int offset=this.getRecordOffset(slotId);
        return new Record(rid,Arrays.copyOfRange(data,offset,offset+Integer.parseInt(fileHeader.get("recordLen").toString())));
    }
    public Record insertRecord(byte[] data)
    {
        Record record=new Record();
        record.setData(data);
        int totalLen=Integer.parseInt(fileHeader.get("recordPerPage").toString());
        int recordLen=Integer.parseInt(fileHeader.get("recordLen").toString());
        int vacancyPage=Integer.parseInt(fileHeader.get("nextVacancyPage").toString());
        int bitmapLength=Integer.parseInt(fileHeader.get("bitmapLength").toString());
        byte[] page;
        if(vacancyPage==0) {
            page = this.appendNewPage();
            vacancyPage=Integer.parseInt(fileHeader.get("nextVacancyPage").toString());
        }
        else
            page=fm.getPageData(fileId,vacancyPage);
        byte[] bitmap=getBitmap(page);
        int slotId=findVacancySlot(bitmap);
        int offset=getRecordOffset(slotId);
//        System.out.println("slotId: "+slotId);
        if(data.length!=recordLen)
            log.info("ERROR:: data size {} is not match recordLen {} ",data.length,recordLen);
        for(int i=0;i<recordLen;i++) {
            page[i + offset] = data[i];
        }
        bitmap=setBitMap(bitmap,slotId,true);
        for(int i=0;i<bitmapLength;i++)
            page[i+HEADER_SIZE]=bitmap[i];

        fileHeader.put("recordNum",Integer.parseInt(fileHeader.get("recordNum").toString())+1);
        modifyHeader=true;
        if(findVacancySlot(bitmap)==-1)
        {
            fileHeader.put("nextVacancyPage",this.getNextVacancy(page));
//            System.out.println(getNextVacancy(page));
            page=this.setNextVacancy(page,vacancyPage);
        }
        fm.putPage(fileId,vacancyPage,page);
        record.setRid(new RID(vacancyPage,slotId));
        return record;
    }
    public byte[] appendNewPage()
    {
        int originPageId=Integer.parseInt(fileHeader.get("nextVacancyPage").toString());
        byte[] newData=new byte[PAGE_SIZE];
        byte b=0;
        Arrays.fill(newData,b);
        newData[0]=0;
        newData=this.setNextVacancy(newData,originPageId);
        int pageId=fm.createPage(fileId,newData);
        fileHeader.put("nextVacancyPage",pageId);
        fileHeader.put("pageNum",Integer.parseInt(fileHeader.get("pageNum").toString())+1);
        modifyHeader=true;
        return newData;
    }
    public void deletePage(RID rid)
    {
        int slotId=rid.getSlotId();
        int pageId=rid.getPageId();
        byte[] page=fm.getPageData(fileId,pageId);
        byte[] bitmap=getBitmap(page);
        int bitmapLength=Integer.parseInt(fileHeader.get("bitmapLength").toString());
        setBitMap(bitmap,slotId,false);
        fileHeader.put("recordNum",Integer.parseInt(fileHeader.get("recordNum").toString())-1);
        modifyHeader=true;
        for(int i=0;i<bitmapLength;i++)
            page[i+HEADER_SIZE]=bitmap[i];
        if(getNextVacancy(page)==pageId)
        {
            setNextVacancy(page,Integer.parseInt(fileHeader.get("nextVacancyPage").toString()));
            fileHeader.put("nextVacancyPage",pageId);
        }
        fm.putPage(fileId,pageId,page);
    }
    public void updateRecord(Record record)
    {
        int pageId=record.getRid().getPageId();
        int slotId=record.getRid().getSlotId();
        int recordLen=Integer.parseInt(fileHeader.get("recordLen").toString());
        byte[] data=record.getData();
        if(data.length!=recordLen)
            log.info("ERROR:: data size {} is not match recordLen {} ",data.length,recordLen);
        byte[] page=fm.getPageData(fileId,pageId);
        int offset =getRecordOffset(slotId);
        for(int i=0;i<recordLen;i++)
            page[i+offset]=data[i];
        fm.putPage(fileId,pageId,page);
    }
}
