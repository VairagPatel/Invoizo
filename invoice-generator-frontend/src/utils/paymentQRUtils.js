/**
 * Payment QR Code Utilities
 * Helper functions for generating and handling Razorpay payment QR codes
 */

/**
 * Generate QR data from payment link
 * @param {string} paymentLink - Razorpay payment link URL
 * @returns {string} QR data string
 */
export const generateQRData = (paymentLink) => {
  if (!paymentLink) {
    console.warn('No payment link provided for QR generation');
    return null;
  }
  
  // The payment link itself is the QR data
  // When scanned, it opens the Razorpay payment page
  return paymentLink;
};

/**
 * Generate UPI QR data (alternative format)
 * @param {Object} paymentDetails - Payment details
 * @returns {string} UPI QR string
 */
export const generateUPIQRData = (paymentDetails) => {
  const {
    upiId,
    payeeName,
    amount,
    transactionNote,
    merchantCode
  } = paymentDetails;

  if (!upiId || !payeeName) {
    console.warn('Missing required UPI details');
    return null;
  }

  // UPI QR format: upi://pay?pa=UPI_ID&pn=NAME&am=AMOUNT&tn=NOTE&mc=CODE
  const params = new URLSearchParams({
    pa: upiId,
    pn: payeeName,
    ...(amount && { am: amount.toFixed(2) }),
    ...(transactionNote && { tn: transactionNote }),
    ...(merchantCode && { mc: merchantCode })
  });

  return `upi://pay?${params.toString()}`;
};

/**
 * Validate payment link format
 * @param {string} paymentLink - Payment link to validate
 * @returns {boolean} True if valid
 */
export const isValidPaymentLink = (paymentLink) => {
  if (!paymentLink || typeof paymentLink !== 'string') {
    return false;
  }

  // Check if it's a valid URL
  try {
    const url = new URL(paymentLink);
    
    // Check if it's a Razorpay link
    const isRazorpay = url.hostname.includes('razorpay') || 
                       url.hostname.includes('rzp.io');
    
    return isRazorpay;
  } catch (error) {
    return false;
  }
};

/**
 * Extract payment link from invoice data
 * @param {Object} invoiceData - Invoice data object
 * @returns {string|null} Payment link or null
 */
export const extractPaymentLink = (invoiceData) => {
  // Try multiple possible locations
  return invoiceData?.paymentDetails?.paymentLink ||
         invoiceData?.razorpayPaymentLink ||
         invoiceData?.paymentLink ||
         null;
};

/**
 * Extract QR data from invoice data
 * @param {Object} invoiceData - Invoice data object
 * @returns {string|null} QR data or null
 */
export const extractQRData = (invoiceData) => {
  // Try QR data first, fallback to payment link
  return invoiceData?.paymentDetails?.qrData ||
         extractPaymentLink(invoiceData);
};

/**
 * Check if payment is completed
 * @param {Object} invoiceData - Invoice data object
 * @returns {boolean} True if paid
 */
export const isPaymentCompleted = (invoiceData) => {
  const status = invoiceData?.status || 
                 invoiceData?.paymentStatus || 
                 invoiceData?.paymentDetails?.paymentStatus;
  
  return status === 'PAID';
};

/**
 * Get payment status details
 * @param {Object} invoiceData - Invoice data object
 * @returns {Object} Status details
 */
export const getPaymentStatus = (invoiceData) => {
  const status = invoiceData?.status || 
                 invoiceData?.paymentStatus || 
                 'PENDING';

  const statusMap = {
    PAID: {
      label: 'Paid',
      color: '#22C55E',
      icon: '✓',
      description: 'Payment received successfully'
    },
    PENDING: {
      label: 'Pending',
      color: '#F59E0B',
      icon: '⏱',
      description: 'Awaiting payment'
    },
    OVERDUE: {
      label: 'Overdue',
      color: '#EF4444',
      icon: '⚠',
      description: 'Payment is overdue'
    },
    DRAFT: {
      label: 'Draft',
      color: '#6B7280',
      icon: '📝',
      description: 'Invoice not yet sent'
    }
  };

  return statusMap[status] || statusMap.PENDING;
};

/**
 * Calculate days until due date
 * @param {string} dueDate - Due date string (ISO format)
 * @returns {Object} Days info
 */
export const calculateDaysUntilDue = (dueDate) => {
  if (!dueDate) {
    return { days: null, isOverdue: false };
  }

  const due = new Date(dueDate);
  const today = new Date();
  
  // Reset time to midnight for accurate day calculation
  due.setHours(0, 0, 0, 0);
  today.setHours(0, 0, 0, 0);

  const diffTime = due - today;
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  return {
    days: Math.abs(diffDays),
    isOverdue: diffDays < 0,
    isToday: diffDays === 0,
    isSoon: diffDays > 0 && diffDays <= 7
  };
};

/**
 * Format payment amount for display
 * @param {number} amount - Amount in rupees
 * @param {string} currency - Currency code (default: INR)
 * @returns {string} Formatted amount
 */
export const formatPaymentAmount = (amount, currency = 'INR') => {
  if (typeof amount !== 'number') {
    return '₹0.00';
  }

  const formatted = amount.toFixed(2);
  
  if (currency === 'INR') {
    return `₹${formatted}`;
  }
  
  return `${currency} ${formatted}`;
};

/**
 * Generate payment button text based on status
 * @param {Object} invoiceData - Invoice data object
 * @returns {string} Button text
 */
export const getPaymentButtonText = (invoiceData) => {
  const dueInfo = calculateDaysUntilDue(invoiceData.dueDate);
  
  if (dueInfo.isOverdue) {
    return '⚠ PAY OVERDUE INVOICE';
  }
  
  if (dueInfo.isToday) {
    return '⏰ PAY TODAY';
  }
  
  if (dueInfo.isSoon) {
    return '🔒 PAY NOW SECURELY';
  }
  
  return '💳 PAY NOW SECURELY';
};

/**
 * Get QR code size based on device
 * @returns {number} QR code size in pixels
 */
export const getQRCodeSize = () => {
  const isMobile = window.innerWidth < 768;
  return isMobile ? 160 : 140;
};

/**
 * Download QR code as image
 * @param {string} qrData - QR data string
 * @param {string} fileName - File name for download
 */
export const downloadQRCode = (qrData, fileName = 'payment-qr.png') => {
  // This would require additional implementation with canvas
  // For now, just log the action
  console.log('QR Code download requested:', { qrData, fileName });
  
  // Implementation would involve:
  // 1. Render QR code to canvas
  // 2. Convert canvas to blob
  // 3. Create download link
  // 4. Trigger download
};

/**
 * Share payment link via Web Share API
 * @param {string} paymentLink - Payment link to share
 * @param {Object} invoiceDetails - Invoice details for share text
 */
export const sharePaymentLink = async (paymentLink, invoiceDetails) => {
  if (!navigator.share) {
    console.warn('Web Share API not supported');
    return false;
  }

  try {
    await navigator.share({
      title: `Invoice ${invoiceDetails.invoiceNumber}`,
      text: `Payment for Invoice ${invoiceDetails.invoiceNumber} - Amount: ₹${invoiceDetails.total}`,
      url: paymentLink
    });
    return true;
  } catch (error) {
    console.error('Error sharing payment link:', error);
    return false;
  }
};

/**
 * Copy payment link to clipboard
 * @param {string} paymentLink - Payment link to copy
 * @returns {Promise<boolean>} Success status
 */
export const copyPaymentLink = async (paymentLink) => {
  try {
    await navigator.clipboard.writeText(paymentLink);
    return true;
  } catch (error) {
    console.error('Error copying payment link:', error);
    
    // Fallback for older browsers
    try {
      const textArea = document.createElement('textarea');
      textArea.value = paymentLink;
      textArea.style.position = 'fixed';
      textArea.style.left = '-999999px';
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
      return true;
    } catch (fallbackError) {
      console.error('Fallback copy failed:', fallbackError);
      return false;
    }
  }
};

export default {
  generateQRData,
  generateUPIQRData,
  isValidPaymentLink,
  extractPaymentLink,
  extractQRData,
  isPaymentCompleted,
  getPaymentStatus,
  calculateDaysUntilDue,
  formatPaymentAmount,
  getPaymentButtonText,
  getQRCodeSize,
  downloadQRCode,
  sharePaymentLink,
  copyPaymentLink
};
