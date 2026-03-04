import React, { useState } from 'react';
import ModernInvoice from '../templates/ModernInvoice/ModernInvoice';
import { generateModernPdf } from '../utils/modernPdfUtils';
import { 
  generateQRData, 
  getPaymentButtonText,
  calculateDaysUntilDue 
} from '../utils/paymentQRUtils';

/**
 * Enhanced Invoice Demo Component
 * Demonstrates all features of the redesigned invoice PDF with Razorpay integration
 */
const EnhancedInvoiceDemo = () => {
  const [status, setStatus] = useState('PENDING');
  const [showPayment, setShowPayment] = useState(true);

  // Sample invoice data with all features
  const sampleInvoiceData = {
    // Company Details
    companyName: "Acme Corporation",
    companyAddress: "123 Business Street, Mumbai, Maharashtra 400001",
    companyPhone: "+91 22 1234 5678",
    companyGSTNumber: "27AAAAA0000A1Z5",
    companyEmail: "billing@acmecorp.com",
    companyTagline: "Excellence in Every Transaction",
    companyLogo: null, // Add logo URL if available

    // Invoice Details
    invoiceNumber: "INV-2024-001",
    invoiceDate: "2024-02-01",
    dueDate: "2024-03-01",
    status: status,

    // Customer Details
    billingName: "Tech Solutions Pvt Ltd",
    billingAddress: "456 Client Avenue, Bangalore, Karnataka 560001",
    billingPhone: "+91 80 9876 5432",

    // Items with GST
    items: [
      {
        name: "Web Development Services",
        qty: 1,
        rate: 50000,
        gstRate: 18,
        cgstAmount: 4500,
        sgstAmount: 4500,
        igstAmount: 0,
        totalWithGST: 59000
      },
      {
        name: "UI/UX Design Package",
        qty: 2,
        rate: 15000,
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
    cgstTotal: 9000,
    sgstTotal: 9000,
    igstTotal: 0,
    gstTotal: 18000,

    // Totals
    subtotal: 100000,
    total: 118000,

    // Payment Details (Razorpay)
    paymentDetails: showPayment ? {
      paymentLink: "https://rzp.io/i/demo123abc",
      qrData: "https://rzp.io/i/demo123abc",
      paymentStatus: status
    } : null,

    // Bank Details (Optional)
    accountName: "Acme Corporation",
    accountNumber: "1234567890",
    accountIfscCode: "HDFC0001234",

    // Notes
    notes: "Thank you for your business! Please make payment within 30 days.",
    termsAndConditions: "Payment due within 30 days. Late payments may incur additional charges.",
    supportEmail: "support@acmecorp.com"
  };

  // Calculate due date info
  const dueInfo = calculateDaysUntilDue(sampleInvoiceData.dueDate);
  const buttonText = getPaymentButtonText(sampleInvoiceData);

  // Handle PDF download
  const handleDownloadPDF = async () => {
    try {
      const element = document.getElementById('invoice-preview');
      if (!element) {
        alert('Invoice preview not found');
        return;
      }

      await generateModernPdf(
        element, 
        `Invoice_${sampleInvoiceData.invoiceNumber}.pdf`
      );
      
      alert('PDF downloaded successfully!');
    } catch (error) {
      console.error('Error generating PDF:', error);
      alert('Failed to generate PDF. Please try again.');
    }
  };

  // Handle status change
  const handleStatusChange = (newStatus) => {
    setStatus(newStatus);
    setShowPayment(newStatus !== 'PAID');
  };

  return (
    <div style={styles.container}>
      {/* Control Panel */}
      <div style={styles.controlPanel}>
        <h2 style={styles.title}>🎨 Enhanced Invoice PDF Demo</h2>
        
        <div style={styles.controls}>
          <div style={styles.controlGroup}>
            <label style={styles.label}>Invoice Status:</label>
            <div style={styles.buttonGroup}>
              <button
                style={{
                  ...styles.statusButton,
                  ...(status === 'PENDING' ? styles.activeButton : {})
                }}
                onClick={() => handleStatusChange('PENDING')}
              >
                ⏱ Pending
              </button>
              <button
                style={{
                  ...styles.statusButton,
                  ...(status === 'PAID' ? styles.activeButton : {})
                }}
                onClick={() => handleStatusChange('PAID')}
              >
                ✓ Paid
              </button>
              <button
                style={{
                  ...styles.statusButton,
                  ...(status === 'OVERDUE' ? styles.activeButton : {})
                }}
                onClick={() => handleStatusChange('OVERDUE')}
              >
                ⚠ Overdue
              </button>
            </div>
          </div>

          <div style={styles.controlGroup}>
            <label style={styles.label}>Payment Options:</label>
            <label style={styles.checkbox}>
              <input
                type="checkbox"
                checked={showPayment}
                onChange={(e) => setShowPayment(e.target.checked)}
                disabled={status === 'PAID'}
              />
              <span style={styles.checkboxLabel}>
                Show Razorpay Payment Section
              </span>
            </label>
          </div>

          <button style={styles.downloadButton} onClick={handleDownloadPDF}>
            📥 Download PDF
          </button>
        </div>

        {/* Info Panel */}
        <div style={styles.infoPanel}>
          <h3 style={styles.infoTitle}>📊 Invoice Info</h3>
          <div style={styles.infoGrid}>
            <div style={styles.infoItem}>
              <span style={styles.infoLabel}>Status:</span>
              <span style={styles.infoValue}>{status}</span>
            </div>
            <div style={styles.infoItem}>
              <span style={styles.infoLabel}>Total Amount:</span>
              <span style={styles.infoValue}>₹{sampleInvoiceData.total.toLocaleString()}</span>
            </div>
            <div style={styles.infoItem}>
              <span style={styles.infoLabel}>Due Date:</span>
              <span style={styles.infoValue}>{sampleInvoiceData.dueDate}</span>
            </div>
            <div style={styles.infoItem}>
              <span style={styles.infoLabel}>Days Until Due:</span>
              <span style={{
                ...styles.infoValue,
                color: dueInfo.isOverdue ? '#EF4444' : '#22C55E'
              }}>
                {dueInfo.isOverdue ? `${dueInfo.days} days overdue` : `${dueInfo.days} days`}
              </span>
            </div>
          </div>
          
          {showPayment && status !== 'PAID' && (
            <div style={styles.paymentInfo}>
              <p style={styles.paymentText}>
                💳 Payment Button Text: <strong>{buttonText}</strong>
              </p>
              <p style={styles.paymentText}>
                📱 QR Code: <strong>Enabled</strong>
              </p>
            </div>
          )}
        </div>

        {/* Feature Checklist */}
        <div style={styles.featureList}>
          <h3 style={styles.infoTitle}>✅ Active Features</h3>
          <ul style={styles.list}>
            <li style={styles.listItem}>✓ Modern gradient header (Primary → Secondary)</li>
            <li style={styles.listItem}>✓ Status badge with icon ({status})</li>
            <li style={styles.listItem}>
              {dueInfo.days !== null ? '✓' : '○'} Due date countdown
            </li>
            <li style={styles.listItem}>✓ Two-column seller/buyer cards</li>
            <li style={styles.listItem}>✓ Professional items table with GST</li>
            <li style={styles.listItem}>
              {showPayment && status !== 'PAID' ? '✓' : '○'} Razorpay payment button
            </li>
            <li style={styles.listItem}>
              {showPayment && status !== 'PAID' ? '✓' : '○'} QR code for mobile payments
            </li>
            <li style={styles.listItem}>
              {status === 'PAID' ? '✓' : '○'} Paid confirmation display
            </li>
            <li style={styles.listItem}>✓ GST-compliant breakdown (CGST/SGST)</li>
            <li style={styles.listItem}>✓ Responsive design</li>
          </ul>
        </div>
      </div>

      {/* Invoice Preview */}
      <div style={styles.previewPanel}>
        <div style={styles.previewHeader}>
          <h3 style={styles.previewTitle}>📄 Invoice Preview</h3>
          <p style={styles.previewSubtitle}>
            This is how your invoice will look in the PDF
          </p>
        </div>
        
        <div id="invoice-preview" style={styles.invoiceContainer}>
          <ModernInvoice data={sampleInvoiceData} />
        </div>
      </div>
    </div>
  );
};

// Inline styles for demo component
const styles = {
  container: {
    display: 'grid',
    gridTemplateColumns: '400px 1fr',
    gap: '24px',
    padding: '24px',
    backgroundColor: '#f5f7fa',
    minHeight: '100vh',
    fontFamily: 'Inter, system-ui, sans-serif'
  },
  controlPanel: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px'
  },
  title: {
    fontSize: '24px',
    fontWeight: '700',
    color: '#0F4C81',
    margin: '0'
  },
  controls: {
    backgroundColor: '#ffffff',
    padding: '20px',
    borderRadius: '12px',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
    display: 'flex',
    flexDirection: 'column',
    gap: '16px'
  },
  controlGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px'
  },
  label: {
    fontSize: '14px',
    fontWeight: '600',
    color: '#374151'
  },
  buttonGroup: {
    display: 'flex',
    gap: '8px'
  },
  statusButton: {
    flex: 1,
    padding: '10px 16px',
    border: '2px solid #e5e7eb',
    borderRadius: '8px',
    backgroundColor: '#ffffff',
    color: '#6b7280',
    fontSize: '13px',
    fontWeight: '600',
    cursor: 'pointer',
    transition: 'all 0.2s'
  },
  activeButton: {
    borderColor: '#0F4C81',
    backgroundColor: '#0F4C81',
    color: '#ffffff'
  },
  checkbox: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    cursor: 'pointer'
  },
  checkboxLabel: {
    fontSize: '13px',
    color: '#4b5563'
  },
  downloadButton: {
    padding: '14px 24px',
    backgroundColor: '#22C55E',
    color: '#ffffff',
    border: 'none',
    borderRadius: '8px',
    fontSize: '15px',
    fontWeight: '700',
    cursor: 'pointer',
    transition: 'all 0.2s',
    boxShadow: '0 4px 6px rgba(34, 197, 94, 0.3)'
  },
  infoPanel: {
    backgroundColor: '#ffffff',
    padding: '20px',
    borderRadius: '12px',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
  },
  infoTitle: {
    fontSize: '16px',
    fontWeight: '700',
    color: '#0F4C81',
    margin: '0 0 12px 0'
  },
  infoGrid: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: '12px'
  },
  infoItem: {
    display: 'flex',
    flexDirection: 'column',
    gap: '4px'
  },
  infoLabel: {
    fontSize: '11px',
    color: '#9ca3af',
    textTransform: 'uppercase',
    letterSpacing: '0.5px'
  },
  infoValue: {
    fontSize: '14px',
    fontWeight: '600',
    color: '#111827'
  },
  paymentInfo: {
    marginTop: '16px',
    paddingTop: '16px',
    borderTop: '1px solid #e5e7eb'
  },
  paymentText: {
    fontSize: '13px',
    color: '#4b5563',
    margin: '4px 0'
  },
  featureList: {
    backgroundColor: '#ffffff',
    padding: '20px',
    borderRadius: '12px',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
  },
  list: {
    margin: '0',
    padding: '0 0 0 20px'
  },
  listItem: {
    fontSize: '13px',
    color: '#4b5563',
    marginBottom: '8px',
    lineHeight: '1.5'
  },
  previewPanel: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px'
  },
  previewHeader: {
    backgroundColor: '#ffffff',
    padding: '20px',
    borderRadius: '12px',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
  },
  previewTitle: {
    fontSize: '18px',
    fontWeight: '700',
    color: '#0F4C81',
    margin: '0 0 4px 0'
  },
  previewSubtitle: {
    fontSize: '13px',
    color: '#6b7280',
    margin: '0'
  },
  invoiceContainer: {
    backgroundColor: '#ffffff',
    borderRadius: '12px',
    boxShadow: '0 4px 8px rgba(0,0,0,0.1)',
    overflow: 'hidden'
  }
};

export default EnhancedInvoiceDemo;
