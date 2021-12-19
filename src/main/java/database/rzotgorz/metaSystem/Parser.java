package database.rzotgorz.metaSystem;

import database.rzotgorz.exceptions.TypeException;
import database.rzotgorz.recordsystem.ByteIntegerConverter;
import database.rzotgorz.recordsystem.Record;
import database.rzotgorz.utils.ByteFloatConverter;
import database.rzotgorz.utils.ByteLongConverter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Data
@Slf4j
public class Parser {
    public static Calendar parserDate(String s) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(new Date(s));
        } catch (Exception e) {
            throw new TypeException(String.format("cannot converter %s to Date type", s));
        }
        return calendar;
    }

    public static byte[] parserType(String value, String type) {

        switch (type) {
            case "INT": {
                if (value == null || value.equals("null")) {
                    int _value;
                    _value = 0;
                    byte[] process = ByteIntegerConverter.intToBytes(_value);
                    byte[] bytes = new byte[process.length + 1];
                    bytes[0] = (byte) 0;
                    for (int i = 0; i < process.length; i++) {
                        bytes[i + 1] = process[i];
                    }
                    return process;
                } else {
                    try {
                        int _value = Integer.parseInt(value);
                        byte[] process = ByteIntegerConverter.intToBytes(_value);
                        byte[] bytes = new byte[process.length + 1];
                        bytes[0] = (byte) 1;
                        for (int i = 0; i < process.length; i++) {
                            bytes[i + 1] = process[i];
                        }
                        return bytes;
                    } catch (Exception e) {
                        throw new TypeException(String.format("cannot converter %s to Integer type", value));
                    }
                }
            }
            case "FLOAT": {
                float _value;
                if (value == null || value.equals("null")) {
                    _value = (float) 0.0;
                    byte[] process = ByteFloatConverter.float2byte(_value);
                    byte[] bytes = new byte[process.length + 1];
                    bytes[0] = (byte) 0;
                    for (int i = 0; i < process.length; i++) {
                        bytes[i + 1] = process[i];
                    }
                    return bytes;
                } else {
                    try {
                        _value = Float.parseFloat(value);
                        byte[] process = ByteFloatConverter.float2byte(_value);
                        byte[] bytes = new byte[process.length + 1];
                        bytes[0] = (byte) 1;
                        for (int i = 0; i < process.length; i++) {
                            bytes[i + 1] = process[i];
                        }
                        return bytes;
                    } catch (Exception e) {
                        throw new TypeException(String.format("cannot converter %s to Float type", value));
                    }
                }
            }
            case "DATE": {
                if (value == null || value.equals("null")) {
                    byte[] process = ByteLongConverter.long2Bytes(0);
                    byte[] bytes = new byte[process.length + 1];
                    bytes[0] = (byte) 0;
                    for (int i = 0; i < process.length; i++) {
                        bytes[i + 1] = process[i];
                    }
                    return bytes;
                } else {
                    Long _value = Long.parseLong(value);
                    byte[] process = ByteLongConverter.long2Bytes(_value);
                    byte[] bytes = new byte[process.length + 1];
                    bytes[0] = (byte) 0;
                    for (int i = 0; i < process.length; i++) {
                        bytes[i + 1] = process[i];
                    }
                    return bytes;
                }
            }
            default:
                throw new TypeException(String.format("Invalid Type : %s", type));
        }
    }

    public static byte[] encode(List<Integer> sizeList, List<String> typeList, int totalSize, List<String> valueList) {
        assert (valueList.size() == sizeList.size());
        byte[] data = new byte[totalSize];
        byte b = 0;
        Arrays.fill(data, b);
        int pos = 0;
        for (int item = 0; item < sizeList.size(); item++) {
            int length;
            byte[] bytes;
            if (typeList.get(item).equals("VARCHAR")) {
                if (valueList.get(item) == null || valueList.get(item).equals("null")) {
                    length = 1;
                    bytes = new byte[1];
                    bytes[0] = (byte) 0;
                } else {
                    byte[] strBytes = valueList.get(item).getBytes(StandardCharsets.UTF_8);
                    bytes = new byte[strBytes.length + 1];
                    Arrays.fill(bytes, (byte) 0);
                    bytes[0] = (byte) 1;
                    for (int i = 0; i < bytes.length - 1; i++) {
                        bytes[i + 1] = strBytes[i];
                    }
                    length = bytes.length;
                    if (length > sizeList.get(item)) {
                        throw new TypeException(String.format("String length :%s is longer than VARCHAR[%s]", length, sizeList.get(item)));
                    }
                }
            } else {
                bytes = parserType(valueList.get(item), typeList.get(item));
                length = bytes.length;
            }
            for (int head = pos; head < pos + length; head++) {
//                log.info("data length: {}, bytes length: {}", data.length, bytes.length);
                data[head] = bytes[head - pos];
            }
            pos += sizeList.get(item);
        }
        return data;
    }

    public static String antiParser(byte[] data, String type) throws UnsupportedEncodingException {

        switch (type) {
            case "VARCHAR": {
                byte[] process = new byte[data.length - 1];
                for (int i = 0; i < process.length; i++) {
                    if (data[i + 1] == 0)
                        break;
                    process[i] = data[i + 1];
                }
                String asked = new String(process, "UTF-8");
                return (data[0] == (byte) 0) ? null : asked;
            }
            case "INT": {
                byte[] process = new byte[4];
                for (int i = 0; i < process.length; i++) {
                    process[i] = data[i + 1];
                }
                return (data[0] == (byte) 0) ? null : (((Integer) ByteIntegerConverter.bytesToInt(process)).toString());
            }
            case "FLOAT": {
                byte[] process = new byte[4];
                for (int i = 0; i < process.length; i++) {
                    process[i] = data[i + 1];
                }
                return (data[0] == (byte) 0) ? null : (((Float) ByteFloatConverter.byte2float(process)).toString());
            }
            case "Date": {
                byte[] process = new byte[8];
                for (int i = 0; i < process.length; i++) {
                    process[i] = data[i + 1];
                }
                return (data[0] == (byte) 0) ? null : (((Long) ByteLongConverter.bytes2Long(process)).toString());
            }
            default: {
                throw new RuntimeException(String.format("Invalid Type : %s", type));
            }
        }
    }

    public static List<Object> decode(List<Integer> sizeList, List<String> typeList, int totalList, Record record) throws UnsupportedEncodingException {
        byte[] data = record.getData();
        assert (sizeList.size() == typeList.size());
        int pos = 0;
        List<Object> list = new ArrayList<>();
//        Map<Integer, String> map = new HashMap<>();
        for (int i = 0; i < sizeList.size(); i++) {
            byte[] process = new byte[sizeList.get(i)];
            for (int j = 0; j < sizeList.get(i); j++) {
                process[j] = data[pos + j];
            }
            list.add(antiParser(process, typeList.get(i)));
//            map.put(i, antiParser(process, typeList.get(i)));
            pos += sizeList.get(i);
        }
        return list;
    }
}
