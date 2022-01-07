package database.rzotgorz.indexsystem;

import database.rzotgorz.utils.ByteFloatConverter;
import database.rzotgorz.utils.ByteLongConverter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Data
public class IndexUtility {
    static private byte[] nullBytes = "null".getBytes(StandardCharsets.UTF_8);

    static public int calcSize(List<String> list) {
        int size = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).contains("INT")) {
                size += 9;
            } else if (list.get(i).contains("FLOAT")) {
                size += 5;
            } else if (list.get(i).contains("VARCHAR")) {
                String str = list.get(i).replace(")", "");
                String[] s = str.split("\\(");
                size += Integer.parseInt(s[1]) + 1;
            } else if (list.get(i).contains("DATE")) {
                size += 9;
            }
        }
        return size;
    }

    static public int getStringSize(String s) {
        int size;
        String s2 = s.replace(")", "");
        String[] s1 = s2.split("\\(");
        size = Integer.parseInt(s1[1]);
        return size;
    }


    static public byte[] turnToBytes(int size, IndexContent content, List<String> list) {
        byte[] data = new byte[size];
        Arrays.fill(data, (byte) 0);
        int head = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).contains("INT")) {
                if (content.getIndexList().get(i) == null) {
                    data[head] = 0;
                    head++;
                } else {
                    data[head] = 1;
                    head++;
                    byte[] res;
                    if (content.getIndexList().get(i) instanceof Integer)
                        res = ByteLongConverter.long2Bytes((int) content.getIndexList().get(i));
                    else
                        res = ByteLongConverter.long2Bytes((long) content.getIndexList().get(i));
                    System.arraycopy(res, 0, data, head, 8);
                }
                head += 8;
            } else if (list.get(i).contains("FLOAT")) {
                if (content.getIndexList().get(i) == null) {
                    data[head] = 0;
                    head++;
                } else {
                    data[head] = 1;
                    head++;
                    byte[] res = ByteFloatConverter.float2byte((float) content.getIndexList().get(i));
                    System.arraycopy(res, 0, data, head, 4);
                }
                head += 4;
            } else if (list.get(i).contains("DATE")) {
                if (content.getIndexList().get(i) == null) {
                    data[head] = 0;
                    head++;
                } else {
                    data[head] = 1;
                    head++;
                    byte[] res = ByteLongConverter.long2Bytes((long) content.getIndexList().get(i));
                    System.arraycopy(res, 0, data, head, 8);
                }
                head += 8;
            } else if (list.get(i).contains("VARCHAR")) {
                int len = getStringSize(list.get(i));
                if (content.getIndexList().get(i) == null) {
                    data[head] = 0;
                    head++;
                } else {
                    data[head] = 1;
                    head++;
                    byte[] strData = content.indexList.get(i).toString().getBytes(StandardCharsets.UTF_8);
                    assert (len >= strData.length);
                    System.arraycopy(strData, 0, data, head, strData.length);
                }
                head += len;
            }
        }
        return data;
    }

    static public String processStringBytes(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == (byte) 0)
                break;
            builder.append((char) bytes[i]);
        }
        String str = builder.toString();
        return str;
    }

    static public IndexContent parserBytes(byte[] data, List<String> list) {
        int head = 0;
        List<Comparable> resList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).contains("INT")) {
                byte[] longBytes = new byte[8];
                if (data[head] == (byte) 0) {
                    resList.add(null);
                    head++;
                } else {
                    head++;
                    System.arraycopy(data, head, longBytes, 0, 8);
                    long res = ByteLongConverter.bytes2Long(longBytes);
                    resList.add(res);
                }
                head += 8;
            } else if (list.get(i).contains("FLOAT")) {
                byte[] floatBytes = new byte[4];
                if (data[head] == (byte) 0) {
                    resList.add(null);
                    head++;
                } else {
                    head++;
                    System.arraycopy(data, head, floatBytes, 0, 4);
                    float res = ByteFloatConverter.byte2float(floatBytes);
                    resList.add(res);
                }
                head += 4;
            } else if (list.get(i).contains("VARCHAR")) {
                int len = getStringSize(list.get(i));
                byte[] strBytes = new byte[len];
                if (data[head] == (byte) 0) {
                    resList.add(null);
                    head++;
                } else {
                    head++;
                    System.arraycopy(data, head, strBytes, 0, len);
                    String str = processStringBytes(strBytes);
                    resList.add(str);
                }
                head += len;
            }
        }
        return new IndexContent(resList);
    }
}
