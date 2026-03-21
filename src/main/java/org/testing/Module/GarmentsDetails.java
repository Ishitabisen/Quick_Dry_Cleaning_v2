package org.testing.Module;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testing.DriverClass.Drivers;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.*;

public class GarmentsDetails {

    public static void getTodayGarmentsDetails()
    {
        WebDriver driver = Drivers.openChromeBrowser();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50));

        try {
            driver.get("https://subs3.quickdrycleaning.com/superadmin/Login");
            //String userId = System.getenv("USER_ID");
            //String userPass = System.getenv("USER_PASS");

            WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("txtUserId")));
            userField.clear();
            userField.sendKeys("drjaskaransingh@outlook.com");

            WebElement passField = driver.findElement(By.id("txtPassword"));
            passField.clear();
            passField.sendKeys("admin@123");

            WebElement loginBtn = driver.findElement(By.id("btnLogin"));
            try {
                loginBtn.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);
            }

            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("Login")));

            WebElement accountsButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//ul[@class='nav navbar-nav']/child::li[7]")));
            WebElement garmentsDetailsTypeButton = driver.findElement(By.xpath("//ul[@class='nav navbar-nav']/child::li[7]/ul/child::li[last()-1]"));
            try {
                accountsButton.click();
                garmentsDetailsTypeButton.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", accountsButton);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", garmentsDetailsTypeButton);
            }
            List<Map<String, String>> garmentsDetailsJsonList = new ArrayList<>();
            List<String> allowedHeaders = Arrays.asList(
                    "Store", "Order #", "Date", "Time", "Garment", "Sub Garment",
                    "Barcode", "Primary Service", "Top Up 1", "Top Up 2", "Top Up 3",
                    "Top Up 4", "Defects", "Color", "Brand", "Due on", "Gross Amount",
                    "Discount", "Net Amount", "Package", "Garment Status",
                    "Send to Workshop", "Workshop (Name)", "Send on",
                    "Received from Workshop", "Delivered on"
            );
            WebElement paymentTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("tblTbodyData")));
            List<WebElement> headerElements = paymentTable.findElements(By.xpath(".//thead/tr/th/div/div/div"));
            Map<Integer, String> colMap = new LinkedHashMap<>();
            for (int i = 0; i < headerElements.size(); i++) {
                String headerText = headerElements.get(i).getText().trim();
                if (allowedHeaders.contains(headerText)) {
                    colMap.put(i, headerText);
                }
            }
            List<WebElement> rows = paymentTable.findElements(By.xpath(".//tbody/tr"));
            for(WebElement row : rows)
            {
                List<WebElement> cells = row.findElements(By.tagName("td"));
                if (cells.size() < colMap.size()) continue;
                Map<String, String> jsonObject = new LinkedHashMap<>();
                for (Map.Entry<Integer, String> entry : colMap.entrySet()) {
                    int colIndex = entry.getKey();
                    if (colIndex < cells.size()) {
                        String headerName = entry.getValue();
                        String cellValue = cells.get(colIndex).getAttribute("textContent").replaceAll("\\u00A0", " ").trim();
                        jsonObject.put(headerName, cellValue);
                    }
                }
                garmentsDetailsJsonList.add(jsonObject);
            }
            //Logout
            WebElement logoutBtn = driver.findElement(By.xpath("//div[@class='col-sm-6'][2]/ul[@class='navbar-nav pull-right']/li[last()]/div/a[@id='btnLogOutPage']"));
            try {
                logoutBtn.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logoutBtn);
            }
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(garmentsDetailsJsonList);
            Path path = Paths.get("src/main/resources/Output/GarmentsDetialsApi/garmentApi.json");
            Files.createDirectories(path.getParent());
            Files.write(path, jsonString.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        }
        catch(Exception e)
        {
            System.out.println("Runtime error Occurred"+e);
        }
        finally
        {
            driver.quit();
        }
    }
}
