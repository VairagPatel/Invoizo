/**
 * Invoice Export Helper
 * Utility functions to integrate Modern Invoice Template with existing invoice flow
 */

import { generateModernPdf, downloadInvoicePdf, generatePdfBlob } from './modernPdfUtils';
import paymentService from '../service/paymentService';
import toast from 'react-hot-toast';

/**
 * Transform existing invoice data to Modern Invoice format
 * Handles data mapping from backend format to template format
 */
export const transformInvoiceData = (backendInvoice) => {
  // Extract nested data safely
  const invoice = backendInvoice.invoice || {};
  const company = backendInvoice.company || {};
  const billing = backendInvoice.billing || {};
  const gstDetails = backendInvoice.gstDetails || {};
  const items = backendInvoice.items || [];

  // Calculate totals
  const subtotal = items.reduce((sum, item) => sum + (item.qty * item.amount), 0);
  const gstTotal = gstDetails.gstTotal || 0;
  const total = subtotal + gstTotal;

  return {
    // Company Details
    companyName: company.name || backendInvoice.companyName || '',
    companyLogo: company.logo || backendInvoice.companyLogo || null,
    companyTagline: company.tagline || 'Professional Invoice Solutions',
    companyAddress: company.address || backendInvoice.companyAddress || '',
    companyPhone: company.phone || backendInvoice.companyPhone || '',
    companyGSTNumber: backendInvoice.companyGSTNumber || '',
    companyEmail: company.email || backendInvoice.companyEmail || '',

    // Invoice Metadata
    invoiceNumber: invoice.number || backendInvoice.invoiceNumber || '',
    invoiceDate: invoice.date || backendInvoice.invoiceDate || '',
    paymentDate: invoice.dueDate || backendInvoice.paymentDate || '',
    dueDate: invoice.dueDate || backendInvoice.dueDate || '',
    status: backendInvoice.status || 'PENDING',

    // Customer Details
    billingName: billing.name || backendInvoice.billingName || '',
    billingAddress: billing.address || backendInvoice.billingAddress || '',
    billingPhone: billing.phone || backendInvoice.billingPhone || '',

    // Items with GST calculations
    items: items.map(item => ({
      name: item.name || item.description || '',
      qty: item.qty || item.quantity || 0,
      rate: item.amount || item.rate || 0,
      amount: item.amount || item.rate || 0,
      gstRate: item.gstRate || 0,
      cgstAmount: item.cgstAmount || 0,
      sgstAmount: item.sgstAmount || 0,
      igstAmount: item.igstAmount || 0,
      totalWithGST: item.totalWithGST || (item.qty * item.amount)
    })),

    // GST Details
    hasGST: backendInvoice.hasGST || gstDetails.gstTotal > 0,
    transactionType: backendInvoice.transactionType || 'INTRA_STATE',
    subtotal: subtotal,
    cgstTotal: gstDetails.cgstTotal || 0,
    sgstTotal: gstDetails.sgstTotal || 0,
    igstTotal: gstDetails.igstTotal || 0,
    gstTotal: gstTotal,
    total: total,
    tax: backendInvoice.tax || 0,
    taxAmount: backendInvoice.taxAmount || 0,

    // Payment Details
    razorpayPaymentLink: backendInvoice.razorpayPaymentLink || null,
    paymentDetails: {
      paymentLink: backendInvoice.razorpayPaymentLink || null,
      qrData: backendInvoice.razorpayPaymentLink || null,
      cashPaymentAllowed: backendInvoice.cashPaymentAllowed || false
    },

    // Bank Details
    accountName: backendInvoice.accountName || company.accountName || '',
    accountNumber: backendInvoice.accountNumber || company.accountNumber || '',
    accountIfscCode: backendInvoice.accountIfscCode || company.ifscCode || '',

    // Additional
    notes: backendInvoice.notes || '',
    termsAndConditions: backendInvoice.termsAndConditions || 
      'Payment is due within the specified due date. Thank you for your business!',
    supportEmail: backendInvoice.supportEmail || company.email || 'support@invoizo.in'
  };
};

/**
 * Generate and download modern invoice PDF
 */
export const exportModernInvoice = async (invoiceRef, invoiceData) => {
  try {
    if (!invoiceRef || !invoiceRef.current) {
      throw new Error('Invoice reference not found');
    }

    toast.loading('Generating modern invoice PDF...', { id: 'export-pdf' });

    await downloadInvoicePdf(
      invoiceRef.current,
      invoiceData.invoiceNumber || 'invoice'
    );

    toast.success('Invoice PDF downloaded successfully!', { id: 'export-pdf' });
    return true;
  } catch (error) {
    console.error('Error exporting invoice:', error);
    toast.error('Failed to generate invoice PDF', { id: 'export-pdf' });
    return false;
  }
};

/**
 * Generate invoice with payment link
 */
export const exportInvoiceWithPayment = async (invoiceRef, invoiceData, invoiceId, authToken) => {
  try {
    toast.loading('Generating payment link...', { id: 'payment-export' });

    // Generate Razorpay payment link if not exists
    let paymentLink = invoiceData.razorpayPaymentLink;
    
    if (!paymentLink && invoiceData.status !== 'PAID') {
      try {
        const response = await paymentService.generatePaymentLink(invoiceId, authToken);
        paymentLink = response.paymentLink;
        
        // Update invoice data with payment link
        invoiceData.razorpayPaymentLink = paymentLink;
        invoiceData.paymentDetails = {
          paymentLink: paymentLink,
          qrData: paymentLink,
          cashPaymentAllowed: invoiceData.cashPaymentAllowed || false
        };
      } catch (error) {
        console.warn('Could not generate payment link:', error);
        toast.warning('Generating invoice without payment link', { id: 'payment-export' });
      }
    }

    // Generate PDF
    toast.loading('Generating invoice PDF...', { id: 'payment-export' });
    
    await downloadInvoicePdf(
      invoiceRef.current,
      invoiceData.invoiceNumber || 'invoice'
    );

    toast.success('Invoice with payment link downloaded!', { id: 'payment-export' });
    return { success: true, paymentLink };
  } catch (error) {
    console.error('Error exporting invoice with payment:', error);
    toast.error('Failed to generate invoice', { id: 'payment-export' });
    return { success: false, paymentLink: null };
  }
};

/**
 * Send invoice via email with payment link
 */
export const sendInvoiceEmail = async (invoiceId, authToken) => {
  try {
    toast.loading('Sending invoice via email...', { id: 'email-send' });

    await paymentService.sendInvoiceWithPaymentLink(invoiceId, authToken);

    toast.success('Invoice sent successfully!', { id: 'email-send' });
    return true;
  } catch (error) {
    console.error('Error sending invoice email:', error);
    toast.error('Failed to send invoice', { id: 'email-send' });
    return false;
  }
};

/**
 * Generate PDF blob for API upload or email attachment
 */
export const generateInvoiceBlob = async (invoiceRef) => {
  try {
    if (!invoiceRef || !invoiceRef.current) {
      throw new Error('Invoice reference not found');
    }

    const blob = await generatePdfBlob(invoiceRef.current);
    return blob;
  } catch (error) {
    console.error('Error generating invoice blob:', error);
    throw error;
  }
};

/**
 * Validate invoice data before export
 */
export const validateInvoiceData = (invoiceData) => {
  const errors = [];

  // Required fields
  if (!invoiceData.companyName) errors.push('Company name is required');
  if (!invoiceData.invoiceNumber) errors.push('Invoice number is required');
  if (!invoiceData.invoiceDate) errors.push('Invoice date is required');
  if (!invoiceData.billingName) errors.push('Customer name is required');
  if (!invoiceData.items || invoiceData.items.length === 0) {
    errors.push('At least one item is required');
  }
  if (!invoiceData.total || invoiceData.total <= 0) {
    errors.push('Invoice total must be greater than 0');
  }

  return {
    isValid: errors.length === 0,
    errors
  };
};

/**
 * Get invoice status configuration
 */
export const getInvoiceStatusConfig = (status, dueDate) => {
  const today = new Date();
  const due = new Date(dueDate);

  // Auto-determine overdue status
  if (status === 'PENDING' && due < today) {
    return {
      status: 'OVERDUE',
      label: 'OVERDUE',
      color: '#EF4444',
      message: 'This invoice is overdue'
    };
  }

  switch (status) {
    case 'PAID':
      return {
        status: 'PAID',
        label: 'PAID',
        color: '#22C55E',
        message: 'Payment received'
      };
    case 'OVERDUE':
      return {
        status: 'OVERDUE',
        label: 'OVERDUE',
        color: '#EF4444',
        message: 'Payment overdue'
      };
    case 'PENDING':
    default:
      return {
        status: 'PENDING',
        label: 'PENDING',
        color: '#F59E0B',
        message: 'Awaiting payment'
      };
  }
};

/**
 * Format currency for display
 */
export const formatCurrency = (amount, currency = 'INR') => {
  if (currency === 'INR') {
    return `₹${Number(amount).toFixed(2)}`;
  }
  return `${currency} ${Number(amount).toFixed(2)}`;
};

/**
 * Calculate days until due date
 */
export const getDaysUntilDue = (dueDate) => {
  const today = new Date();
  const due = new Date(dueDate);
  const diffTime = due - today;
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  
  return {
    days: diffDays,
    isOverdue: diffDays < 0,
    isDueSoon: diffDays >= 0 && diffDays <= 7
  };
};

export default {
  transformInvoiceData,
  exportModernInvoice,
  exportInvoiceWithPayment,
  sendInvoiceEmail,
  generateInvoiceBlob,
  validateInvoiceData,
  getInvoiceStatusConfig,
  formatCurrency,
  getDaysUntilDue
};
