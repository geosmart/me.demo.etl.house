package me.demo.etl.crawler;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 * Created by Think on 2017/2/5.
 */
public class HousePageProcessorTest {
    @Test
    public void testSelenium() {
        System.getProperties().setProperty("webdriver.chrome.driver", "D:\\JavaWorkspace\\me.demo.etl.house\\src\\main\\resources\\chromedriver");
        WebDriver webDriver = new ChromeDriver();
        webDriver.get("http://huaban.com/");
        WebElement webElement = webDriver.findElement(By.xpath("/html"));
        System.out.println(webElement.getAttribute("outerHTML"));
        webDriver.close();
    }
}