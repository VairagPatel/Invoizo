import html2canvas from "html2canvas";
import jsPDF from "jspdf";

/**
 * Generate PDF from Modern Invoice Template
 * Optimized for the new design with better quality and color preservation
 */
export const generateModernPdf = async (element, fileName = "invoice.pdf", returnBlob = false) => {
    try {
        // Higher scale for better quality
        const canvas = await html2canvas(element, {
            scale: 3,
            useCORS: true,
            allowTaint: true,
            backgroundColor: "#ffffff",
            scrollY: -window.scrollY,
            scrollX: -window.scrollX,
            windowWidth: element.scrollWidth,
            windowHeight: element.scrollHeight,
            logging: false,
            imageTimeout: 0,
            // Preserve colors and gradients
            foreignObjectRendering: false,
        });

        const imgData = canvas.toDataURL("image/png", 1.0);
        const pdf = new jsPDF({
            orientation: "portrait",
            unit: "pt",
            format: "a4",
            compress: true,
        });

        const imgProps = pdf.getImageProperties(imgData);
        const pdfWidth = pdf.internal.pageSize.getWidth();
        const pdfHeight = pdf.internal.pageSize.getHeight();
        
        // Calculate dimensions to fit page
        const imgWidth = pdfWidth;
        const imgHeight = (imgProps.height * pdfWidth) / imgProps.width;

        // Handle multi-page PDFs if content is too long
        let heightLeft = imgHeight;
        let position = 0;

        pdf.addImage(imgData, "PNG", 0, position, imgWidth, imgHeight, undefined, "FAST");
        heightLeft -= pdfHeight;

        // Add additional pages if needed
        while (heightLeft > 0) {
            position = heightLeft - imgHeight;
            pdf.addPage();
            pdf.addImage(imgData, "PNG", 0, position, imgWidth, imgHeight, undefined, "FAST");
            heightLeft -= pdfHeight;
        }

        if (returnBlob) {
            return pdf.output("blob");
        } else {
            pdf.save(fileName);
        }
    } catch (error) {
        console.error("Error generating PDF:", error);
        throw new Error("Failed to generate PDF. Please try again.");
    }
};

/**
 * Generate PDF with Razorpay payment details embedded
 */
export const generateInvoicePdfWithPayment = async (
    element,
    invoiceData,
    fileName = "invoice.pdf"
) => {
    try {
        // Ensure payment details are included in the rendered element
        if (invoiceData.razorpayPaymentLink || invoiceData.paymentDetails?.paymentLink) {
            console.log("Generating invoice with payment link");
        }

        return await generateModernPdf(element, fileName, false);
    } catch (error) {
        console.error("Error generating invoice PDF with payment:", error);
        throw error;
    }
};

/**
 * Generate PDF blob for email attachment
 */
export const generatePdfBlob = async (element) => {
    return await generateModernPdf(element, "invoice.pdf", true);
};

/**
 * Download PDF with custom filename based on invoice number
 */
export const downloadInvoicePdf = async (element, invoiceNumber) => {
    const fileName = `Invoice_${invoiceNumber}_${new Date().getTime()}.pdf`;
    return await generateModernPdf(element, fileName, false);
};

/**
 * Preview PDF in new tab (for testing)
 */
export const previewPdf = async (element) => {
    try {
        const blob = await generateModernPdf(element, "invoice.pdf", true);
        const url = URL.createObjectURL(blob);
        window.open(url, "_blank");
        
        // Clean up after a delay
        setTimeout(() => URL.revokeObjectURL(url), 100);
    } catch (error) {
        console.error("Error previewing PDF:", error);
        throw error;
    }
};
