package com.github.smartbooks;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * 解析sogo词库工具类
 * 到这里去挑选下载词库文件（.scel）
 **/
public class ParseSogo {

    public static void main(String[] args) throws Exception {

        urlDecoding();

        if (true) return;

        String inputDirectory = "E:\\github\\lexicon\\data\\scel";

        File inputFile = new File(inputDirectory);

        File[] files = inputFile.listFiles(pathname -> pathname.getName().endsWith(".scel"));

        for (int i = 0; i < files.length; i++) {
            String itemInput = files[i].toURI().getPath().replace("/", "\\\\");
            itemInput = itemInput.substring(2, itemInput.length());
            String itemOutput = itemInput + ".txt";

            System.out.println(String.format("%s\r\n%s", itemInput, itemOutput));

            sogou(itemInput, itemOutput, false);
        }
    }

    public static void urlDecoding() {
        String input = "E:\\github\\lexicon\\data\\scel.txt";
        String output = "E:\\github\\lexicon\\data\\scel.decode.txt";

        try {

            OutputStreamWriter sw = new OutputStreamWriter(new FileOutputStream(output));

            BufferedReader br = new BufferedReader(new FileReader(input));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() > 0) {
                    line = java.net.URLDecoder.decode(line, "utf-8");
                    sw.write(String.format("%s\n", line));
                }
            }
            br.close();

            sw.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 读取scel的词库文件
     * 生成txt格式的文件
     *
     * @param inputPath  输入路径
     * @param outputPath 输出路径
     * @param isAppend   是否拼接追加词库内容
     *                   true 代表追加,false代表重建
     **/
    private static void sogou(String inputPath, String outputPath, boolean isAppend) throws IOException {
        File file = new File(inputPath);
        if (!isAppend) {
            if (Files.exists(Paths.get(outputPath), LinkOption.values())) {
                System.out.println("存储此文件已经删除");
                Files.deleteIfExists(Paths.get(outputPath));
            }
        }
        RandomAccessFile raf = new RandomAccessFile(outputPath, "rw");

        int count = 0;
        SougouScelMdel model = new SougouScelReader().read(file);
        Map<String, List<String>> words = model.getWordMap(); //词<拼音,词>
        Set<Entry<String, List<String>>> set = words.entrySet();
        Iterator<Entry<String, List<String>>> iter = set.iterator();
        while (iter.hasNext()) {
            Entry<String, List<String>> entry = iter.next();
            List<String> list = entry.getValue();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                String word = list.get(i);
                String wordPinYin = entry.getKey();

                System.out.println(String.format("word:%s pinyin:%s", word, wordPinYin));

                raf.seek(raf.getFilePointer());
                raf.write(String.format("%s,%s\n", word, wordPinYin).getBytes());//写入txt文件
                count++;
            }
        }
        raf.close();
        System.out.println("生成txt成功！,总计写入: " + count + " 条数据！");
    }

}
