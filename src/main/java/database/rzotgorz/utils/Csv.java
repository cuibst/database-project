package database.rzotgorz.utils;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import database.rzotgorz.metaSystem.NameAndTypePack;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class Csv {
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

    public static List<String[]> readCsv(String path, String name) {
        List<String[]> list = new ArrayList<>();
        try {
            CsvReader reader = new CsvReader(name, ',', Charset.forName("UTF-8"));
            reader.readHeaders();
            while (reader.readRecord()) {
                list.add(reader.getValues());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void createCsv(String path, String name, String[] headers, List<Object> data, Boolean created) {
        try {
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
