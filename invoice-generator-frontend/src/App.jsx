import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import MainPage from "./pages/MainPage.jsx";
import PreviewPage from "./components/PreviewPage.jsx";
import { Toaster } from "react-hot-toast";
import Dashboard from "./pages/Dashboard.jsx";
import MenubarModern from "./components/MenubarModern.jsx";
import LandingPageModern from "./pages/LandingPage/LandingPageModern.jsx";
import PaymentSuccess from "./pages/PaymentSuccess.jsx";
import TestPayment from "./pages/TestPayment.jsx";
import UserSyncHandler from "./components/UserSyncHandler.jsx";
import { RedirectToSignIn, SignedIn, SignedOut } from "@clerk/clerk-react";

function App() {
  return (
    <Router>
      <UserSyncHandler />
      <MenubarModern />
      <Toaster />

      <Routes>
        {/* Public Route */}
        <Route path="/" element={<LandingPageModern />} />

        {/* Protected Routes - only show if signed in */}
        <Route
          path="/dashboard"
          element={
            <>
              <SignedIn>
                <Dashboard />
              </SignedIn>
              <SignedOut>
                <RedirectToSignIn />
              </SignedOut>
            </>
          }
        />
        <Route
          path="/generate"
          element={
            <>
              <SignedIn>
                <MainPage />
              </SignedIn>
              <SignedOut>
                <RedirectToSignIn />
              </SignedOut>
            </>
          }
        />
        <Route
          path="/preview"
          element={
            <>
              <SignedIn>
                <PreviewPage />
              </SignedIn>
              <SignedOut>
                <RedirectToSignIn />
              </SignedOut>
            </>
          }
        />
        
        {/* Payment Success Route - Public but requires payment params */}
        <Route path="/payment/success" element={<PaymentSuccess />} />
        
        {/* Test Payment Route - Protected */}
        <Route
          path="/test-payment"
          element={
            <>
              <SignedIn>
                <TestPayment />
              </SignedIn>
              <SignedOut>
                <RedirectToSignIn />
              </SignedOut>
            </>
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
