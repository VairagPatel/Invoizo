import React, { useState } from 'react';
import { DollarSign, X, Link as LinkIcon, Mail, Copy, Check } from 'lucide-react';
import paymentService from '../service/paymentService';
import toast from 'react-hot-toast';
import { useAuth } from '@clerk/clerk-react';

const PaymentModal = ({ show, onHide, invoice, onPaymentSuccess }) => {
    const [loading, setLoading] = useState(false);
    const [paymentLink, setPaymentLink] = useState(invoice?.paymentDetails?.paymentLink || null);
    const [copied, setCopied] = useState(false);
    const { getToken } = useAuth();

    const handleGeneratePaymentLink = async () => {
        setLoading(true);
        try {
            const invoiceId = invoice.id || invoice._id;
            if (!invoiceId) {
                toast.error('Invoice ID not found');
                return;
            }
            const token = await getToken();
            const response = await paymentService.generatePaymentLink(invoiceId, token);
            
            if (response.success && response.paymentLink) {
                setPaymentLink(response.paymentLink);
                toast.success('Payment link generated successfully!');
            } else {
                toast.error(response.message || 'Failed to generate payment link');
            }
        } catch (error) {
            console.error('Error generating payment link:', error);
            toast.error('Failed to generate payment link');
        } finally {
            setLoading(false);
        }
    };

    const handleSendInvoiceWithLink = async () => {
        setLoading(true);
        try {
            const invoiceId = invoice.id || invoice._id;
            if (!invoiceId) {
                toast.error('Invoice ID not found');
                return;
            }
            const token = await getToken();
            const response = await paymentService.sendInvoiceWithPaymentLink(invoiceId, token);
            
            if (response.success) {
                toast.success('Invoice sent with payment link!');
                onPaymentSuccess();
                onHide();
            } else {
                toast.error(response.message || 'Failed to send invoice');
            }
        } catch (error) {
            console.error('Error sending invoice:', error);
            toast.error('Failed to send invoice');
        } finally {
            setLoading(false);
        }
    };

    const handleCashPayment = async () => {
        setLoading(true);
        try {
            const invoiceId = invoice.id || invoice._id;
            if (!invoiceId) {
                toast.error('Invoice ID not found');
                return;
            }
            const token = await getToken();
            const response = await paymentService.markCashPayment(invoiceId, token);
            
            if (response.success) {
                toast.success('Cash payment marked successfully!');
                onPaymentSuccess();
                onHide();
            } else {
                toast.error(response.message || 'Failed to mark cash payment');
            }
        } catch (error) {
            console.error('Error marking cash payment:', error);
            toast.error('Failed to mark cash payment');
        } finally {
            setLoading(false);
        }
    };

    const handleCopyLink = () => {
        if (paymentLink) {
            navigator.clipboard.writeText(paymentLink);
            setCopied(true);
            toast.success('Payment link copied to clipboard!');
            setTimeout(() => setCopied(false), 2000);
        }
    };

    const calculateTotal = () => {
        if (!invoice) return 0;
        
        const subtotal = invoice.items?.reduce((sum, item) => 
            sum + (item.qty * item.amount), 0) || 0;
        
        const gstTotal = invoice.gstDetails?.gstTotal || 0;
        const tax = invoice.tax || 0;
        
        return subtotal + gstTotal + tax;
    };

    if (!show) return null;

    return (
        <div className="modal fade show d-block" tabIndex="-1" style={{ backgroundColor: 'rgba(0,0,0,0.5)' }}>
            <div className="modal-dialog modal-dialog-centered modal-lg">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">Payment Options</h5>
                        <button type="button" className="btn-close" onClick={onHide}>
                            <X size={20} />
                        </button>
                    </div>
                    <div className="modal-body">
                        {invoice && (
                            <div>
                                <div className="mb-4 p-3 bg-light rounded">
                                    <h6>Invoice Details</h6>
                                    <p className="mb-1"><strong>Invoice #:</strong> {invoice.invoice?.number}</p>
                                    <p className="mb-1"><strong>Amount:</strong> ₹{calculateTotal().toFixed(2)}</p>
                                    <p className="mb-0"><strong>Due Date:</strong> {invoice.invoice?.dueDate}</p>
                                </div>

                                {/* Online Payment Section */}
                                <div className="mb-4 p-3 border rounded">
                                    <h6 className="d-flex align-items-center gap-2">
                                        <LinkIcon size={20} />
                                        Online Payment (Razorpay)
                                    </h6>
                                    <p className="text-muted small mb-3">
                                        Generate a secure payment link for your customer to pay online via UPI, Cards, Net Banking, or Wallets.
                                    </p>

                                    {paymentLink ? (
                                        <div>
                                            <div className="alert alert-success d-flex align-items-center justify-content-between">
                                                <div className="flex-grow-1">
                                                    <small className="d-block text-muted">Payment Link:</small>
                                                    <a href={paymentLink} target="_blank" rel="noopener noreferrer" className="text-break">
                                                        {paymentLink}
                                                    </a>
                                                </div>
                                                <button 
                                                    className="btn btn-sm btn-outline-success ms-2"
                                                    onClick={handleCopyLink}
                                                >
                                                    {copied ? <Check size={16} /> : <Copy size={16} />}
                                                </button>
                                            </div>
                                            <button
                                                className="btn btn-primary d-flex align-items-center gap-2"
                                                onClick={handleSendInvoiceWithLink}
                                                disabled={loading}
                                            >
                                                {loading ? (
                                                    <div className="spinner-border spinner-border-sm" role="status">
                                                        <span className="visually-hidden">Loading...</span>
                                                    </div>
                                                ) : (
                                                    <Mail size={16} />
                                                )}
                                                Send Invoice via Email
                                            </button>
                                        </div>
                                    ) : (
                                        <button
                                            className="btn btn-primary d-flex align-items-center gap-2"
                                            onClick={handleGeneratePaymentLink}
                                            disabled={loading}
                                        >
                                            {loading ? (
                                                <div className="spinner-border spinner-border-sm" role="status">
                                                    <span className="visually-hidden">Loading...</span>
                                                </div>
                                            ) : (
                                                <LinkIcon size={16} />
                                            )}
                                            Generate Payment Link
                                        </button>
                                    )}
                                </div>

                                {/* Cash Payment Section */}
                                <div className="mb-3 p-3 border rounded">
                                    <h6 className="d-flex align-items-center gap-2">
                                        <DollarSign size={20} />
                                        Cash Payment
                                    </h6>
                                    <p className="text-muted small mb-3">
                                        Mark this invoice as paid if you have received cash payment from the customer.
                                    </p>
                                    <button
                                        className="btn btn-success d-flex align-items-center gap-2"
                                        onClick={handleCashPayment}
                                        disabled={loading}
                                    >
                                        {loading ? (
                                            <div className="spinner-border spinner-border-sm" role="status">
                                                <span className="visually-hidden">Loading...</span>
                                            </div>
                                        ) : (
                                            <DollarSign size={16} />
                                        )}
                                        Mark as Paid (Cash)
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                    <div className="modal-footer">
                        <button className="btn btn-secondary" onClick={onHide}>
                            Close
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PaymentModal;
