import React from "react";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import { Layout } from "./component/Layout";
import { OperatorPanel } from "./modules/OperatorPanel";
import { Settlements } from "./modules/Settlements";
import { ExchangeRates } from "./modules/ExchangeRates";
import { GlobalReset } from "./styles/GlobalStyles";

export const App: React.FC = () => (
  <BrowserRouter>
    <GlobalReset />
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<OperatorPanel />} />
        <Route path="settlements" element={<Settlements />} />
        <Route path="exchange-rates" element={<ExchangeRates />} />
      </Route>
    </Routes>
    <ToastContainer position="top-right" autoClose={4000} newestOnTop />
  </BrowserRouter>
);
