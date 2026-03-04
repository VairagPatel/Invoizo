package in.invoizo.invoicegeneratorapi.service;

import in.invoizo.invoicegeneratorapi.entity.Invoice;
import in.invoizo.invoicegeneratorapi.exception.ExportGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class ExportService {

    /**
     * Export invoices to Excel format (XLSX)
     * Includes all invoice fields and line item details
     */
    public byte[] exportToExcel(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            throw new ExportGenerationException("No invoices provided for export");
        }
        
        log.info("Starting Excel export for {} invoices", invoices.size());
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Invoices");
            
            // Create header row with styling
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            String[] headers = {
                "Invoice Number", "Date", "Due Date", "Customer Name", 
                "Customer Phone", "Customer Email", "Customer Address", 
                "Item Description", "Quantity", "Rate", "Item Amount",
                "GST Rate (%)", "CGST", "SGST", "IGST", "Item Total with GST",
                "Subtotal", "Tax", "CGST Total", "SGST Total", "IGST Total", 
                "GST Total", "Grand Total", "Status", "Company Name", 
                "Company GST Number", "Transaction Type"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Populate data rows
            int rowNum = 1;
            for (Invoice invoice : invoices) {
                try {
                    rowNum = populateInvoiceRowsWithItems(sheet, rowNum, invoice, workbook);
                } catch (Exception e) {
                    log.error("Error populating row for invoice: {}", invoice.getId(), e);
                    // Continue with other invoices
                }
            }
            
            // Auto-size columns for better readability
            for (int i = 0; i < headers.length; i++) {
                try {
                    sheet.autoSizeColumn(i);
                } catch (Exception e) {
                    log.warn("Failed to auto-size column {}", i);
                }
            }
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            log.info("Excel export completed successfully");
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("Failed to generate Excel export", e);
            throw new ExportGenerationException("Failed to generate Excel export: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during Excel export", e);
            throw new ExportGenerationException("Unexpected error during Excel export: " + e.getMessage(), e);
        }
    }

    /**
     * Export invoices to CSV format
     * Properly escapes commas, quotes, and newlines
     */
    public byte[] exportToCSV(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            throw new ExportGenerationException("No invoices provided for export");
        }
        
        log.info("Starting CSV export for {} invoices", invoices.size());
        
        try {
            StringBuilder csv = new StringBuilder();
            
            // Add header row
            csv.append("Invoice Number,Date,Due Date,Customer Name,Customer Phone,Customer Email,Customer Address,");
            csv.append("Item Description,Quantity,Rate,Item Amount,GST Rate (%),CGST,SGST,IGST,Item Total with GST,");
            csv.append("Subtotal,Tax,CGST Total,SGST Total,IGST Total,GST Total,Grand Total,");
            csv.append("Status,Company Name,Company GST Number,Transaction Type\n");
            
            // Add data rows
            for (Invoice invoice : invoices) {
                try {
                    csv.append(formatCSVRowsWithItems(invoice));
                } catch (Exception e) {
                    log.error("Error formatting CSV row for invoice: {}", invoice.getId(), e);
                    // Continue with other invoices
                }
            }
            
            log.info("CSV export completed successfully");
            return csv.toString().getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Failed to generate CSV export", e);
            throw new ExportGenerationException("Failed to generate CSV export: " + e.getMessage(), e);
        }
    }

    /**
     * Create styled header for Excel export
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Populate Excel rows with invoice data including all items
     * Returns the next available row number
     */
    private int populateInvoiceRowsWithItems(Sheet sheet, int startRowNum, Invoice invoice, Workbook workbook) {
        int rowNum = startRowNum;
        
        // Calculate totals
        double subtotal = calculateSubtotal(invoice);
        double gstTotal = invoice.getGstDetails() != null ? invoice.getGstDetails().getGstTotal() : 0.0;
        double grandTotal = calculateTotal(invoice);
        
        // If invoice has items, create a row for each item
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            for (int i = 0; i < invoice.getItems().size(); i++) {
                Invoice.Item item = invoice.getItems().get(i);
                Row row = sheet.createRow(rowNum++);
                int cellNum = 0;
                
                // Invoice details (repeated for each item)
                row.createCell(cellNum++).setCellValue(
                    invoice.getInvoice() != null ? invoice.getInvoice().getNumber() : ""
                );
                row.createCell(cellNum++).setCellValue(
                    invoice.getInvoice() != null ? invoice.getInvoice().getDate() : ""
                );
                row.createCell(cellNum++).setCellValue(
                    invoice.getInvoice() != null ? invoice.getInvoice().getDueDate() : ""
                );
                
                // Customer details
                row.createCell(cellNum++).setCellValue(
                    invoice.getBilling() != null ? invoice.getBilling().getName() : ""
                );
                row.createCell(cellNum++).setCellValue(
                    invoice.getBilling() != null ? invoice.getBilling().getPhone() : ""
                );
                row.createCell(cellNum++).setCellValue(
                    invoice.getBilling() != null ? invoice.getBilling().getEmail() : ""
                );
                row.createCell(cellNum++).setCellValue(
                    invoice.getBilling() != null ? invoice.getBilling().getAddress() : ""
                );
                
                // Item details
                row.createCell(cellNum++).setCellValue(item.getDescription() != null ? item.getDescription() : "");
                row.createCell(cellNum++).setCellValue(item.getQty());
                row.createCell(cellNum++).setCellValue(item.getAmount());
                
                double itemAmount = item.getQty() * item.getAmount();
                row.createCell(cellNum++).setCellValue(itemAmount);
                
                // Item GST details
                if (item.getGstRate() > 0) {
                    row.createCell(cellNum++).setCellValue(item.getGstRate());
                    row.createCell(cellNum++).setCellValue(item.getCgstAmount());
                    row.createCell(cellNum++).setCellValue(item.getSgstAmount());
                    row.createCell(cellNum++).setCellValue(item.getIgstAmount());
                    row.createCell(cellNum++).setCellValue(item.getTotalWithGST());
                } else {
                    row.createCell(cellNum++).setCellValue(0.0); // GST Rate
                    row.createCell(cellNum++).setCellValue(0.0); // CGST
                    row.createCell(cellNum++).setCellValue(0.0); // SGST
                    row.createCell(cellNum++).setCellValue(0.0); // IGST
                    row.createCell(cellNum++).setCellValue(itemAmount); // Item Total
                }
                
                // Invoice totals (same for all items of this invoice)
                row.createCell(cellNum++).setCellValue(subtotal);
                row.createCell(cellNum++).setCellValue(invoice.getTax());
                
                // GST totals
                if (invoice.getGstDetails() != null) {
                    row.createCell(cellNum++).setCellValue(invoice.getGstDetails().getCgstTotal());
                    row.createCell(cellNum++).setCellValue(invoice.getGstDetails().getSgstTotal());
                    row.createCell(cellNum++).setCellValue(invoice.getGstDetails().getIgstTotal());
                    row.createCell(cellNum++).setCellValue(invoice.getGstDetails().getGstTotal());
                } else {
                    row.createCell(cellNum++).setCellValue(0.0);
                    row.createCell(cellNum++).setCellValue(0.0);
                    row.createCell(cellNum++).setCellValue(0.0);
                    row.createCell(cellNum++).setCellValue(0.0);
                }
                
                row.createCell(cellNum++).setCellValue(grandTotal);
                
                // Status and company details
                row.createCell(cellNum++).setCellValue(
                    invoice.getStatus() != null ? invoice.getStatus().toString() : "DRAFT"
                );
                row.createCell(cellNum++).setCellValue(
                    invoice.getCompany() != null ? invoice.getCompany().getName() : ""
                );
                row.createCell(cellNum++).setCellValue(
                    invoice.getCompanyGSTNumber() != null ? invoice.getCompanyGSTNumber() : ""
                );
                row.createCell(cellNum++).setCellValue(
                    invoice.getTransactionType() != null ? invoice.getTransactionType().toString() : ""
                );
            }
        } else {
            // No items, create a single row with invoice summary
            Row row = sheet.createRow(rowNum++);
            int cellNum = 0;
            
            // Invoice details
            row.createCell(cellNum++).setCellValue(
                invoice.getInvoice() != null ? invoice.getInvoice().getNumber() : ""
            );
            row.createCell(cellNum++).setCellValue(
                invoice.getInvoice() != null ? invoice.getInvoice().getDate() : ""
            );
            row.createCell(cellNum++).setCellValue(
                invoice.getInvoice() != null ? invoice.getInvoice().getDueDate() : ""
            );
            
            // Customer details
            row.createCell(cellNum++).setCellValue(
                invoice.getBilling() != null ? invoice.getBilling().getName() : ""
            );
            row.createCell(cellNum++).setCellValue(
                invoice.getBilling() != null ? invoice.getBilling().getPhone() : ""
            );
            row.createCell(cellNum++).setCellValue(
                invoice.getBilling() != null ? invoice.getBilling().getEmail() : ""
            );
            row.createCell(cellNum++).setCellValue(
                invoice.getBilling() != null ? invoice.getBilling().getAddress() : ""
            );
            
            // Empty item details
            row.createCell(cellNum++).setCellValue(""); // Description
            row.createCell(cellNum++).setCellValue(0.0); // Qty
            row.createCell(cellNum++).setCellValue(0.0); // Rate
            row.createCell(cellNum++).setCellValue(0.0); // Item Amount
            row.createCell(cellNum++).setCellValue(0.0); // GST Rate
            row.createCell(cellNum++).setCellValue(0.0); // CGST
            row.createCell(cellNum++).setCellValue(0.0); // SGST
            row.createCell(cellNum++).setCellValue(0.0); // IGST
            row.createCell(cellNum++).setCellValue(0.0); // Item Total
            
            // Invoice totals
            row.createCell(cellNum++).setCellValue(subtotal);
            row.createCell(cellNum++).setCellValue(invoice.getTax());
            
            if (invoice.getGstDetails() != null) {
                row.createCell(cellNum++).setCellValue(invoice.getGstDetails().getCgstTotal());
                row.createCell(cellNum++).setCellValue(invoice.getGstDetails().getSgstTotal());
                row.createCell(cellNum++).setCellValue(invoice.getGstDetails().getIgstTotal());
                row.createCell(cellNum++).setCellValue(invoice.getGstDetails().getGstTotal());
            } else {
                row.createCell(cellNum++).setCellValue(0.0);
                row.createCell(cellNum++).setCellValue(0.0);
                row.createCell(cellNum++).setCellValue(0.0);
                row.createCell(cellNum++).setCellValue(0.0);
            }
            
            row.createCell(cellNum++).setCellValue(grandTotal);
            
            // Status and company details
            row.createCell(cellNum++).setCellValue(
                invoice.getStatus() != null ? invoice.getStatus().toString() : "DRAFT"
            );
            row.createCell(cellNum++).setCellValue(
                invoice.getCompany() != null ? invoice.getCompany().getName() : ""
            );
            row.createCell(cellNum++).setCellValue(
                invoice.getCompanyGSTNumber() != null ? invoice.getCompanyGSTNumber() : ""
            );
            row.createCell(cellNum++).setCellValue(
                invoice.getTransactionType() != null ? invoice.getTransactionType().toString() : ""
            );
        }
        
        return rowNum;
    }

    /**
     * Populate a single Excel row with invoice data
     */
    private void populateInvoiceRow(Row row, Invoice invoice) {
        int cellNum = 0;
        
        // Invoice details
        row.createCell(cellNum++).setCellValue(
            invoice.getInvoice() != null ? invoice.getInvoice().getNumber() : ""
        );
        row.createCell(cellNum++).setCellValue(
            invoice.getInvoice() != null ? invoice.getInvoice().getDate() : ""
        );
        row.createCell(cellNum++).setCellValue(
            invoice.getInvoice() != null ? invoice.getInvoice().getDueDate() : ""
        );
        
        // Customer details
        row.createCell(cellNum++).setCellValue(
            invoice.getBilling() != null ? invoice.getBilling().getName() : ""
        );
        row.createCell(cellNum++).setCellValue(
            invoice.getBilling() != null ? invoice.getBilling().getPhone() : ""
        );
        row.createCell(cellNum++).setCellValue(
            invoice.getBilling() != null ? invoice.getBilling().getAddress() : ""
        );
        
        // Financial details
        double subtotal = calculateSubtotal(invoice);
        row.createCell(cellNum++).setCellValue(subtotal);
        row.createCell(cellNum++).setCellValue(invoice.getTax());
        
        double total = calculateTotal(invoice);
        row.createCell(cellNum++).setCellValue(total);
        
        // Status
        row.createCell(cellNum++).setCellValue(
            invoice.getStatus() != null ? invoice.getStatus().toString() : "DRAFT"
        );
        
        // Company details
        row.createCell(cellNum++).setCellValue(
            invoice.getCompany() != null ? invoice.getCompany().getName() : ""
        );
        row.createCell(cellNum++).setCellValue(
            invoice.getCompanyGSTNumber() != null ? invoice.getCompanyGSTNumber() : ""
        );
        row.createCell(cellNum++).setCellValue(
            invoice.getTransactionType() != null ? invoice.getTransactionType().toString() : ""
        );
        
        // GST details
        if (invoice.getGstDetails() != null) {
            row.createCell(cellNum++).setCellValue(invoice.getGstDetails().getCgstTotal());
            row.createCell(cellNum++).setCellValue(invoice.getGstDetails().getSgstTotal());
            row.createCell(cellNum++).setCellValue(invoice.getGstDetails().getIgstTotal());
        } else {
            row.createCell(cellNum++).setCellValue(0.0);
            row.createCell(cellNum++).setCellValue(0.0);
            row.createCell(cellNum++).setCellValue(0.0);
        }
    }

    /**
     * Format invoice with all items as CSV rows
     */
    private String formatCSVRowsWithItems(Invoice invoice) {
        StringBuilder rows = new StringBuilder();
        
        // Calculate totals
        double subtotal = calculateSubtotal(invoice);
        double gstTotal = invoice.getGstDetails() != null ? invoice.getGstDetails().getGstTotal() : 0.0;
        double grandTotal = calculateTotal(invoice);
        
        // If invoice has items, create a row for each item
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            for (Invoice.Item item : invoice.getItems()) {
                StringBuilder row = new StringBuilder();
                
                // Invoice details
                row.append(escapeCSV(invoice.getInvoice() != null ? invoice.getInvoice().getNumber() : "")).append(",");
                row.append(escapeCSV(invoice.getInvoice() != null ? invoice.getInvoice().getDate() : "")).append(",");
                row.append(escapeCSV(invoice.getInvoice() != null ? invoice.getInvoice().getDueDate() : "")).append(",");
                
                // Customer details
                row.append(escapeCSV(invoice.getBilling() != null ? invoice.getBilling().getName() : "")).append(",");
                row.append(escapeCSV(invoice.getBilling() != null ? invoice.getBilling().getPhone() : "")).append(",");
                row.append(escapeCSV(invoice.getBilling() != null ? invoice.getBilling().getEmail() : "")).append(",");
                row.append(escapeCSV(invoice.getBilling() != null ? invoice.getBilling().getAddress() : "")).append(",");
                
                // Item details
                row.append(escapeCSV(item.getDescription() != null ? item.getDescription() : "")).append(",");
                row.append(item.getQty()).append(",");
                row.append(item.getAmount()).append(",");
                
                double itemAmount = item.getQty() * item.getAmount();
                row.append(itemAmount).append(",");
                
                // Item GST details
                if (item.getGstRate() > 0) {
                    row.append(item.getGstRate()).append(",");
                    row.append(item.getCgstAmount()).append(",");
                    row.append(item.getSgstAmount()).append(",");
                    row.append(item.getIgstAmount()).append(",");
                    row.append(item.getTotalWithGST()).append(",");
                } else {
                    row.append("0.0,0.0,0.0,0.0,");
                    row.append(itemAmount).append(",");
                }
                
                // Invoice totals
                row.append(subtotal).append(",");
                row.append(invoice.getTax()).append(",");
                
                // GST totals
                if (invoice.getGstDetails() != null) {
                    row.append(invoice.getGstDetails().getCgstTotal()).append(",");
                    row.append(invoice.getGstDetails().getSgstTotal()).append(",");
                    row.append(invoice.getGstDetails().getIgstTotal()).append(",");
                    row.append(invoice.getGstDetails().getGstTotal()).append(",");
                } else {
                    row.append("0.0,0.0,0.0,0.0,");
                }
                
                row.append(grandTotal).append(",");
                
                // Status and company details
                row.append(escapeCSV(invoice.getStatus() != null ? invoice.getStatus().toString() : "DRAFT")).append(",");
                row.append(escapeCSV(invoice.getCompany() != null ? invoice.getCompany().getName() : "")).append(",");
                row.append(escapeCSV(invoice.getCompanyGSTNumber() != null ? invoice.getCompanyGSTNumber() : "")).append(",");
                row.append(escapeCSV(invoice.getTransactionType() != null ? invoice.getTransactionType().toString() : ""));
                
                row.append("\n");
                rows.append(row);
            }
        } else {
            // No items, create a single row with invoice summary
            StringBuilder row = new StringBuilder();
            
            // Invoice details
            row.append(escapeCSV(invoice.getInvoice() != null ? invoice.getInvoice().getNumber() : "")).append(",");
            row.append(escapeCSV(invoice.getInvoice() != null ? invoice.getInvoice().getDate() : "")).append(",");
            row.append(escapeCSV(invoice.getInvoice() != null ? invoice.getInvoice().getDueDate() : "")).append(",");
            
            // Customer details
            row.append(escapeCSV(invoice.getBilling() != null ? invoice.getBilling().getName() : "")).append(",");
            row.append(escapeCSV(invoice.getBilling() != null ? invoice.getBilling().getPhone() : "")).append(",");
            row.append(escapeCSV(invoice.getBilling() != null ? invoice.getBilling().getEmail() : "")).append(",");
            row.append(escapeCSV(invoice.getBilling() != null ? invoice.getBilling().getAddress() : "")).append(",");
            
            // Empty item details
            row.append(",0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,");
            
            // Invoice totals
            row.append(subtotal).append(",");
            row.append(invoice.getTax()).append(",");
            
            // GST totals
            if (invoice.getGstDetails() != null) {
                row.append(invoice.getGstDetails().getCgstTotal()).append(",");
                row.append(invoice.getGstDetails().getSgstTotal()).append(",");
                row.append(invoice.getGstDetails().getIgstTotal()).append(",");
                row.append(invoice.getGstDetails().getGstTotal()).append(",");
            } else {
                row.append("0.0,0.0,0.0,0.0,");
            }
            
            row.append(grandTotal).append(",");
            
            // Status and company details
            row.append(escapeCSV(invoice.getStatus() != null ? invoice.getStatus().toString() : "DRAFT")).append(",");
            row.append(escapeCSV(invoice.getCompany() != null ? invoice.getCompany().getName() : "")).append(",");
            row.append(escapeCSV(invoice.getCompanyGSTNumber() != null ? invoice.getCompanyGSTNumber() : "")).append(",");
            row.append(escapeCSV(invoice.getTransactionType() != null ? invoice.getTransactionType().toString() : ""));
            
            row.append("\n");
            rows.append(row);
        }
        
        return rows.toString();
    }

    /**
     * Format a single invoice as a CSV row with proper escaping
     */
    private String formatCSVRow(Invoice invoice) {
        StringBuilder row = new StringBuilder();
        
        // Invoice details
        row.append(escapeCSV(invoice.getInvoice() != null ? invoice.getInvoice().getNumber() : "")).append(",");
        row.append(escapeCSV(invoice.getInvoice() != null ? invoice.getInvoice().getDate() : "")).append(",");
        row.append(escapeCSV(invoice.getInvoice() != null ? invoice.getInvoice().getDueDate() : "")).append(",");
        
        // Customer details
        row.append(escapeCSV(invoice.getBilling() != null ? invoice.getBilling().getName() : "")).append(",");
        row.append(escapeCSV(invoice.getBilling() != null ? invoice.getBilling().getPhone() : "")).append(",");
        row.append(escapeCSV(invoice.getBilling() != null ? invoice.getBilling().getAddress() : "")).append(",");
        
        // Financial details
        double subtotal = calculateSubtotal(invoice);
        row.append(subtotal).append(",");
        row.append(invoice.getTax()).append(",");
        
        double total = calculateTotal(invoice);
        row.append(total).append(",");
        
        // Status
        row.append(escapeCSV(invoice.getStatus() != null ? invoice.getStatus().toString() : "DRAFT")).append(",");
        
        // Company details
        row.append(escapeCSV(invoice.getCompany() != null ? invoice.getCompany().getName() : "")).append(",");
        row.append(escapeCSV(invoice.getCompanyGSTNumber() != null ? invoice.getCompanyGSTNumber() : "")).append(",");
        row.append(escapeCSV(invoice.getTransactionType() != null ? invoice.getTransactionType().toString() : "")).append(",");
        
        // GST details
        if (invoice.getGstDetails() != null) {
            row.append(invoice.getGstDetails().getCgstTotal()).append(",");
            row.append(invoice.getGstDetails().getSgstTotal()).append(",");
            row.append(invoice.getGstDetails().getIgstTotal()).append(",");
        } else {
            row.append("0.0,0.0,0.0,");
        }
        
        
        row.append("\n");
        return row.toString();
    }

    /**
     * Escape special characters in CSV fields
     * Handles commas, quotes, and newlines according to RFC 4180
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        
        // If the value contains comma, quote, or newline, wrap it in quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            // Escape existing quotes by doubling them
            String escaped = value.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        
        return value;
    }

    /**
     * Calculate subtotal from invoice items
     */
    private double calculateSubtotal(Invoice invoice) {
        if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
            return 0.0;
        }
        
        return invoice.getItems().stream()
            .mapToDouble(item -> item.getQty() * item.getAmount())
            .sum();
    }

    /**
     * Calculate total including GST
     */
    private double calculateTotal(Invoice invoice) {
        double subtotal = calculateSubtotal(invoice);
        
        if (invoice.getGstDetails() != null) {
            return subtotal + invoice.getGstDetails().getGstTotal();
        }
        
        return subtotal + invoice.getTax();
    }
}
