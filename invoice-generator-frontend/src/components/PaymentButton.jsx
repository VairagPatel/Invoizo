import React, { useState } from 'react';
import { DollarSign, CreditCard } from 'lucide-react';
import PaymentModal from './PaymentModal';

const PaymentButton = ({ invoice, onPaymentSuccess, size = 'md', variant = 'primary', showModal = true }) => {
    const [showPaymentModal, setShowPaymentModal] = useState(false);

    const handleClick = () => {
        if (showModal) {
            setShowPaymentModal(true);
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

    const getSizeClass = () => {
        switch (size) {
            case 'sm': return 'btn-sm';
            case 'lg': return 'btn-lg';
            default: return '';
        }
    };

    const getVariantClass = () => {
        switch (variant) {
            case 'success': return 'btn-success';
            case 'outline': return 'btn-outline-primary';
            case 'outline-success': return 'btn-outline-success';
            default: return 'btn-primary';
        }
    };

    return (
        <>
            <button
                className={`btn ${getVariantClass()} ${getSizeClass()} d-flex align-items-center gap-2`}
                onClick={handleClick}
                title={`Payment Options - ₹${calculateTotal().toFixed(2)}`}
            >
                <CreditCard size={size === 'sm' ? 14 : size === 'lg' ? 20 : 16} />
                {size !== 'sm' && 'Payment'}
            </button>

            {showModal && (
                <PaymentModal
                    show={showPaymentModal}
                    onHide={() => setShowPaymentModal(false)}
                    invoice={invoice}
                    onPaymentSuccess={() => {
                        setShowPaymentModal(false);
                        if (onPaymentSuccess) {
                            onPaymentSuccess();
                        }
                    }}
                />
            )}
        </>
    );
};

export default PaymentButton;
