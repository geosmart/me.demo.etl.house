package me.demo.etl.crawler.demo;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * Github个人Repository列表readme抓取
 * Created by geosamrt on 2017/1/18.
 */
public class GithubRepoPageProcessor implements PageProcessor {
    // 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

    public static void main(String[] args) {
        Spider.create(new GithubRepoPageProcessor())
                //从"https://github.com/geosmart"开始抓
                .addUrl("https://github.com/geosmart?tab=repositories")
                .addPipeline(new ConsolePipeline())
                //开启5个线程抓取
                .thread(5)
                //启动爬虫
                .run();
    }

    @Override
    // process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑
    public void process(Page page) {
        //抓取所有repository的项目和链接
        // 部分二：定义如何抽取页面信息，并保存下来
        page.putField("author", page.getUrl().regex("https://github\\.com/(\\w+)?.*").toString());

        page.putField("repository", page.getUrl().regex("https://github\\.com/\\w+/(.*)").toString());

        page.putField("readme", page.getHtml().xpath("//div[@id='readme']/tidyText()"));

        page.addTargetRequests(page.getHtml().xpath("//a[@itemprop='name codeRepository']").links().all());
    }

    @Override
    public Site getSite() {
        return site;
    }
}
