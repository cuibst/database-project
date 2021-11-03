package database.rzotgorz.recordsystem;

import com.alibaba.fastjson.JSONObject;
import database.rzotgorz.filesystem.FileManager;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Data
@Slf4j
@NoArgsConstructor
public class RecordManager {
    private FileManager fm=new FileManager();
    private Map<String,FileHandler> openFiles=new HashMap();
    private static final ResourceBundle bundle = ResourceBundle.getBundle("configurations");
    private static final int PAGE_SIZE = Integer.parseInt(bundle.getString("PAGE_SIZE"));
    public byte[] changeJSONObjectToBytes(JSONObject header)
    {
        byte[] headerBytes=new byte[54];
        Arrays.fill(headerBytes,(byte)0);
        byte[] result;
        result=ByteIntegerConverter.intToBytes(Integer.parseInt(header.getString("recordNum").toString()));
        System.arraycopy(result,0,headerBytes,0,4);
        result=ByteIntegerConverter.intToBytes(Integer.parseInt(header.getString("recordLen").toString()));
        System.arraycopy(result,0,headerBytes,4,4);
        result=ByteIntegerConverter.intToBytes(Integer.parseInt(header.getString("recordPerPage").toString()));
        System.arraycopy(result,0,headerBytes,8,4);
        result=ByteIntegerConverter.intToBytes(Integer.parseInt(header.getString("bitmapLength").toString()));
        System.arraycopy(result,0,headerBytes,12,4);
        result=ByteIntegerConverter.intToBytes(Integer.parseInt(header.getString("nextVacancyPage").toString()));
        System.arraycopy(result,0,headerBytes,16,4);
        result=ByteIntegerConverter.intToBytes(Integer.parseInt(header.getString("pageNum").toString()));
        System.arraycopy(result,0,headerBytes,20,4);
        byte[] str=header.getString("filename").toString().getBytes(StandardCharsets.UTF_8);
        for(int i=0;i<30&&i<str.length;i++)
        {
            headerBytes[i+24]=str[i];
        }
        return headerBytes;
    }
    public JSONObject changeBytesToJsonObject(byte[] bytes)
    {
        JSONObject header=new JSONObject();
        int result;
        byte[] ans=new byte[4];
        System.arraycopy(bytes,0,ans,0,4);
        result=ByteIntegerConverter.bytesToInt(ans);
        header.put("recordNum",result);
        System.arraycopy(bytes,4,ans,0,4);
        result=ByteIntegerConverter.bytesToInt(ans);
        header.put("recordLen",result);
        System.arraycopy(bytes,8,ans,0,4);
        result=ByteIntegerConverter.bytesToInt(ans);
        header.put("recordPerPage",result);
        System.arraycopy(bytes,12,ans,0,4);
        result=ByteIntegerConverter.bytesToInt(ans);
        header.put("bitmapLength",result);
        System.arraycopy(bytes,16,ans,0,4);
        result=ByteIntegerConverter.bytesToInt(ans);
        header.put("nextVacancyPage",result);
        System.arraycopy(bytes,20,ans,0,4);
        result=ByteIntegerConverter.bytesToInt(ans);
        header.put("pageNum",result);
        ArrayList<Byte> bytes1=new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<30;i++)
        {
            if(bytes[i+24]==0)
                break;
            builder.append((char)bytes[i+24]);
        }
        String str = builder.toString();
        header.put("filename",str);
        return header;
    }
    public void createFile(String filename,int recordLen)
    {
        File file=new File(filename);
        if(!file.exists())
        {
            try{
                file.createNewFile();
                int fileId=fm.openFile(filename);
                int recordPerPage= Header.calPageCapacity(recordLen);
                int bitmapLength=Header.calBitmapLength(recordPerPage);
                JSONObject fileHeader=new JSONObject();
                fileHeader.put("recordNum",0);
                fileHeader.put("recordLen",recordLen);
                fileHeader.put("recordPerPage",recordPerPage);
                fileHeader.put("bitmapLength",bitmapLength);
                fileHeader.put("filename",filename);
                fileHeader.put("nextVacancyPage",0);
                fileHeader.put("pageNum",1);
                byte[] headerBytes=changeJSONObjectToBytes(fileHeader);
                byte[] page=new byte[PAGE_SIZE];
                System.arraycopy(headerBytes,0,page,0,headerBytes.length);
                fm.createPage(fileId,page);
                fm.closeFile(fileId);
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

    }
    public void deleteFile(String filename)
    {
        File file=new File(filename);
        try{
            file.delete();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public FileHandler openFile(String filename){
        if(openFiles.keySet().contains(filename))
            return openFiles.get(filename);
        int fileId=fm.openFile(filename);
        byte[] header=fm.readPage(fileId,0);
        JSONObject fileHeader=changeBytesToJsonObject(header);
        System.out.println(fileHeader);
        FileHandler handler=new FileHandler(filename,fileId,fileHeader,fm);
        openFiles.put(filename,handler);
        return handler;
    }

    public void closeFile(String filename){
        FileHandler handler=openFiles.get(filename);
        if(handler==null)
            return;
        if(handler.getModifyHeader()) {
            JSONObject newHeader=handler.getFileHeader();
            System.out.println(newHeader);
            byte[] headerBytes=changeJSONObjectToBytes(newHeader);
            byte[] page=new byte[PAGE_SIZE];
            System.arraycopy(headerBytes,0,page,0,headerBytes.length);
            fm.putPage(handler.getFileId(),0,page);
        }
        openFiles.remove(filename);
        fm.closeFile(handler.getFileId());
        handler.setOpened(false);
    }
}
