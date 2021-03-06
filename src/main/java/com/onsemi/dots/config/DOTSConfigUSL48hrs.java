/*
    * To change this license header, choose License Headers in Project Properties.
    * To change this template file, choose Tools | Templates
    * and open the template in the editor.
 */
package com.onsemi.dots.config;

import com.onsemi.dots.dao.EmailConfigDAO;
import com.onsemi.dots.dao.EmailTimelapseDAO;
import com.onsemi.dots.tools.EmailSender;
import com.onsemi.dots.dao.LotPenangDAO;
import com.onsemi.dots.dao.UslTimelapseDAO;
import com.onsemi.dots.model.EmailConfig;
import com.onsemi.dots.model.EmailTimelapse;
import com.onsemi.dots.model.LotPenang;
import com.onsemi.dots.model.UslTimelapse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletContext;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 *
 * @author fg79cj
 */
@Configuration
@EnableScheduling
public class DOTSConfigUSL48hrs {

    private static final Logger LOGGER = LoggerFactory.getLogger(DOTSConfigUSL48hrs.class);
    String[] args = {};

    @Autowired
    ServletContext servletContext;

//    @Scheduled(cron = "0 0 */6 * * *") //every 6 hour
//    @Scheduled(cron = "0 3 8 * * *") //every 8:00 AM - cron (sec min hr daysOfMth month daysOfWeek year(optional))
    public void cronRun() throws FileNotFoundException, IOException {
        LOGGER.info("Upper Spec Limit (USL Shipping) executed at everyday on every 6 hours. Current time is : " + new Date());

        DateFormat dateFormat = new SimpleDateFormat("ddMMMyyyy");
        Date date = new Date();
        String todayDate = dateFormat.format(date);

        String reportName = "\\\\mysed-rel-app03\\d$\\DOTS\\DOTS_TIMELAPSE\\DOTS Timelapse Report (" + todayDate + ").xls";

        FileOutputStream fileOut = new FileOutputStream(reportName);
        HSSFWorkbook workbook = new HSSFWorkbook();
        //        HSSFSheet sheet = workbook.createSheet("DOTS PROCESS EXCEED USL");
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(HSSFFont.COLOR_NORMAL);
        font.setBold(true);
        font.setColor(HSSFColor.DARK_BLUE.index);
        style.setFont(font);
        style.setWrapText(true);

        //2nd sheet
        HSSFSheet sheet2 = workbook.createSheet("DOTS PROCESS EXCEED USL");
        sheet2.createFreezePane(0, 1); // Freeze 1st Row

        String shipToReceivedPenang = "";
        String receivedToLoading = "";
        String unloadingToShipRel = "";
        String shipToReceivedRel = "";
        String actualEstimateUnload = "";

        int uslLimitShipPenang = 24;
        int uslLimitReceivedPenang = 24;
        int uslLimitUnloading = 24;
        int uslLimitShipRel = 24;
        int uslLimitActualEstimateUnload = 6;

        UslTimelapseDAO uslD = new UslTimelapseDAO();
        int uslShipPenang = uslD.getCountUslByProcess("Ship to Penang");
        uslD = new UslTimelapseDAO();
        int uslReceivedPenang = uslD.getCountUslByProcess("Received in Penang");
        uslD = new UslTimelapseDAO();
        int uslUnloading = uslD.getCountUslByProcess("Unloading Process");
        uslD = new UslTimelapseDAO();
        int uslShipRel = uslD.getCountUslByProcess("Ship to SBN Rel Lab");
        uslD = new UslTimelapseDAO();
        int uslActualEstimateUnload = uslD.getCountUslByProcess("Actual VS Estimate Unloading");

        //check hour limit from usl table
        if (uslShipPenang == 1) {
            uslD = new UslTimelapseDAO();
            UslTimelapse uslShipP = uslD.getUslTimelapseByProcess("Ship to Penang");
            uslLimitShipPenang = Integer.parseInt(uslShipP.getHour());
        }
        if (uslReceivedPenang == 1) {
            uslD = new UslTimelapseDAO();
            UslTimelapse uslReceivedP = uslD.getUslTimelapseByProcess("Received in Penang");
            uslLimitReceivedPenang = Integer.parseInt(uslReceivedP.getHour());
        }
        if (uslUnloading == 1) {
            uslD = new UslTimelapseDAO();
            UslTimelapse uslUnload = uslD.getUslTimelapseByProcess("Unloading Process");
            uslLimitUnloading = Integer.parseInt(uslUnload.getHour());
        }
        if (uslShipRel == 1) {
            uslD = new UslTimelapseDAO();
            UslTimelapse uslShipR = uslD.getUslTimelapseByProcess("Ship to SBN Rel Lab");
            uslLimitShipRel = Integer.parseInt(uslShipR.getHour());
        }
        if (uslActualEstimateUnload == 1) {
            uslD = new UslTimelapseDAO();
            UslTimelapse uslActualEstimate = uslD.getUslTimelapseByProcess("Actual VS Estimate Unloading");
            uslLimitActualEstimateUnload = Integer.parseInt(uslActualEstimate.getHour());
        }

        HSSFRow rowhead2 = sheet2.createRow((short) 0);
        rowhead2.setRowStyle(style);

        HSSFCell cell2_0 = rowhead2.createCell(0);
        cell2_0.setCellStyle(style);
        cell2_0.setCellValue("RMS_EVENT");

        HSSFCell cell2_1 = rowhead2.createCell(1);
        cell2_1.setCellStyle(style);
        cell2_1.setCellValue("INTERVAL");

        HSSFCell cell2_4 = rowhead2.createCell(2);
        cell2_4.setCellStyle(style);
        cell2_4.setCellValue("DO NUMBER");

        HSSFCell cell2_5 = rowhead2.createCell(3);
        cell2_5.setCellStyle(style);
        cell2_5.setCellValue("SHIP TO PENANG - RECEIVED IN PENANG \n (Limit: " + uslLimitShipPenang + " hrs)");

        HSSFCell cell2_6 = rowhead2.createCell(4);
        cell2_6.setCellStyle(style);
        cell2_6.setCellValue("RECEIVED IN PENANG - LOADING PROCESS \n (Limit: " + uslLimitReceivedPenang + " hrs)");

        HSSFCell cell2_9 = rowhead2.createCell(5);
        cell2_9.setCellStyle(style);
        cell2_9.setCellValue("ESTIMATE VS ACTUAL UNLOADING DATE \n (Limit: " + uslLimitActualEstimateUnload + " hrs)");

        HSSFCell cell2_7 = rowhead2.createCell(6);
        cell2_7.setCellStyle(style);
        cell2_7.setCellValue("UNLOADING PROCESS - SHIP TO REL LAB \n (Limit: " + uslLimitUnloading + " hrs)");

        HSSFCell cell2_8 = rowhead2.createCell(7);
        cell2_8.setCellStyle(style);
        cell2_8.setCellValue("SHIP TO REL LAB - RECEIVED IN REL LAB \n (Limit: " + uslLimitShipRel + " hrs)");

        String rmsEvent = "";
        String interval = "";
        String doNumber = "";
        //        String actualUnloadingDate = "";
        //        String estimateUnloadingDate = "";
        String nowDate = "";

        LotPenangDAO lotP = new LotPenangDAO();
        List<LotPenang> whUslList = lotP.getLotPenangListForTimelapseReport();

        boolean checksize1 = false;
        for (int i = 0; i < whUslList.size(); i++) {

            rmsEvent = whUslList.get(i).getRmsEvent();
            interval = whUslList.get(i).getInterval();
            doNumber = whUslList.get(i).getDoNumber();
            String actualUnloadingDate = whUslList.get(i).getUnloadingDate();
            String estimateUnloadingDate = whUslList.get(i).getExpectedUnloadingDate();
            nowDate = whUslList.get(i).getNowDate();

            String hourShipReceived = whUslList.get(i).getShipToReceived24();
            String hourShipReceivedIfNull = whUslList.get(i).getShipToReceivedIfNull24();

            String hourReceivedLoad = whUslList.get(i).getReceivedToLoad24();
            String hourReceivedLoadIfNull = whUslList.get(i).getReceivedToLoadIfNull24();

            String hourUnloadShip = whUslList.get(i).getUnloadToShip24();
            String hourUnloadShipIfNull = whUslList.get(i).getUnloadToShipIfNull24();

            String hourShipClosed = whUslList.get(i).getShipToClosed24();
            String hourShipClosedIfNull = whUslList.get(i).getShipToClosedIfNull24();

            String hourActualExpectedUnload = whUslList.get(i).getActualVsEstimateUnloading24();
            String hourActualExpectedUnloadIfNull = whUslList.get(i).getActualVsEstimateUnloadingIfNull24();

            boolean flag = false;

            //check hour limit from usl table
            if (uslShipPenang == 1) {
                uslD = new UslTimelapseDAO();
                UslTimelapse uslShipP = uslD.getUslTimelapseByProcess("Ship to Penang");
                uslLimitShipPenang = Integer.parseInt(uslShipP.getHour());
            }
            if (uslReceivedPenang == 1) {
                uslD = new UslTimelapseDAO();
                UslTimelapse uslReceivedP = uslD.getUslTimelapseByProcess("Received in Penang");
                uslLimitReceivedPenang = Integer.parseInt(uslReceivedP.getHour());
            }
            if (uslUnloading == 1) {
                uslD = new UslTimelapseDAO();
                UslTimelapse uslUnload = uslD.getUslTimelapseByProcess("Unloading Process");
                uslLimitUnloading = Integer.parseInt(uslUnload.getHour());
            }
            if (uslShipRel == 1) {
                uslD = new UslTimelapseDAO();
                UslTimelapse uslShipR = uslD.getUslTimelapseByProcess("Ship to SBN Rel Lab");
                uslLimitShipRel = Integer.parseInt(uslShipR.getHour());
            }
            if (uslActualEstimateUnload == 1) {
                uslD = new UslTimelapseDAO();
                UslTimelapse uslActualEstimate = uslD.getUslTimelapseByProcess("Actual VS Estimate Unloading");
                uslLimitActualEstimateUnload = Integer.parseInt(uslActualEstimate.getHour());
            }

            //check lag process more than usl
            if (hourShipReceivedIfNull != null) {
                if (Integer.parseInt(hourShipReceivedIfNull) >= uslLimitShipPenang && hourShipReceived == null) {
                    shipToReceivedPenang = whUslList.get(i).getShipmentFromRel() + " - \n" + whUslList.get(i).getShipToReceivedIfNullDetail();
                    //                shipToReceivedPenang = whUslList.get(i).getShipmentFromRel() + " - " + whUslList.get(i).getNowDateView() + "\n" + whUslList.get(i).getShipToReceivedIfNullDetail();

                    receivedToLoading = "-";
                    actualEstimateUnload = "-";
                    unloadingToShipRel = "-";
                    shipToReceivedRel = "-";
                    flag = true;
                    checksize1 = true;
                }
            }

            if (hourReceivedLoadIfNull != null) {
                if (Integer.parseInt(hourReceivedLoadIfNull) >= uslLimitReceivedPenang && hourReceivedLoad == null && hourShipReceived != null) {
                    shipToReceivedPenang = whUslList.get(i).getShipmentFromRel() + " - " + whUslList.get(i).getReceivedDate() + "\n" + whUslList.get(i).getShipToReceivedIfNullDetail();
                    receivedToLoading = whUslList.get(i).getReceivedDate() + " - \n" + whUslList.get(i).getReceivedToLoadIfNullDetail();
                    //                receivedToLoading = whUslList.get(i).getReceivedDate() + " - " + whUslList.get(i).getNowDateView() + "\n" + whUslList.get(i).getReceivedToLoadIfNullDetail();
                    actualEstimateUnload = "-";
                    unloadingToShipRel = "-";
                    shipToReceivedRel = "-";
                    flag = true;
                    checksize1 = true;
                }
            }

            if (actualUnloadingDate == null) {
                java.sql.Timestamp nowDate1 = java.sql.Timestamp.valueOf(nowDate);
                java.sql.Timestamp estimateUnloadingDate1 = java.sql.Timestamp.valueOf(estimateUnloadingDate);
                if (nowDate1.after(estimateUnloadingDate1)) {
                    if (Integer.parseInt(hourActualExpectedUnloadIfNull) > uslLimitActualEstimateUnload && hourActualExpectedUnload == null
                            && hourShipReceived != null && hourReceivedLoad != null) {
                        shipToReceivedPenang = whUslList.get(i).getShipmentFromRel() + " - " + whUslList.get(i).getReceivedDate() + "\n" + whUslList.get(i).getShipToReceivedIfNullDetail();
                        receivedToLoading = whUslList.get(i).getReceivedDate() + " - " + whUslList.get(i).getLoadingDate() + "\n" + whUslList.get(i).getReceivedToLoadIfNullDetail();
                        actualEstimateUnload = whUslList.get(i).getExpectedUnloadingDateView() + " - \n" + whUslList.get(i).getActualVsEstimateUnloadingIfNullDetail();
                        //                actualEstimateUnload = whUslList.get(i).getExpectedUnloadingDateView() + " - " + whUslList.get(i).getNowDateView() + "\n" + whUslList.get(i).getActualVsEstimateUnloadingIfNullDetail();
                        unloadingToShipRel = "-";
                        shipToReceivedRel = "-";
                        flag = true;
                        checksize1 = true;

                    }
                }
            }

            if (hourUnloadShipIfNull != null) {
                if (Integer.parseInt(hourUnloadShipIfNull) >= uslLimitUnloading && hourUnloadShip == null && hourReceivedLoad != null && hourShipReceived != null && hourActualExpectedUnload != null) {
                    shipToReceivedPenang = whUslList.get(i).getShipmentFromRel() + " - " + whUslList.get(i).getReceivedDate() + "\n" + whUslList.get(i).getShipToReceivedIfNullDetail();
                    receivedToLoading = whUslList.get(i).getReceivedDate() + " - " + whUslList.get(i).getLoadingDate() + "\n" + whUslList.get(i).getReceivedToLoadIfNullDetail();
                    actualEstimateUnload = whUslList.get(i).getExpectedUnloadingDateView() + " - " + whUslList.get(i).getUnloadingDateView() + "\n" + whUslList.get(i).getActualVsEstimateUnloadingIfNullDetail();
                    unloadingToShipRel = whUslList.get(i).getUnloadingDateView() + " - \n" + whUslList.get(i).getUnloadToShipIfNullDetail();
                    //                 unloadingToShipRel = whUslList.get(i).getUnloadingDateView() + " - " + whUslList.get(i).getNowDateView() + "\n" + whUslList.get(i).getUnloadToShipIfNullDetail();
                    shipToReceivedRel = "-";
                    flag = true;
                    checksize1 = true;
                }
            }

            if (hourShipClosedIfNull != null) {
                if (Integer.parseInt(hourShipClosedIfNull) >= uslLimitShipRel && hourShipClosed == null && hourShipReceived != null && hourReceivedLoad != null && hourActualExpectedUnload != null && hourUnloadShip != null) {
                    shipToReceivedPenang = whUslList.get(i).getShipmentFromRel() + " - " + whUslList.get(i).getReceivedDate() + "\n" + whUslList.get(i).getShipToReceivedIfNullDetail();
                    receivedToLoading = whUslList.get(i).getReceivedDate() + " - " + whUslList.get(i).getLoadingDate() + "\n" + whUslList.get(i).getReceivedToLoadIfNullDetail();
                    actualEstimateUnload = whUslList.get(i).getExpectedUnloadingDateView() + " - " + whUslList.get(i).getUnloadingDateView() + "\n" + whUslList.get(i).getActualVsEstimateUnloadingIfNullDetail();
                    unloadingToShipRel = whUslList.get(i).getUnloadingDateView() + " - " + whUslList.get(i).getShipmentDate() + "\n" + whUslList.get(i).getUnloadToShipIfNullDetail();
                    shipToReceivedRel = whUslList.get(i).getShipmentDate() + " - \n" + whUslList.get(i).getShipToClosedIfNullDetail();
                    //                shipToReceivedRel = whUslList.get(i).getShipmentDate() + " - " + whUslList.get(i).getNowDateView() + "\n" + whUslList.get(i).getShipToClosedIfNullDetail();
                    flag = true;
                    checksize1 = true;
                }
            }

            if (flag == true) {

                CellStyle style2 = workbook.createCellStyle();
                Font font2 = workbook.createFont();
                font2.setColor(HSSFColor.RED.index);
                style2.setFont(font2);
                style2.setWrapText(true);

                CellStyle style3 = workbook.createCellStyle();
                Font font3 = workbook.createFont();
                font3.setColor(HSSFColor.BLACK.index);
                style3.setFont(font3);
                style3.setWrapText(true);

                HSSFRow contents = sheet2.createRow(sheet2.getLastRowNum() + 1);
                //                
                HSSFCell cell3_0 = contents.createCell(0);
                cell3_0.setCellValue(rmsEvent);
                cell3_0.setCellStyle(style3);

                HSSFCell cell3_1 = contents.createCell(1);
                cell3_1.setCellValue(interval);
                cell3_1.setCellStyle(style3);

                HSSFCell cell3_4 = contents.createCell(2);
                cell3_4.setCellValue(doNumber);
                cell3_4.setCellStyle(style3);

                HSSFCell cell3_5 = contents.createCell(3);
                if (hourShipReceivedIfNull != null && Integer.parseInt(hourShipReceivedIfNull) >= uslLimitShipPenang) {
                    cell3_5.setCellStyle(style2);
                } else {
                    cell3_5.setCellStyle(style3);
                }
                cell3_5.setCellValue(shipToReceivedPenang);

                HSSFCell cell3_6 = contents.createCell(4);
                if (hourReceivedLoadIfNull != null && Integer.parseInt(hourReceivedLoadIfNull) >= uslLimitReceivedPenang) {
                    cell3_6.setCellStyle(style2);
                } else {
                    cell3_6.setCellStyle(style3);
                }
                cell3_6.setCellValue(receivedToLoading);

                HSSFCell cell3_9 = contents.createCell(5);
                if (actualUnloadingDate == null) {
                    java.sql.Timestamp nowDate1 = java.sql.Timestamp.valueOf(nowDate);
                    java.sql.Timestamp estimateUnloadingDate1 = java.sql.Timestamp.valueOf(estimateUnloadingDate);
                    if (nowDate1.after(estimateUnloadingDate1)) {
                        if (Integer.parseInt(hourActualExpectedUnloadIfNull) >= uslLimitActualEstimateUnload) {
                            cell3_9.setCellStyle(style2);
                        } else {
                            cell3_9.setCellStyle(style3);
                        }
                    } else {
                        cell3_9.setCellStyle(style3);
                    }
                }
                if (actualUnloadingDate != null) {
                    java.sql.Timestamp actualUnloadingDate1 = java.sql.Timestamp.valueOf(actualUnloadingDate);
                    java.sql.Timestamp estimateUnloadingDate1 = java.sql.Timestamp.valueOf(estimateUnloadingDate);
                    if (actualUnloadingDate1.after(estimateUnloadingDate1)) {
                        if (Integer.parseInt(hourActualExpectedUnload) >= uslLimitActualEstimateUnload) {
                            cell3_9.setCellStyle(style2);
                        } else {
                            cell3_9.setCellStyle(style3);
                        }
                    } else {
                        cell3_9.setCellStyle(style3);
                    }
                }
                cell3_9.setCellValue(actualEstimateUnload);

                HSSFCell cell3_7 = contents.createCell(6);
                if (hourUnloadShipIfNull != null && Integer.parseInt(hourUnloadShipIfNull) >= uslLimitUnloading) {
                    cell3_7.setCellStyle(style2);
                } else {
                    cell3_7.setCellStyle(style3);
                }
                cell3_7.setCellValue(unloadingToShipRel);

                HSSFCell cell3_8 = contents.createCell(7);
                if (hourShipClosedIfNull != null && Integer.parseInt(hourShipClosedIfNull) >= uslLimitShipRel) {
                    cell3_8.setCellStyle(style2);
                } else {
                    cell3_8.setCellStyle(style3);
                }
                cell3_8.setCellValue(shipToReceivedRel);

                sheet2.autoSizeColumn(0);
                sheet2.autoSizeColumn(1);
                sheet2.autoSizeColumn(2);
                sheet2.autoSizeColumn(3);
                sheet2.autoSizeColumn(4);
                sheet2.autoSizeColumn(5);
                sheet2.autoSizeColumn(6);
                sheet2.autoSizeColumn(7);
            }
        }
        //end 2nd sheet2

        if (checksize1 == true) {
            workbook.write(fileOut);
            workbook.close();

            //send email
            LOGGER.info("send email to person in charge");
            EmailSender emailSender = new EmailSender();
            com.onsemi.dots.model.User user = new com.onsemi.dots.model.User();
            user.setFullname("All");

            List<String> a = new ArrayList<String>();

            String emailApprover = "";
            String emaildistList1 = "";
            String emaildistList2 = "";
            String emaildistList3 = "";
            String emaildistList4 = "";
            String emaildistList5 = "";
            String emaildistList6 = "";
            String emaildistList7 = "";
            String emaildistList8 = "";
            String emaildistList9 = "";

            EmailConfigDAO econfD = new EmailConfigDAO();
            int countDistList1 = econfD.getCountTask("Dist List 1");
            if (countDistList1 == 1) {
                econfD = new EmailConfigDAO();
                EmailConfig distList1 = econfD.getEmailConfigByTask("Dist List 1");
                emaildistList1 = distList1.getEmail();
                a.add(emaildistList1);
            }
            econfD = new EmailConfigDAO();
            int countDistList2 = econfD.getCountTask("Dist List 2");
            if (countDistList2 == 1) {
                econfD = new EmailConfigDAO();
                EmailConfig distList2 = econfD.getEmailConfigByTask("Dist List 2");
                emaildistList2 = distList2.getEmail();
                a.add(emaildistList2);
            }
            econfD = new EmailConfigDAO();
            int countDistList3 = econfD.getCountTask("Dist List 3");
            if (countDistList3 == 1) {
                econfD = new EmailConfigDAO();
                EmailConfig distList3 = econfD.getEmailConfigByTask("Dist List 3");
                emaildistList3 = distList3.getEmail();
                a.add(emaildistList3);
            }
            econfD = new EmailConfigDAO();
            int countDistList4 = econfD.getCountTask("Dist List 4");
            if (countDistList4 == 1) {
                econfD = new EmailConfigDAO();
                EmailConfig distList4 = econfD.getEmailConfigByTask("Dist List 4");
                emaildistList4 = distList4.getEmail();
                a.add(emaildistList4);
            }
            econfD = new EmailConfigDAO();
            int countDistList5 = econfD.getCountTask("Dist List 5");
            if (countDistList5 == 1) {
                econfD = new EmailConfigDAO();
                EmailConfig distList5 = econfD.getEmailConfigByTask("Dist List 5");
                emaildistList5 = distList5.getEmail();
                a.add(emaildistList5);
            }
            econfD = new EmailConfigDAO();
            int countDistList6 = econfD.getCountTask("Dist List 6");
            if (countDistList6 == 1) {
                econfD = new EmailConfigDAO();
                EmailConfig distList6 = econfD.getEmailConfigByTask("Dist List 6");
                emaildistList6 = distList6.getEmail();
                a.add(emaildistList6);
            }
            econfD = new EmailConfigDAO();
            int countDistList7 = econfD.getCountTask("Dist List 7");
            if (countDistList7 == 1) {
                econfD = new EmailConfigDAO();
                EmailConfig distList7 = econfD.getEmailConfigByTask("Dist List 7");
                emaildistList7 = distList7.getEmail();
                a.add(emaildistList7);
            }
            econfD = new EmailConfigDAO();
            int countDistList8 = econfD.getCountTask("Dist List 8");
            if (countDistList8 == 1) {
                econfD = new EmailConfigDAO();
                EmailConfig distList8 = econfD.getEmailConfigByTask("Dist List 8");
                emaildistList8 = distList8.getEmail();
                a.add(emaildistList8);
            }
            econfD = new EmailConfigDAO();
            int countDistList9 = econfD.getCountTask("Dist List 9");
            if (countDistList9 == 1) {
                econfD = new EmailConfigDAO();
                EmailConfig distList9 = econfD.getEmailConfigByTask("Dist List 9");
                emaildistList9 = distList9.getEmail();
                a.add(emaildistList9);
            }
            String[] myArray = new String[a.size()];
            //            String[] emailTo = a.toArray(myArray);
            String[] to = {"fg79cj@onsemi.com", "ffycrt@onsemi.com"};
            emailSender.htmlEmailWithAttachment(
                    servletContext,
                    user, //user name requestor
                    to,
                    new File("\\\\mysed-rel-app03\\d$\\DOTS\\DOTS_TIMELAPSE\\DOTS Timelapse Report (" + todayDate + ").xls"),
                    "List of RMS#_Event in Penang Exceed USL", //subject
                    "Report for RMS#_Event in Penang that exceed Upper Specs Limit has been made. <br />"
                    + "Hence, attached is the report file for your view and perusal. <br /><br />"
                    + "<br /><br /> "
                    + "<style>table, th, td {border: 1px solid black;} </style>"
                    //                    + "<table style=\"width:100%\">" //tbl
                    + "<table>" //tbl
                    + "<tr>"
                    + "<th>RMS_EVENT</th> "
                    + "<th>INTERVAL</th> "
                    //                    + "<th>DEVICE</th>"
                    //                    + "<th>PACKAGE</th>"
                    + "<th>DO NUMBER</th>"
                    + "<th>DATE START - DATE END</th>"
                    + "<th>DURATION</th>"
                    + "<th>USL LIMIT (hrs)</th>"
                    + "<th>CURRENT STATUS</th>"
                    + "</tr>"
                    + table()
                    + "</table>"
                    + "<br />Thank you." //msg
            );
        }
    }

    private String table() {
        String rmsEvent = "";
        String interval = "";
//        String device = "";
//        String packages = "";
        String doNumber = "";
        String duration = "";
        String status = "";
        String text = "";
        String date = "";
        int uslLimit = 24;

        LotPenangDAO lotP = new LotPenangDAO();
        List<LotPenang> whUslList = lotP.getLotPenangListForTimelapseReport();

        for (int i = 0; i < whUslList.size(); i++) {

            rmsEvent = whUslList.get(i).getRmsEvent();
            interval = whUslList.get(i).getInterval();
//            device = whUslList.get(i).getDevice();
//            packages = whUslList.get(i).getPackages();
            doNumber = whUslList.get(i).getDoNumber();
            String actualUnloadingDate = whUslList.get(i).getUnloadingDate();
            String estimateUnloadingDate = whUslList.get(i).getExpectedUnloadingDate();
            String nowDate = whUslList.get(i).getNowDate();

            String hourShipReceived = whUslList.get(i).getShipToReceived24();
            String hourShipReceivedIfNull = whUslList.get(i).getShipToReceivedIfNull24();

            String hourReceivedLoad = whUslList.get(i).getReceivedToLoad24();
            String hourReceivedLoadIfNull = whUslList.get(i).getReceivedToLoadIfNull24();

            String hourUnloadShip = whUslList.get(i).getUnloadToShip24();
            String hourUnloadShipIfNull = whUslList.get(i).getUnloadToShipIfNull24();

            String hourShipClosed = whUslList.get(i).getShipToClosed24();
            String hourShipClosedIfNull = whUslList.get(i).getShipToClosedIfNull24();

            String hourActualExpectedUnload = whUslList.get(i).getActualVsEstimateUnloading24();
            String hourActualExpectedUnloadIfNull = whUslList.get(i).getActualVsEstimateUnloadingIfNull24();

            boolean flag = false;

            int uslLimitShipPenang = 24;
            int uslLimitReceivedPenang = 24;
            int uslLimitUnloading = 24;
            int uslLimitShipRel = 24;
            int uslLimitActualEstimateUnload = 6;

            UslTimelapseDAO uslD = new UslTimelapseDAO();
            int uslShipPenang = uslD.getCountUslByProcess("Ship to Penang");
            uslD = new UslTimelapseDAO();
            int uslReceivedPenang = uslD.getCountUslByProcess("Received in Penang");
            uslD = new UslTimelapseDAO();
            int uslUnloading = uslD.getCountUslByProcess("Unloading Process");
            uslD = new UslTimelapseDAO();
            int uslShipRel = uslD.getCountUslByProcess("Ship to SBN Rel Lab");
            uslD = new UslTimelapseDAO();
            int uslActualEstimateUnload = uslD.getCountUslByProcess("Actual VS Estimate Unloading");

            //check hour limit from usl table
            if (uslShipPenang == 1) {
                uslD = new UslTimelapseDAO();
                UslTimelapse uslShipP = uslD.getUslTimelapseByProcess("Ship to Penang");
                uslLimitShipPenang = Integer.parseInt(uslShipP.getHour());
            }
            if (uslReceivedPenang == 1) {
                uslD = new UslTimelapseDAO();
                UslTimelapse uslReceivedP = uslD.getUslTimelapseByProcess("Received in Penang");
                uslLimitReceivedPenang = Integer.parseInt(uslReceivedP.getHour());
            }
            if (uslUnloading == 1) {
                uslD = new UslTimelapseDAO();
                UslTimelapse uslUnload = uslD.getUslTimelapseByProcess("Unloading Process");
                uslLimitUnloading = Integer.parseInt(uslUnload.getHour());
            }
            if (uslShipRel == 1) {
                uslD = new UslTimelapseDAO();
                UslTimelapse uslShipR = uslD.getUslTimelapseByProcess("Ship to SBN Rel Lab");
                uslLimitShipRel = Integer.parseInt(uslShipR.getHour());
            }
            if (uslActualEstimateUnload == 1) {
                uslD = new UslTimelapseDAO();
                UslTimelapse uslActualEstimate = uslD.getUslTimelapseByProcess("Actual VS Estimate Unloading");
                uslLimitActualEstimateUnload = Integer.parseInt(uslActualEstimate.getHour());
            }

            if (hourShipReceivedIfNull != null) {
                if (Integer.parseInt(hourShipReceivedIfNull) >= uslLimitShipPenang && hourShipReceived == null) {
                    duration = whUslList.get(i).getShipToReceivedIfNullDetail();
                    status = "Ship to Penang";
//                    date = whUslList.get(i).getShipmentFromRel() + " - " + whUslList.get(i).getNowDateView();
                    date = whUslList.get(i).getShipmentFromRel() + " - ";
                    flag = true;
                    uslLimit = uslLimitShipPenang;
                }
            }

            if (hourReceivedLoadIfNull != null) {
                if (Integer.parseInt(hourReceivedLoadIfNull) >= uslLimitReceivedPenang && hourReceivedLoad == null && hourShipReceived != null) {
                    duration = whUslList.get(i).getReceivedToLoadIfNullDetail();
                    status = "Received in Penang";
//                    date = whUslList.get(i).getReceivedDate() + " - " + whUslList.get(i).getNowDateView();
                    date = whUslList.get(i).getReceivedDate() + " - ";
                    flag = true;
                    uslLimit = uslLimitReceivedPenang;
                }
            }

            if (actualUnloadingDate == null) {
                java.sql.Timestamp nowDate1 = java.sql.Timestamp.valueOf(nowDate);
                java.sql.Timestamp estimateUnloadingDate1 = java.sql.Timestamp.valueOf(estimateUnloadingDate);
                if (nowDate1.after(estimateUnloadingDate1)) {
                    if (Integer.parseInt(hourActualExpectedUnloadIfNull) > uslLimitActualEstimateUnload && hourActualExpectedUnload == null
                            && hourShipReceived != null && hourReceivedLoad != null) {
                        duration = whUslList.get(i).getActualVsEstimateUnloadingIfNullDetail();
                        status = "Loading Process";
//                        date = whUslList.get(i).getExpectedUnloadingDateView() + " - " + whUslList.get(i).getNowDateView();
                        date = whUslList.get(i).getExpectedUnloadingDateView() + " - ";
                        flag = true;
                        uslLimit = uslLimitActualEstimateUnload;

                    }
                }
            }

            if (hourUnloadShipIfNull != null) {
                if (Integer.parseInt(hourUnloadShipIfNull) >= uslLimitUnloading && hourUnloadShip == null && hourReceivedLoad != null && hourShipReceived != null) {
                    duration = whUslList.get(i).getUnloadToShipIfNullDetail();
                    status = "Unloading Process";
//                    date = whUslList.get(i).getUnloadingDateView() + " - " + whUslList.get(i).getNowDateView();
                    date = whUslList.get(i).getUnloadingDateView() + " - ";
                    flag = true;
                    uslLimit = uslLimitUnloading;
                }
            }

            if (hourShipClosedIfNull != null) {
                //                if (Integer.parseInt(hourShipClosedIfNull) >= 48 && hourShipClosed == null && hourShipReceived != null && hourReceivedLoad != null && hourUnloadShip != null) {
                if (Integer.parseInt(hourShipClosedIfNull) >= uslLimitShipRel && hourShipClosed == null && hourShipReceived != null && hourReceivedLoad != null && hourUnloadShip != null) {
                    duration = whUslList.get(i).getShipToClosedIfNullDetail();
                    status = "Ship to SBN Rel Lab";
//                    date = whUslList.get(i).getShipmentDate() + " - " + whUslList.get(i).getNowDateView();
                    date = whUslList.get(i).getShipmentDate() + " - ";
                    flag = true;
                    uslLimit = uslLimitShipRel;
                }
            }

            if (flag == true) {
                text = text + "<tr align = \"center\">";
                text = text + "<td>" + rmsEvent + "</td>";
                text = text + "<td>" + interval + "</td>";
//                text = text + "<td>" + device + "</td>";
//                text = text + "<td>" + packages + "</td>";
                text = text + "<td>" + doNumber + "</td>";
                text = text + "<td>" + date + "</td>";
                text = text + "<td>" + duration + "</td>";
                text = text + "<td>" + uslLimit + "</td>";
                text = text + "<td>" + status + "</td>";
                text = text + "</tr>";
            }
        }
        return text;
    }

//    @Scheduled(cron = "0 40 09 * * *") //every 6 hour
//    @Scheduled(cron = "0 30 09 01 * ?") //every 1st day of month at 9:00 AM - cron (sec min hr daysOfMth month daysOfWeek year(optional))
    public void cronForMonthlyReport() throws FileNotFoundException, IOException {
        LOGGER.info("Upper Spec Limit (USL DOTS) executed at every 1st day of month on 9:30 am. Current time is : " + new Date());

        String username = System.getProperty("user.name");
        username = "mysed-rel-app03";

//        DateFormat dateFormat = new SimpleDateFormat("MMM yyyy");
//        Date date = new Date();
//        String todayDate = dateFormat.format(date);
        Calendar now = Calendar.getInstance();
        Integer year = now.get(Calendar.YEAR);

        //dont plus one bcoz wanna get previous month 
//        Integer monthTemp = now.get(Calendar.MONTH) + 1; //month start from 0 - 11
        Integer monthTemp = now.get(Calendar.MONTH);
//        Integer monthTemp = 2;
        if (monthTemp < 1) {
            year = year - 1;
            monthTemp = 12;
        }
        Month monthHead = Month.of(monthTemp);
        String monthNameHeadFull = monthHead.name();

        String monthNameHead = monthNameHeadFull.substring(1, 3).toLowerCase();
        String monthNameHead2 = monthNameHeadFull.substring(0, 1);
        String todayDate = monthNameHead2 + monthNameHead + " " + year;

        String reportName = "\\\\mysed-rel-app03\\d$\\DOTS\\DOTS_CSV\\DOTS - Time Lapse Monthly Performance Report (" + todayDate + ").xls";

        FileOutputStream fileOut = new FileOutputStream(reportName);
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("DOTS - Time Lapse Monthly Performance Report (" + todayDate + ")");
        sheet.setDefaultColumnWidth(10);
        sheet.setDisplayGridlines(false);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setBoldweight(HSSFFont.COLOR_NORMAL);
        font.setBold(true);
        font.setColor(HSSFColor.DARK_BLUE.index);
        style.setFont(font);
//        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);

        CellStyle styleBlueWithBorder = workbook.createCellStyle();
        Font fontBlue = workbook.createFont();
        fontBlue.setFontHeightInPoints((short) 10);
        fontBlue.setFontName(HSSFFont.FONT_ARIAL);
        fontBlue.setBoldweight(HSSFFont.COLOR_NORMAL);
        fontBlue.setBold(true);
        fontBlue.setColor(HSSFColor.DARK_BLUE.index);
        styleBlueWithBorder.setFont(fontBlue);
        styleBlueWithBorder.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        styleBlueWithBorder.setBorderRight(HSSFCellStyle.BORDER_THIN);
        styleBlueWithBorder.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        styleBlueWithBorder.setBorderTop(HSSFCellStyle.BORDER_THIN);

        CellStyle styleWithBorder = workbook.createCellStyle();
        styleWithBorder.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        styleWithBorder.setBorderRight(HSSFCellStyle.BORDER_THIN);
        styleWithBorder.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        styleWithBorder.setBorderTop(HSSFCellStyle.BORDER_THIN);

        CellStyle style2 = workbook.createCellStyle();
        Font font2 = workbook.createFont();
        font2.setFontHeightInPoints((short) 10);
        font2.setFontName(HSSFFont.FONT_ARIAL);
        font2.setBoldweight(HSSFFont.COLOR_NORMAL);
        font2.setBold(true);
        font2.setColor(HSSFColor.RED.index);
        style2.setFont(font2);

        CellStyle styleGreen = workbook.createCellStyle();
        Font fontGreen = workbook.createFont();
        fontGreen.setFontHeightInPoints((short) 10);
        fontGreen.setFontName(HSSFFont.FONT_ARIAL);
        fontGreen.setBoldweight(HSSFFont.COLOR_NORMAL);
        fontGreen.setBold(true);
        fontGreen.setColor(HSSFColor.GREEN.index);
        styleGreen.setFont(fontGreen);

        CellStyle styleRed = workbook.createCellStyle();
        Font fontRedRemark = workbook.createFont();
        fontRedRemark.setFontHeightInPoints((short) 9);
//        fontRedRemark.setColor(HSSFColor.RED.index);
        styleRed.setFont(fontRedRemark);

        CellStyle styleBlueandFillGrey = workbook.createCellStyle();
        Font fontBlueNGray = workbook.createFont();
        fontBlueNGray.setFontHeightInPoints((short) 10);
        fontBlueNGray.setFontName(HSSFFont.FONT_ARIAL);
        fontBlueNGray.setBoldweight(HSSFFont.COLOR_NORMAL);
        fontBlueNGray.setBold(true);
        fontBlueNGray.setColor(HSSFColor.BLACK.index);
        styleBlueandFillGrey.setFont(fontBlueNGray);
        styleBlueandFillGrey.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        styleBlueandFillGrey.setFillPattern(CellStyle.SOLID_FOREGROUND);
        styleBlueandFillGrey.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        styleBlueandFillGrey.setBorderRight(HSSFCellStyle.BORDER_THIN);
        styleBlueandFillGrey.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        styleBlueandFillGrey.setBorderTop(HSSFCellStyle.BORDER_THIN);

//        sheet.createFreezePane(0, 1); // Freeze 1st Row
        //create dynamic rownum
        Short rowNum = 0;

        HSSFRow rowtitle = sheet.createRow((short) rowNum);
//        rowtitle.setRowStyle(style);

        HSSFCell cellt_0 = rowtitle.createCell(0);
        cellt_0.setCellStyle(style);
        cellt_0.setCellValue("DOTS - Time Lapse Monthly Performance Report (" + todayDate + ")");

        //add 1 row to current rowNum
        rowNum = (short) (rowNum + 2);

        HSSFRow rowhead = sheet.createRow((short) rowNum);

        HSSFCell cell1_0 = rowhead.createCell(0);
        cell1_0.setCellStyle(styleBlueWithBorder);
        cell1_0.setCellValue("Event");

        //add 1 row to current rowNum
        rowNum = (short) (rowNum + 1);

        //insert to first row for ship to SF
        HSSFRow rowforShiptoSF = sheet.createRow((short) rowNum);

        HSSFCell celltitleShpToSf = rowforShiptoSF.createCell(0);
        celltitleShpToSf.setCellStyle(styleWithBorder);
        celltitleShpToSf.setCellValue("TC");

        //add 1 row to current rowNum
        rowNum = (short) (rowNum + 1);

        //insert to 2nd row for ship to RL
        HSSFRow rowforShiptoRL = sheet.createRow((short) rowNum);

        HSSFCell celltitleShpToRL = rowforShiptoRL.createCell(0);
        celltitleShpToRL.setCellStyle(styleWithBorder);
        celltitleShpToRL.setCellValue("HTSL");

        HSSFFont fontRed = workbook.createFont();
        fontRed.setColor(HSSFColor.RED.index);

        Integer cellCol = 1;

        //current month minus 1 coz wanna start from previous month
//        monthTemp = monthTemp - 1;
        //loop for total activity
        for (int x = 1; x <= 12; x++) {
            if (monthTemp < 1) {
//                year = year - 1;
                year -= 1;
                monthTemp = 12;
            }

            Month month = Month.of(monthTemp);
            String monthNameFull = month.name();

            String monthName = monthNameFull.substring(0, 3);
            String year1 = year.toString().substring(2, 4);

            //total Ship for TC
            UslTimelapseDAO count = new UslTimelapseDAO();
            Integer totalItemTC = count.getCountTotalByEventAndMonthAndYear("TC", monthTemp.toString(), year.toString());

            count = new UslTimelapseDAO();
            Integer totalFailTC = count.getCountFailByEventAndMonthAndYear("TC", monthTemp.toString(), year.toString());

            count = new UslTimelapseDAO();
            Integer totalItemHTSL = count.getCountTotalByEventAndMonthAndYear("HTSL", monthTemp.toString(), year.toString());

            count = new UslTimelapseDAO();
            Integer totalFailHTSL = count.getCountFailByEventAndMonthAndYear("HTSL", monthTemp.toString(), year.toString());

            HSSFCell cell1_month = rowhead.createCell(cellCol);
            cell1_month.setCellStyle(style);
            cell1_month.setCellStyle(styleBlueWithBorder);
            cell1_month.setCellValue(monthName + "-" + year1);

            HSSFRichTextString ship = new HSSFRichTextString(totalFailTC + "/" + totalItemTC);
            ship.applyFont(0, Integer.toString(totalFailTC).length(), fontRed);

            HSSFCell cellContentDec16 = rowforShiptoSF.createCell(cellCol);
            cellContentDec16.setCellStyle(styleWithBorder);
            cellContentDec16.setCellValue(ship);

            HSSFRichTextString retrieve = new HSSFRichTextString(totalFailHTSL + "/" + totalItemHTSL);
            retrieve.applyFont(0, Integer.toString(totalFailHTSL).length(), fontRed);

            HSSFCell cellContentRetrieveDec16 = rowforShiptoRL.createCell(cellCol);
            cellContentRetrieveDec16.setCellStyle(styleWithBorder);
            cellContentRetrieveDec16.setCellValue(retrieve);

//            monthTemp = monthTemp - 1;
//            cellCol = cellCol + 1;
            monthTemp -= 1;
            cellCol += 1;
        }

        rowNum = (short) (rowNum + 3);

        //remark
        HSSFRow rowforRemarks = sheet.createRow((short) rowNum);

        HSSFCell celltitleRemarks = rowforRemarks.createCell(0);
//        celltitleRemarks.setCellStyle(style);
        celltitleRemarks.setCellStyle(styleRed);
        celltitleRemarks.setCellValue("Remarks *");

        HSSFCell celltitleRemarksContent = rowforRemarks.createCell(1);
        celltitleRemarksContent.setCellStyle(styleRed);
        celltitleRemarksContent.setCellValue("- Number of items fail from total activity USL / number of items");

        //add 1 row to current rowNum
        rowNum = (short) (rowNum + 1);

        //remark
        HSSFRow rowforRemarks2 = sheet.createRow((short) rowNum);

        HSSFCell celltitleRemarksContent2 = rowforRemarks2.createCell(1);
        celltitleRemarksContent2.setCellStyle(styleRed);
        celltitleRemarksContent2.setCellValue("- Total USL = 74 hours");

        //add 1 row to current rowNum
        rowNum = (short) (rowNum + 1);

        //remark
        HSSFRow rowforRemarks3 = sheet.createRow((short) rowNum);

        HSSFCell celltitleRemarksContent3 = rowforRemarks3.createCell(1);
        celltitleRemarksContent3.setCellStyle(styleRed);
        celltitleRemarksContent3.setCellValue("- Shipped to Received (12h), Received to Loading (24h), Unloading to Shipped (14h), Shipped to Closed (24h)");

        //add 2 row to current rowNum
        rowNum = (short) (rowNum + 2);

        //Title
        HSSFRow rowfortitle = sheet.createRow((short) rowNum);

        HSSFCell celltitle = rowfortitle.createCell(0);
        celltitle.setCellStyle(style);
        celltitle.setCellValue("List of Failed Items");

        Calendar now2 = Calendar.getInstance();
        Integer year2 = now2.get(Calendar.YEAR);
//        Integer monthTemp2 = now2.get(Calendar.MONTH) + 1; //dont add 1 becoz wanna start with previous month
        Integer monthTemp2 = now2.get(Calendar.MONTH);
//        Integer monthTemp2 = 2;

        Integer cellColumn = 0;
        Integer stop = 0; //to stop on december

        //loop for all item details in month
//        for (int x = 12; x > 0; x--) {
        for (int x = 12; x > 0 && stop == 0; x--) { //stop loop when reach december

            if (monthTemp2 < 1) {
//                year2 = year2 - 1;
                year2 -= 1;
                monthTemp2 = 12;
                stop = 1;
            }

            if (monthTemp2 == 12) {
                stop = 1;
            }

            //insert activity failed
            String failSteps = "";
            String flag = "0";
            String usl = "";

            //add 2 row to current rowNum
            rowNum = (short) (rowNum + 2);

            Month month = Month.of(monthTemp2);
            String monthName = month.name();
            String yearSub = year2.toString().substring(2, 4);

            //Item fail header Dec 17
            HSSFRow rowforItemFailDec17 = sheet.createRow((short) rowNum);
            HSSFCell cell1Dec17 = rowforItemFailDec17.createCell(0);
            cell1Dec17.setCellStyle(style2);
            cell1Dec17.setCellValue(monthName + " " + yearSub);

            //add 1 row to current rowNum
            rowNum = (short) (rowNum + 1);

            HSSFRow rowforItemFailDec17Ship = sheet.createRow((short) rowNum);
            HSSFCell cell1Dec17Fail = rowforItemFailDec17Ship.createCell(0);
            cell1Dec17Fail.setCellStyle(styleGreen);
            cell1Dec17Fail.setCellValue("TC");

            //add 1 row to current rowNum
            rowNum = (short) (rowNum + 1);

            //Item fail details Dec 17
            HSSFRow rowforItemFailHeaderDec17 = sheet.createRow((short) rowNum);

            HSSFCell cell1ItemtypeDec17 = rowforItemFailHeaderDec17.createCell(0);
            cell1ItemtypeDec17.setCellStyle(styleBlueandFillGrey);
            cell1ItemtypeDec17.setCellValue("RMS_Event");

//            HSSFCell cell1ItemIdDec17 = rowforItemFailHeaderDec17.createCell(2);
//            cell1ItemIdDec17.setCellStyle(styleBlueandFillGrey);
//            cell1ItemIdDec17.setCellValue("Lot");
            HSSFCell cell1mpNoDec17 = rowforItemFailHeaderDec17.createCell(2);
            cell1mpNoDec17.setCellStyle(styleBlueandFillGrey);
            cell1mpNoDec17.setCellValue("Interval");

            HSSFCell cell1DurationDec17 = rowforItemFailHeaderDec17.createCell(4);
            cell1DurationDec17.setCellStyle(styleBlueandFillGrey);
            cell1DurationDec17.setCellValue("Duration (hrs)");

            HSSFCell cell1FailedDec17 = rowforItemFailHeaderDec17.createCell(6);
            cell1FailedDec17.setCellStyle(styleBlueandFillGrey);
            cell1FailedDec17.setCellValue("Process Steps Over USL");

//            HSSFCell cell1Rc = rowforItemFailHeaderDec17.createCell(13);
//            cell1Rc.setCellStyle(styleBlueandFillGrey);
//            cell1Rc.setCellValue("Root Cause");
//
//            HSSFCell cell1Ca = rowforItemFailHeaderDec17.createCell(17);
//            cell1Ca.setCellStyle(styleBlueandFillGrey);
//            cell1Ca.setCellValue("Correlative Action");
            HSSFCell cel1mpNoDec17 = rowforItemFailHeaderDec17.createCell(1);
            cel1mpNoDec17.setCellStyle(styleBlueandFillGrey);

            HSSFCell cell3mpNoDec17 = rowforItemFailHeaderDec17.createCell(3);
            cell3mpNoDec17.setCellStyle(styleBlueandFillGrey);

            HSSFCell cell5mpNoDec17 = rowforItemFailHeaderDec17.createCell(5);
            cell5mpNoDec17.setCellStyle(styleBlueandFillGrey);

            HSSFCell cell7mpNoDec17 = rowforItemFailHeaderDec17.createCell(7);
            cell7mpNoDec17.setCellStyle(styleBlueandFillGrey);

            HSSFCell cell8mpNoDec17 = rowforItemFailHeaderDec17.createCell(8);
            cell8mpNoDec17.setCellStyle(styleBlueandFillGrey);

            HSSFCell cell9mpNoDec17 = rowforItemFailHeaderDec17.createCell(9);
            cell9mpNoDec17.setCellStyle(styleBlueandFillGrey);

            HSSFCell cell0mpNoDec17 = rowforItemFailHeaderDec17.createCell(10);
            cell0mpNoDec17.setCellStyle(styleBlueandFillGrey);

            HSSFCell cell11mpNoDec17 = rowforItemFailHeaderDec17.createCell(11);
            cell11mpNoDec17.setCellStyle(styleBlueandFillGrey);

//            HSSFCell cell13mpNoDec17 = rowforItemFailHeaderDec17.createCell(12);
//            cell13mpNoDec17.setCellStyle(styleBlueandFillGrey);
//            HSSFCell cell14mpNoDec17 = rowforItemFailHeaderDec17.createCell(14);
//            cell14mpNoDec17.setCellStyle(styleBlueandFillGrey);
//
//            HSSFCell cell15mpNoDec17 = rowforItemFailHeaderDec17.createCell(15);
//            cell15mpNoDec17.setCellStyle(styleBlueandFillGrey);
//
//            HSSFCell cell117mpNoDec17 = rowforItemFailHeaderDec17.createCell(16);
//            cell117mpNoDec17.setCellStyle(styleBlueandFillGrey);
//
//            HSSFCell cell118mpNoDec17 = rowforItemFailHeaderDec17.createCell(18);
//            cell118mpNoDec17.setCellStyle(styleBlueandFillGrey);
//
//            HSSFCell cell19mpNoDec17 = rowforItemFailHeaderDec17.createCell(19);
//            cell19mpNoDec17.setCellStyle(styleBlueandFillGrey);
//
//            HSSFCell cell19mpNoDec20 = rowforItemFailHeaderDec17.createCell(20);
//            cell19mpNoDec20.setCellStyle(styleBlueandFillGrey);
            UslTimelapseDAO countTCFail = new UslTimelapseDAO();
            Integer totalFailTC = countTCFail.getCountFailByEventAndMonthAndYear("TC", monthTemp2.toString(), year2.toString());

            if (totalFailTC > 0) {

                UslTimelapseDAO fail = new UslTimelapseDAO();
                List<UslTimelapse> failTc = fail.GetListOfFailedItemByEventAndMonthAndYear("TC", monthTemp2.toString(), year2.toString());
                for (int i = 0; i < failTc.size(); i++) {
                    //add 1 row to current rowNum
                    rowNum = (short) (rowNum + 1);
                    //insert to failed item details for dec 17
                    HSSFRow rowforShiptoSFFail = sheet.createRow((short) rowNum);

                    HSSFCell celltitleShpToSFFailType = rowforShiptoSFFail.createCell(0);
                    celltitleShpToSFFailType.setCellStyle(styleWithBorder);
                    celltitleShpToSFFailType.setCellValue(failTc.get(i).getRms() + failTc.get(i).getLot() + "_" + failTc.get(i).getEvent());

//                    HSSFCell celltitleShpToSFFailId = rowforShiptoSFFail.createCell(2);
//                    celltitleShpToSFFailId.setCellStyle(styleWithBorder);
//                    celltitleShpToSFFailId.setCellValue(failTc.get(i).getLot());
                    HSSFCell celltitleShpToSFFailMP = rowforShiptoSFFail.createCell(2);
                    celltitleShpToSFFailMP.setCellStyle(styleWithBorder);
                    celltitleShpToSFFailMP.setCellValue(failTc.get(i).getIntervals());

//                    HSSFCell celltitleShpToSFFailDur = rowforShiptoSFFail.createCell(4);
//                    celltitleShpToSFFailDur.setCellStyle(styleWithBorder);
//                    celltitleShpToSFFailDur.setCellValue(failTc.get(i).getTotalUSl());
                    failSteps = "";
                    flag = "0";
                    usl = "";

                    if (Integer.parseInt(failTc.get(i).getShipToReceived()) > 12) {
                        failSteps = "Shipped to Received in Penang";
                        flag = "1";
                        usl = failTc.get(i).getShipToReceived();
                    }

                    if (Integer.parseInt(failTc.get(i).getReceivedToLoad()) > 24) {
                        if ("1".equals(flag)) {
                            failSteps = failSteps + "; Received to Loading";
                            flag = "1";
                            usl = usl + "; " + failTc.get(i).getReceivedToLoad();
                        } else {
                            failSteps = "Received to Loading";
                            flag = "1";
                            usl = failTc.get(i).getReceivedToLoad();
                        }
                    }

                    if (Integer.parseInt(failTc.get(i).getUnloadToShip()) > 14) {
                        if ("1".equals(flag)) {
                            failSteps = failSteps + "; Unloading to Shipped to SBN Rel Lab";
                            flag = "1";
                            usl = usl + "; " + failTc.get(i).getUnloadToShip();
                        } else {
                            failSteps = "Unloading to Shipped to SBN Rel Lab";
                            flag = "1";
                            usl = failTc.get(i).getUnloadToShip();
                        }
                    }

                    if (Integer.parseInt(failTc.get(i).getShipToClosed()) > 24) {
                        if ("1".equals(flag)) {
                            failSteps = failSteps + "; Shipped from Penang to Closed in DOTS";
                            flag = "1";
                            usl = usl + "; " + failTc.get(i).getShipToClosed();
                        } else {
                            failSteps = "Shipped from Penang to Closed in DOTS";
                            flag = "1";
                            usl = failTc.get(i).getShipToClosed();
                        }
                    }

                    HSSFCell celltitleShpToSFFailDur = rowforShiptoSFFail.createCell(4);
                    celltitleShpToSFFailDur.setCellStyle(styleWithBorder);
                    celltitleShpToSFFailDur.setCellValue(usl);

                    HSSFCell celltitleFailStepFailDur = rowforShiptoSFFail.createCell(6);
                    celltitleFailStepFailDur.setCellStyle(styleWithBorder);
                    celltitleFailStepFailDur.setCellValue(failSteps);

//                    HSSFCell celltitleFailShipRc = rowforShiptoSFFail.createCell(13);
//                    celltitleFailShipRc.setCellStyle(styleWithBorder);
//                    celltitleFailShipRc.setCellValue("");
//
//                    HSSFCell celltitleFailShipCa = rowforShiptoSFFail.createCell(17);
//                    celltitleFailShipCa.setCellStyle(styleWithBorder);
//                    celltitleFailShipCa.setCellValue("");
                    HSSFCell cell2 = rowforShiptoSFFail.createCell(1);
                    cell2.setCellStyle(styleWithBorder);

                    HSSFCell cell4 = rowforShiptoSFFail.createCell(3);
                    cell4.setCellStyle(styleWithBorder);

                    HSSFCell cell7 = rowforShiptoSFFail.createCell(5);
                    cell7.setCellStyle(styleWithBorder);

                    HSSFCell cell8 = rowforShiptoSFFail.createCell(7);
                    cell8.setCellStyle(styleWithBorder);

                    HSSFCell cell88 = rowforShiptoSFFail.createCell(8);
                    cell88.setCellStyle(styleWithBorder);

                    HSSFCell cell9 = rowforShiptoSFFail.createCell(9);
                    cell9.setCellStyle(styleWithBorder);

                    HSSFCell cell10 = rowforShiptoSFFail.createCell(10);
                    cell10.setCellStyle(styleWithBorder);

                    HSSFCell cell11 = rowforShiptoSFFail.createCell(11);
                    cell11.setCellStyle(styleWithBorder);

//                    HSSFCell cell12 = rowforShiptoSFFail.createCell(12);
//                    cell12.setCellStyle(styleWithBorder);
//                    HSSFCell cell14 = rowforShiptoSFFail.createCell(14);
//                    cell14.setCellStyle(styleWithBorder);
//
//                    HSSFCell cell15 = rowforShiptoSFFail.createCell(15);
//                    cell15.setCellStyle(styleWithBorder);
//
//                    HSSFCell cell16 = rowforShiptoSFFail.createCell(16);
//                    cell16.setCellStyle(styleWithBorder);
//
//                    HSSFCell cell18 = rowforShiptoSFFail.createCell(18);
//                    cell18.setCellStyle(styleWithBorder);
//
//                    HSSFCell cell19 = rowforShiptoSFFail.createCell(19);
//                    cell19.setCellStyle(styleWithBorder);
//
//                    HSSFCell cell120 = rowforShiptoSFFail.createCell(20);
//                    cell120.setCellStyle(styleWithBorder);
                }
            } else {
                rowNum = (short) (rowNum + 1);
                //insert to failed item details for dec 17
                HSSFRow rowforShiptoSFFail = sheet.createRow((short) rowNum);
                HSSFCell celltitleShpToSFFailType = rowforShiptoSFFail.createCell(0);
                celltitleShpToSFFailType.setCellValue("N/A");

            }

            //add 2 row to current rowNum
            rowNum = (short) (rowNum + 2);

            HSSFRow rowforItemFailDec17Retrieve = sheet.createRow((short) rowNum);
            HSSFCell cell1Dec17FailRet = rowforItemFailDec17Retrieve.createCell(0);
            cell1Dec17FailRet.setCellStyle(styleGreen);
            cell1Dec17FailRet.setCellValue("HTSL");

            //add 1 row to current rowNum
            rowNum = (short) (rowNum + 1);

            //Item fail details Dec 17
            HSSFRow rowforItemFailHeaderDec17Retrieve = sheet.createRow((short) rowNum);

            HSSFCell cell1ItemtypeDec17Retrieve = rowforItemFailHeaderDec17Retrieve.createCell(0);
            cell1ItemtypeDec17Retrieve.setCellStyle(styleBlueandFillGrey);
            cell1ItemtypeDec17Retrieve.setCellValue("RMS_Event");

//            HSSFCell cell1ItemIdDec17Retrieve = rowforItemFailHeaderDec17Retrieve.createCell(2);
//            cell1ItemIdDec17Retrieve.setCellStyle(styleBlueandFillGrey);
//            cell1ItemIdDec17Retrieve.setCellValue("Lot");
            HSSFCell cell1mpNoDec17Retrieve = rowforItemFailHeaderDec17Retrieve.createCell(2);
            cell1mpNoDec17Retrieve.setCellStyle(styleBlueandFillGrey);
            cell1mpNoDec17Retrieve.setCellValue("Interval");

            HSSFCell cell1DurationDec17Retrieve = rowforItemFailHeaderDec17Retrieve.createCell(4);
            cell1DurationDec17Retrieve.setCellStyle(styleBlueandFillGrey);
            cell1DurationDec17Retrieve.setCellValue("Duration (hrs)");

            HSSFCell cell1StepDec17Retrieve = rowforItemFailHeaderDec17Retrieve.createCell(6);
            cell1StepDec17Retrieve.setCellStyle(styleBlueandFillGrey);
            cell1StepDec17Retrieve.setCellValue("Process Steps Over USL");

//            HSSFCell cell1RcDec17Retrieve = rowforItemFailHeaderDec17Retrieve.createCell(13);
//            cell1RcDec17Retrieve.setCellStyle(styleBlueandFillGrey);
//            cell1RcDec17Retrieve.setCellValue("Root Cause");
//
//            HSSFCell cell1CaDec17Retrieve = rowforItemFailHeaderDec17Retrieve.createCell(17);
//            cell1CaDec17Retrieve.setCellStyle(styleBlueandFillGrey);
//            cell1CaDec17Retrieve.setCellValue("Correlative Action");
            HSSFCell cell22 = rowforItemFailHeaderDec17Retrieve.createCell(1);
            cell22.setCellStyle(styleBlueandFillGrey);

            HSSFCell cell44 = rowforItemFailHeaderDec17Retrieve.createCell(3);
            cell44.setCellStyle(styleBlueandFillGrey);

            HSSFCell cell77 = rowforItemFailHeaderDec17Retrieve.createCell(5);
            cell77.setCellStyle(styleBlueandFillGrey);

            HSSFCell cell88 = rowforItemFailHeaderDec17Retrieve.createCell(7);
            cell88.setCellStyle(styleBlueandFillGrey);

            HSSFCell cell888 = rowforItemFailHeaderDec17Retrieve.createCell(8);
            cell888.setCellStyle(styleBlueandFillGrey);

            HSSFCell cell99 = rowforItemFailHeaderDec17Retrieve.createCell(9);
            cell99.setCellStyle(styleBlueandFillGrey);

            HSSFCell cell100 = rowforItemFailHeaderDec17Retrieve.createCell(10);
            cell100.setCellStyle(styleBlueandFillGrey);

            HSSFCell cell11 = rowforItemFailHeaderDec17Retrieve.createCell(11);
            cell11.setCellStyle(styleBlueandFillGrey);

//            HSSFCell cell12 = rowforItemFailHeaderDec17Retrieve.createCell(12);
//            cell12.setCellStyle(styleBlueandFillGrey);
//            HSSFCell cell14 = rowforItemFailHeaderDec17Retrieve.createCell(14);
//            cell14.setCellStyle(styleBlueandFillGrey);
//
//            HSSFCell cell15 = rowforItemFailHeaderDec17Retrieve.createCell(15);
//            cell15.setCellStyle(styleBlueandFillGrey);
//
//            HSSFCell cell16 = rowforItemFailHeaderDec17Retrieve.createCell(16);
//            cell16.setCellStyle(styleBlueandFillGrey);
//
//            HSSFCell cell18 = rowforItemFailHeaderDec17Retrieve.createCell(18);
//            cell18.setCellStyle(styleBlueandFillGrey);
//
//            HSSFCell cell19 = rowforItemFailHeaderDec17Retrieve.createCell(19);
//            cell19.setCellStyle(styleBlueandFillGrey);
//
//            HSSFCell cell20 = rowforItemFailHeaderDec17Retrieve.createCell(20);
//            cell20.setCellStyle(styleBlueandFillGrey);
            UslTimelapseDAO countHtslFail = new UslTimelapseDAO();
            Integer totalHtslFail = countHtslFail.getCountFailByEventAndMonthAndYear("HTSL", monthTemp2.toString(), year2.toString());

            if (totalHtslFail > 0) {

                UslTimelapseDAO fail = new UslTimelapseDAO();
                List<UslTimelapse> failHtsl = fail.GetListOfFailedItemByEventAndMonthAndYear("HTSL", monthTemp2.toString(), year2.toString());
                for (int i = 0; i < failHtsl.size(); i++) {
                    //add 1 row to current rowNum
                    rowNum = (short) (rowNum + 1);
                    //insert to failed item details for dec 17
                    HSSFRow rowforShiptoSFFail = sheet.createRow((short) rowNum);

                    HSSFCell celltitleShpToSFFailType = rowforShiptoSFFail.createCell(0);
                    celltitleShpToSFFailType.setCellStyle(styleWithBorder);
                    celltitleShpToSFFailType.setCellValue(failHtsl.get(i).getRms() + failHtsl.get(i).getLot() + "_" + failHtsl.get(i).getEvent());

//                    HSSFCell celltitleShpToSFFailId = rowforShiptoSFFail.createCell(2);
//                    celltitleShpToSFFailId.setCellStyle(styleWithBorder);
//                    celltitleShpToSFFailId.setCellValue(failHtsl.get(i).getLot());
                    HSSFCell celltitleShpToSFFailMP = rowforShiptoSFFail.createCell(2);
                    celltitleShpToSFFailMP.setCellStyle(styleWithBorder);
                    celltitleShpToSFFailMP.setCellValue(failHtsl.get(i).getIntervals());

//                    HSSFCell celltitleShpToSFFailDur = rowforShiptoSFFail.createCell(4);
//                    celltitleShpToSFFailDur.setCellStyle(styleWithBorder);
//                    celltitleShpToSFFailDur.setCellValue(failHtsl.get(i).getTotalUSl());
                    flag = "0";
                    failSteps = "";
                    usl = "";

                    if (Integer.parseInt(failHtsl.get(i).getShipToReceived()) > 12) {
                        failSteps = "Shipped to Received in Penang";
                        flag = "1";
                        usl = failHtsl.get(i).getShipToReceived();
                    }

                    if (Integer.parseInt(failHtsl.get(i).getReceivedToLoad()) > 24) {
                        if ("1".equals(flag)) {
                            failSteps = failSteps + "; Received to Loading";
                            flag = "1";
                            usl = usl + "; " + failHtsl.get(i).getReceivedToLoad();
                        } else {
                            failSteps = "Received to Loading";
                            flag = "1";
                            usl = failHtsl.get(i).getReceivedToLoad();
                        }
                    }

                    if (Integer.parseInt(failHtsl.get(i).getUnloadToShip()) > 14) {
                        if ("1".equals(flag)) {
                            failSteps = failSteps + "; Unloading to Shipped to SBN Rel Lab";
                            flag = "1";
                            usl = usl + "; " + failHtsl.get(i).getUnloadToShip();
                        } else {
                            failSteps = "Unloading to Shipped to SBN Rel Lab";
                            flag = "1";
                            usl = failHtsl.get(i).getUnloadToShip();
                        }
                    }

                    if (Integer.parseInt(failHtsl.get(i).getShipToClosed()) > 24) {
                        if ("1".equals(flag)) {
                            failSteps = failSteps + "; Shipped from Penang to Closed in DOTS";
                            flag = "1";
                            usl = usl + "; " + failHtsl.get(i).getShipToClosed();
                        } else {
                            failSteps = "Shipped from Penang to Closed in DOTS";
                            flag = "1";
                            usl = failHtsl.get(i).getShipToClosed();
                        }
                    }

                    HSSFCell celltitleShpToSFFailDur = rowforShiptoSFFail.createCell(4);
                    celltitleShpToSFFailDur.setCellStyle(styleWithBorder);
                    celltitleShpToSFFailDur.setCellValue(usl);

                    HSSFCell celltitleFailStepFailDur = rowforShiptoSFFail.createCell(6);
                    celltitleFailStepFailDur.setCellStyle(styleWithBorder);
                    celltitleFailStepFailDur.setCellValue(failSteps);

//                    HSSFCell celltitleFailStepFailRc = rowforShiptoSFFail.createCell(13);
//                    celltitleFailStepFailRc.setCellStyle(styleWithBorder);
//                    celltitleFailStepFailRc.setCellValue("");
//
//                    HSSFCell celltitleFailStepFailCa = rowforShiptoSFFail.createCell(17);
//                    celltitleFailStepFailCa.setCellStyle(styleWithBorder);
//                    celltitleFailStepFailCa.setCellValue("");
                    HSSFCell cell1 = rowforShiptoSFFail.createCell(1);
                    cell1.setCellStyle(styleWithBorder);

                    HSSFCell cell3 = rowforShiptoSFFail.createCell(3);
                    cell3.setCellStyle(styleWithBorder);

                    HSSFCell cell5 = rowforShiptoSFFail.createCell(5);
                    cell5.setCellStyle(styleWithBorder);

                    HSSFCell cell7 = rowforShiptoSFFail.createCell(7);
                    cell7.setCellStyle(styleWithBorder);

                    HSSFCell cell8 = rowforShiptoSFFail.createCell(8);
                    cell8.setCellStyle(styleWithBorder);

                    HSSFCell cell9 = rowforShiptoSFFail.createCell(9);
                    cell9.setCellStyle(styleWithBorder);

                    HSSFCell cell10 = rowforShiptoSFFail.createCell(10);
                    cell10.setCellStyle(styleWithBorder);

                    HSSFCell cell11R = rowforShiptoSFFail.createCell(11);
                    cell11R.setCellStyle(styleWithBorder);

//                    HSSFCell cell12R = rowforShiptoSFFail.createCell(12);
//                    cell12R.setCellStyle(styleWithBorder);
//                    HSSFCell cell14R = rowforShiptoSFFail.createCell(14);
//                    cell14R.setCellStyle(styleWithBorder);
//
//                    HSSFCell cell15R = rowforShiptoSFFail.createCell(15);
//                    cell15R.setCellStyle(styleWithBorder);
//
//                    HSSFCell cell16R = rowforShiptoSFFail.createCell(16);
//                    cell16R.setCellStyle(styleWithBorder);
//
//                    HSSFCell cell8R = rowforShiptoSFFail.createCell(18);
//                    cell8R.setCellStyle(styleWithBorder);
//
//                    HSSFCell cell9R = rowforShiptoSFFail.createCell(19);
//                    cell9R.setCellStyle(styleWithBorder);
//
//                    HSSFCell cel20R = rowforShiptoSFFail.createCell(20);
//                    cel20R.setCellStyle(styleWithBorder);
                }
            } else {
                rowNum = (short) (rowNum + 1);
                //insert to failed item details for dec 17
                HSSFRow rowforShiptoSFFail = sheet.createRow((short) rowNum);
                HSSFCell celltitleShpToSFFailType = rowforShiptoSFFail.createCell(0);
                celltitleShpToSFFailType.setCellValue("N/A");

            }

            //add 2 row to current rowNum
//            rowNum = (short) (rowNum + 2);
            flag = "0";
            failSteps = "";
            usl = "";

//            monthTemp2 = monthTemp2 - 1;
//            cellColumn = cellColumn + 1;
            monthTemp2 -= 1;
            cellColumn += 1;

        }
        //end of loop for all items details in month

        //auto resize column
//        sheet.autoSizeColumn(0);    //autosize utk title
//        for (int columnIndex = 2; columnIndex < 15; columnIndex++) {
//            sheet.autoSizeColumn(columnIndex);
//        }
        //merger cell for remark content
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4)); //rowstr, rowend, colstr, colend
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 1, 6)); //rowstr, rowend, colstr, colend
        sheet.addMergedRegion(new CellRangeAddress(6, 6, 1, 6)); //rowstr, rowend, colstr, colend
        sheet.addMergedRegion(new CellRangeAddress(7, 7, 1, 5)); //rowstr, rowend, colstr, colend

        for (int columnIndex = 15; columnIndex < (rowNum + 1); columnIndex++) {
            sheet.addMergedRegion(new CellRangeAddress(columnIndex, columnIndex, 0, 1));  //rms  rowstr, rowend, colstr, colend 
            sheet.addMergedRegion(new CellRangeAddress(columnIndex, columnIndex, 2, 3));  //interval
            sheet.addMergedRegion(new CellRangeAddress(columnIndex, columnIndex, 4, 5));  //duration
            sheet.addMergedRegion(new CellRangeAddress(columnIndex, columnIndex, 6, 11));  //status
//            sheet.addMergedRegion(new CellRangeAddress(columnIndex, columnIndex, 8, 12)); //status
//            sheet.addMergedRegion(new CellRangeAddress(columnIndex, columnIndex, 13, 16));//root cause
//            sheet.addMergedRegion(new CellRangeAddress(columnIndex, columnIndex, 17, 20));//ca
        }

        workbook.write(fileOut);
        workbook.close();

        //send email
        LOGGER.info("send email to person in charge");
        EmailSender emailSender = new EmailSender();
        com.onsemi.dots.model.User user = new com.onsemi.dots.model.User();
        user.setFullname("All");

        List<String> To = new ArrayList<String>();
        EmailTimelapseDAO tlD = new EmailTimelapseDAO();
        List<EmailTimelapse> toList = tlD.getEmailTimelapseListForDistList();
        for (EmailTimelapse to : toList) {
            To.add(to.getEmail());
        }
        String[] myArrayTo = new String[To.size()];
        String[] emailTo = To.toArray(myArrayTo);

//        String[] emailTo = {"fg79cj@onsemi.com"};
        emailSender.htmlEmailWithAttachment(
                servletContext,
                user, //user name requestor
                emailTo,
                new File("\\\\mysed-rel-app03\\d$\\DOTS\\DOTS_CSV\\DOTS - Time Lapse Monthly Performance Report (" + todayDate + ").xls"),
                "DOTS - Time Lapse Monthly Performance Report (" + todayDate + ")", //subject
                "Attached is the DOTS time-lapse report for " + todayDate + " . <br />"
                + "<br />"
                + "<br />Thank you." //msg
        );

//        EmailTimelapseDAO tlD = new EmailTimelapseDAO();
//        Integer countCc = tlD.getCountCc();
//
//        if (countCc > 0) { //use htmlEmailWithAttachmentWithCc
//            List<String> Cc = new ArrayList<String>();
//            tlD = new EmailTimelapseDAO();
//            List<EmailTimelapse> CcList = tlD.getEmailTimelapseListForCc();
//            for (EmailTimelapse b : CcList) {
//                Cc.add(b.getEmail());
//            }
//            String[] myArrayCc = new String[Cc.size()];
//            String[] emailCc = Cc.toArray(myArrayCc);
//
//            List<String> To = new ArrayList<String>();
//            tlD = new EmailTimelapseDAO();
//            List<EmailTimelapse> toList = tlD.getEmailTimelapseListForDistList();
//            for (EmailTimelapse to : toList) {
//                To.add(to.getEmail());
//            }
//            String[] myArrayTo = new String[To.size()];
////            String[] emailTo = To.toArray(myArrayTo);
//            
//             String[] emailTo = {"fg79cj@onsemi.com", "ffycrt@onsemi.com"};
//      String[] emailTo = {"fg79cj@onsemi.com", "ffycrt@onsemi.com"};
//
//
//            emailSender.htmlEmailWithAttachmentWithCc(
//                    servletContext,
//                    user, //user name requestor
//                    emailTo,
//                    emailCc,
//                    new File("C:\\Users\\" + username + "\\Documents\\CDARS\\SF and SBN Rel - Time Lapse Report Monthly Performance (" + todayDate + ").xls"),
//                    "SF and SBN Rel - Time Lapse Report Monthly Performance (" + todayDate + ")", //subject
//                    "Attached is the HIMS time-lapse report for " + todayDate + " . <br />"
//                    + "<br />"
//                    + "<br />Thank you." //msg
//            );
//
//        } else { //use htmlEmailWithAttachment 
//
//            List<String> To = new ArrayList<String>();
//            tlD = new EmailTimelapseDAO();
//            List<EmailTimelapse> toList = tlD.getEmailTimelapseListForDistList();
//            for (EmailTimelapse to : toList) {
//                To.add(to.getEmail());
//            }
//            String[] myArrayTo = new String[To.size()];
//            String[] emailTo = To.toArray(myArrayTo);
//
//            emailSender.htmlEmailWithAttachment(
//                    servletContext,
//                    user, //user name requestor
//                    emailTo,
//                    new File("C:\\Users\\" + username + "\\Documents\\CDARS\\SF and SBN Rel - Time Lapse Report Monthly Performance (" + todayDate + ").xls"),
//                    "SF and SBN Rel - Time Lapse Report Monthly Performance (" + todayDate + ")", //subject
//                    "Attached is the HIMS time-lapse report for " + todayDate + " . <br />"
//                    + "<br />"
//                    + "<br />Thank you." //msg
//            );
//        }
    }
}
