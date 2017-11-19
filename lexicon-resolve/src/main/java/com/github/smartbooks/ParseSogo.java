package com.github.smartbooks;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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

                    String filename = line.split("=")[2] + ".scel";

                    try {
                        downLoadFromUrl(line, filename, "E:\\github\\lexicon\\data\\scel");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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

    /**
     * 从网络Url中下载文件
     *
     * @param urlStr
     * @param fileName
     * @param savePath
     * @throws IOException
     */
    public static void downLoadFromUrl(String urlStr, String fileName, String savePath) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3 * 1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        //得到输入流
        InputStream inputStream = conn.getInputStream();
        //获取自己数组
        byte[] getData = readInputStream(inputStream);

        //文件保存位置
        File saveDir = new File(savePath);
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }
        File file = new File(saveDir + File.separator + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if (fos != null) {
            fos.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }


        System.out.println("info:" + url + " download success");

    }

    /**
     * 从输入流中获取字节数组
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

}
