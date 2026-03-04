import "./ModernInvoice.css";
import { QRCodeSVG } from 'qrcode.react';
import { useState, useEffect } from 'react';

const ModernInvoice = ({ data }) => {
  const subtotal = data.subtotal || data.items.reduce(
    (acc, item) => acc + item.qty * (item.rate || item.amount || 0),
    0
  );
  
  const hasGST = data.hasGST || false;
  const isIntraState = data.transactionType === 'INTRA_STATE';
  const gstTotal = data.gstTotal || 0;
  const total = data.total || (subtotal + gstTotal);
  
  // Payment details
  const paymentLink = data.paymentDetails?.paymentLink || data.razorpayPaymentLink;
  const qrData = data.paymentDetails?.qrData || paymentLink;
  const paymentStatus = data.status || data.paymentStatus;
  const isPaid = paymentStatus === 'PAID';

  // Due date countdown
  const [daysUntilDue, setDaysUntilDue] = useState(null);
  const [isOverdue, setIsOverdue] = useState(false);

  useEffect(() => {
    if (data.paymentDate || data.dueDate) {
      const dueDate = new Date(data.paymentDate || data.dueDate);
      const today = new Date();
      const diffTime = dueDate - today;
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      
      setDaysUntilDue(Math.abs(diffDays));
      setIsOverdue(diffDays < 0);
    }
  }, [data.paymentDate, data.dueDate]);

  // Status badge configuration
  const getStatusConfig = (status) => {
    switch(status) {
      case 'PAID':
        return { label: 'PAID', className: 'status-paid', icon: '✓' };
      case 'OVERDUE':
        return { label: 'OVERDUE', className: 'status-overdue', icon: '⚠' };
      case 'PENDING':
      default:
        return { label: 'PENDING', className: 'status-pending', icon: '⏱' };
    }
  };

  const statusConfig = getStatusConfig(paymentStatus);

  return (
    <div className="modern-invoice">
      {/* Header Section with Gradient */}
      <div className="invoice-header">
        <div className="header-content">
          <div className="company-section">
            {data.companyLogo && (
              <img src={data.companyLogo} alt="Company Logo" className="company-logo" />
            )}
            <h1 className="company-name">{data.companyName}</h1>
            {data.companyTagline && (
              <p className="company-tagline">{data.companyTagline}</p>
            )}
          </div>
          <div className="invoice-meta">
            <h2 className="invoice-title">INVOICE</h2>
            <div className="meta-details">
              <div className="meta-row">
                <span className="meta-label">Invoice #:</span>
                <span className="meta-value">{data.invoiceNumber}</span>
              </div>
              <div className="meta-row">
                <span className="meta-label">Date:</span>
                <span className="meta-value">{data.invoiceDate}</span>
              </div>
              <div className="meta-row">
                <span className="meta-label">Due Date:</span>
                <span className="meta-value">{data.paymentDate || data.dueDate}</span>
              </div>
            </div>
            {/* Status Badge */}
            <div className={`status-badge ${statusConfig.className}`}>
              <span className="status-icon">{statusConfig.icon}</span> {statusConfig.label}
            </div>
            {/* Due Date Countdown */}
            {!isPaid && daysUntilDue !== null && (
              <div className={`due-countdown ${isOverdue ? 'overdue' : ''}`}>
                {isOverdue ? (
                  <span>⚠ {daysUntilDue} days overdue</span>
                ) : (
                  <span>⏰ Due in {daysUntilDue} days</span>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Seller & Buyer Cards */}
      <div className="parties-section">
        <div className="party-card seller-card">
          <h3 className="card-title">From</h3>
          <div className="card-content">
            <p className="party-name">{data.companyName}</p>
            <p className="party-detail">{data.companyAddress}</p>
            <p className="party-detail">Phone: {data.companyPhone}</p>
            {data.companyGSTNumber && (
              <p className="party-detail gst-number">GST: {data.companyGSTNumber}</p>
            )}
          </div>
        </div>

        <div className="party-card buyer-card">
          <h3 className="card-title">Billed To</h3>
          <div className="card-content">
            <p className="party-name">{data.billingName}</p>
            <p className="party-detail">{data.billingAddress}</p>
            <p className="party-detail">Phone: {data.billingPhone}</p>
          </div>
        </div>
      </div>

      {/* Items Table */}
      <div className="items-section">
        <table className="items-table">
          <thead>
            <tr>
              <th className="col-description">Item Description</th>
              <th className="col-qty">Qty</th>
              <th className="col-rate">Rate</th>
              {hasGST && <th className="col-gst">GST %</th>}
              {hasGST && <th className="col-tax">Tax Amount</th>}
              <th className="col-amount">Amount</th>
            </tr>
          </thead>
          <tbody>
            {data.items.map((item, index) => {
              const rate = item.rate || item.amount || 0;
              const itemTotal = hasGST && item.totalWithGST 
                ? item.totalWithGST 
                : item.qty * rate;
              const taxAmount = hasGST 
                ? (item.cgstAmount || 0) + (item.sgstAmount || 0) + (item.igstAmount || 0)
                : 0;

              return (
                <tr key={index} className={index % 2 === 0 ? 'row-even' : 'row-odd'}>
                  <td className="item-name">{item.name}</td>
                  <td className="text-center">{item.qty}</td>
                  <td className="text-right">₹{Number(rate).toFixed(2)}</td>
                  {hasGST && <td className="text-center">{item.gstRate || 0}%</td>}
                  {hasGST && <td className="text-right">₹{Number(taxAmount).toFixed(2)}</td>}
                  <td className="text-right amount-col">₹{Number(itemTotal).toFixed(2)}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Summary & Payment Section */}
      <div className="bottom-section">
        <div className="payment-info-section">
          {/* Bank Details (if available) */}
          {(data.accountName || data.accountNumber) && (
            <div className="bank-details">
              <h4 className="section-subtitle">Bank Details</h4>
              {data.accountName && <p><strong>Account:</strong> {data.accountName}</p>}
              {data.accountNumber && <p><strong>Number:</strong> {data.accountNumber}</p>}
              {data.accountIfscCode && <p><strong>IFSC:</strong> {data.accountIfscCode}</p>}
            </div>
          )}

          {/* Payment Section - Only show if not paid */}
          {!isPaid && (paymentLink || qrData) && (
            <div className="payment-section">
              <h4 className="section-subtitle">💳 Secure Payment Options</h4>
              
              {/* Payment Link Button */}
              {paymentLink && (
                <div className="payment-option">
                  <a 
                    href={paymentLink} 
                    target="_blank" 
                    rel="noopener noreferrer"
                    className="payment-button"
                  >
                    🔒 PAY NOW SECURELY
                  </a>
                  <p className="payment-methods">
                    <span className="payment-method-badge">UPI</span>
                    <span className="payment-method-badge">Cards</span>
                    <span className="payment-method-badge">Net Banking</span>
                    <span className="payment-method-badge">Wallets</span>
                  </p>
                  <p className="payment-powered">Powered by Razorpay</p>
                </div>
              )}

              {/* QR Code */}
              {qrData && (
                <div className="qr-section">
                  <div className="qr-code-container">
                    <QRCodeSVG 
                      value={qrData} 
                      size={140}
                      level="H"
                      includeMargin={true}
                      fgColor="#0F4C81"
                    />
                  </div>
                  <p className="qr-label">📱 Scan to Pay</p>
                  <p className="qr-sublabel">UPI • Cards • Wallets</p>
                </div>
              )}
            </div>
          )}

          {/* Paid Badge */}
          {isPaid && (
            <div className="paid-confirmation">
              <div className="paid-icon">✓</div>
              <h4>Payment Received</h4>
              <p>Thank you for your payment!</p>
            </div>
          )}
        </div>

        {/* Summary Box */}
        <div className="summary-box">
          {data.transactionType && (
            <div className="summary-row info-row">
              <span>Transaction Type:</span>
              <span>{isIntraState ? 'Intra-State' : 'Inter-State'}</span>
            </div>
          )}
          
          <div className="summary-row">
            <span>Subtotal:</span>
            <span>₹{Number(subtotal).toFixed(2)}</span>
          </div>

          {hasGST ? (
            <>
              {isIntraState ? (
                <>
                  <div className="summary-row">
                    <span>CGST:</span>
                    <span>₹{Number(data.cgstTotal || 0).toFixed(2)}</span>
                  </div>
                  <div className="summary-row">
                    <span>SGST:</span>
                    <span>₹{Number(data.sgstTotal || 0).toFixed(2)}</span>
                  </div>
                </>
              ) : (
                <div className="summary-row">
                  <span>IGST:</span>
                  <span>₹{Number(data.igstTotal || 0).toFixed(2)}</span>
                </div>
              )}
              <div className="summary-row">
                <span>Total GST:</span>
                <span>₹{Number(gstTotal).toFixed(2)}</span>
              </div>
            </>
          ) : (
            data.tax > 0 && (
              <div className="summary-row">
                <span>Tax ({data.tax}%):</span>
                <span>₹{Number(data.taxAmount || 0).toFixed(2)}</span>
              </div>
            )
          )}

          <div className="summary-row total-row">
            <span>TOTAL DUE</span>
            <span>₹{Number(total).toFixed(2)}</span>
          </div>
        </div>
      </div>

      {/* Notes Section */}
      {data.notes && (
        <div className="notes-section">
          <h4 className="section-subtitle">Notes</h4>
          <p className="notes-text">{data.notes}</p>
        </div>
      )}

      {/* Footer */}
      <div className="invoice-footer">
        <div className="footer-content">
          <p className="footer-text">
            {data.termsAndConditions || 'Thank you for your business!'}
          </p>
          <p className="footer-branding">
            Generated by <strong>Invoizo</strong> • {data.companyEmail || data.supportEmail || 'support@invoizo.in'}
          </p>
        </div>
      </div>
    </div>
  );
};

export default ModernInvoice;
