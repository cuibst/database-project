package database.rzotgorz.utils;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import database.rzotgorz.metaSystem.ColumnInfo;
import database.rzotgorz.metaSystem.NameAndTypePack;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

@Data
@Slf4j
public class Csv {

    public static LinkedHashMap<String, ColumnInfo> parserHeader(String csvName) {
        LinkedHashMap<String, ColumnInfo> map = new LinkedHashMap<>();
        String[] headers;
        try {
            CsvReader reader = new CsvReader(csvName, ',', Charset.forName("UTF-8"));
            reader.readHeaders();
            headers = reader.getHeaders();
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].replace(")", "");
                String[] contents = header.split("\\(");
                ColumnInfo columnInfo = new ColumnInfo(contents[1], contents[0], 0, null);
                if (header.contains("VARCHAR")) {
                    columnInfo.setSize(Integer.parseInt(contents[2]));
                }
                map.put(columnInfo.getName(), columnInfo);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static String[] processHeader(List<NameAndTypePack> list) {
        String[] headers = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            String name;
            if (list.get(i).getType().equals("VARCHAR"))
                name = list.get(i).getName() + "(" + list.get(i).getType() + "(" + list.get(i).getLength() + ")" + ")";
            else
                name = list.get(i).getName() + "(" + list.get(i).getType() + ")";
            headers[i] = name;
        }
        return headers;
    }

    public static List<Object[]> readCsv(String path, String name) {
        List<Object[]> list = new ArrayList<>();
        String[] headers;
        try {
            CsvReader reader = new CsvReader(name, ',', Charset.forName("UTF-8"));
            reader.readHeaders();
            headers = reader.getHeaders();
            String[] types = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].replace(")", "");
                String[] contents = header.split("\\(");
                ColumnInfo columnInfo = new ColumnInfo(contents[1], contents[0], 0, null);
                if (header.contains("VARCHAR")) {
                    columnInfo.setSize(Integer.parseInt(contents[2]));
                }
                types[i] = contents[1];
            }
            while (reader.readRecord()) {
                String[] values = reader.getValues();
                Object[] objects = new Object[values.length];
                for (int i = 0; i < values.length; i++) {
                    if (types[i].equals("INT")) {
                        objects[i] = Integer.parseInt(values[i]);
                    } else if (types[i].equals("FLOAT")) {
                        objects[i] = Float.parseFloat(values[i]);
                    } else if (types[i].equals("DATE")) {
                        objects[i] = Long.parseLong(values[i]);
                    } else
                        objects[i] = values[i];
                }
                list.add(objects);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void createCsv(String path, String name, String[] headers, List<Object> data, Boolean created) {
        try {
            name = "." + File.separator + "csv" + File.separator + name;
            File file = new File(name);
            if (!created) {
                if (file.exists())
                    file.delete();
                file.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name, true), "UTF-8"));
            CsvWriter csvWriter = new CsvWriter(out, ',');
            if (!created) {
                csvWriter.writeRecord(headers);
                csvWriter.close();
                return;
            }
            for (int i = 0; i < data.size(); i++) {
                csvWriter.write(data.get(i).toString(), false);
            }
            csvWriter.endRecord();
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
