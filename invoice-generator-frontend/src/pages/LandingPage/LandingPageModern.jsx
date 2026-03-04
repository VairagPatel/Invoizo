import { useContext, useEffect, useRef } from "react";
import { motion, useInView, useScroll, useTransform } from "framer-motion";
import { useClerk, useUser } from "@clerk/clerk-react";
import { useNavigate } from "react-router-dom";
import { AppContext, initialInvoiceData } from "../../context/AppContext.jsx";
import { assets } from "../../assets/assets.js";
import { 
    Sparkles, 
    Zap, 
    Shield, 
    Clock, 
    CheckCircle2, 
    ArrowRight,
    FileText,
    Download,
    Send,
    Star
} from "lucide-react";
import './LandingPageModern.css';

const LandingPageModern = () => {
    const { user } = useUser();
    const navigate = useNavigate();
    const { openSignIn } = useClerk();
    const { setInvoiceData, setSelectedTemplate, setInvoiceTitle } = useContext(AppContext);
    
    const heroRef = useRef(null);
    const { scrollYProgress } = useScroll();
    const opacity = useTransform(scrollYProgress, [0, 0.2], [1, 0]);
    const scale = useTransform(scrollYProgress, [0, 0.2], [1, 0.95]);

    const handleActionButton = () => {
        if (user) {
            setInvoiceData(initialInvoiceData);
            setSelectedTemplate("template1");
            setInvoiceTitle("Create Invoice");
            navigate("/generate");
        } else {
            openSignIn({});
        }
    };

    // Animation variants
    const containerVariants = {
        hidden: { opacity: 0 },
        visible: {
            opacity: 1,
            transition: {
                staggerChildren: 0.1,
                delayChildren: 0.2
            }
        }
    };

    const itemVariants = {
        hidden: { opacity: 0, y: 20 },
        visible: {
            opacity: 1,
            y: 0,
            transition: {
                duration: 0.5,
                ease: "easeOut"
            }
        }
    };

    const cardVariants = {
        hidden: { opacity: 0, scale: 0.9 },
        visible: {
            opacity: 1,
            scale: 1,
            transition: {
                duration: 0.4,
                ease: "easeOut"
            }
        },
        hover: {
            scale: 1.05,
            y: -10,
            transition: {
                duration: 0.3,
                ease: "easeInOut"
            }
        }
    };

    const features = [
        {
            icon: <Zap size={32} />,
            title: "Lightning Fast",
            description: "Create professional invoices in under 2 minutes with our intuitive interface"
        },
        {
            icon: <Shield size={32} />,
            title: "GST Compliant",
            description: "Automatic GST calculations and compliance with Indian tax regulations"
        },
        {
            icon: <Clock size={32} />,
            title: "Save Time",
            description: "Reuse templates and client data to generate invoices 10x faster"
        },
        {
            icon: <Sparkles size={32} />,
            title: "Beautiful Templates",
            description: "Choose from 5+ professionally designed templates that impress clients"
        }
    ];

    const steps = [
        {
            number: "01",
            title: "Enter Details",
            description: "Fill in your business and client information with our smart form",
            icon: <FileText size={40} />
        },
        {
            number: "02",
            title: "Choose Template",
            description: "Select from our gallery of stunning, professional templates",
            icon: <Star size={40} />
        },
        {
            number: "03",
            title: "Preview & Edit",
            description: "See your invoice in real-time and make instant adjustments",
            icon: <CheckCircle2 size={40} />
        },
        {
            number: "04",
            title: "Download & Send",
            description: "Export as PDF and send directly to your clients via email",
            icon: <Send size={40} />
        }
    ];

    return (
        <div className="landing-modern">
            {/* Hero Section */}
            <motion.section 
                ref={heroRef}
                className="hero-modern"
                style={{ opacity, scale }}
            >
                <div className="hero-background">
                    <div className="hero-gradient"></div>
                    <div className="floating-shapes">
                        <motion.div 
                            className="shape shape-1"
                            animate={{ 
                                y: [0, -30, 0],
                                rotate: [0, 180, 360]
                            }}
                            transition={{ 
                                duration: 20, 
                                repeat: Infinity,
                                ease: "linear"
                            }}
                        />
                        <motion.div 
                            className="shape shape-2"
                            animate={{ 
                                y: [0, 40, 0],
                                rotate: [0, -180, -360]
                            }}
                            transition={{ 
                                duration: 25, 
                                repeat: Infinity,
                                ease: "linear"
                            }}
                        />
                        <motion.div 
                            className="shape shape-3"
                            animate={{ 
                                y: [0, -20, 0],
                                x: [0, 20, 0]
                            }}
                            transition={{ 
                                duration: 15, 
                                repeat: Infinity,
                                ease: "linear"
                            }}
                        />
                    </div>
                </div>

                <div className="container hero-content">
                    <motion.div
                        initial={{ opacity: 0, y: 30 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.8, delay: 0.2 }}
                        className="hero-text"
                    >
                        <motion.div
                            initial={{ opacity: 0, scale: 0.9 }}
                            animate={{ opacity: 1, scale: 1 }}
                            transition={{ duration: 0.5 }}
                            className="hero-badge"
                        >
                            <Sparkles size={16} />
                            <span>Trusted by 10,000+ businesses</span>
                        </motion.div>

                        <h1 className="hero-title">
                            Create <span className="gradient-text-animated">Professional Invoices</span> in Minutes
                        </h1>

                        <p className="hero-description">
                            Stop wasting time on spreadsheets. Generate beautiful, GST-compliant invoices 
                            with our intuitive platform. Get paid faster and focus on growing your business.
                        </p>

                        <motion.div 
                            className="hero-actions"
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ duration: 0.6, delay: 0.4 }}
                        >
                            <motion.button
                                className="btn-hero-primary"
                                onClick={handleActionButton}
                                whileHover={{ scale: 1.05 }}
                                whileTap={{ scale: 0.95 }}
                            >
                                Start Creating Free
                                <ArrowRight size={20} />
                            </motion.button>

                            <motion.a
                                href="#features"
                                className="btn-hero-secondary"
                                whileHover={{ scale: 1.05 }}
                                whileTap={{ scale: 0.95 }}
                            >
                                See How It Works
                            </motion.a>
                        </motion.div>

                        <motion.div 
                            className="hero-stats"
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            transition={{ duration: 0.8, delay: 0.6 }}
                        >
                            <div className="stat-item">
                                <div className="stat-number">10K+</div>
                                <div className="stat-label">Active Users</div>
                            </div>
                            <div className="stat-divider"></div>
                            <div className="stat-item">
                                <div className="stat-number">50K+</div>
                                <div className="stat-label">Invoices Created</div>
                            </div>
                            <div className="stat-divider"></div>
                            <div className="stat-item">
                                <div className="stat-number">4.9/5</div>
                                <div className="stat-label">User Rating</div>
                            </div>
                        </motion.div>
                    </motion.div>
                </div>

                <motion.div 
                    className="scroll-indicator"
                    animate={{ y: [0, 10, 0] }}
                    transition={{ duration: 1.5, repeat: Infinity }}
                >
                    <div className="scroll-line"></div>
                </motion.div>
            </motion.section>

            {/* Features Section */}
            <section id="features" className="features-modern">
                <div className="container">
                    <motion.div
                        initial="hidden"
                        whileInView="visible"
                        viewport={{ once: true, margin: "-100px" }}
                        variants={containerVariants}
                        className="section-header"
                    >
                        <motion.h2 variants={itemVariants} className="section-title">
                            Why Choose <span className="gradient-text-primary">Invoizo</span>?
                        </motion.h2>
                        <motion.p variants={itemVariants} className="section-description">
                            Everything you need to create, manage, and send professional invoices
                        </motion.p>
                    </motion.div>

                    <motion.div 
                        className="features-grid"
                        initial="hidden"
                        whileInView="visible"
                        viewport={{ once: true, margin: "-100px" }}
                        variants={containerVariants}
                    >
                        {features.map((feature, index) => (
                            <FeatureCard key={index} feature={feature} index={index} />
                        ))}
                    </motion.div>
                </div>
            </section>

            {/* How It Works Section */}
            <section className="steps-modern">
                <div className="container">
                    <motion.div
                        initial="hidden"
                        whileInView="visible"
                        viewport={{ once: true, margin: "-100px" }}
                        variants={containerVariants}
                        className="section-header"
                    >
                        <motion.h2 variants={itemVariants} className="section-title">
                            Get Started in <span className="gradient-text-primary">4 Simple Steps</span>
                        </motion.h2>
                        <motion.p variants={itemVariants} className="section-description">
                            From zero to invoice in under 2 minutes
                        </motion.p>
                    </motion.div>

                    <div className="steps-container">
                        {steps.map((step, index) => (
                            <StepCard key={index} step={step} index={index} />
                        ))}
                    </div>
                </div>
            </section>

            {/* CTA Section */}
            <section className="cta-modern">
                <div className="container">
                    <motion.div
                        initial={{ opacity: 0, y: 30 }}
                        whileInView={{ opacity: 1, y: 0 }}
                        viewport={{ once: true }}
                        transition={{ duration: 0.6 }}
                        className="cta-content"
                    >
                        <div className="cta-glow"></div>
                        <h2 className="cta-title">
                            Ready to Transform Your Invoicing?
                        </h2>
                        <p className="cta-description">
                            Join thousands of businesses creating professional invoices with Invoizo
                        </p>
                        <motion.button
                            className="btn-cta"
                            onClick={handleActionButton}
                            whileHover={{ scale: 1.05 }}
                            whileTap={{ scale: 0.95 }}
                        >
                            Start Creating Invoices Now
                            <ArrowRight size={20} />
                        </motion.button>
                    </motion.div>
                </div>
            </section>
        </div>
    );
};

// Feature Card Component
const FeatureCard = ({ feature, index }) => {
    const ref = useRef(null);
    const isInView = useInView(ref, { once: true, margin: "-100px" });

    return (
        <motion.div
            ref={ref}
            initial={{ opacity: 0, y: 50 }}
            animate={isInView ? { opacity: 1, y: 0 } : { opacity: 0, y: 50 }}
            transition={{ duration: 0.5, delay: index * 0.1 }}
            whileHover={{ y: -10 }}
            className="feature-card glass-card"
        >
            <motion.div 
                className="feature-icon"
                whileHover={{ rotate: 360, scale: 1.1 }}
                transition={{ duration: 0.6 }}
            >
                {feature.icon}
            </motion.div>
            <h3 className="feature-title">{feature.title}</h3>
            <p className="feature-description">{feature.description}</p>
        </motion.div>
    );
};

// Step Card Component
const StepCard = ({ step, index }) => {
    const ref = useRef(null);
    const isInView = useInView(ref, { once: true, margin: "-100px" });

    return (
        <motion.div
            ref={ref}
            initial={{ opacity: 0, x: index % 2 === 0 ? -50 : 50 }}
            animate={isInView ? { opacity: 1, x: 0 } : { opacity: 0, x: index % 2 === 0 ? -50 : 50 }}
            transition={{ duration: 0.6, delay: index * 0.15 }}
            className="step-card"
        >
            <div className="step-number">{step.number}</div>
            <motion.div 
                className="step-icon"
                whileHover={{ scale: 1.1, rotate: 5 }}
                transition={{ duration: 0.3 }}
            >
                {step.icon}
            </motion.div>
            <h3 className="step-title">{step.title}</h3>
            <p className="step-description">{step.description}</p>
            {index < 3 && <div className="step-connector"></div>}
        </motion.div>
    );
};

export default LandingPageModern;
