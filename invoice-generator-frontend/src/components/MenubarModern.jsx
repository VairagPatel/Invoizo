import { Link, useNavigate, useLocation } from "react-router-dom";
import { AppContext, initialInvoiceData } from "../context/AppContext.jsx";
import { useContext, useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import Logo from "./Logo.jsx";
import {
  SignedIn,
  SignedOut,
  useClerk,
  UserButton,
} from "@clerk/clerk-react";
import { Menu, X, Home, LayoutDashboard, FileText, Clock } from "lucide-react";
import './MenubarModern.css';

const MenubarModern = () => {
  const { setInvoiceData, setSelectedTemplate, setInvoiceTitle } = useContext(AppContext);
  const navigate = useNavigate();
  const location = useLocation();
  const { openSignIn } = useClerk();
  const [isScrolled, setIsScrolled] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [currentDateTime, setCurrentDateTime] = useState(new Date());

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20);
    };

    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    // Update time every minute
    const timer = setInterval(() => {
      setCurrentDateTime(new Date());
    }, 60000);

    return () => clearInterval(timer);
  }, []);

  const formatDateTime = () => {
    const options = {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    };
    return currentDateTime.toLocaleDateString('en-US', options);
  };

  const handleGenerateClick = () => {
    setInvoiceData(initialInvoiceData);
    setSelectedTemplate("template1");
    setInvoiceTitle("Create Invoice");
    navigate("/generate");
    setIsMobileMenuOpen(false);
  };

  const openLogin = () => {
    openSignIn({});
  };

  const isActive = (path) => location.pathname === path;

  return (
    <motion.nav 
      className={`menubar-modern ${isScrolled ? 'scrolled' : ''}`}
      initial={{ y: -100 }}
      animate={{ y: 0 }}
      transition={{ duration: 0.5, ease: "easeOut" }}
    >
      <div className="menubar-container">
        {/* Brand */}
        <Link className="menubar-brand" to="/">
          <div className="brand-content">
            <Logo />
            <span className="brand-text">Invoizo</span>
          </div>
        </Link>

        {/* Desktop Navigation */}
        <div className="menubar-nav desktop-nav">
          <SignedIn>
            <NavLink to="/" icon={<Home size={18} />} isActive={isActive('/')}>
              Home
            </NavLink>
            <NavLink to="/dashboard" icon={<LayoutDashboard size={18} />} isActive={isActive('/dashboard')}>
              Dashboard
            </NavLink>
            <button
              className="nav-link-btn"
              onClick={handleGenerateClick}
            >
              <FileText size={18} />
              <span>Generate</span>
            </button>
          </SignedIn>

          <SignedOut>
            <NavLink to="/" icon={<Home size={18} />} isActive={isActive('/')}>
              Home
            </NavLink>
          </SignedOut>
        </div>

        {/* Current Date/Time */}
        <div className="current-datetime">
          <Clock size={14} />
          <span>{formatDateTime()}</span>
        </div>

        {/* Actions */}
        <div className="menubar-actions">
          <SignedIn>
            <div className="user-button-wrapper">
              <UserButton 
                appearance={{
                  elements: {
                    avatarBox: "w-10 h-10"
                  }
                }}
              />
            </div>
          </SignedIn>

          <SignedOut>
            <button
              className="btn-login"
              onClick={openLogin}
            >
              Login / Signup
            </button>
          </SignedOut>

          {/* Mobile Menu Toggle */}
          <button
            className="mobile-menu-toggle"
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          >
            {isMobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>
      </div>

      {/* Mobile Menu */}
      <AnimatePresence>
        {isMobileMenuOpen && (
          <motion.div
            className="mobile-menu"
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            transition={{ duration: 0.3 }}
          >
            <div className="mobile-menu-content">
              <SignedIn>
                <MobileNavLink 
                  to="/" 
                  icon={<Home size={20} />} 
                  onClick={() => setIsMobileMenuOpen(false)}
                  isActive={isActive('/')}
                >
                  Home
                </MobileNavLink>
                <MobileNavLink 
                  to="/dashboard" 
                  icon={<LayoutDashboard size={20} />} 
                  onClick={() => setIsMobileMenuOpen(false)}
                  isActive={isActive('/dashboard')}
                >
                  Dashboard
                </MobileNavLink>
                <button
                  className="mobile-nav-btn"
                  onClick={handleGenerateClick}
                >
                  <FileText size={20} />
                  <span>Generate Invoice</span>
                </button>
              </SignedIn>

              <SignedOut>
                <MobileNavLink 
                  to="/" 
                  icon={<Home size={20} />} 
                  onClick={() => setIsMobileMenuOpen(false)}
                  isActive={isActive('/')}
                >
                  Home
                </MobileNavLink>
              </SignedOut>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.nav>
  );
};

// Desktop Nav Link Component
const NavLink = ({ to, icon, children, isActive }) => (
  <Link to={to} className={`nav-link ${isActive ? 'active' : ''}`}>
    <div className="nav-link-content">
      {icon}
      <span>{children}</span>
    </div>
  </Link>
);

// Mobile Nav Link Component
const MobileNavLink = ({ to, icon, children, onClick, isActive }) => (
  <Link to={to} className={`mobile-nav-link ${isActive ? 'active' : ''}`} onClick={onClick}>
    <div className="mobile-nav-link-content">
      {icon}
      <span>{children}</span>
    </div>
  </Link>
);

export default MenubarModern;
