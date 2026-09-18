import type { CurrencyCode, ReceivableType } from "../../../types/Receivable";

export interface SettlementRow {
  id: number;
  assignorId: number;
  assignorName: string;
  documentNumber: string;
  receivableType: ReceivableType;
  faceValue: number;
  dueDate: string;
  termInMonths: number;
  monthlyBaseRate: number;
  monthlySpread: number;
  presentValueBrl: number;
  discountBrl: number;
  paymentCurrency: CurrencyCode;
  paymentAmount: number;
  exchangeRateBrlPerUnit: number | null;
  settledAt: string;
}

export interface StatementFilters {
  settledFrom: string;
  settledTo: string;
  assignorId: string;
  paymentCurrency: CurrencyCode | "";
}
