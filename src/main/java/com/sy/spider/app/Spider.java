package com.sy.spider.app;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Spider {

	private static ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
	public static AtomicInteger ai = new AtomicInteger(0);
	public static Boolean f = false;

	public static void main(String[] args) throws Exception {

		ExecutorService executor = Executors.newCachedThreadPool();

		for (int i = 0; i < 5; i++) {
			executor.execute(new Runnable() {
				public void run() {
					while (!f) {
						if(queue.isEmpty()) continue;
						try {
							String remove = queue.remove();
							String fn = ai.incrementAndGet() + ".jpg";
							Spider.download(remove, fn);
							System.err.println("线程:" + Thread.currentThread().getName() + "下载了:" + fn);
							Thread.sleep(100);
						} catch (Exception e) {
							continue;
						}
					}
				}
			});
		}

		String startUrl = "http://www.zcool.com.cn/index!1.html#mainList";
		next(startUrl);
		f=true;
		executor.shutdownNow();
	}
	
	public static void next(String urlTemplate,int param) throws Exception{
		Document doc = Jsoup.connect(String.format(urlTemplate, param)).get();
		Elements es = doc.select("ul[class='layout camWholeBoxUl'] > li");
		Iterator<Element> iterator = es.iterator();
//		System.err.println(es.size());
		while (iterator.hasNext()) {
			Element e = iterator.next();
			Element first = e.select("a > img").first();
			if(null == first) continue;
			queue.add(first.attr("src"));
		}
		next(urlTemplate,param+1);
	}
	
	public static void next(String urlTemplate) throws Exception{
		Document doc = Jsoup.connect(urlTemplate).get();
		Elements es = doc.select("ul[class='layout camWholeBoxUl'] > li");
		Iterator<Element> iterator = es.iterator();
//		System.err.println(es.size());
		while (iterator.hasNext()) {
			Element e = iterator.next();
			Element first = e.select("a > img").first();
			if(null == first) continue;
			queue.add(first.attr("src"));
		}
		Element e = doc.select("a[class='pageNext']").first();
		String nextUrl = "http://www.zcool.com.cn"+e.attr("href");
		System.err.println("下一页链接:"+nextUrl);
		next(nextUrl);
	}

	public static void download(String urlStr, String fName) throws Exception {
		// 下载网络文件
		// int bytesum = 0;
		int byteread = 0;

		URL url = new URL(urlStr);

		FileOutputStream fs = null;
		InputStream inStream = null;
		try {
			URLConnection conn = url.openConnection();
			inStream = conn.getInputStream();
			fs = new FileOutputStream("F:/temp/" + fName);

			byte[] buffer = new byte[1204];
			while ((byteread = inStream.read(buffer)) != -1) {
				// bytesum += byteread;
				fs.write(buffer, 0, byteread);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			fs.close();
			inStream.close();
		}
	}

}
