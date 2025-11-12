package org.yourcompany.yourproject.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public class FileDAO implements IDAO {
    private final File file;

    public FileDAO(String filePath) {
        this.file = new File(filePath);
    }

    @Override
    public boolean save(Hashtable<String, String> data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            String line = String.join(",", data.values());
            writer.write(line);
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        try {
            File tempFile = new File(file.getAbsolutePath() + ".tmp");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(id + ",")) {
                    writer.write(line + System.lineSeparator());
                }
            }

            reader.close();
            writer.close();
            file.delete();
            tempFile.renameTo(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Hashtable<String, String> load(String id) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(id + ",")) {
                    String[] parts = line.split(",");
                    Hashtable<String, String> record = new Hashtable<>();
                    record.put("id", parts[0]);
                    record.put("title", parts[1]);
                    record.put("start", parts[2]);
                    record.put("end", parts[3]);
                    return record;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<Hashtable<String, String>> load() {
        ArrayList<Hashtable<String, String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                Hashtable<String, String> record = new Hashtable<>();
                record.put("id", parts[0]);
                record.put("title", parts[1]);
                record.put("start", parts[2]);
                record.put("end", parts[3]);
                records.add(record);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }
}
