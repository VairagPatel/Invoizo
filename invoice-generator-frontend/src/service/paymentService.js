import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

class PaymentService {
    constructor() {
        this.api = axios.create({
            baseURL: API_BASE_URL,
            headers: {
                'Content-Type': 'application/json',
            },
        });
    }

    /**
     * Generate Razorpay payment link for an invoice
     */
    async generatePaymentLink(invoiceId, token = null) {
        try {
            const headers = {};
            if (token) {
                headers.Authorization = `Bearer ${token}`;
            }
            const response = await this.api.post(`/payments/generate-link/${invoiceId}`, {}, { headers });
            return response.data;
        } catch (error) {
            console.error('Error generating payment link:', error);
            throw error;
        }
    }

    /**
     * Alias for generatePaymentLink (for backward compatibility)
     */
    async createPaymentLink(invoiceId, token = null) {
        return this.generatePaymentLink(invoiceId, token);
    }

    /**
     * Send invoice with payment link via email
     */
    async sendInvoiceWithPaymentLink(invoiceId, token = null) {
        try {
            const headers = {};
            if (token) {
                headers.Authorization = `Bearer ${token}`;
            }
            const response = await this.api.post(`/payments/send-invoice/${invoiceId}`, {}, { headers });
            return response.data;
        } catch (error) {
            console.error('Error sending invoice with payment link:', error);
            throw error;
        }
    }

    /**
     * Mark cash payment for an invoice
     */
    async markCashPayment(invoiceId, token = null) {
        try {
            const headers = {};
            if (token) {
                headers.Authorization = `Bearer ${token}`;
            }
            const response = await this.api.post(`/payments/mark-cash-payment/${invoiceId}`, {}, { headers });
            return response.data;
        } catch (error) {
            console.error('Error marking cash payment:', error);
            throw error;
        }
    }

    /**
     * Verify Razorpay payment (client-side verification)
     */
    verifyPaymentSignature(orderId, paymentId, signature) {
        // This is typically done on the backend via webhook
        // But can be used for client-side validation if needed
        return {
            orderId,
            paymentId,
            signature
        };
    }
}

export default new PaymentService();
