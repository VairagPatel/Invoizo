import { useState, useRef } from 'react';
import ModernInvoice from '../templates/ModernInvoice/ModernInvoice';
import { downloadInvoicePdf, previewPdf } from '../utils/modernPdfUtils';
import paymentService from '../service/paymentService';
import toast from 'react-hot-toast';
import './ModernInvoiceDemo.css';

/**
 * Demo page showcasing the Modern Invoice Template
 * with Razorpay payment integration
 */
const ModernInvoiceDemo = () => {
  const invoiceRef = useRef(null);
  const [isGenerating, setIsGenerating] = useState(false);
  
  // Sample invoice data
  const [invoiceData, setInvoiceData] = useState({
    // Company Details
    companyName: "Invoizo Solutions Pvt Ltd",
    companyLogo: "/logo.png",
    companyTagline: "Professional Invoice Management",
    companyAddress: "123 Business Park, MG Road, Bangalore, Karnataka 560001",
    companyPhone: "+91 98765 43210",
    companyGSTNumber: "29ABCDE1234F1Z5",
    companyEmail: "contact@invoizo.in",
    
    // Invoice Metadata
    invoiceNumber: "INV-2024-001",
    invoiceDate: "2024-02-10",
    paymentDate: "2024-03-10",
    dueDate: "2024-03-10",
    status: "PENDING",
    
    // Customer Details
    billingName: "Acme Corporation",
    billingAddress: "456 Client Street, Whitefield, Bangalore 560066",
    billingPhone: "+91 87654 32109",
    
    // Items
    items: [
      {
        name: "Web Development Services",
        qty: 1,
        rate: 50000,
        amount: 50000,
        gstRate: 18,
        cgstAmount: 4500,
        sgstAmount: 4500,
        igstAmount: 0,
        totalWithGST: 59000
      },
      {
        name: "UI/UX Design Package",
        qty: 1,
        rate: 30000,
        amount: 30000,
        gstRate: 18,
        cgstAmount: 2700,
        sgstAmount: 2700,
        igstAmount: 0,
        totalWithGST: 35400
      },
      {
        name: "SEO Optimization",
        qty: 1,
        rate: 20000,
        amount: 20000,
        gstRate: 18,
        cgstAmount: 1800,
        sgstAmount: 1800,
        igstAmount: 0,
        totalWithGST: 23600
      }
    ],
    
    // GST Details
    hasGST: true,
    transactionType: "INTRA_STATE",
    subtotal: 100000,
    cgstTotal: 9000,
    sgstTotal: 9000,
    igstTotal: 0,
    gstTotal: 18000,
    total: 118000,
    tax: 0,
    taxAmount: 0,
    
    // Payment Details
    razorpayPaymentLink: null,
    paymentDetails: {
      paymentLink: null,
      qrData: null,
      cashPaymentAllowed: false
    },
    
    // Bank Details
    accountName: "Invoizo Solutions Pvt Ltd",
    accountNumber: "1234567890123456",
    accountIfscCode: "HDFC0001234",
    
    // Additional
    notes: "Thank you for choosing Invoizo! We appreciate your business and look forward to serving you again.",
    termsAndConditions: "Payment is due within 30 days. Late payments may incur additional charges. Please make payment to the bank account mentioned above or use the online payment link.",
    supportEmail: "support@invoizo.in"
  });

  // Generate mock Razorpay payment link (for demo)
  const generateMockPaymentLink = () => {
    const mockLink = `https://rzp.io/l/demo-${Date.now()}`;
    setInvoiceData({
      ...invoiceData,
      razorpayPaymentLink: mockLink,
      paymentDetails: {
        paymentLink: mockLink,
        qrData: mockLink,
        cashPaymentAllowed: false
      }
    });
    toast.success('Mock payment link generated!');
  };

  // Generate real Razorpay payment link
  const generateRealPaymentLink = async () => {
    try {
      setIsGenerating(true);
      toast.loading('Generating payment link...', { id: 'payment-gen' });
      
      const token = localStorage.getItem('authToken');
      const response = await paymentService.generatePaymentLink(invoiceData.id, token);
      
      if (response.paymentLink) {
        setInvoiceData({
          ...invoiceData,
          razorpayPaymentLink: response.paymentLink,
          paymentDetails: {
            paymentLink: response.paymentLink,
            qrData: response.paymentLink,
            cashPaymentAllowed: false
          }
        });
        toast.success('Payment link generated!', { id: 'payment-gen' });
      }
    } catch (error) {
      console.error('Error generating payment link:', error);
      toast.error('Failed to generate payment link. Using mock link.', { id: 'payment-gen' });
      generateMockPaymentLink();
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
      
      await downloadInvoicePdf(invoiceRef.current, invoiceData.invoiceNumber);
      
      toast.success('PDF downloaded successfully!', { id: 'pdf-gen' });
    } catch (error) {
      console.error('Error generating PDF:', error);
      toast.error('Failed to generate PDF', { id: 'pdf-gen' });
    } finally {
      setIsGenerating(false);
    }
  };

  // Preview PDF
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

  // Toggle status for demo
  const toggleStatus = () => {
    const statuses = ['PENDING', 'PAID', 'OVERDUE'];
    const currentIndex = statuses.indexOf(invoiceData.status);
    const nextIndex = (currentIndex + 1) % statuses.length;
    setInvoiceData({ ...invoiceData, status: statuses[nextIndex] });
  };

  // Toggle transaction type
  const toggleTransactionType = () => {
    const newType = invoiceData.transactionType === 'INTRA_STATE' ? 'INTER_STATE' : 'INTRA_STATE';
    
    // Recalculate GST based on transaction type
    let updatedData = { ...invoiceData, transactionType: newType };
    
    if (newType === 'INTER_STATE') {
      // Convert CGST+SGST to IGST
      updatedData.igstTotal = invoiceData.cgstTotal + invoiceData.sgstTotal;
      updatedData.cgstTotal = 0;
      updatedData.sgstTotal = 0;
      
      updatedData.items = invoiceData.items.map(item => ({
        ...item,
        igstAmount: (item.cgstAmount || 0) + (item.sgstAmount || 0),
        cgstAmount: 0,
        sgstAmount: 0
      }));
    } else {
      // Convert IGST to CGST+SGST
      const halfIgst = invoiceData.igstTotal / 2;
      updatedData.cgstTotal = halfIgst;
      updatedData.sgstTotal = halfIgst;
      updatedData.igstTotal = 0;
      
      updatedData.items = invoiceData.items.map(item => {
        const halfItemIgst = (item.igstAmount || 0) / 2;
        return {
          ...item,
          cgstAmount: halfItemIgst,
          sgstAmount: halfItemIgst,
          igstAmount: 0
        };
      });
    }
    
    setInvoiceData(updatedData);
  };

  return (
    <div className="modern-invoice-demo">
      <div className="demo-header">
        <h1>🎨 Modern Invoice PDF Demo</h1>
        <p>Experience the new invoice design with Razorpay payment integration</p>
      </div>

      {/* Control Panel */}
      <div className="demo-controls">
        <div className="control-section">
          <h3>📄 PDF Actions</h3>
          <div className="button-group">
            <button
              onClick={handlePreviewPdf}
              disabled={isGenerating}
              className="btn btn-preview"
            >
              👁️ Preview PDF
            </button>
            <button
              onClick={handleDownloadPdf}
              disabled={isGenerating}
              className="btn btn-download"
            >
              📥 Download PDF
            </button>
          </div>
        </div>

        <div className="control-section">
          <h3>💳 Payment Actions</h3>
          <div className="button-group">
            <button
              onClick={generateMockPaymentLink}
              disabled={isGenerating}
              className="btn btn-payment-mock"
            >
              🔗 Generate Mock Link
            </button>
            <button
              onClick={generateRealPaymentLink}
              disabled={isGenerating}
              className="btn btn-payment-real"
            >
              💰 Generate Real Link
            </button>
          </div>
          {invoiceData.razorpayPaymentLink && (
            <div className="payment-link-display">
              <strong>Payment Link:</strong>
              <a href={invoiceData.razorpayPaymentLink} target="_blank" rel="noopener noreferrer">
                {invoiceData.razorpayPaymentLink}
              </a>
            </div>
          )}
        </div>

        <div className="control-section">
          <h3>🎛️ Demo Controls</h3>
          <div className="button-group">
            <button onClick={toggleStatus} className="btn btn-toggle">
              Status: <strong>{invoiceData.status}</strong>
            </button>
            <button onClick={toggleTransactionType} className="btn btn-toggle">
              Type: <strong>{invoiceData.transactionType}</strong>
            </button>
          </div>
        </div>
      </div>

      {/* Invoice Preview */}
      <div className="demo-preview">
        <div className="preview-label">
          <h3>📋 Invoice Preview</h3>
          <p>This is how your invoice will look in the PDF</p>
        </div>
        <div className="preview-container">
          <div ref={invoiceRef}>
            <ModernInvoice data={invoiceData} />
          </div>
        </div>
      </div>

      {/* Feature Highlights */}
      <div className="demo-features">
        <h3>✨ Features</h3>
        <div className="features-grid">
          <div className="feature-card">
            <span className="feature-icon">🎨</span>
            <h4>Modern Design</h4>
            <p>Gradient header with brand colors</p>
          </div>
          <div className="feature-card">
            <span className="feature-icon">💳</span>
            <h4>Razorpay Integration</h4>
            <p>Payment link + QR code</p>
          </div>
          <div className="feature-card">
            <span className="feature-icon">📊</span>
            <h4>GST Compliant</h4>
            <p>CGST/SGST/IGST support</p>
          </div>
          <div className="feature-card">
            <span className="feature-icon">🏷️</span>
            <h4>Status Badges</h4>
            <p>PAID/PENDING/OVERDUE</p>
          </div>
          <div className="feature-card">
            <span className="feature-icon">📱</span>
            <h4>Responsive</h4>
            <p>Mobile-friendly design</p>
          </div>
          <div className="feature-card">
            <span className="feature-icon">🖨️</span>
            <h4>Print Ready</h4>
            <p>A4 optimized output</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ModernInvoiceDemo;
