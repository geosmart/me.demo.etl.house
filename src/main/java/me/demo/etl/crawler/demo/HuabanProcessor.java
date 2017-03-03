package me.demo.etl.crawler.demo;


import me.demo.etl.crawler.downloader.SeleniumDownloader;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * 花瓣网抽取器。<br>
 * 使用Selenium做页面动态渲染。<br>
 * Created by Think on 2017/2/5.
 */
public class HuabanProcessor implements PageProcessor {
    private Site site;

    public static void main(String[] args) {
        Spider.create(new HuabanProcessor()).thread(5)
//                .scheduler(new RedisScheduler("localhost"))
                .pipeline(new ConsolePipeline())
                .downloader(new SeleniumDownloader("D:\\JavaWorkspace\\me.demo.etl.house\\src\\main\\resources\\chromedriver.exe"))
                .run();
    }

    @Override
    public void process(Page page) {
        page.addTargetRequests(page.getHtml().links().regex("http://huaban\\.com/.*").all());
        if (page.getUrl().toString().contains("pins")) {
            page.putField("img", page.getHtml().xpath("//div[@id='pin_img']/img/@src").toString());
        } else {
            page.getResultItems().setSkip(true);
        }
    }

    @Override
    public Site getSite() {
        if (site == null) {
            site = Site.me().setDomain("huaban.com").addStartUrl("http://huaban.com/").setSleepTime(1000);
        }
        return site;
    }
}