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

public class ServiceWiseOrders {

    public static void getAllServiceWiseOrders() {
        WebDriver driver = Drivers.openChromeBrowser();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50));

        try {
            driver.get("https://subs3.quickdrycleaning.com/superadmin/Login");
            String userId = System.getenv("USER_ID");
            String userPass = System.getenv("USER_PASS");

            WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("txtUserId")));
            userField.clear();
            userField.sendKeys(userId);

            WebElement passField = driver.findElement(By.id("txtPassword"));
            passField.clear();
            passField.sendKeys(userPass);
            WebElement loginBtn = driver.findElement(By.id("btnLogin"));
            try {
                loginBtn.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);
            }

            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("Login")));

            WebElement reportsButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//ul[@class='nav navbar-nav']/child::li[7]")));
            WebElement serviceWiseOrderButton = driver.findElement(By.xpath("//ul[@class='nav navbar-nav']/child::li[7]/ul/child::li[5]"));

            try {
                reportsButton.click();
                serviceWiseOrderButton.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", reportsButton);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", serviceWiseOrderButton);
            }

            WebElement filterButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='panel-heading']/h3/div[@id='reportrange']")));
            WebElement filterMonth = driver.findElement(By.xpath("//div[@class='daterangepicker dropdown-menu opensleft']/child::div[last()]/ul/child::li[last()-6]"));
            try {
                filterButton.click();
                filterMonth.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", filterButton);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", filterMonth);
            }
            List<Map<String, String>> crParkFinalJsonList = new ArrayList<>();
            List<Map<String, String>> defenceColonyFinalJsonList = new ArrayList<>();
            List<Map<String, String>> greenParkFinalJsonList = new ArrayList<>();
            List<Map<String, String>> saketFinalJsonList = new ArrayList<>();
            List<Map<String, String>> internalHistoryJsonList = new ArrayList<>();
            WebElement serviceWiseOrderTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_ContentPlaceHolder1_grdReport")));
            List<WebElement> rowsData = serviceWiseOrderTable.findElements(By.xpath(".//tr[position() > 1]/td[1]/a"));
            String originalTab = driver.getWindowHandle();
            for (WebElement rowData : rowsData) {
                String rowName = rowData.getText().trim();
                try {
                    rowData.click();
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", rowData);
                }
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!originalTab.contentEquals(windowHandle)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }
                List<String> allowedHeaders = Arrays.asList(
                        "Store Name",
                        "Service Name",
                        "Service Type",
                        "Amount without Tax & Discount",
                        "Quantity",
                        "Pcs"
                );
                List<String> internalHeaders = Arrays.asList(
                        "Service Name",
                        "Store Name",
                        "Order Number",
                        "Customer Details",
                        "Order Date",
                        "Due Date",
                        "Garment Details Qty Wise",
                        "Qty",
                        "Pcs",
                        "Amount without Tax & Discount"
                );
                WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_ContentPlaceHolder1_grdReport")));
                List<WebElement> headerElements = table.findElements(By.xpath(".//tr[1]/th"));
                Map<Integer, String> colMap = new LinkedHashMap<>();
                for (int i = 0; i < headerElements.size(); i++) {
                    String headerText = headerElements.get(i).getText().trim();
                    if (allowedHeaders.contains(headerText)) {
                        colMap.put(i, headerText);
                    }
                }
                List<WebElement> rows = table.findElements(By.xpath(".//tr[position() > 1 and position() < last()]"));
                for (WebElement row : rows) {
                    List<WebElement> cells = row.findElements(By.tagName("td"));
                    if (cells.size() < colMap.size()) continue;
                    Map<String, String> jsonObject = new LinkedHashMap<>();
                    for (Map.Entry<Integer, String> entry : colMap.entrySet()) {
                        int colIndex = entry.getKey();
                        if (colIndex < cells.size()) {
                            WebElement currentCell = cells.get(colIndex);
                            String headerName = entry.getValue();
                            List<WebElement> anchors = currentCell.findElements(By.tagName("a"));
                            if(anchors.size()==1)
                            {
                                WebElement internalCell = anchors.get(0);
                                String serviceName = internalCell.getText();
                                String nextOriginalTab = driver.getWindowHandle();
                                try {
                                    internalCell.click();
                                } catch (Exception e) {
                                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", internalCell);
                                }
                                for (String windowHandle : driver.getWindowHandles()) {
                                    if (!originalTab.equals(windowHandle) && !nextOriginalTab.contentEquals(windowHandle)) {
                                        driver.switchTo().window(windowHandle);
                                        break;
                                    }
                                }
                                wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_ContentPlaceHolder1_grdReport")));
                                WebElement internalTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ctl00_ContentPlaceHolder1_grdReport")));
                                List<WebElement> internalHeaderElements = internalTable.findElements(By.xpath(".//tr[1]/th"));
                                Map<Integer, String> internalColMap = new LinkedHashMap<>();
                                internalColMap.put(110022,"Service Name");
                                for (int i = 0; i < internalHeaderElements.size(); i++) {
                                    String internalHeaderText = internalHeaderElements.get(i).getText().trim();
                                    if (internalHeaders.contains(internalHeaderText)) {
                                        internalColMap.put(i, internalHeaderText);
                                    }
                                }
                                List<WebElement> internalRows = internalTable.findElements(By.xpath(".//tr[position() > 1 and position() < last()]"));
                                for(WebElement internalRow: internalRows)
                                {
                                    List<WebElement> internalCells = internalRow.findElements(By.tagName("td"));
                                    if (internalCells.size() < colMap.size()) continue;
                                    Map<String, String> internalJsonObject = new LinkedHashMap<>();
                                    for (Map.Entry<Integer, String> internalEntry : internalColMap.entrySet()) {
                                        int internalColIndex = internalEntry.getKey();
                                        if(internalColIndex==110022)
                                        {
                                            internalJsonObject.put(internalEntry.getValue(),serviceName);
                                        }
                                        else if(internalColIndex < internalCells.size())
                                        {
                                            String internalHeaderName = internalEntry.getValue();
                                            String internalCellValue = internalCells.get(internalColIndex).getAttribute("textContent").replaceAll("\\u00A0", " ").trim();
                                            internalJsonObject.put(internalHeaderName, internalCellValue);
                                        }
                                    }
                                    internalHistoryJsonList.add(internalJsonObject);
                                }
                                driver.close();
                                driver.switchTo().window(nextOriginalTab);

                            }
                            String cellValue = cells.get(colIndex).getAttribute("textContent").replaceAll("\\u00A0", " ").trim();
                            jsonObject.put(headerName, cellValue);
                        }
                    }

                    if (rowName.equalsIgnoreCase("CR Park"))
                        crParkFinalJsonList.add(jsonObject);
                    else if (rowName.equalsIgnoreCase("Defence Colony"))
                        defenceColonyFinalJsonList.add(jsonObject);
                    else if (rowName.equalsIgnoreCase("Greenpark"))
                        greenParkFinalJsonList.add(jsonObject);
                    else if (rowName.equalsIgnoreCase("Saket"))
                        saketFinalJsonList.add(jsonObject);
                }
                driver.close();
                driver.switchTo().window(originalTab);
            }
            //Logout
            WebElement logoutBtn = driver.findElement(By.id("ctl00_btnLogOut"));
            try {
                logoutBtn.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logoutBtn);
            }
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("ServiceWiseOrderCrParkApi.json", crParkFinalJsonList);
            dataMap.put("ServiceWiseOrderDefenceApi.json", defenceColonyFinalJsonList);
            dataMap.put("ServiceWiseOrderGreenParkApi.json", greenParkFinalJsonList);
            dataMap.put("ServiceWiseOrderSaketApi.json", saketFinalJsonList);
            dataMap.put("InternalHistoryOrderApi.json", internalHistoryJsonList);

            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(entry.getValue());
                Path path = Paths.get("src/main/resources/Output/AllServiceWiseOrderApi/", entry.getKey());

                Files.createDirectories(path.getParent());
                Files.write(path, jsonString.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }

        } catch (Exception e) {
            System.out.println("Exception Occurred: " + e);
            ;
        } finally {
            driver.quit();
        }
    }
}
