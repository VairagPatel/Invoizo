import { useRef, useState } from 'react';
import ModernInvoice from '../templates/ModernInvoice/ModernInvoice';
import { generateModernPdf, downloadInvoicePdf, previewPdf } from '../utils/modernPdfUtils';
import paymentService from '../service/paymentService';
import toast from 'react-hot-toast';

/**
 * Modern Invoice Export Component
 * Handles PDF generation with Razorpay payment integration
 */
const ModernInvoiceExport = ({ invoiceData, onClose }) => {
  const invoiceRef = useRef(null);
  const [isGenerating, setIsGenerating] = useState(false);
  const [paymentLink, setPaymentLink] = useState(invoiceData.razorpayPaymentLink || null);

  // Prepare invoice data with payment details
  const prepareInvoiceData = () => {
    return {
      ...invoiceData,
      razorpayPaymentLink: paymentLink,
      paymentDetails: {
        paymentLink: paymentLink,
        qrData: paymentLink,
        cashPaymentAllowed: invoiceData.cashPaymentAllowed || false,
      },
      // Add default values if missing
      companyTagline: invoiceData.companyTagline || "Professional Invoice Solutions",
      termsAndConditions: invoiceData.termsAndConditions || "Payment is due within the specified due date. Thank you for your business!",
      supportEmail: invoiceData.supportEmail || "support@invoizo.in",
    };
  };

  // Generate Razorpay payment link if not already generated
  const generatePaymentLink = async () => {
    if (paymentLink) {
      toast.success('Payment link already generated');
      return;
    }

    try {
      setIsGenerating(true);
      const token = localStorage.getItem('authToken');
      const response = await paymentService.generatePaymentLink(invoiceData.id, token);
      
      if (response.paymentLink) {
        setPaymentLink(response.paymentLink);
        toast.success('Payment link generated successfully!');
      }
    } catch (error) {
      console.error('Error generating payment link:', error);
      toast.error('Failed to generate payment link');
    } finally {
      setIsGenerating(false);
    }
  };

  // Download PDF
  const handleDownloadPdf = async () => {
    if (!invoiceRef.current) return;

    try {
      setIsGenerating(true);
      toast.loading('Generating PDF...', { id: 'pdf-gen' });
      
      await downloadInvoicePdf(
        invoiceRef.current,
        invoiceData.invoiceNumber || 'invoice'
      );
      
      toast.success('PDF downloaded successfully!', { id: 'pdf-gen' });
    } catch (error) {
      console.error('Error generating PDF:', error);
      toast.error('Failed to generate PDF', { id: 'pdf-gen' });
    } finally {
      setIsGenerating(false);
    }
  };

  // Preview PDF in new tab
  const handlePreviewPdf = async () => {
    if (!invoiceRef.current) return;

    try {
      setIsGenerating(true);
      toast.loading('Generating preview...', { id: 'pdf-preview' });
      
      await previewPdf(invoiceRef.current);
      
      toast.success('Preview opened in new tab', { id: 'pdf-preview' });
    } catch (error) {
      console.error('Error previewing PDF:', error);
      toast.error('Failed to preview PDF', { id: 'pdf-preview' });
    } finally {
      setIsGenerating(false);
    }
  };

  // Send invoice via email with payment link
  const handleSendEmail = async () => {
    try {
      setIsGenerating(true);
      toast.loading('Sending invoice...', { id: 'email-send' });
      
      const token = localStorage.getItem('authToken');
      await paymentService.sendInvoiceWithPaymentLink(invoiceData.id, token);
      
      toast.success('Invoice sent successfully!', { id: 'email-send' });
    } catch (error) {
      console.error('Error sending invoice:', error);
      toast.error('Failed to send invoice', { id: 'email-send' });
    } finally {
      setIsGenerating(false);
    }
  };

  const enrichedData = prepareInvoiceData();

  return (
    <div className="modern-invoice-export">
      {/* Control Panel */}
      <div className="export-controls" style={controlsStyle}>
        <h3 style={titleStyle}>Modern Invoice Export</h3>
        
        <div style={buttonGroupStyle}>
          {!paymentLink && invoiceData.status !== 'PAID' && (
            <button
              onClick={generatePaymentLink}
              disabled={isGenerating}
              style={buttonStyle}
              className="btn-payment"
            >
              🔗 Generate Payment Link
            </button>
          )}
          
          <button
            onClick={handlePreviewPdf}
            disabled={isGenerating}
            style={buttonStyle}
            className="btn-preview"
          >
            👁️ Preview PDF
          </button>
          
          <button
            onClick={handleDownloadPdf}
            disabled={isGenerating}
            style={buttonStyle}
            className="btn-download"
          >
            📥 Download PDF
          </button>
          
          <button
            onClick={handleSendEmail}
            disabled={isGenerating}
            style={buttonStyle}
            className="btn-email"
          >
            📧 Send via Email
          </button>
          
          {onClose && (
            <button
              onClick={onClose}
              style={{ ...buttonStyle, ...closeButtonStyle }}
              className="btn-close"
            >
              ✕ Close
            </button>
          )}
        </div>

        {paymentLink && (
          <div style={paymentLinkDisplayStyle}>
            <strong>Payment Link:</strong>
            <a href={paymentLink} target="_blank" rel="noopener noreferrer" style={linkStyle}>
              {paymentLink}
            </a>
          </div>
        )}
      </div>

      {/* Invoice Preview (Hidden during PDF generation) */}
      <div style={previewContainerStyle}>
        <div ref={invoiceRef}>
          <ModernInvoice data={enrichedData} />
        </div>
      </div>
    </div>
  );
};

// Inline styles for the component
const controlsStyle = {
  background: '#ffffff',
  padding: '24px',
  borderRadius: '12px',
  marginBottom: '24px',
  boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
};

const titleStyle = {
  margin: '0 0 16px 0',
  fontSize: '20px',
  fontWeight: '700',
  color: '#0F4C81',
};

const buttonGroupStyle = {
  display: 'flex',
  gap: '12px',
  flexWrap: 'wrap',
};

const buttonStyle = {
  padding: '10px 20px',
  borderRadius: '8px',
  border: 'none',
  fontSize: '14px',
  fontWeight: '600',
  cursor: 'pointer',
  transition: 'all 0.2s',
  background: '#0F4C81',
  color: '#ffffff',
};

const closeButtonStyle = {
  background: '#6b7280',
};

const paymentLinkDisplayStyle = {
  marginTop: '16px',
  padding: '12px',
  background: '#f0fdf4',
  borderRadius: '8px',
  border: '1px solid #22C55E',
  fontSize: '14px',
};

const linkStyle = {
  color: '#0F4C81',
  marginLeft: '8px',
  wordBreak: 'break-all',
};

const previewContainerStyle = {
  background: '#f5f7fa',
  padding: '24px',
  borderRadius: '12px',
  overflow: 'auto',
};

export default ModernInvoiceExport;
