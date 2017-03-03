package me.demo.etl.crawler;

import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

import me.demo.etl.crawler.downloader.SeleniumDownloader;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

/**
 * 58同城租房信息爬取
 *
 * @author geosamrt
 * @date 2017/03/03
 */
public class HousePageProcessor implements PageProcessor {

    static Logger log = LoggerFactory.getLogger(HousePageProcessor.class);

    static {
        System.getProperties().setProperty("webdriver.chrome.driver", "D:\\JavaWorkspace\\me.demo.etl.house\\src\\main\\resources\\chromedriver");

    }

    // 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

    public static void main(String[] args) {
        Spider.create(new HousePageProcessor())
                .addUrl("http://hz.58.com/chuzu")
                .addPipeline(new JsonFilePipeline("bin"))
                .downloader(new SeleniumDownloader("D:\\JavaWorkspace\\me.demo.etl.house\\src\\main\\resources\\chromedriver.exe"))
                //开启5个线程抓取
                .thread(4)
                //启动爬虫
                .runAsync();
    }

    /**
     * 生成url
     *
     * @param cit  城市，如：hz
     * @param dis  区划，如：binjiang
     * @param site 网站模块。如：chuzu
     * @param type 房源类型，0（个人），1（经纪人）
     * @param key  关键词
     */
    public static String generateUrl(String cit, String dis, String site, String type, String key) {
        String baseUrl = "http://{cit}.58.com/{dis}/{site}/{type}/?key={key}";
        baseUrl = baseUrl.replace("{cit}", cit)
                .replace("{dis}", dis)
                .replace("{site}", site)
                .replace("{type}", type)
                .replace("{key}", URLEncoder.encode(key));
        log.debug(baseUrl);
        return baseUrl;
    }

    @Override
    public void process(Page page) {
        //分页查询,如：http://hz.58.com/chuzu/pn9/
        page.addTargetRequests(page.getHtml().links().regex("http://hz.58.com.chuzu.pn\\d*").all());
        //房屋发布信息
        List<Selectable> houseList = page.getHtml().xpath("/html/body/div[3]/div[1]/div[5]/div[2]/ul//li").nodes();
        for (Selectable house : houseList) {
            try {
                JSONObject json = new JSONObject();
                Selectable img = house.$(".img_list").xpath("//img/@src");
                json.put("img", img);
                //TODO 细化 简介+区域+小区名称+发布人
                List<String> desc = house.$(".des").xpath("//a/text()").all();
                if (desc.size() >= 2) {
                    json.put("title", desc.get(0));
                    json.put("region", desc.get(1));
                    json.put("name", desc.get(2));
                    if (desc.size() > 3) {
                        json.put("landlord", desc.get(3));
                    }
                }
                String sendTime = house.$(".listliright").xpath("//div[@class='sendTime']/text()").get();
                String price = house.$(".listliright").xpath("//div[@class='money']//b/text()").get();
                json.put("sendTime", sendTime);
                json.put("price", price);
                page.putField(UUID.randomUUID().toString(), json);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public Site getSite() {
        return site;
    }
}
